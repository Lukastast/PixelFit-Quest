package com.pixelfitquest.feature.home

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.pixelfitquest.feature.home.model.Achievement
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
import com.pixelfitquest.local.CloudSyncPolicy
import com.pixelfitquest.viewmodel.PixelFitViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
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
) : PixelFitViewModel() {
    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData.asStateFlow()

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

    private val _rank = MutableStateFlow(0)
    val rank: StateFlow<Int> = _rank.asStateFlow()

    private val _totalUsers = MutableStateFlow(0)
    val totalUsers: StateFlow<Int> = _totalUsers.asStateFlow()

    private val _dailyMissions = MutableStateFlow(listOf<Pair<String, String>>())
    val dailyMissions: StateFlow<List<Pair<String, String>>> = _dailyMissions.asStateFlow()

    private val _completedMissions = MutableStateFlow(setOf<String>())
    val completedMissions: StateFlow<Set<String>> = _completedMissions.asStateFlow()

    private val _achievements = MutableStateFlow<List<Pair<Achievement, Boolean>>>(emptyList())
    val achievements: StateFlow<List<Pair<Achievement, Boolean>>> = _achievements.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _leaderboardLocked = MutableStateFlow(true)
    val leaderboardLocked: StateFlow<Boolean> = _leaderboardLocked.asStateFlow()

    fun initialize() {
        refreshHealthMetrics()

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _leaderboardLocked.value = !cloudSyncPolicy.isLeaderboardEnabled()

                val data = userRepository.fetchUserDataOnce() ?: UserData()
                _userData.value = data
                updateMaxExp(data)

                loadUserData()

                userData.first { it != null }
                grantPendingStreakXp()
                weeklyStreakRepository.reconcile()

                fetchCompletedWorkouts()
                if (cloudSyncPolicy.isLeaderboardEnabled()) {
                    fetchLeaderboard()
                }
                generateDailyMissions()
                refreshHealthMetrics()

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

    private suspend fun fetchLeaderboard() {
        try {
            val leaderboard = userRepository.getLeaderboard()
            val uid = accountService.currentUser.first()?.id ?: return
            val position = leaderboard.indexOfFirst { it.first == uid } + 1
            if (position > 0) {
                _rank.value = position
                _totalUsers.value = leaderboard.size
            }
        } catch (e: Exception) {
            _error.value = e.message ?: "Failed to fetch leaderboard"
            Log.e("HomeVM", "Fetch leaderboard error", e)
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
                    checkAndAwardStepsReward()
                    checkMissionsCompletion()
                }
            } catch (e: Exception) {
                Log.e("HomeVM", "Health Connect refresh failed", e)
            } finally {
                _healthReady.value = true
            }
        }
    }

    private suspend fun checkAndAwardStepsReward() {
        healthAwardMutex.withLock {
            if (_userData.value == null) return
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val today = dateFormat.format(Date())
            val lastRewardDate = userRepository.getUserField("last_steps_reward_date") as? String ?: ""
            val metrics = _healthMetrics.value
            if (HealthRewards.shouldAwardDailyGoal(metrics.steps, metrics.stepGoal, lastRewardDate, today)) {
                addExp(50)
                addCoins(10)
                userRepository.updateUserData(mapOf("last_steps_reward_date" to today))
                Log.d("HomeVM", "Awarded +50 EXP and +10 coins for steps goal on $today")
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

    private fun generateDailyMissions() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = dateFormat.format(Date())
        val seed = today.hashCode().toLong()
        val random = Random(seed)
        val selectedMissions = missionsPool.shuffled(random).take(3)
        val selectedRewards = rewardsPool.shuffled(random).take(3)
        _dailyMissions.value = selectedMissions.zip(selectedRewards)
    }

    private fun checkMissionsCompletion() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val today = dateFormat.format(Date())
        val currentCompleted = mutableSetOf<String>()

        val todaysWorkouts = _workouts.value.count { it.date == today }
        val todaySteps = _healthMetrics.value.steps

        for ((mission, reward) in _dailyMissions.value) {
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

        val newCompleted = currentCompleted - _completedMissions.value
        for (mission in newCompleted) {
            val reward = _dailyMissions.value.firstOrNull { it.first == mission }?.second ?: continue
            val (type, valueStr) = reward.split(":")
            val amount = valueStr.toIntOrNull() ?: continue
            if (type == "exp") {
                addExp(amount)
            } else if (type == "coins") {
                addCoins(amount)
            }
        }
        _completedMissions.value = currentCompleted
    }
}
