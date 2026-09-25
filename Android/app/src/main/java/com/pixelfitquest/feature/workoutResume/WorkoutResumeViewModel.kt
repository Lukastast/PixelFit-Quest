package com.pixelfitquest.feature.workoutResume

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelfitquest.feature.customization.model.CharacterData
import com.pixelfitquest.feature.healthbonuses.SessionBonusService
import com.pixelfitquest.feature.progression.RewardBonus
import com.pixelfitquest.feature.progression.RewardSet
import java.time.LocalDate
import com.pixelfitquest.feature.healthbonuses.model.SessionBonus
import com.pixelfitquest.feature.healthbonuses.model.SessionBonusUiState
import com.pixelfitquest.firebase.model.UserData
import com.pixelfitquest.feature.workout.model.ExerciseWithSets
import com.pixelfitquest.feature.workout.model.Workout
import com.pixelfitquest.feature.workoutResume.model.WorkoutSummary
import com.pixelfitquest.feature.streak.data.WeeklyStreakRepository
import com.pixelfitquest.firebase.repository.UserRepository
import com.pixelfitquest.firebase.repository.WorkoutRepository
import com.pixelfitquest.feature.achievements.AchievementSyncService
import com.pixelfitquest.feature.levels.cosmetics.LocalXpPort
import com.pixelfitquest.helpers.SnackbarManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ResumeTab {
    Form,
    Sets,
}

