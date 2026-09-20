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
import com.pixelfitquest.feature.home.model.missionsPool
import com.pixelfitquest.feature.home.model.rewardsPool
import com.pixelfitquest.feature.workout.model.Workout
import com.pixelfitquest.feature.streak.data.WeeklyStreakRepository
import com.pixelfitquest.firebase.model.UserData
import com.pixelfitquest.firebase.repository.UserRepository
import com.pixelfitquest.firebase.repository.WorkoutRepository
import com.pixelfitquest.feature.levels.cosmetics.LocalXpPort
import com.pixelfitquest.firebase.service.AccountService
import com.pixelfitquest.health.HealthConnectStatus
import com.pixelfitquest.health.HealthMetrics
import com.pixelfitquest.health.HealthPermissions
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
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accountService: AccountService,
    private val userRepository: UserRepository,
    private val workoutRepository: WorkoutRepository,
    private val cloudSyncPolicy: CloudSyncPolicy,
    private val healthRepository: HealthRepository,
    private val weeklyStreakRepository: WeeklyStreakRepository,
    private val localXpPort: LocalXpPort,
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

    private val _healthPermissionsGranted = MutableStateFlow(false)
    val healthPermissionsGranted: StateFlow<Boolean> = _healthPermissionsGranted.asStateFlow()

    private val _healthReady = MutableStateFlow(false)
    val healthReady: StateFlow<Boolean> = _healthReady.asStateFlow()

    val healthPermissions: Set<String> = HealthPermissions.required()

    private val healthAwardMutex = Mutex()

    private val missionPrefs: SharedPreferences =
        context.getSharedPreferences("pixelfitquest_prefs", Context.MODE_PRIVATE)

    private val _characterPose = MutableStateFlow(TimeOfDayProvider.getDefaultPoseForCurrentTime())
    val characterPose: StateFlow<CharacterPose> = _characterPose.asStateFlow()

    private val _weeklyMissions = MutableStateFlow(listOf<Pair<String, String>>())
    val weeklyMissions: StateFlow<List<Pair<String, String>>> = _weeklyMissions.asStateFlow()

    private val _completedMissions = MutableStateFlow(
        missionPrefs.getString("completed_missions_${getIsoWeekKey()}", "")
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?.toSet()
            ?: emptySet()
    )
    val completedMissions: StateFlow<Set<String>> = _completedMissions.asStateFlow()

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
        generateWeeklyMissions()
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

    fun onHealthPermissionsResult(granted: Set<String>) {
        _healthPermissionsGranted.value = HealthPermissions.hasStepsRead(granted)
        viewModelScope.launch { refreshHealthMetrics() }
    }

    fun refreshHealthMetrics() {
        viewModelScope.launch {
            try {
                _healthStatus.value = healthRepository.availability()
                val granted = healthRepository.grantedPermissions()
                _healthPermissionsGranted.value = HealthPermissions.hasStepsRead(granted)
                if (_healthStatus.value == HealthConnectStatus.AVAILABLE) {
                    _healthMetrics.value = healthRepository.readTodayMetrics()
                    checkAndAwardHealthRewards()
                    checkMissionsCompletion()
                }
            } catch (e: Exception) {
                Log.e("HomeVM", "Health Connect refresh failed", e)
            } finally {
                _healthReady.value = true
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
                val list = workoutRepository.getAllCompletedWorkouts()
                _workouts.value = list.sortedByDescending { it.date }
                val total = list.size
                _achievements.value = achievementsList.map { it to (total >= it.requiredWorkouts) }
                checkMissionsCompletion()
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

    /** Returns an ISO week string like "2026-W38" used as the persistence key. */
    private fun getIsoWeekKey(): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US)
        cal.minimalDaysInFirstWeek = 4 // ISO 8601
        cal.firstDayOfWeek = Calendar.MONDAY
        val year = cal.get(Calendar.YEAR)
        val week = cal.get(Calendar.WEEK_OF_YEAR)
        return "$year-W${week.toString().padStart(2, '0')}"
    }

    private fun generateWeeklyMissions() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US)
        cal.minimalDaysInFirstWeek = 4
        cal.firstDayOfWeek = Calendar.MONDAY
        val weekSeed = cal.get(Calendar.YEAR) * 100L + cal.get(Calendar.WEEK_OF_YEAR)
        val random = Random(weekSeed)
        val selectedMissions = missionsPool.shuffled(random).take(3)
        val selectedRewards = rewardsPool.shuffled(random).take(3)
        _weeklyMissions.value = selectedMissions.zip(selectedRewards)
    }

    private fun checkMissionsCompletion() {
        if (_weeklyMissions.value.isEmpty()) return

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val today = dateFormat.format(Date())
        val currentCompleted = mutableSetOf<String>()

        // Fix: use startsWith so ISO timestamps like "2026-09-18T12:00:00Z" match "2026-09-18"
        val todaysWorkouts = _workouts.value.count { it.date.startsWith(today) }
        val todaySteps = _healthMetrics.value.steps

        for ((mission, reward) in _weeklyMissions.value) {
            if (mission.startsWith("Walk")) {
                val target = mission.split(" ")[1].toLongOrNull() ?: continue
                if (todaySteps >= target) {
                    currentCompleted.add(mission)
                }
            } else if (mission.startsWith("Complete")) {
                val target = mission.split(" ")[1].toIntOrNull() ?: continue
                if (todaysWorkouts >= target) {
                    currentCompleted.add(mission)
                }
            }
        }

        // Only award missions that have not been completed before in this week
        val newCompleted = currentCompleted - _completedMissions.value
        for (mission in newCompleted) {
            val reward = _weeklyMissions.value.firstOrNull { it.first == mission }?.second ?: continue
            val parts = reward.split(":")
            if (parts.size < 2) continue
            val type = parts[0].trim()
            val amount = parts[1].trim().toIntOrNull() ?: continue
            if (type == "exp") {
                addExp(amount)
            } else if (type == "coins") {
                addCoins(amount)
            }
        }

        // Missions accumulate and are never un-completed during the week
        val allCompleted = _completedMissions.value + currentCompleted
        _completedMissions.value = allCompleted

        // Persist so missions aren't re-awarded after ViewModel recreation or app restart
        missionPrefs.edit()
            .putString("completed_missions_${getIsoWeekKey()}", allCompleted.joinToString(","))
            .apply()
    }
}
