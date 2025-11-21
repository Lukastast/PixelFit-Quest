package com.pixelfitquest.viewmodel

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.pixelfitquest.Helpers.SPLASH_SCREEN
import com.pixelfitquest.model.UserGameData
import com.pixelfitquest.model.Workout
import com.pixelfitquest.model.service.AccountService
import com.pixelfitquest.repository.UserRepository
import com.pixelfitquest.repository.WorkoutRepository
import com.pixelfitquest.utils.NotificationHelper
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.collections.emptyList

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accountService: AccountService,
    private val userRepository: UserRepository,
    private val workoutRepository: WorkoutRepository,
    @ApplicationContext private val context: Context  // NEW: Injected app context
) : PixelFitViewModel() {

    private val _userGameData = MutableStateFlow<UserGameData?>(null)
    val userGameData: StateFlow<UserGameData?> = _userGameData.asStateFlow()

    private val _currentMaxExp = MutableStateFlow(100)  // Default for level 1 to 2
    val currentMaxExp: StateFlow<Int> = _currentMaxExp.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _workouts = MutableStateFlow<List<Workout>>(emptyList())
    val workouts: StateFlow<List<Workout>> = _workouts.asStateFlow()

    // NEW: Steps states
    private val _todaySteps = MutableStateFlow(0L)
    val todaySteps: StateFlow<Long> = _todaySteps.asStateFlow()

    private val _stepGoal = MutableStateFlow(0)
    val stepGoal: StateFlow<Int> = _stepGoal.asStateFlow()

    // NEW: Leaderboard states
    private val _rank = MutableStateFlow(0)
    val rank: StateFlow<Int> = _rank.asStateFlow()

    private val _totalUsers = MutableStateFlow(0)
    val totalUsers: StateFlow<Int> = _totalUsers.asStateFlow()

    // NEW: HealthDataStore
    private var healthDataStore: HealthDataStore? = null

    // NEW: Permissions (using DataTypes for v1.0.0)
    private val stepPermissions = setOf(
        Permission.of(DataTypes.STEPS, AccessType.READ),
        Permission.of(DataTypes.STEPS_GOAL, AccessType.READ)
    )

    // UPDATED: Changed param to Activity (fixes unsafe cast / type mismatch)
    fun initialize(restartApp: (String) -> Unit, activity: Activity?) {
        // Auth subscription: Restart on logout
        launchCatching {
            accountService.currentUser.collect { user ->
                if (user == null) {
                    restartApp(SPLASH_SCREEN)
                }
            }
        }

        // Load progression config first (for XP calculations), then load user data
        viewModelScope.launch {
            try {
                userRepository.loadProgressionConfig()
                // Only load user data after config is loaded
                loadUserData()
                fetchCompletedWorkouts()

                // NEW: Fetch leaderboard after user data
                fetchLeaderboard()

                // NEW: Initialize Health connection and steps
                initializeHealthConnection(activity)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load progression config"
            }
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                userRepository.getUserGameData().collect { data ->
                    _userGameData.value = data
                    // Fixed: Update maxExp for progress toward NEXT level (level + 1)
                    if (data != null) {
                        val nextLevel = data.level + 1
                        val maxLevel = userRepository.getMaxLevel()
                        val max = if (nextLevel > maxLevel) {
                            // At max level: Use exp for max level (progress toward "staying max")
                            userRepository.getExpRequiredForLevel(maxLevel)
                        } else {
                            userRepository.getExpRequiredForLevel(nextLevel)
                        }
                        _currentMaxExp.value = max
                    } else {
                        _currentMaxExp.value = 100  // Fallback for level 1 to 2
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load user data"
            }
        }
    }

    fun addCoins(amount: Int) {
        if (amount <= 0) return
        viewModelScope.launch {
            try {
                val charData = userRepository.fetchCharacterDataOnce() ?: return@launch
                val variant = charData.variant
                val isFitness = variant.contains("fitness")
                val bonusAmount = if (isFitness) amount + 2 else amount
                val current = _userGameData.value ?: return@launch
                userRepository.updateUserGameData(mapOf("coins" to current.coins + bonusAmount))
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update coins"
            }
        }
    }

    fun addExp(amount: Int) {
        if (amount <= 0) return
        viewModelScope.launch {
            try {
                val charData = userRepository.fetchCharacterDataOnce() ?: return@launch
                val variant = charData.variant
                val isFitness = variant.contains("fitness")
                val bonusAmount = if (isFitness) amount + 2 else amount
                userRepository.updateExp(bonusAmount)
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

    fun resetUserData() {
        viewModelScope.launch {
            try {
                userRepository.updateUserGameData(mapOf(
                    "level" to 1,
                    "exp" to 0,
                    "coins" to 0,  // Optional: Reset coins too
                    "streak" to 0  // Optional: Reset streak
                ))
                Log.d("HomeVM", "User data reset to level 1")
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to reset user data"
            }
        }
    }

    fun resetUnlockedVariants() {
        viewModelScope.launch {
            try {
                userRepository.resetUnlockedVariants()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to reset unlocked variants"
            }
        }
    }

    // NEW: Fetch and compute leaderboard rank
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
        }
    }

    // UPDATED: Changed param to Activity (fixes unsafe cast / type mismatch)
    private fun initializeHealthConnection(activity: Activity?) {
        healthDataStore = activity?.let { HealthDataService.getStore(it) }
        // No connection listener or connectService needed—store is ready
        viewModelScope.launch {
            activity?.let { requestPermissionsAndFetchSteps(it) }
        }
    }

    // UPDATED: Changed param to Activity (fixes unsafe cast / type mismatch)
    private suspend fun requestPermissionsAndFetchSteps(activity: Activity) {
        val store = healthDataStore ?: return
        try {
            // Check granted permissions (suspend call)
            val granted = store.getGrantedPermissions(stepPermissions)
            Log.d("HomeVM", "Granted perms: $granted")  // NEW: Debug log
            val missing = stepPermissions - granted

            if (missing.isNotEmpty()) {
                // Request missing permissions (suspend call; returns newly granted set)
                val newlyGranted = store.requestPermissions(missing, activity)
                if (newlyGranted.size < missing.size) {
                    // Partial grant—log warning, but proceed (user may have denied some)
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

    // NEW: Check and award steps goal reward (once per UTC day)
    private suspend fun checkAndAwardStepsReward() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val today = dateFormat.format(Date())

        val lastRewardDate = userRepository.getUserField("last_steps_reward_date") as? String ?: ""
        if (_todaySteps.value >= _stepGoal.value.toLong() && lastRewardDate != today) {
            addExp(50)
            addCoins(10)
            userRepository.updateUserGameData(mapOf("last_steps_reward_date" to today))
            Log.d("HomeVM", "Awarded +50 EXP and +10 coins for steps goal on $today")

            // NEW: Show notification
            NotificationHelper.showStepGoalCompletedNotification(context)
        }
    }

    // NEW: Fetch data (v1.0.0 aggregation via aggregateData)
    private suspend fun fetchStepsData(store: HealthDataStore) {
        try {
            val now = LocalDateTime.now(ZoneId.systemDefault())
            val startOfDay = now.withHour(0).withMinute(0).withSecond(0).withNano(0)
            val endOfDay = now
            Log.d("HomeVM", "Fetching for range: $startOfDay to $endOfDay")  // NEW: Debug log

            // Steps: Total from today (StepsType.TOTAL)
            val stepFilter = LocalTimeFilter.of(startOfDay, endOfDay)
            val stepRequest = DataType.StepsType.TOTAL.requestBuilder
                .setLocalTimeFilter(stepFilter)
                .build()

            val stepResult = store.aggregateData(stepRequest)
            Log.d("HomeVM", "Steps result size: ${stepResult.dataList.size}")  // NEW: Debug log
            // UPDATED: Use sumOf for idiomatic accumulation (nitpick fix)
            val totalSteps = stepResult.dataList.sumOf { it.value ?: 0L }
            _todaySteps.value = totalSteps

            // Goal: Latest today (StepsGoalType.LAST)
            val today = LocalDate.now(ZoneId.systemDefault())
            val tomorrow = today.plusDays(1)
            val goalFilter = LocalDateFilter.of(today, tomorrow)
            val goalRequest = DataType.StepsGoalType.LAST.requestBuilder
                .setLocalDateFilter(goalFilter)
                .build()

            val goalResult = store.aggregateData(goalRequest)
            Log.d("HomeVM", "Goal result size: ${goalResult.dataList.size}")  // NEW: Debug log
            // UPDATED: Use firstOrNull for cleaner LAST processing
            val goal = goalResult.dataList.firstOrNull()?.value?.toInt() ?: 0
            _stepGoal.value = goal

            // NEW: Progress check and reminder
            val progressPercent = if (_stepGoal.value > 0) ((_todaySteps.value * 100) / _stepGoal.value).toInt() else 0
            // Nudge if progress is low (e.g., <80%—adjust threshold/time of day as needed)
            if (progressPercent < 80 && progressPercent > 0) {
                NotificationHelper.showStepGoalReminderNotification(context)
            }
            // If goal met but not rewarded (though checkAndAwardStepsReward already handles reward + notification)
            checkAndAwardStepsReward()

            Log.d("HomeVM", "Fetched steps: $totalSteps / Goal: $goal")  // NEW: Success log

        } catch (e: HealthDataException) {
            Log.e("HomeVM", "Fetch failed", e)
            _error.value = "Steps fetch error: ${e.message}"
        }
    }

    private fun fetchCompletedWorkouts() {
        viewModelScope.launch {
            try {
                val list = workoutRepository.getAllCompletedWorkouts()  // ← You'll add this method
                _workouts.value = list.sortedByDescending { it.date }  // Latest first
            } catch (e: Exception) {
                _error.value = "Failed to load workout history"
                Log.e("HomeVM", "Error loading workouts", e)
            }
        }
    }

    // UPDATED: Changed param to Activity (fixes unsafe cast / type mismatch)
    fun refreshSteps(activity: Activity?) {
        val store = healthDataStore
        if (store != null) {
            viewModelScope.launch { fetchStepsData(store) }
        } else {
            initializeHealthConnection(activity)
        }
    }

    // NEW: Call this when a workout is completed (e.g., from your workout UI/ViewModel)
    fun completeWorkout(workout: Workout) {  // Pass the Workout object or ID as needed
        viewModelScope.launch {
            try {
                // Save or update as completed (adapt to your model; assume Workout has a 'completed' field)
                workoutRepository.saveWorkout(workout.apply { /* Set completed = true if needed */ })
                // Or: workoutRepository.updateWorkout(workout.id, mapOf("completed" to true))

                // Award rewards
                addExp(100)  // Example amount
                addCoins(20)
                incrementStreak()

                // Show notification
                NotificationHelper.showWorkoutCompletedNotification(context)

                // Refresh workouts list
                fetchCompletedWorkouts()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to complete workout"
            }
        }
    }

    // NEW: Check for no workout today (used in worker or on app start)
    suspend fun checkWorkoutReminder(): Boolean {
        val latestWorkouts = workoutRepository.fetchWorkoutsOnce(1)
        val lastWorkoutDateStr = latestWorkouts.firstOrNull()?.date ?: return true  // No workouts ever: Remind
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val today = dateFormat.format(Date())

        return lastWorkoutDateStr != today  // Return true if reminder needed
    }

    fun testStepGoalCompletedNotification() {
        NotificationHelper.showStepGoalCompletedNotification(context)
    }

    fun testWorkoutCompletedNotification() {
        NotificationHelper.showWorkoutCompletedNotification(context)
    }

    fun testWorkoutReminderNotification() {
        NotificationHelper.showWorkoutReminderNotification(context)
    }

    fun testStepGoalReminderNotification() {
        NotificationHelper.showStepGoalReminderNotification(context)
    }
}