@HiltViewModel
class WorkoutResumeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
    private val userRepository: UserRepository,
    private val weeklyStreakRepository: WeeklyStreakRepository,
    private val localXpPort: LocalXpPort,
    private val sessionBonusService: SessionBonusService,
    private val achievementSyncService: AchievementSyncService,
) : ViewModel() {

    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData.asStateFlow()
    private val _characterData = MutableStateFlow(CharacterData())
    val characterData: StateFlow<CharacterData> = _characterData.asStateFlow()
    private val workoutId: String = savedStateHandle.get<String>("workoutId") ?: ""

    private val _summary = MutableStateFlow(WorkoutSummary(0, 0, 0f))
    val summary: StateFlow<WorkoutSummary> = _summary.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private val _exercisesWithSets = MutableStateFlow<List<ExerciseWithSets>>(emptyList())
    val exercisesWithSets: StateFlow<List<ExerciseWithSets>> = _exercisesWithSets.asStateFlow()
    private val _bonusUi = MutableStateFlow(SessionBonusUiState())
    val bonusUi: StateFlow<SessionBonusUiState> = _bonusUi.asStateFlow()
    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()
    private val _deleted = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val deleted: SharedFlow<Unit> = _deleted.asSharedFlow()

    private val _selectedTab = MutableStateFlow(ResumeTab.Form)
    val selectedTab: StateFlow<ResumeTab> = _selectedTab.asStateFlow()

    private val _selectedSetIndex = MutableStateFlow(0)
    val selectedSetIndex: StateFlow<Int> = _selectedSetIndex.asStateFlow()

    init {
        loaduserData()
        loadCharacterData()
        if (workoutId.isNotBlank()) {
            loadWorkoutData()
        }
    }

    fun selectTab(tab: ResumeTab) {
        _selectedTab.value = tab
    }

    fun selectSetIndex(index: Int) {
        _selectedSetIndex.value = index.coerceAtLeast(0)
    }

    private fun loadCharacterData() {
        viewModelScope.launch {
            try {
                userRepository.getCharacterData().collect { data ->
                    if (data != null) {
                        _characterData.value = data
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun loaduserData() {
        viewModelScope.launch {
            try {
                userRepository.getUserData().collect { data ->
                   _userData.value = data

                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load user data"
            }
        }
    }
    private fun loadWorkoutData() {
        viewModelScope.launch {
            val workout = workoutRepository.getWorkout(workoutId)
            if (workout == null) {
                resolveHealthBonuses()
                return@launch
            }
            loadExercisesAndSets(workout)
        }
    }

    private fun loadExercisesAndSets(workout: Workout) {
        viewModelScope.launch {
            try {
                // 1. Load all exercises for this workout
                val exercises = workoutRepository.getExercisesByWorkoutId(workoutId)

                // 2. Load ALL sets for this workout (no exercise filter yet)
                val allSets = workoutRepository.getSetsByWorkoutId(workoutId)

                // 3. Group sets by exerciseId
                val groupedSets = allSets.groupBy { it.exerciseId }

                // 4. Combine exercise + its sets + calculate average score
                val exercisesWithSetsList = exercises
                    .mapNotNull { exercise ->
                        val exerciseSets = groupedSets[exercise.id] ?: emptyList()
                        if (exerciseSets.isNotEmpty()) {
                            val avgScore = exerciseSets
                                .map { it.formScore }
                                .average()
                                .toFloat()
                                .coerceIn(0f, 100f)

                            ExerciseWithSets(
                                exercise = exercise,
                                sets = exerciseSets,
                                avgFormScore = avgScore
                            )
                        } else null
                    }


                _exercisesWithSets.value = exercisesWithSetsList

                val baseSummary = calculateSummary(exercisesWithSetsList)
                if (workout.rewardsAwarded && workout.awardedXp != null) {
                    _summary.value = baseSummary.copy(
                        totalXp = workout.awardedXp,
                        totalCoins = workout.awardedCoins ?: 0,
                        rewardClipped = workout.rewardClipped,
                    )
                } else if (!workout.rewardsAwarded) {
                    val claim = userRepository.claimWorkoutReward(
                        workoutId = workoutId,
                        sets = exercisesWithSetsList.flatMap { exercise ->
                            exercise.sets.map { set ->
                                RewardSet(reps = set.reps, formScore = set.formScore)
                            }
                        },
                        today = LocalDate.now().toString(),
                    )
                    if (claim != null) {
                        _summary.value = baseSummary.copy(
                            totalXp = claim.xp,
                            totalCoins = claim.coins,
                            rewardClipped = claim.clipped,
                        )
                        if (claim.fresh) {
                            awardRewards(_summary.value)
                        }
                    } else {
                        _summary.value = baseSummary
                    }
                } else {
                    _summary.value = baseSummary
                }
                grantPendingStreakXp()
                achievementSyncService.sync()

            } catch (e: Exception) {
                Log.e("WorkoutResumeVM", "Failed to load exercises/sets", e)
                _exercisesWithSets.value = emptyList()
            }
            resolveHealthBonuses()
        }
    }

    private suspend fun resolveHealthBonuses() {
        try {
            val resolution = sessionBonusService.resolveForWorkout(workoutId)
            _bonusUi.value = SessionBonusUiState(
                loaded = true,
                snapshot = resolution.snapshot,
                bonuses = resolution.bonuses,
            )
            if (resolution.isNewAward) {
                awardHealthBonuses(resolution.bonuses)
            }
        } catch (e: Exception) {
            Log.w("WorkoutResumeVM", "Health session bonuses skipped", e)
            _bonusUi.value = SessionBonusUiState(loaded = true)
        }
    }

    private fun awardHealthBonuses(bonuses: List<SessionBonus>) {
        val xp = bonuses.sumOf { it.xp }
        val coins = bonuses.sumOf { it.coins }
        if (xp <= 0 && coins <= 0) return
        viewModelScope.launch {
            val variant = userRepository.fetchCharacterDataOnce()?.variant
            val payout = RewardBonus.withFitnessFlat(xp, coins, variant)
            if (payout.xp > 0) {
                try {
                    localXpPort.awardXp(payout.xp, "health_bonus")
                    Log.d("ResumeVM", "Added ${payout.xp} health-bonus XP")
                } catch (e: Exception) {
                    Log.w("ResumeVM", "Health-bonus XP award failed", e)
                }
            }
            if (payout.coins > 0) {
                try {
                    val current = userRepository.fetchUserDataOnce() ?: return@launch
                    userRepository.updateUserData(mapOf("coins" to current.coins + payout.coins))
                    Log.d("ResumeVM", "Added ${payout.coins} health-bonus coins")
                } catch (e: Exception) {
                    Log.w("ResumeVM", "Health-bonus coins not saved", e)
                }
            }
        }
    }

    private fun calculateSummary(exercisesWithSets: List<ExerciseWithSets>): WorkoutSummary {
        val allSets = exercisesWithSets.flatMap { it.sets }

        var totalXp = 0
        var totalReps = 0

        allSets.forEach { set ->
            val reps = set.reps.coerceAtLeast(0)
            val score = set.formScore.coerceIn(0f, 100f)

            totalReps += reps

            val multiplier = when {
                score >= 90 -> 2.0
                score >= 80 -> 1.5
                else -> 1.0
            }

            totalXp += (reps * multiplier).toInt()
        }

        val totalCoins = totalReps / 5
        val avgScore = if (allSets.isNotEmpty()) allSets.map { it.formScore }.average().toFloat() else 0f

        return WorkoutSummary(
            totalXp = totalXp,
            totalCoins = totalCoins,
            avgScore = avgScore
        )
    }

    private fun awardRewards(summary: WorkoutSummary) {
        addXp(summary.totalXp)
        addCoins(summary.totalCoins)
    }

    private fun grantPendingStreakXp() {
        viewModelScope.launch {
            try {
                weeklyStreakRepository.withConsumedPendingXp { amount ->
                    localXpPort.awardXp(amount, "streak")
                }
            } catch (e: Exception) {
                Log.i("ResumeVM", "Streak XP kept on device until account is available")
            }
        }
    }

    private fun addXp(amount: Int) {
        if (amount <= 0) return
        viewModelScope.launch {
            try {
                // Single wallet: user_profile via LevelsRepository / UserProgression
                localXpPort.awardXp(amount, "workout")
                Log.d("ResumeVM", "Added $amount XP")
            } catch (e: Exception) {
                Log.w("ResumeVM", "XP award failed", e)
            }
        }
    }

    fun deleteWorkout() {
        if (workoutId.isBlank() || _isDeleting.value) return
        viewModelScope.launch {
            _isDeleting.value = true
            try {
                workoutRepository.deleteWorkout(workoutId)
                try {
                    weeklyStreakRepository.forgetSession(workoutId)
                } catch (e: Exception) {
                    Log.w("WorkoutResumeVM", "Streak session not removed", e)
                }
                _deleted.emit(Unit)
            } catch (e: Exception) {
                Log.e("WorkoutResumeVM", "Failed to delete workout", e)
                _error.value = e.message ?: "Failed to delete workout"
                SnackbarManager.showMessage(e.message ?: "Couldn't delete this workout")
                _isDeleting.value = false
            }
        }
    }

    private fun addCoins(amount: Int) {
        if (amount <= 0) return
        viewModelScope.launch {
            try {
                val current = userRepository.fetchUserDataOnce()
                    ?: _userData.value
                    ?: return@launch
                userRepository.updateUserData(mapOf("coins" to current.coins + amount))
                Log.d("ResumeVM", "Added $amount Coins")
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update coins"
            }
        }
    }



}
