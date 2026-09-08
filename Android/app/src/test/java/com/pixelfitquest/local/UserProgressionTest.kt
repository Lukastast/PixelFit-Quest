package com.pixelfitquest.local

import org.junit.Assert.assertEquals
import org.junit.Test

class UserProgressionTest {
    private val expForLevel: (Int) -> Int = { level -> 100 * level }

    @Test
    fun applyExpIgnoresNonPositiveAmounts() {
        val result = UserProgression.applyExp(1, 10, 0, 30, expForLevel)
        assertEquals(1, result.level)
        assertEquals(10, result.exp)
    }

    @Test
    fun applyExpLevelsUpWhenThresholdCrossed() {
        val result = UserProgression.applyExp(
            level = 1,
            exp = 90,
            amount = 20,
            maxLevel = 30,
            expRequiredForLevel = expForLevel,
        )
        assertEquals(2, result.level)
        assertEquals(10, result.exp)
    }

    @Test
    fun applyExpCapsAtMaxLevel() {
        val result = UserProgression.applyExp(
            level = 30,
            exp = 0,
            amount = 9999,
            maxLevel = 30,
            expRequiredForLevel = expForLevel,
        )
        assertEquals(30, result.level)
        assertEquals(3000, result.exp)
    }

    @Test
    fun streakStartsAtOneOnFirstActivity() {
        val result = UserProgression.applyStreak(
            currentStreak = 0,
            lastActivityDate = "",
            today = "2026-09-08",
            yesterday = "2026-09-07",
            increment = true,
            reset = false,
        )
        assertEquals(1, result.streak)
        assertEquals("2026-09-08", result.lastActivityDate)
    }

    @Test
    fun streakDoesNotDoubleCountSameDay() {
        val result = UserProgression.applyStreak(
            currentStreak = 3,
            lastActivityDate = "2026-09-08",
            today = "2026-09-08",
            yesterday = "2026-09-07",
            increment = true,
            reset = false,
        )
        assertEquals(3, result.streak)
    }

    @Test
    fun streakIncrementsOnConsecutiveDay() {
        val result = UserProgression.applyStreak(
            currentStreak = 3,
            lastActivityDate = "2026-09-07",
            today = "2026-09-08",
            yesterday = "2026-09-07",
            increment = true,
            reset = false,
        )
        assertEquals(4, result.streak)
    }

    @Test
    fun streakResetsAfterAGap() {
        val result = UserProgression.applyStreak(
            currentStreak = 5,
            lastActivityDate = "2026-09-01",
            today = "2026-09-08",
            yesterday = "2026-09-07",
            increment = true,
            reset = false,
        )
        assertEquals(1, result.streak)
    }
}
