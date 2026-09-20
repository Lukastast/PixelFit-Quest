package com.pixelfitquest.health

import java.time.Instant

data class HealthMetrics(
    val steps: Long = 0L,
    val stepGoal: Int = DEFAULT_STEP_GOAL,
    val heartRateBpm: Long? = null,
    val sleepMinutes: Long? = null,
    val caloriesBurned: Long? = null,
    val weeklyHeartPoints: Int = 0,
    val weeklyHeartGoal: Int = DEFAULT_WEEKLY_HEART_GOAL,
) {
    val progressPercent: Int
        get() = if (stepGoal <= 0) {
            0
        } else {
            ((steps * 100) / stepGoal).toInt().coerceIn(0, 100)
        }

    val weeklyHeartProgressPercent: Int
        get() = if (weeklyHeartGoal <= 0) {
            0
        } else {
            ((weeklyHeartPoints * 100) / weeklyHeartGoal).coerceIn(0, 100)
        }

    companion object {
        const val DEFAULT_STEP_GOAL = 10_000
        const val DEFAULT_WEEKLY_HEART_GOAL = 150
        val EMPTY = HealthMetrics()
    }
}

enum class HealthConnectStatus {
    AVAILABLE,
    UPDATE_REQUIRED,
    UNAVAILABLE,
}

data class HealthExerciseWrite(
    val title: String,
    val startTime: Instant,
    val endTime: Instant,
)

sealed class HealthWriteResult {
    data object Stubbed : HealthWriteResult()
}

object HealthRewards {
    const val DEFAULT_WEEKLY_HEART_GOAL = 150
    const val SLEEP_HOURS_MIN = 7
    const val SLEEP_HOURS_MAX = 9
    const val SLEEP_MINUTES_MIN = 7 * 60L
    const val SLEEP_MINUTES_MAX = 9 * 60L

    const val STEPS_REWARD_EXP = 50
    const val STEPS_REWARD_COINS = 10

    const val SLEEP_REWARD_EXP = 50
    const val SLEEP_REWARD_COINS = 15

    const val WEEKLY_HEART_REWARD_EXP = 150
    const val WEEKLY_HEART_REWARD_COINS = 30

    fun shouldAwardDailyGoal(
        steps: Long,
        goal: Int,
        lastRewardDate: String,
        todayUtc: String,
    ): Boolean {
        if (goal <= 0) return false
        if (steps < goal.toLong()) return false
        return lastRewardDate != todayUtc
    }

    fun shouldAwardSleepMilestone(
        sleepMinutes: Long?,
        lastRewardDate: String,
        todayUtc: String,
    ): Boolean {
        if (sleepMinutes == null) return false
        if (sleepMinutes !in SLEEP_MINUTES_MIN..SLEEP_MINUTES_MAX) return false
        return lastRewardDate != todayUtc
    }

    fun shouldAwardWeeklyHeartGoal(
        weeklyHeartPoints: Int,
        weeklyGoal: Int = DEFAULT_WEEKLY_HEART_GOAL,
        lastRewardWeek: String,
        currentWeekUtc: String,
    ): Boolean {
        if (weeklyGoal <= 0) return false
        if (weeklyHeartPoints < weeklyGoal) return false
        return lastRewardWeek != currentWeekUtc
    }
}
