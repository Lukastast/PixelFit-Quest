package com.pixelfitquest.feature.health

import androidx.lifecycle.viewModelScope
import com.pixelfitquest.feature.levels.cosmetics.LocalXpPort
import com.pixelfitquest.firebase.repository.UserRepository
import com.pixelfitquest.health.HealthConnectStatus
import com.pixelfitquest.health.HealthMetrics
import com.pixelfitquest.health.HealthPermissions
import com.pixelfitquest.health.HealthRepository
import com.pixelfitquest.health.HealthRewards
import com.pixelfitquest.health.HealthTime
import com.pixelfitquest.viewmodel.PixelFitViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class HealthCenterViewModel @Inject constructor(
    private val healthRepository: HealthRepository,
    private val userRepository: UserRepository,
    private val localXpPort: LocalXpPort,
) : PixelFitViewModel() {

    private val _healthStatus = MutableStateFlow(HealthConnectStatus.UNAVAILABLE)
    val healthStatus: StateFlow<HealthConnectStatus> = _healthStatus.asStateFlow()

    private val _healthMetrics = MutableStateFlow(HealthMetrics.EMPTY)
    val healthMetrics: StateFlow<HealthMetrics> = _healthMetrics.asStateFlow()

    private val _permissionsGranted = MutableStateFlow(false)
    val permissionsGranted: StateFlow<Boolean> = _permissionsGranted.asStateFlow()

    private val healthAwardMutex = Mutex()

    val healthPermissions: Set<String> = HealthPermissions.required()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                _healthStatus.value = healthRepository.availability()
                val granted = healthRepository.grantedPermissions()
                _permissionsGranted.value = HealthPermissions.hasStepsRead(granted)
                if (_healthStatus.value == HealthConnectStatus.AVAILABLE) {
                    _healthMetrics.value = healthRepository.readTodayMetrics()
                    checkAndAwardHealthRewards()
                }
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun checkAndAwardHealthRewards() {
        healthAwardMutex.withLock {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val today = dateFormat.format(Date())
            val currentWeek = HealthTime.currentWeekIso()
            val metrics = _healthMetrics.value

            val updates = mutableMapOf<String, Any>()

            // 1. Steps Reward
            val lastStepsRewardDate = userRepository.getUserField("last_steps_reward_date") as? String ?: ""
            if (HealthRewards.shouldAwardDailyGoal(metrics.steps, metrics.stepGoal, lastStepsRewardDate, today)) {
                awardExp(HealthRewards.STEPS_REWARD_EXP)
                awardCoins(HealthRewards.STEPS_REWARD_COINS)
                updates["last_steps_reward_date"] = today
            }

            // 2. Sleep Milestone Reward (7-9 hours)
            val lastSleepRewardDate = userRepository.getUserField("last_sleep_reward_date") as? String ?: ""
            if (HealthRewards.shouldAwardSleepMilestone(metrics.sleepMinutes, lastSleepRewardDate, today)) {
                awardExp(HealthRewards.SLEEP_REWARD_EXP)
                awardCoins(HealthRewards.SLEEP_REWARD_COINS)
                updates["last_sleep_reward_date"] = today
            }

            // 3. Weekly Heart Goal Reward (>= 150 points)
            val lastWeeklyHeartRewardWeek = userRepository.getUserField("last_weekly_heart_reward_week") as? String ?: ""
            if (HealthRewards.shouldAwardWeeklyHeartGoal(metrics.weeklyHeartPoints, metrics.weeklyHeartGoal, lastWeeklyHeartRewardWeek, currentWeek)) {
                awardExp(HealthRewards.WEEKLY_HEART_REWARD_EXP)
                awardCoins(HealthRewards.WEEKLY_HEART_REWARD_COINS)
                updates["last_weekly_heart_reward_week"] = currentWeek
            }

            if (updates.isNotEmpty()) {
                userRepository.updateUserData(updates)
            }
        }
    }

    private fun awardCoins(amount: Int) {
        viewModelScope.launch {
            try {
                val currentCoins = (userRepository.getUserField("coins") as? Number)?.toInt() ?: 0
                userRepository.updateUserData(mapOf("coins" to currentCoins + amount))
            } catch (_: Exception) {
            }
        }
    }

    private fun awardExp(amount: Int) {
        viewModelScope.launch {
            try {
                localXpPort.awardXp(amount, "health_center")
            } catch (_: Exception) {
            }
        }
    }

    fun onPermissionsResult(granted: Set<String>) {
        _permissionsGranted.value = HealthPermissions.hasStepsRead(granted)
        refresh()
    }
}
