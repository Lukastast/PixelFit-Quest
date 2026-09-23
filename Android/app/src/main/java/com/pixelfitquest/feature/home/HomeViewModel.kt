package com.pixelfitquest.feature.home

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.pixelfitquest.feature.customization.model.CharacterData
import com.pixelfitquest.feature.home.model.Achievement
import com.pixelfitquest.feature.home.model.CharacterPose
import com.pixelfitquest.feature.home.model.TimeOfDayProvider
import com.pixelfitquest.feature.home.model.achievementsList
import com.pixelfitquest.feature.missions.WeeklyMissionBoard
import com.pixelfitquest.feature.missions.WeeklyMissionService
import com.pixelfitquest.feature.workout.model.Workout
import com.pixelfitquest.feature.streak.data.WeeklyStreakRepository
import com.pixelfitquest.firebase.model.UserData
import com.pixelfitquest.firebase.repository.UserRepository
import com.pixelfitquest.firebase.repository.WorkoutRepository
import com.pixelfitquest.feature.levels.cosmetics.LocalXpPort
import com.pixelfitquest.firebase.service.AccountService
import com.pixelfitquest.health.HealthConnectStatus
import com.pixelfitquest.health.HealthMetrics
import com.pixelfitquest.health.HealthRepository
import com.pixelfitquest.health.HealthRewards
import com.pixelfitquest.health.HealthTime
import com.pixelfitquest.local.CloudSyncPolicy
import com.pixelfitquest.viewmodel.PixelFitViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accountService: AccountService,
    private val userRepository: UserRepository,
    private val workoutRepository: WorkoutRepository,
    private val cloudSyncPolicy: CloudSyncPolicy,
    private val healthRepository: HealthRepository,
    private val weeklyStreakRepository: WeeklyStreakRepository,
    private val localXpPort: LocalXpPort,
    private val weeklyMissionService: WeeklyMissionService,
    @ApplicationContext private val context: Context,
) : PixelFitViewModel() {
    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData.asStateFlow()

    private val _characterData = MutableStateFlow(CharacterData())
    val characterData: StateFlow<CharacterData> = _characterData.asStateFlow()

    private val _currentMaxExp = MutableStateFlow(100)
    val currentMaxExp: StateFlow<Int> = _currentMaxExp.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _workouts = MutableStateFlow<List<Workout>>(emptyList())
    val workouts: StateFlow<List<Workout>> = _workouts.asStateFlow()

    private val _healthMetrics = MutableStateFlow(HealthMetrics.EMPTY)
    val healthMetrics: StateFlow<HealthMetrics> = _healthMetrics.asStateFlow()

    private val _healthStatus = MutableStateFlow(HealthConnectStatus.UNAVAILABLE)
    val healthStatus: StateFlow<HealthConnectStatus> = _healthStatus.asStateFlow()

    private val healthAwardMutex = Mutex()

    private val missionPrefs: SharedPreferences =
        context.getSharedPreferences("pixelfitquest_prefs", Context.MODE_PRIVATE)

    private val _characterPose = MutableStateFlow(TimeOfDayProvider.getDefaultPoseForCurrentTime())
    val characterPose: StateFlow<CharacterPose> = _characterPose.asStateFlow()

    val weeklyBoard: StateFlow<WeeklyMissionBoard> = weeklyMissionService.board

    private var workoutsLoaded = false

    private val _achievements = MutableStateFlow<List<Pair<Achievement, Boolean>>>(emptyList())
    val achievements: StateFlow<List<Pair<Achievement, Boolean>>> = _achievements.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun cyclePose() {
        _characterPose.value = _characterPose.value.next()
    }

    fun setPose(pose: CharacterPose) {
        _characterPose.value = pose
    }

    fun initialize() {
        refreshHealthMetrics()

        viewModelScope.launch {
            try {
                _isLoading.value = true

                val data = userRepository.fetchUserDataOnce() ?: UserData()
                _userData.value = data
                updateMaxExp(data)

                loadUserData()

                userData.first { it != null }
                grantPendingStreakXp()
                weeklyStreakRepository.reconcile()

                fetchCompletedWorkouts()

                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load data"
                _isLoading.value = false
                Log.e("HomeVM", "Initialize error", e)
            }
        }
        viewModelScope.launch {
            try {
                userRepository.loadProgressionConfig()
                _userData.value?.let { updateMaxExp(it) }
            } catch (e: Exception) {
                Log.w("HomeVM", "Progression config unavailable; using defaults", e)
            }
        }
    }

    private fun updateMaxExp(data: UserData) {
        val nextLevel = data.level + 1
        val maxLevel = userRepository.getMaxLevel()
        _currentMaxExp.value = if (nextLevel > maxLevel) {
            userRepository.getExpRequiredForLevel(maxLevel)
        } else {
            userRepository.getExpRequiredForLevel(nextLevel)
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                userRepository.getUserData().collect { data ->
                    _userData.value = data
                    if (data != null) {
                        updateMaxExp(data)
                    } else {
                        _currentMaxExp.value = 100
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load user data"
                Log.e("HomeVM", "Load user data error", e)
            }
        }
        viewModelScope.launch {
            try {
                userRepository.getCharacterData().collect { data ->
                    if (data != null) {
                        _characterData.value = data
                    }
                }
            } catch (e: Exception) {
                Log.w("HomeVM", "Load character data error", e)
            }
        }
    }

    fun addCoins(amount: Int) {
        if (amount <= 0) return
        viewModelScope.launch {
            try {
                val current = _userData.value ?: return@launch
                userRepository.updateUserData(mapOf("coins" to current.coins + amount))
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update coins"
            }
        }
    }

    fun addExp(amount: Int) {
        if (amount <= 0) return
        viewModelScope.launch {
            try {
                // Single wallet: user_profile via LevelsRepository / UserProgression
                localXpPort.awardXp(amount, "home")
            } catch (e: Exception) {
                Log.w("HomeVM", "XP award failed", e)
            }
        }
    }

    private suspend fun grantPendingStreakXp() {
        try {
            weeklyStreakRepository.withConsumedPendingXp { amount ->
                localXpPort.awardXp(amount, "streak")
            }
        } catch (e: Exception) {
            Log.i("HomeVM", "Streak XP kept on device until account is available")
        }
    }

    fun refreshHealthMetrics() {
        viewModelScope.launch {
            try {
                _healthStatus.value = healthRepository.availability()
                if (_healthStatus.value == HealthConnectStatus.AVAILABLE) {
                    _healthMetrics.value = healthRepository.readTodayMetrics()
                    checkAndAwardHealthRewards()
                }
            } catch (e: Exception) {
                Log.e("HomeVM", "Health Connect refresh failed", e)
            } finally {
                syncWeeklyMissions()
            }
        }
    }

    private suspend fun checkAndAwardHealthRewards() {
        healthAwardMutex.withLock {
            if (_userData.value == null) return
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val today = dateFormat.format(Date())
            val currentWeek = HealthTime.currentWeekIso()
            val metrics = _healthMetrics.value

            val updates = mutableMapOf<String, Any>()

            // 1. Steps Reward
            val lastStepsRewardDate = (userRepository.getUserField("last_steps_reward_date") as? String)
                ?.takeIf { it.isNotBlank() }
                ?: missionPrefs.getString("last_steps_reward_date", "") ?: ""
            if (HealthRewards.shouldAwardDailyGoal(metrics.steps, metrics.stepGoal, lastStepsRewardDate, today)) {
                addExp(HealthRewards.STEPS_REWARD_EXP)
                addCoins(HealthRewards.STEPS_REWARD_COINS)
                updates["last_steps_reward_date"] = today
                missionPrefs.edit().putString("last_steps_reward_date", today).apply()
                Log.d("HomeVM", "Awarded +${HealthRewards.STEPS_REWARD_EXP} EXP and +${HealthRewards.STEPS_REWARD_COINS} coins for steps goal on $today")
            }

            // 2. Sleep Milestone Reward (7-9 hours)
            val lastSleepRewardDate = (userRepository.getUserField("last_sleep_reward_date") as? String)
                ?.takeIf { it.isNotBlank() }
                ?: missionPrefs.getString("last_sleep_reward_date", "") ?: ""
            if (HealthRewards.shouldAwardSleepMilestone(metrics.sleepMinutes, lastSleepRewardDate, today)) {
                addExp(HealthRewards.SLEEP_REWARD_EXP)
                addCoins(HealthRewards.SLEEP_REWARD_COINS)
                updates["last_sleep_reward_date"] = today
                missionPrefs.edit().putString("last_sleep_reward_date", today).apply()
                Log.d("HomeVM", "Awarded +${HealthRewards.SLEEP_REWARD_EXP} EXP and +${HealthRewards.SLEEP_REWARD_COINS} coins for 7-9h sleep on $today")
            }

            // 3. Weekly Heart Goal Reward (>= 150 points)
            val lastWeeklyHeartRewardWeek = (userRepository.getUserField("last_weekly_heart_reward_week") as? String)
                ?.takeIf { it.isNotBlank() }
                ?: missionPrefs.getString("last_weekly_heart_reward_week", "") ?: ""
            if (HealthRewards.shouldAwardWeeklyHeartGoal(metrics.weeklyHeartPoints, metrics.weeklyHeartGoal, lastWeeklyHeartRewardWeek, currentWeek)) {
                addExp(HealthRewards.WEEKLY_HEART_REWARD_EXP)
                addCoins(HealthRewards.WEEKLY_HEART_REWARD_COINS)
                updates["last_weekly_heart_reward_week"] = currentWeek
                missionPrefs.edit().putString("last_weekly_heart_reward_week", currentWeek).apply()
                Log.d("HomeVM", "Awarded +${HealthRewards.WEEKLY_HEART_REWARD_EXP} EXP and +${HealthRewards.WEEKLY_HEART_REWARD_COINS} coins for weekly heart goal on $currentWeek")
            }

            if (updates.isNotEmpty()) {
                userRepository.updateUserData(updates)
            }
        }
    }

    private fun fetchCompletedWorkouts() {
        viewModelScope.launch {
            try {
                val list = workoutRepository.getAllCompletedWorkouts().filter { it.totalExercises > 0 }
                _workouts.value = list.sortedByDescending { it.date }
                workoutsLoaded = true
                val total = list.size
                _achievements.value = achievementsList.map { it to (total >= it.requiredWorkouts) }
                syncWeeklyMissions()
            } catch (e: Exception) {
                _error.value = "Failed to load workout history"
                Log.e("HomeVM", "Error loading workouts", e)
            }
        }
    }

    fun incrementStreak() {
        viewModelScope.launch {
            try {
                userRepository.updateStreak(increment = true)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update streak"
            }
        }
    }

    fun resetStreak() {
        viewModelScope.launch {
            try {
                userRepository.updateStreak(reset = true)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to reset streak"
            }
        }
    }

    fun completeWorkout(workout: Workout) {
        viewModelScope.launch {
            try {
                workoutRepository.saveWorkout(workout)
                addExp(100)
                addCoins(20)
                try {
                    // Also aligns UserProfileEntity.streak / lastActivityDate / lastStreakUpdateDate
                    weeklyStreakRepository.recordCompletedSession(workout.id)
                    grantPendingStreakXp()
                } catch (e: Exception) {
                    Log.i("HomeVM", "Weekly streak record failed", e)
                }
                fetchCompletedWorkouts()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to complete workout"
            }
        }
    }

    suspend fun checkWorkoutReminder(): Boolean {
        val latestWorkouts = workoutRepository.fetchWorkoutsOnce(1)
        val lastWorkoutDateStr = latestWorkouts.firstOrNull()?.date ?: return true
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val today = dateFormat.format(Date())
        return lastWorkoutDateStr != today
    }

    private suspend fun syncWeeklyMissions() {
        if (!workoutsLoaded) return
        weeklyMissionService.sync(_workouts.value, _healthMetrics.value.weeklySteps)
    }
}
