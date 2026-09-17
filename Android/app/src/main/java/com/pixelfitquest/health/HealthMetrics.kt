package com.pixelfitquest.health

import java.time.Instant

data class HealthMetrics(
    val steps: Long = 0L,
    val stepGoal: Int = DEFAULT_STEP_GOAL,
    val heartRateBpm: Long? = null,
    val sleepMinutes: Long? = null,
    val caloriesBurned: Long? = null,
) {
    val progressPercent: Int
        get() = if (stepGoal <= 0) {
            0
        } else {
            ((steps * 100) / stepGoal).toInt().coerceIn(0, 100)
        }

    companion object {
        const val DEFAULT_STEP_GOAL = 10_000
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
}
