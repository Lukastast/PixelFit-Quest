package com.pixelfitquest.viewmodel
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.pixelfitquest.helpers.NotificationHelper
import com.pixelfitquest.helpers.SPLASH_SCREEN
import com.pixelfitquest.model.Workout
import com.pixelfitquest.model.Achievement
import com.pixelfitquest.model.UserData
import com.pixelfitquest.model.achievementsList
import com.pixelfitquest.model.missionsPool
import com.pixelfitquest.model.rewardsPool
import com.pixelfitquest.model.service.AccountService
import com.pixelfitquest.repository.UserRepository
import com.pixelfitquest.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.samsung.android.sdk.health.data.*
import com.samsung.android.sdk.health.data.permission.*
import com.samsung.android.sdk.health.data.error.HealthDataException
import com.samsung.android.sdk.health.data.request.DataType
import com.samsung.android.sdk.health.data.request.DataTypes
import com.samsung.android.sdk.health.data.request.LocalDateFilter
import com.samsung.android.sdk.health.data.request.LocalTimeFilter
import com.samsung.android.sdk.health.data.HealthDataService
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.collections.emptyList
import kotlin.random.Random

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accountService: AccountService,
    private val userRepository: UserRepository,
    private val workoutRepository: WorkoutRepository,
    @ApplicationContext private val context: Context
) : PixelFitViewModel() {
    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData.asStateFlow()

    private val _currentMaxExp = MutableStateFlow(100)
    val currentMaxExp: StateFlow<Int> = _currentMaxExp.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _workouts = MutableStateFlow<List<Workout>>(emptyList())
    val workouts: StateFlow<List<Workout>> = _workouts.asStateFlow()

    private val _todaySteps = MutableStateFlow(0L)
    val todaySteps: StateFlow<Long> = _todaySteps.asStateFlow()

    private val _stepGoal = MutableStateFlow(0)
    val stepGoal: StateFlow<Int> = _stepGoal.asStateFlow()

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

    private var healthDataStore: HealthDataStore? = null

    private val stepPermissions = setOf(
        Permission.of(DataTypes.STEPS, AccessType.READ),
        Permission.of(DataTypes.STEPS_GOAL, AccessType.READ)
    )

    fun initialize(restartApp: (String) -> Unit, activity: Activity?) {
        launchCatching {
            accountService.currentUser.collect { user ->
                if (user == null) {
                    restartApp(SPLASH_SCREEN)
                }
            }
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true

                userRepository.loadProgressionConfig()

                loadUserData()

                userData.first { it != null }

                fetchCompletedWorkouts()
                fetchLeaderboard()
                generateDailyMissions()

                initializeHealthConnection(activity)

                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load data"
                _isLoading.value = false
                Log.e("HomeVM", "Initialize error", e)
            }
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                userRepository.getUserData().collect { data ->
                    _userData.value = data
                    if (data != null) {
                        val nextLevel = data.level + 1
                        val maxLevel = userRepository.getMaxLevel()
                        val max = if (nextLevel > maxLevel) {
                            userRepository.getExpRequiredForLevel(maxLevel)
                        } else {
                            userRepository.getExpRequiredForLevel(nextLevel)
                        }
                        _currentMaxExp.value = max
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
                userRepository.updateExp(amount)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update exp"
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

    private fun initializeHealthConnection(activity: Activity?) {
        healthDataStore = activity?.let { HealthDataService.getStore(it) }
        viewModelScope.launch {
            activity?.let { requestPermissionsAndFetchSteps(it) }
        }
    }

    private suspend fun requestPermissionsAndFetchSteps(activity: Activity) {
        val store = healthDataStore ?: return
        try {
            val granted = store.getGrantedPermissions(stepPermissions)
            Log.d("HomeVM", "Granted perms: $granted")
            val missing = stepPermissions - granted
            if (missing.isNotEmpty()) {
                val newlyGranted = store.requestPermissions(missing, activity)
                if (newlyGranted.size < missing.size) {
                    Log.w("HomeVM", "Partial permissions granted: ${missing.size - newlyGranted.size} denied")
                    _error.value = "Partial steps access granted—some features limited"
                }
            }
            fetchStepsData(store)
        } catch (e: HealthDataException) {
            Log.e("HomeVM", "Permission request failed", e)
            _error.value = "Permission error: ${e.message}"
        }
    }

    private suspend fun checkAndAwardStepsReward() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val today = dateFormat.format(Date())
        val lastRewardDate = userRepository.getUserField("last_steps_reward_date") as? String ?: ""
        if (_todaySteps.value >= _stepGoal.value.toLong() && lastRewardDate != today) {
            addExp(50)
            addCoins(10)
            userRepository.updateUserData(mapOf("last_steps_reward_date" to today))
            Log.d("HomeVM", "Awarded +50 EXP and +10 coins for steps goal on $today")
            NotificationHelper.showStepGoalCompletedNotification(context)
        }
    }

    private suspend fun fetchStepsData(store: HealthDataStore) {
        try {
            val now = LocalDateTime.now(ZoneId.systemDefault())
            val startOfDay = now.withHour(0).withMinute(0).withSecond(0).withNano(0)
            val endOfDay = now
            Log.d("HomeVM", "Fetching for range: $startOfDay to $endOfDay")

            val stepFilter = LocalTimeFilter.of(startOfDay, endOfDay)
            val stepRequest = DataType.StepsType.TOTAL.requestBuilder
                .setLocalTimeFilter(stepFilter)
                .build()
            val stepResult = store.aggregateData(stepRequest)
            Log.d("HomeVM", "Steps result size: ${stepResult.dataList.size}")

            val totalSteps = stepResult.dataList.sumOf { it.value ?: 0L }
            _todaySteps.value = totalSteps

            val today = LocalDate.now(ZoneId.systemDefault())
            val tomorrow = today.plusDays(1)
            val goalFilter = LocalDateFilter.of(today, tomorrow)
            val goalRequest = DataType.StepsGoalType.LAST.requestBuilder
                .setLocalDateFilter(goalFilter)
                .build()
            val goalResult = store.aggregateData(goalRequest)
            Log.d("HomeVM", "Goal result size: ${goalResult.dataList.size}")

            val goal = goalResult.dataList.firstOrNull()?.value?.toInt() ?: 0
            _stepGoal.value = goal

            val progressPercent = if (_stepGoal.value > 0) ((_todaySteps.value * 100) / _stepGoal.value).toInt() else 0
            if (progressPercent < 80 && progressPercent > 0) {
                NotificationHelper.showStepGoalReminderNotification(context)
            }

            checkAndAwardStepsReward()
            checkMissionsCompletion()
            Log.d("HomeVM", "Fetched steps: $totalSteps / Goal: $goal")
        } catch (e: HealthDataException) {
            Log.e("HomeVM", "Fetch failed", e)
            _error.value = "Steps fetch error: ${e.message}"
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

    fun refreshSteps(activity: Activity?) {
        val store = healthDataStore
        if (store != null) {
            viewModelScope.launch { fetchStepsData(store) }
        } else {
            initializeHealthConnection(activity)
        }
    }

    fun completeWorkout(workout: Workout) {
        viewModelScope.launch {
            try {
                workoutRepository.saveWorkout(workout)
                addExp(100)
                addCoins(20)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                val today = dateFormat.format(Date())
                val lastStreakUpdateDate = userRepository.getUserField("last_streak_update_date") as? String ?: ""

                if (lastStreakUpdateDate != today) {
                    incrementStreak()
                    userRepository.updateUserData(mapOf("last_streak_update_date" to today))
                }

                NotificationHelper.showWorkoutCompletedNotification(context)
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

        for ((mission, reward) in _dailyMissions.value) {
            if (mission.startsWith("Walk")) {
                val target = mission.split(" ")[1].toLongOrNull() ?: continue
                if (_todaySteps.value >= target) {
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
            NotificationHelper.showMissionCompletedNotification(context, mission, reward)
        }
        _completedMissions.value = currentCompleted
    }
}