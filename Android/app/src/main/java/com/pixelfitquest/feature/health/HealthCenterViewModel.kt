package com.pixelfitquest.feature.health

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.pixelfitquest.feature.achievements.AchievementSyncService
import com.pixelfitquest.feature.levels.cosmetics.LocalXpPort
import com.pixelfitquest.feature.missions.RerollResult
import com.pixelfitquest.feature.missions.WeeklyMissionBoard
import com.pixelfitquest.feature.missions.WeeklyMissionService
import com.pixelfitquest.feature.progression.RewardBonus
import com.pixelfitquest.feature.progression.SkillTree
import com.pixelfitquest.helpers.SnackbarManager
import com.pixelfitquest.firebase.repository.UserRepository
import com.pixelfitquest.firebase.repository.WorkoutRepository
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
import com.pixelfitquest.firebase.model.UserData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

enum class QuestSection {
    MISSIONS,
    ACHIEVEMENTS,
    HEALTH,
}

data class HealthGoalClaims(
    val steps: Boolean = false,
    val sleep: Boolean = false,
    val heart: Boolean = false,
    val vitalityBonus: Boolean = false,
)

@HiltViewModel
class HealthCenterViewModel @Inject constructor(
    private val healthRepository: HealthRepository,
    private val userRepository: UserRepository,
    private val localXpPort: LocalXpPort,
    private val workoutRepository: WorkoutRepository,
    private val weeklyMissionService: WeeklyMissionService,
    private val achievementSyncService: AchievementSyncService,
) : PixelFitViewModel() {

    private val _healthStatus = MutableStateFlow(HealthConnectStatus.UNAVAILABLE)
    val healthStatus: StateFlow<HealthConnectStatus> = _healthStatus.asStateFlow()

    private val _healthMetrics = MutableStateFlow(HealthMetrics.EMPTY)
    val healthMetrics: StateFlow<HealthMetrics> = _healthMetrics.asStateFlow()

    private val _permissionsGranted = MutableStateFlow(false)
    val permissionsGranted: StateFlow<Boolean> = _permissionsGranted.asStateFlow()

    private val _section = MutableStateFlow(QuestSection.MISSIONS)
    val section: StateFlow<QuestSection> = _section.asStateFlow()

    private val _healthClaims = MutableStateFlow(HealthGoalClaims())
    val healthClaims: StateFlow<HealthGoalClaims> = _healthClaims.asStateFlow()

    val weeklyBoard: StateFlow<WeeklyMissionBoard> = weeklyMissionService.board

    val userData: StateFlow<UserData?> = userRepository.getUserData().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    private val healthAwardMutex = Mutex()

    val healthPermissions: Set<String> = HealthPermissions.required()

    init {
        refresh()
    }

    fun onSectionSelected(section: QuestSection) {
        _section.value = section
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
            try {
                refreshHealthClaims()
                val workouts = workoutRepository.getAllCompletedWorkouts()
                weeklyMissionService.sync(workouts, _healthMetrics.value.weeklySteps)
                val steps = maxOf(_healthMetrics.value.steps, _healthMetrics.value.weeklySteps)
                achievementSyncService.sync(workouts, steps)
            } catch (e: Exception) {
                Log.w("HealthCenter", "Quest center refresh failed", e)
            }
        }
    }

    private suspend fun refreshHealthClaims() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val today = dateFormat.format(Date())
        val week = HealthTime.currentWeekIso()
        _healthClaims.value = HealthGoalClaims(
            steps = rewardStamp("last_steps_reward_date") == today,
            sleep = rewardStamp("last_sleep_reward_date") == today,
            heart = rewardStamp("last_weekly_heart_reward_week") == week,
            vitalityBonus = rewardStamp("last_vitality_bonus_date") == today,
        )
    }

    private suspend fun rewardStamp(field: String): String {
        return (userRepository.getUserField(field) as? String).orEmpty()
    }

    private suspend fun checkAndAwardHealthRewards() {
        healthAwardMutex.withLock {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val today = dateFormat.format(Date())
            val currentWeek = HealthTime.currentWeekIso()
            val metrics = _healthMetrics.value

            val updates = mutableMapOf<String, Any>()
            val variant = userRepository.fetchCharacterDataOnce()?.variant
            val vitalityRank = userRepository.skillLoadout().vitality

            // 1. Steps Reward
            val lastStepsRewardDate = userRepository.getUserField("last_steps_reward_date") as? String ?: ""
            if (HealthRewards.shouldAwardDailyGoal(metrics.steps, metrics.stepGoal, lastStepsRewardDate, today)) {
                awardFitnessBonus(
                    HealthRewards.STEPS_REWARD_EXP,
                    SkillTree.applyVitalityCoins(HealthRewards.STEPS_REWARD_COINS, vitalityRank),
                    variant,
                )
                updates["last_steps_reward_date"] = today
            }

            // 2. Sleep Milestone Reward (7-9 hours)
            val lastSleepRewardDate = userRepository.getUserField("last_sleep_reward_date") as? String ?: ""
            if (HealthRewards.shouldAwardSleepMilestone(metrics.sleepMinutes, lastSleepRewardDate, today)) {
                awardFitnessBonus(
                    HealthRewards.SLEEP_REWARD_EXP,
                    SkillTree.applyVitalityCoins(HealthRewards.SLEEP_REWARD_COINS, vitalityRank),
                    variant,
                )
                updates["last_sleep_reward_date"] = today
            }

            // 3. Weekly Heart Goal Reward (>= 150 points)
            val lastWeeklyHeartRewardWeek = userRepository.getUserField("last_weekly_heart_reward_week") as? String ?: ""
            if (HealthRewards.shouldAwardWeeklyHeartGoal(metrics.weeklyHeartPoints, metrics.weeklyHeartGoal, lastWeeklyHeartRewardWeek, currentWeek)) {
                awardFitnessBonus(HealthRewards.WEEKLY_HEART_REWARD_EXP, HealthRewards.WEEKLY_HEART_REWARD_COINS, variant)
                updates["last_weekly_heart_reward_week"] = currentWeek
            }

            // 4. Daily Vitality All Goals Completed Bonus
            val lastVitalityBonusDate = userRepository.getUserField("last_vitality_bonus_date") as? String ?: ""
            if (HealthRewards.shouldAwardVitalityBonus(metrics.rewardedGoalsMet, HealthRewards.REWARDED_GOAL_COUNT, lastVitalityBonusDate, today)) {
                awardFitnessBonus(HealthRewards.VITALITY_BONUS_EXP, HealthRewards.VITALITY_BONUS_COINS, variant)
                updates["last_vitality_bonus_date"] = today
            }

            if (updates.isNotEmpty()) {
                userRepository.updateUserData(updates)
            }
        }
    }

    private fun awardFitnessBonus(xp: Int, coins: Int, variant: String?) {
        val payout = RewardBonus.withFitnessFlat(xp, coins, variant)
        awardExp(payout.xp)
        awardCoins(payout.coins)
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

    fun rerollMission(missionId: String) {
        viewModelScope.launch {
            if (weeklyMissionService.reroll(missionId) == RerollResult.CANT_AFFORD) {
                SnackbarManager.showMessage("Need 50 coins to reroll a mission")
            }
        }
    }

    fun onPermissionsResult(granted: Set<String>) {
        _permissionsGranted.value = HealthPermissions.hasStepsRead(granted)
        refresh()
    }
}
