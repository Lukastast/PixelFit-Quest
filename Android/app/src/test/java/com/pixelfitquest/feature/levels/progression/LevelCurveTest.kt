package com.pixelfitquest.feature.levels.progression

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelCurveTest {

    @Test
    fun xpToAdvance_startsAtOneWorkoutAndRisesSlowly() {
        assertEquals(100, LevelCurve.xpToAdvance(1))
        assertEquals(112, LevelCurve.xpToAdvance(2))
        assertEquals(210, LevelCurve.xpToAdvance(10))
        assertEquals(466, LevelCurve.xpToAdvance(30))
        assertEquals(1488, LevelCurve.xpToAdvance(100))
        for (level in 2..LevelCurve.MAX_LEVEL) {
            assertTrue(
                "cost should rise at $level",
                LevelCurve.xpToAdvance(level) > LevelCurve.xpToAdvance(level - 1),
            )
        }
    }

    @Test
    fun totalXpForLevel_matchesTheSumOfEarlierCosts() {
        assertEquals(0, LevelCurve.totalXpForLevel(1))
        assertEquals(100, LevelCurve.totalXpForLevel(2))
        assertEquals(212, LevelCurve.totalXpForLevel(3))
        var sum = 0
        for (level in 1 until LevelCurve.MAX_LEVEL) {
            sum += LevelCurve.xpToAdvance(level)
        }
        assertEquals(sum, LevelCurve.totalXpForLevel(100))
        assertEquals(74_634, LevelCurve.totalXpForLevel(100))
    }

    @Test
    fun coinsForReaching_growsEveryFiveLevels() {
        assertEquals(0, LevelCurve.coinsForReaching(1))
        assertEquals(10, LevelCurve.coinsForReaching(2))
        assertEquals(15, LevelCurve.coinsForReaching(5))
        assertEquals(20, LevelCurve.coinsForReaching(10))
        assertEquals(110, LevelCurve.coinsForReaching(100))
        assertEquals(15, LevelCurve.coinsForLevels(previousLevel = 4, newLevel = 5))
        assertEquals(0, LevelCurve.coinsForLevels(previousLevel = 8, newLevel = 8))
    }

    @Test
    fun progressFromTotalXp_startsAtLevelOne() {
        val progress = LevelCurve.progressFromTotalXp(0)
        assertEquals(1, progress.level)
        assertEquals(0, progress.xpIntoLevel)
        assertEquals(100, progress.xpToNext)
        assertFalse(progress.isMaxLevel)
    }

    @Test
    fun progressFromTotalXp_levelsUpWhenCostMet() {
        val atTwo = LevelCurve.progressFromTotalXp(100)
        assertEquals(2, atTwo.level)
        assertEquals(0, atTwo.xpIntoLevel)

        val midTwo = LevelCurve.progressFromTotalXp(150)
        assertEquals(2, midTwo.level)
        assertEquals(50, midTwo.xpIntoLevel)
        assertEquals(112, midTwo.xpToNext)
    }

    @Test
    fun progressFromTotalXp_capsAtMaxLevel() {
        val maxed = LevelCurve.progressFromTotalXp(LevelCurve.maxTotalXp() + 9999)
        assertEquals(LevelCurve.MAX_LEVEL, maxed.level)
        assertEquals(100, maxed.level)
        assertTrue(maxed.isMaxLevel)
        assertEquals(LevelCurve.xpToAdvance(LevelCurve.MAX_LEVEL), maxed.xpIntoLevel)
        assertEquals(LevelCurve.maxTotalXp(), maxed.totalXp)
    }

    @Test
    fun totalXpFromRemote_roundTrips() {
        val total = LevelCurve.totalXpFromRemote(level = 4, xpIntoLevel = 40)
        val progress = LevelCurve.progressFromTotalXp(total)
        assertEquals(4, progress.level)
        assertEquals(40, progress.xpIntoLevel)
    }

    @Test
    fun xpBarIndex_spansSixFrames() {
        assertEquals(0, LevelCurve.progressFromTotalXp(0).xpBarIndex)
        assertEquals(0, LevelCurve.progressFromTotalXp(100).xpBarIndex)
        val almost = LevelCurve.progressFromTotalXp(99)
        assertEquals(1, almost.level)
        assertEquals(4, almost.xpBarIndex)
        val maxed = LevelCurve.progressFromTotalXp(LevelCurve.maxTotalXp())
        assertEquals(5, maxed.xpBarIndex)
    }
}
