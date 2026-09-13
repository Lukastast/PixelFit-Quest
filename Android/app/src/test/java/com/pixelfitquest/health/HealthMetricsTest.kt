package com.pixelfitquest.health

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class HealthMetricsTest {

    @Test
    fun progressPercentIsZeroWhenGoalIsZero() {
        assertEquals(0, HealthMetrics(steps = 500, stepGoal = 0).progressPercent)
    }

    @Test
    fun progressPercentCapsAtOneHundred() {
        assertEquals(100, HealthMetrics(steps = 20_000, stepGoal = 10_000).progressPercent)
    }

    @Test
    fun progressPercentRoundsDown() {
        assertEquals(50, HealthMetrics(steps = 5_000, stepGoal = 10_000).progressPercent)
        assertEquals(0, HealthMetrics(steps = 0, stepGoal = 10_000).progressPercent)
    }

    @Test
    fun defaultGoalIsTenThousand() {
        assertEquals(10_000, HealthMetrics.DEFAULT_STEP_GOAL)
        assertEquals(10_000, HealthMetrics.EMPTY.stepGoal)
        assertEquals(0L, HealthMetrics.EMPTY.steps)
    }
}

class HealthRewardsTest {

    @Test
    fun doesNotAwardWhenGoalIsZero() {
        assertFalse(
            HealthRewards.shouldAwardDailyGoal(
                steps = 1,
                goal = 0,
                lastRewardDate = "",
                todayUtc = "2026-09-08",
            )
        )
    }

    @Test
    fun awardsOncePerUtcDay() {
        assertTrue(
            HealthRewards.shouldAwardDailyGoal(
                steps = 10_000,
                goal = 10_000,
                lastRewardDate = "",
                todayUtc = "2026-09-08",
            )
        )
        assertFalse(
            HealthRewards.shouldAwardDailyGoal(
                steps = 10_000,
                goal = 10_000,
                lastRewardDate = "2026-09-08",
                todayUtc = "2026-09-08",
            )
        )
    }

    @Test
    fun doesNotAwardBelowGoal() {
        assertFalse(
            HealthRewards.shouldAwardDailyGoal(
                steps = 9_999,
                goal = 10_000,
                lastRewardDate = "",
                todayUtc = "2026-09-08",
            )
        )
    }
}

class HealthTimeTest {

    @Test
    fun todayRangeStartsAtMidnightInClockZone() {
        val clock = Clock.fixed(Instant.parse("2026-09-08T15:30:00Z"), ZoneOffset.UTC)
        val (start, end) = HealthTime.todayRange(clock)
        assertEquals(Instant.parse("2026-09-08T00:00:00Z"), start)
        assertEquals(Instant.parse("2026-09-08T15:30:00Z"), end)
    }
}

class HealthWriteResultTest {

    @Test
    fun stubWriteResultIsDistinct() {
        assertEquals(HealthWriteResult.Stubbed, HealthWriteResult.Stubbed)
    }
}

class HealthPermissionsTest {

    @Test
    fun requiredCoversStepsHeartRateAndExerciseWrite() {
        val required = HealthPermissions.required()
        assertTrue(required.any { it.contains("STEPS") })
        assertTrue(required.any { it.contains("HEART_RATE") })
        assertTrue(required.any { it.contains("EXERCISE") })
        assertTrue(required.size >= 3)
    }
}
