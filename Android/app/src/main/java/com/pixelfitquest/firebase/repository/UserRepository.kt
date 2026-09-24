package com.pixelfitquest.firebase.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.pixelfitquest.feature.customization.model.CharacterData
import com.pixelfitquest.feature.levels.progression.LevelCurve
import com.pixelfitquest.feature.progression.ClaimedWorkoutReward
import com.pixelfitquest.feature.progression.RespecOutcome
import com.pixelfitquest.feature.progression.RewardSet
import com.pixelfitquest.feature.progression.SkillBranch
import com.pixelfitquest.feature.progression.SkillLoadout
import java.time.LocalDate
import com.pixelfitquest.firebase.model.UserData
import com.pixelfitquest.local.CloudSyncPolicy
import com.pixelfitquest.local.LocalPixelFitStore
import com.pixelfitquest.local.UserProgression
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
@ViewModelScoped
class UserRepository constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val localStore: LocalPixelFitStore,
    private val cloudSyncPolicy: CloudSyncPolicy,
) {
    private val usersCollection = firestore.collection("users")
    private val TAG = "UserRepository"

    companion object {
        private const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L
    }

    fun getUserData(): Flow<UserData?> = localStore.observeUserData().map { it }

    suspend fun fetchUserDataOnce(): UserData? = localStore.getUserData()

    suspend fun initUserData() {
        localStore.ensureProfile()
    }

    suspend fun updateUserData(updates: Map<String, Any>) {
        localStore.updateUserData(updates)
    }

    suspend fun getUserField(field: String): Any? = localStore.getUserField(field)

    /**
     * The level curve lives on the phone in [LevelCurve]. A remote table used to
     * override levels 1–30 and would fight the level-100 balance.
     */
    suspend fun loadProgressionConfig() {
        Log.d(TAG, "Using local level curve through ${LevelCurve.MAX_LEVEL}")
    }

    fun getMaxLevel(): Int = LevelCurve.MAX_LEVEL

    fun getExpRequiredForLevel(level: Int): Int = LevelCurve.xpToAdvance(level)

    suspend fun updateExp(amount: Int) {
        if (amount <= 0) return
        localStore.replaceProfile { current ->
            val result = UserProgression.applyExp(
                level = current.level,
                exp = current.exp,
                amount = amount,
                maxLevel = LevelCurve.MAX_LEVEL,
                expRequiredForLevel = LevelCurve::xpToAdvance,
            )
            val coins = current.coins + LevelCurve.coinsForLevels(current.level, result.level)
            current.copy(level = result.level, exp = result.exp, coins = coins)
        }
    }

    suspend fun updateStreak(increment: Boolean = true, reset: Boolean = false) {
        localStore.replaceProfile { current ->
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val today = dateFormat.format(Date())
            val yesterday = dateFormat.format(Date(System.currentTimeMillis() - MILLIS_PER_DAY))
            val result = UserProgression.applyStreak(
                currentStreak = current.streak,
                lastActivityDate = current.lastActivityDate,
                today = today,
                yesterday = yesterday,
                increment = increment,
                reset = reset,
            )
            current.copy(
                streak = result.streak,
                lastActivityDate = result.lastActivityDate,
            )
        }
    }

    suspend fun saveCharacterData(data: CharacterData) {
        localStore.saveCharacter(data)
    }

    fun getCharacterData(): Flow<CharacterData?> = localStore.observeCharacter().map { it }

    suspend fun fetchCharacterDataOnce(): CharacterData? = localStore.getCharacter()

    fun observeSkills(): Flow<SkillLoadout> = localStore.observeSkills()

    suspend fun skillLoadout(): SkillLoadout = localStore.skillLoadout()

    suspend fun spendSkillPoint(branch: SkillBranch): Boolean = localStore.spendSkillPoint(branch)

    suspend fun respecSkills(today: LocalDate = LocalDate.now()): RespecOutcome =
        localStore.respecSkills(today)

    suspend fun claimWorkoutReward(
        workoutId: String,
        sets: List<RewardSet>,
        today: String = LocalDate.now().toString(),
    ): ClaimedWorkoutReward? = localStore.claimWorkoutReward(workoutId, sets, today)

    suspend fun resetUnlockedVariants() {
        localStore.resetUnlockedVariants()
    }

    suspend fun getLeaderboard(): List<Pair<String, UserData>> {
        if (!cloudSyncPolicy.isLeaderboardEnabled()) {
            return emptyList()
        }
        val user = auth.currentUser ?: return emptyList()
        return try {
            val snapshot = usersCollection.get().await()
            snapshot.documents.mapNotNull { doc ->
                val data = doc.toObject<UserData>() ?: return@mapNotNull null
                Pair(doc.id, data)
            }.sortedWith(
                compareByDescending<Pair<String, UserData>> { it.second.level }
                    .thenByDescending { it.second.exp }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch leaderboard", e)
            emptyList()
        }.also { _ ->
            Log.d(TAG, "Leaderboard fetch for uid=${user.uid}")
        }
    }
}
