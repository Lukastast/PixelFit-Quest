package com.pixelfitquest.viewmodel

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.pixelfitquest.Helpers.SPLASH_SCREEN
import com.pixelfitquest.model.UserGameData
import com.pixelfitquest.model.service.AccountService
import com.pixelfitquest.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId



@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accountService: AccountService,
    private val userRepository: UserRepository
) : PixelFitViewModel() {

    private val _userGameData = MutableStateFlow<UserGameData?>(null)
    val userGameData: StateFlow<UserGameData?> = _userGameData.asStateFlow()

    private val _currentMaxExp = MutableStateFlow(100)  // Default for level 1 to 2
    val currentMaxExp: StateFlow<Int> = _currentMaxExp.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // NEW: Steps states
    private val _todaySteps = MutableStateFlow(0L)
    val todaySteps: StateFlow<Long> = _todaySteps.asStateFlow()

    private val _stepGoal = MutableStateFlow(0)
    val stepGoal: StateFlow<Int> = _stepGoal.asStateFlow()

    // NEW: HealthDataStore
    private var healthDataStore: HealthDataStore? = null

    // NEW: Permissions (using DataTypes for v1.0.0)
    private val stepPermissions = setOf(
        Permission.of(DataTypes.STEPS, AccessType.READ),
        Permission.of(DataTypes.STEPS_GOAL, AccessType.READ)
    )

    fun initialize(restartApp: (String) -> Unit, context: Context) {
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

                // NEW: Initialize Health connection and steps
                initializeHealthConnection(context)
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
                val current = _userGameData.value ?: return@launch
                userRepository.updateUserGameData(mapOf("coins" to current.coins + amount))
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

    // NEW: Connect (Throwable for onConnectionFailed in v1.0.0)
    private fun initializeHealthConnection(context: Context) {
        healthDataStore = HealthDataService.getStore(context)
        // No connection listener or connectService needed—store is ready
        viewModelScope.launch {
            requestPermissionsAndFetchSteps(context as Activity)  // Cast to Activity for permissions
        }
    }

    // NEW: Permissions and fetch (request returns int resultCode)
    private suspend fun requestPermissionsAndFetchSteps(context: Context) {
        val store = healthDataStore ?: return
        try {
            // Check granted permissions (suspend call)
            val granted = store.getGrantedPermissions(stepPermissions)
            val missing = stepPermissions - granted

            if (missing.isNotEmpty()) {
                // Request missing permissions (suspend call; returns newly granted set)
                val newlyGranted = store.requestPermissions(missing, context as Activity)
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

    // NEW: Fetch data (v1.0.0 aggregation via aggregateData)
    private suspend fun fetchStepsData(store: HealthDataStore) {
        try {
            val now = LocalDateTime.now(ZoneId.systemDefault())
            val startOfDay = now.withHour(0).withMinute(0).withSecond(0).withNano(0)
            val endOfDay = now

            // Steps: Total from today (StepsType.TOTAL)
            val stepFilter = LocalTimeFilter.of(startOfDay, endOfDay)
            val stepRequest = DataType.StepsType.TOTAL.requestBuilder
                .setLocalTimeFilter(stepFilter)
                .build()

            val stepResult = store.aggregateData(stepRequest)
            var totalSteps = 0L
            stepResult.dataList.forEach { data ->
                totalSteps += data.value ?: 0L
            }
            _todaySteps.value = totalSteps

            // Goal: Latest today (StepsGoalType.LAST)
            val today = LocalDate.now(ZoneId.systemDefault())
            val tomorrow = today.plusDays(1)
            val goalFilter = LocalDateFilter.of(today, tomorrow)
            val goalRequest = DataType.StepsGoalType.LAST.requestBuilder
                .setLocalDateFilter(goalFilter)
                .build()

            val goalResult = store.aggregateData(goalRequest)
            var goal = 0
            goalResult.dataList.forEach { data ->
                data.value?.let { goal = it.toInt() }
            }
            _stepGoal.value = goal

            Log.d("HomeVM", "Steps: $totalSteps / Goal: $goal")
        } catch (e: HealthDataException) {
            Log.e("HomeVM", "Fetch failed", e)
            _error.value = "Steps fetch error: ${e.message}"
        }
    }

    // NEW: Refresh
    fun refreshSteps(context: Context) {
        val store = healthDataStore
        if (store != null) {
            viewModelScope.launch { fetchStepsData(store) }
        } else {
            initializeHealthConnection(context)
        }
    }
}