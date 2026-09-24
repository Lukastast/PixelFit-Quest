package com.pixelfitquest.local

import androidx.room.withTransaction
import com.pixelfitquest.feature.customization.model.CharacterData
import com.pixelfitquest.feature.customization.model.grandfatherFitnessVariants
import com.pixelfitquest.feature.home.model.DwellingTier
import com.pixelfitquest.feature.progression.ClaimedWorkoutReward
import com.pixelfitquest.feature.progression.DailyTrainingReward
import com.pixelfitquest.feature.progression.RespecOutcome
import com.pixelfitquest.feature.progression.RewardBonus
import com.pixelfitquest.feature.progression.RewardSet
import com.pixelfitquest.feature.progression.SkillBranch
import com.pixelfitquest.feature.progression.SkillLoadout
import com.pixelfitquest.feature.progression.SkillTree
import java.time.LocalDate
import com.pixelfitquest.feature.workout.model.Exercise
import com.pixelfitquest.feature.workout.model.Workout
import com.pixelfitquest.feature.workout.model.WorkoutSet
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutTemplate
import com.pixelfitquest.firebase.model.UserData
import com.pixelfitquest.local.db.PixelFitDatabase
import com.pixelfitquest.local.db.entity.LocalExerciseEntity
import com.pixelfitquest.local.db.entity.LocalSetEntity
import com.pixelfitquest.local.db.entity.LocalTemplateEntity
import com.pixelfitquest.local.db.entity.LocalWorkoutEntity
import com.pixelfitquest.local.db.entity.UserProfileEntity
import com.pixelfitquest.local.export.ExerciseExport
import com.pixelfitquest.local.export.LocalExportSnapshot
import com.pixelfitquest.local.export.WorkoutExport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalPixelFitStore @Inject constructor(
    private val db: PixelFitDatabase,
) {
    private val userDao = db.userProfileDao()
    private val workoutDao = db.workoutDao()
    private val templateDao = db.templateDao()
    private val liftHistoryDao = db.liftHistoryDao()
    private val profileMutex = Mutex()

    suspend fun ensureProfile(): UserProfileEntity = profileMutex.withLock {
        val current = userDao.get() ?: UserProfileEntity.default().also { userDao.upsert(it) }
        if (current.dwellingLegacyMigrated != 0) return@withLock current
        val character = current.toCharacter()
        val migratedHomes = DwellingTier.migrateOwnership(
            level = current.level,
            ownedIds = character.unlockedHomeUpgrades,
            alreadyMigrated = false,
        )
        val migratedVariants = grandfatherFitnessVariants(
            level = current.level,
            ownedVariants = character.unlockedVariants,
            alreadyMigrated = false,
        )
        val next = current.copy(
            dwellingLegacyMigrated = 1,
            unlockedHomeUpgradesCsv = migratedHomes.joinToString(","),
            unlockedVariantsCsv = UserProfileEntity.variantsCsv(migratedVariants),
        )
        userDao.upsert(next)
        next
    }

    fun observeUserData(): Flow<UserData> =
        userDao.observe().onStart { ensureProfile() }.map { entity ->
            (entity ?: UserProfileEntity.default()).toUserData()
        }

    suspend fun getUserData(): UserData = ensureProfile().toUserData()

    suspend fun updateUserData(updates: Map<String, Any>) {
        profileMutex.withLock {
            val current = userDao.get() ?: UserProfileEntity.default()
            userDao.upsert(applyUpdates(current, updates))
        }
    }

    suspend fun getUserField(field: String): Any? {
        val profile = ensureProfile()
        return when (field) {
            "height" -> profile.height
            "armLength" -> profile.armLength
            "musicVolume" -> profile.musicVolume
            "level" -> profile.level
            "coins" -> profile.coins
            "exp" -> profile.exp
            "streak" -> profile.streak
            "last_activity_date" -> profile.lastActivityDate
            "last_steps_reward_date" -> profile.lastStepsRewardDate
            "last_sleep_reward_date" -> profile.lastSleepRewardDate
            "last_weekly_heart_reward_week" -> profile.lastWeeklyHeartRewardWeek
            "last_vitality_bonus_date" -> profile.lastVitalityBonusDate
            "last_streak_update_date" -> profile.lastStreakUpdateDate
            else -> null
        }
    }

    suspend fun replaceProfile(transform: (UserProfileEntity) -> UserProfileEntity) {
        profileMutex.withLock {
            val current = userDao.get() ?: UserProfileEntity.default()
            userDao.upsert(transform(current))
        }
    }

    suspend fun saveCharacter(data: CharacterData) {
        replaceProfile { current ->
            current.copy(
                characterGender = data.gender,
                characterVariant = data.variant,
                unlockedVariantsCsv = UserProfileEntity.variantsCsv(data.unlockedVariants),
                equippedHomeUpgrade = data.equippedHomeUpgrade.orEmpty(),
                unlockedHomeUpgradesCsv = UserProfileEntity.variantsCsv(data.unlockedHomeUpgrades),
                equippedGym = data.equippedGym,
                unlockedGymsCsv = UserProfileEntity.variantsCsv(data.unlockedGyms),
                equippedAppBackground = data.equippedAppBackground,
                unlockedAppBackgroundsCsv = UserProfileEntity.variantsCsv(data.unlockedAppBackgrounds),
            )
        }
    }

    fun observeCharacter(): Flow<CharacterData> =
        userDao.observe().onStart { ensureProfile() }.map { entity ->
            (entity ?: UserProfileEntity.default()).toCharacter()
        }

    suspend fun getCharacter(): CharacterData = ensureProfile().toCharacter()

    fun observeSkills(): Flow<SkillLoadout> =
        userDao.observe().onStart { ensureProfile() }.map { entity ->
            (entity ?: UserProfileEntity.default()).toSkills()
        }

    suspend fun skillLoadout(): SkillLoadout = ensureProfile().toSkills()

    suspend fun spendSkillPoint(branch: SkillBranch): Boolean = profileMutex.withLock {
        val current = userDao.get() ?: return@withLock false
        val rank = current.rankOf(branch)
        if (rank >= SkillTree.MAX_RANK) return@withLock false
        val unspent = SkillTree.unspent(
            current.level,
            current.skillForm,
            current.skillIron,
            current.skillVitality,
        )
        if (unspent <= 0) return@withLock false
        val next = when (branch) {
            SkillBranch.FORM -> current.copy(skillForm = rank + 1)
            SkillBranch.IRON -> current.copy(skillIron = rank + 1)
            SkillBranch.VITALITY -> current.copy(skillVitality = rank + 1)
        }
        userDao.upsert(next)
        true
    }

    suspend fun respecSkills(today: LocalDate = LocalDate.now()): RespecOutcome = profileMutex.withLock {
        val current = userDao.get() ?: return@withLock RespecOutcome.NOTHING_SPENT
        val spent = current.skillForm + current.skillIron + current.skillVitality
        if (spent <= 0) return@withLock RespecOutcome.NOTHING_SPENT
        if (!SkillTree.respecAllowed(today, current.skillRespecDate)) return@withLock RespecOutcome.COOLDOWN
        if (current.coins < SkillTree.RESPEC_COST) return@withLock RespecOutcome.CANT_AFFORD
        userDao.upsert(
            current.copy(
                skillForm = 0,
                skillIron = 0,
                skillVitality = 0,
                skillRespecDate = today.toString(),
                coins = current.coins - SkillTree.RESPEC_COST,
            ),
        )
        RespecOutcome.RESET
    }

    /**
     * Pays a workout at most once. Marks the workout awarded and consumes today's
     * set budget in the same transaction.
     */
    suspend fun claimWorkoutReward(
        workoutId: String,
        sets: List<RewardSet>,
        today: String = LocalDate.now().toString(),
    ): ClaimedWorkoutReward? = profileMutex.withLock {
        db.withTransaction {
            val row = workoutDao.getWorkout(workoutId) ?: return@withTransaction null
            val workout = decodeWorkout(row.payloadJson) ?: return@withTransaction null
            if (workout.rewardsAwarded) {
                val storedXp = workout.awardedXp
                if (storedXp == null) return@withTransaction null
                return@withTransaction ClaimedWorkoutReward(
                    xp = storedXp,
                    coins = workout.awardedCoins ?: 0,
                    clipped = workout.rewardClipped,
                    fresh = false,
                )
            }
            val profile = userDao.get() ?: UserProfileEntity.default()
            val used = if (profile.rewardedSetsDate == today) {
                profile.rewardedSetsCount.coerceIn(0, DailyTrainingReward.MAX_SETS_PER_DAY)
            } else {
                0
            }
            val clip = DailyTrainingReward.clip(sets, used)
            val character = profile.toCharacter()
            val skills = profile.toSkills()
            val payout = RewardBonus.workoutPayout(
                baseXp = clip.xp,
                baseCoins = clip.coins,
                dwelling = DwellingTier.resolve(
                    character.equippedHomeUpgrade,
                    character.unlockedHomeUpgrades,
                ),
                variant = character.variant,
                perfectSetXp = clip.perfectSetXp,
                formRank = skills.form,
                ironRank = skills.iron,
            )
            userDao.upsert(
                profile.copy(
                    rewardedSetsDate = today,
                    rewardedSetsCount = used + clip.setsConsumed,
                ),
            )
            val saved = workout.copy(
                rewardsAwarded = true,
                awardedXp = payout.xp,
                awardedCoins = payout.coins,
                rewardClipped = clip.clipped,
            )
            workoutDao.upsertWorkout(
                com.pixelfitquest.local.db.entity.LocalWorkoutEntity(
                    id = saved.id.ifBlank { workoutId },
                    date = saved.date.ifBlank { row.date },
                    payloadJson = LocalJson.toJson(saved.toMap()),
                ),
            )
            ClaimedWorkoutReward(
                xp = payout.xp,
                coins = payout.coins,
                clipped = clip.clipped,
                fresh = true,
            )
        }
    }

    suspend fun resetUnlockedVariants() {
        replaceProfile { current ->
            current.copy(
                characterVariant = "basic",
                unlockedVariantsCsv = "basic",
            )
        }
    }

    suspend fun saveWorkout(workout: Workout) {
        if (workout.totalExercises <= 0) return
        workoutDao.upsertWorkout(
            LocalWorkoutEntity(
                id = workout.id,
                date = workout.date,
                payloadJson = LocalJson.toJson(workout.toMap()),
            )
        )
    }

    suspend fun saveExercise(exercise: Exercise) {
        workoutDao.upsertExercise(
            LocalExerciseEntity(
                id = exercise.id,
                workoutId = exercise.workoutId,
                payloadJson = LocalJson.toJson(exercise.toMap()),
            )
        )
    }

    suspend fun saveSet(set: WorkoutSet) {
        workoutDao.upsertSet(
            LocalSetEntity(
                id = set.id,
                workoutId = set.workoutId,
                exerciseId = set.exerciseId,
                payloadJson = LocalJson.toJson(set.toMap()),
            )
        )
    }

    fun observeWorkouts(): Flow<List<Workout>> =
        workoutDao.observeWorkouts().map { rows ->
            rows.mapNotNull { decodeWorkout(it.payloadJson) }
                .filter { it.totalExercises > 0 }
        }

    suspend fun getAllWorkouts(): List<Workout> {
        runCatching { workoutDao.deleteWorkoutsWithoutExercises() }
        return workoutDao.getAllWorkouts()
            .mapNotNull { decodeWorkout(it.payloadJson) }
            .filter { it.totalExercises > 0 }
    }

    suspend fun getWorkouts(limit: Int): List<Workout> {
        runCatching { workoutDao.deleteWorkoutsWithoutExercises() }
        return workoutDao.getWorkouts(limit)
            .mapNotNull { decodeWorkout(it.payloadJson) }
            .filter { it.totalExercises > 0 }
    }

    suspend fun getWorkout(id: String): Workout? =
        workoutDao.getWorkout(id)?.let { decodeWorkout(it.payloadJson) }?.takeIf { it.totalExercises > 0 }

    suspend fun getExercises(workoutId: String): List<Exercise> =
        workoutDao.getExercises(workoutId).mapNotNull { decodeExercise(it.payloadJson) }

    suspend fun getSets(workoutId: String): List<WorkoutSet> =
        workoutDao.getSets(workoutId).mapNotNull { decodeSet(it.payloadJson, workoutId) }

    suspend fun deleteWorkout(workoutId: String) {
        db.withTransaction {
            workoutDao.deleteSets(workoutId)
            workoutDao.deleteExercises(workoutId)
            workoutDao.deleteWorkout(workoutId)
            liftHistoryDao.deleteByWorkoutId(workoutId)
        }
    }

    suspend fun updateWorkout(workoutId: String, updates: Map<String, Any>) {
        val existing = workoutDao.getWorkout(workoutId) ?: return
        val merged = LocalJson.toMap(existing.payloadJson).toMutableMap()
        merged.putAll(updates)
        val workout = Workout.fromMap(merged)
        workoutDao.upsertWorkout(
            LocalWorkoutEntity(
                id = workout.id.ifBlank { workoutId },
                date = workout.date.ifBlank { existing.date },
                payloadJson = LocalJson.toJson(workout.toMap()),
            )
        )
    }

    suspend fun saveTemplate(template: WorkoutTemplate) {
        templateDao.upsert(
            LocalTemplateEntity(
                id = template.id,
                name = template.name,
                createdAt = template.createdAt ?: "",
                payloadJson = LocalJson.toJson(template.toMap()),
            )
        )
    }

    fun observeTemplates(): Flow<List<WorkoutTemplate>> =
        templateDao.observe().map { rows -> rows.mapNotNull { decodeTemplate(it) } }

    suspend fun getTemplates(limit: Int = 50): List<WorkoutTemplate> =
        templateDao.getAll(limit).mapNotNull { decodeTemplate(it) }

    suspend fun getTemplateByName(name: String): WorkoutTemplate? =
        templateDao.getByName(name)?.let { decodeTemplate(it) }

    suspend fun deleteTemplate(id: String) {
        templateDao.delete(id)
    }

    suspend fun snapshotForExport(): LocalExportSnapshot {
        val profile = ensureProfile()
        val workouts = getAllWorkouts().map { workout ->
            val exercises = getExercises(workout.id)
            val sets = getSets(workout.id).groupBy { it.exerciseId }
            WorkoutExport(
                workout = workout,
                exercises = exercises.map { exercise ->
                    ExerciseExport(
                        exercise = exercise,
                        sets = sets[exercise.id].orEmpty(),
                    )
                },
            )
        }
        return LocalExportSnapshot(
            profile = profile.toUserData(),
            character = profile.toCharacter(),
            workouts = workouts,
            templates = getTemplates(limit = 500),
        )
    }

    private fun applyUpdates(
        current: UserProfileEntity,
        updates: Map<String, Any>,
    ): UserProfileEntity {
        var next = current
        updates.forEach { (key, value) ->
            next = when (key) {
                "height" -> next.copy(height = intValue(value, next.height))
                "armLength" -> next.copy(armLength = floatValue(value))
                "musicVolume" -> next.copy(musicVolume = intValue(value, next.musicVolume))
                "level" -> next.copy(level = intValue(value, next.level))
                "coins" -> next.copy(coins = intValue(value, next.coins))
                "exp" -> next.copy(exp = intValue(value, next.exp))
                "streak" -> next.copy(streak = intValue(value, next.streak))
                "last_activity_date" -> next.copy(lastActivityDate = value.toString())
                "last_steps_reward_date" -> next.copy(lastStepsRewardDate = value.toString())
                "last_sleep_reward_date" -> next.copy(lastSleepRewardDate = value.toString())
                "last_weekly_heart_reward_week" -> next.copy(lastWeeklyHeartRewardWeek = value.toString())
                "last_vitality_bonus_date" -> next.copy(lastVitalityBonusDate = value.toString())
                "last_streak_update_date" -> next.copy(lastStreakUpdateDate = value.toString())
                else -> next
            }
        }
        return next
    }

    private fun decodeWorkout(json: String): Workout? = try {
        Workout.fromMap(LocalJson.toMap(json)).takeIf { it.id.isNotBlank() }
    } catch (_: Exception) {
        null
    }

    private fun decodeExercise(json: String): Exercise? = try {
        Exercise.fromMap(LocalJson.toMap(json))
    } catch (_: Exception) {
        null
    }

    private fun decodeSet(json: String, workoutId: String): WorkoutSet? = try {
        val map = LocalJson.toMap(json).toMutableMap()
        if (map["workoutId"] == null) map["workoutId"] = workoutId
        WorkoutSet.fromMap(map)
    } catch (_: Exception) {
        null
    }

    private fun decodeTemplate(entity: LocalTemplateEntity): WorkoutTemplate? = try {
        WorkoutTemplate.fromMap(LocalJson.toMap(entity.payloadJson)).copy(
            id = entity.id,
            name = entity.name.ifBlank { entity.name },
        )
    } catch (_: Exception) {
        null
    }

    private fun intValue(value: Any, fallback: Int): Int = when (value) {
        is Number -> value.toInt()
        is String -> value.toIntOrNull() ?: fallback
        else -> fallback
    }

    private fun floatValue(value: Any): Float? = when (value) {
        is Number -> value.toFloat()
        is String -> value.toFloatOrNull()
        else -> null
    }

    private fun UserProfileEntity.rankOf(branch: SkillBranch): Int = when (branch) {
        SkillBranch.FORM -> skillForm
        SkillBranch.IRON -> skillIron
        SkillBranch.VITALITY -> skillVitality
    }.coerceIn(0, SkillTree.MAX_RANK)

    private fun UserProfileEntity.toSkills(): SkillLoadout = SkillLoadout(
        form = skillForm.coerceIn(0, SkillTree.MAX_RANK),
        iron = skillIron.coerceIn(0, SkillTree.MAX_RANK),
        vitality = skillVitality.coerceIn(0, SkillTree.MAX_RANK),
        lastRespecDate = skillRespecDate,
    )
}
