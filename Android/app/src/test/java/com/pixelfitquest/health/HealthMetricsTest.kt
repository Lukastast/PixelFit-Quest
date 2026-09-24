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
        assertEquals(0L, HealthMetrics.EMPTY.weeklySteps)
    }

    @Test
    fun rewardedGoalsCountStepsSleepAndHeartSeparately() {
        assertEquals(0, HealthMetrics.EMPTY.rewardedGoalsMet)
        assertEquals(
            1,
            HealthMetrics(steps = 10_000, sleepMinutes = 300, weeklyHeartPoints = 10).rewardedGoalsMet,
        )
        assertEquals(
            3,
            HealthMetrics(
                steps = 10_000,
                sleepMinutes = 480,
                weeklyHeartPoints = 150,
            ).rewardedGoalsMet,
        )
        assertEquals(
            2,
            HealthMetrics(steps = 10_000, sleepMinutes = 600, weeklyHeartPoints = 150).rewardedGoalsMet,
        )
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

    @Test
    fun sleepMilestone_awardsFor7to9Hours() {
        // 7 hours = 420 mins, 8 hours = 480 mins, 9 hours = 540 mins
        assertTrue(HealthRewards.shouldAwardSleepMilestone(420L, "", "2026-09-20"))
        assertTrue(HealthRewards.shouldAwardSleepMilestone(480L, "", "2026-09-20"))
        assertTrue(HealthRewards.shouldAwardSleepMilestone(540L, "", "2026-09-20"))
    }

    @Test
    fun sleepMilestone_doesNotAwardOutside7to9HoursOrWhenNull() {
        assertFalse(HealthRewards.shouldAwardSleepMilestone(null, "", "2026-09-20"))
        assertFalse(HealthRewards.shouldAwardSleepMilestone(419L, "", "2026-09-20"))
        assertFalse(HealthRewards.shouldAwardSleepMilestone(541L, "", "2026-09-20"))
        assertFalse(HealthRewards.shouldAwardSleepMilestone(0L, "", "2026-09-20"))
    }

    @Test
    fun sleepMilestone_awardsOncePerDay() {
        assertTrue(HealthRewards.shouldAwardSleepMilestone(480L, "", "2026-09-20"))
        assertFalse(HealthRewards.shouldAwardSleepMilestone(480L, "2026-09-20", "2026-09-20"))
    }

    @Test
    fun weeklyHeartGoal_awardsWhenPointsMeetOrExceedGoal() {
        assertTrue(HealthRewards.shouldAwardWeeklyHeartGoal(150, 150, "", "2026-W38"))
        assertTrue(HealthRewards.shouldAwardWeeklyHeartGoal(200, 150, "", "2026-W38"))
    }

    @Test
    fun weeklyHeartGoal_doesNotAwardBelowGoalOrWhenGoalZero() {
        assertFalse(HealthRewards.shouldAwardWeeklyHeartGoal(149, 150, "", "2026-W38"))
        assertFalse(HealthRewards.shouldAwardWeeklyHeartGoal(150, 0, "", "2026-W38"))
    }

    @Test
    fun weeklyHeartGoal_awardsOncePerWeek() {
        assertTrue(HealthRewards.shouldAwardWeeklyHeartGoal(150, 150, "", "2026-W38"))
        assertFalse(HealthRewards.shouldAwardWeeklyHeartGoal(150, 150, "2026-W38", "2026-W38"))
    }

    @Test
    fun vitalityBonus_awardsWhenAllGoalsCompleted() {
        assertTrue(HealthRewards.shouldAwardVitalityBonus(3, 3, "", "2026-09-24"))
        assertTrue(HealthRewards.shouldAwardVitalityBonus(4, 3, "", "2026-09-24"))
    }

    @Test
    fun vitalityBonus_doesNotAwardWhenIncomplete() {
        assertFalse(HealthRewards.shouldAwardVitalityBonus(2, 3, "", "2026-09-24"))
        assertFalse(HealthRewards.shouldAwardVitalityBonus(0, 3, "", "2026-09-24"))
    }

    @Test
    fun vitalityBonus_awardsOncePerDay() {
        assertTrue(HealthRewards.shouldAwardVitalityBonus(3, 3, "2026-09-23", "2026-09-24"))
        assertFalse(HealthRewards.shouldAwardVitalityBonus(3, 3, "2026-09-24", "2026-09-24"))
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

    @Test
    fun weekRangeStartsAtMondayMidnightInClockZone() {
        // 2026-09-20 is a Sunday, Monday of that week was 2026-09-14
        val clock = Clock.fixed(Instant.parse("2026-09-20T12:00:00Z"), ZoneOffset.UTC)
        val (start, end) = HealthTime.weekRange(clock)
        assertEquals(Instant.parse("2026-09-14T00:00:00Z"), start)
        assertEquals(Instant.parse("2026-09-20T12:00:00Z"), end)
    }

    @Test
    fun currentWeekIsoFormat() {
        val clock = Clock.fixed(Instant.parse("2026-09-20T12:00:00Z"), ZoneOffset.UTC)
        assertEquals("2026-W38", HealthTime.currentWeekIso(clock))
    }

    @Test
    fun rolling7DaysRangeSpansExactlySevenDays() {
        val clock = Clock.fixed(Instant.parse("2026-09-20T12:00:00Z"), ZoneOffset.UTC)
        val (start, end) = HealthTime.rolling7DaysRange(clock)
        assertEquals(Instant.parse("2026-09-13T12:00:00Z"), start)
        assertEquals(Instant.parse("2026-09-20T12:00:00Z"), end)
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
    fun requiredCoversStepsHeartRateSleepAndExercise() {
        val required = HealthPermissions.required()
        assertTrue(required.any { it.contains("STEPS") })
        assertTrue(required.any { it.contains("HEART_RATE") })
        assertTrue(required.any { it.contains("RESTING_HEART_RATE") })
        assertTrue(required.any { it.contains("SLEEP") })
        assertTrue(required.any { it.contains("EXERCISE") })
        assertTrue(required.size >= 6)
    }

    @Test
    fun permissionHelpersWorkAsExpected() {
        val granted = setOf(
            "android.permission.health.READ_STEPS",
            "android.permission.health.READ_SLEEP",
            "android.permission.health.READ_RESTING_HEART_RATE",
        )
        assertTrue(HealthPermissions.hasStepsRead(granted))
        assertTrue(HealthPermissions.hasSleepRead(granted))
        assertTrue(HealthPermissions.hasRestingHeartRateRead(granted))
        assertFalse(HealthPermissions.hasHeartRateRead(granted))
        assertFalse(HealthPermissions.hasExerciseRead(granted))
    }
}
