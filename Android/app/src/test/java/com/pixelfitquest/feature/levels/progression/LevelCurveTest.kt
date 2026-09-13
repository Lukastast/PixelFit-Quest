package com.pixelfitquest.feature.levels.progression

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelCurveTest {

    @Test
    fun xpToAdvance_matchesExistingHundredTimesLevel() {
        assertEquals(100, LevelCurve.xpToAdvance(1))
        assertEquals(200, LevelCurve.xpToAdvance(2))
        assertEquals(3000, LevelCurve.xpToAdvance(30))
    }

    @Test
    fun totalXpForLevel_isTriangleNumber() {
        assertEquals(0, LevelCurve.totalXpForLevel(1))
        assertEquals(100, LevelCurve.totalXpForLevel(2))
        assertEquals(300, LevelCurve.totalXpForLevel(3))
        assertEquals(50 * 10 * 9, LevelCurve.totalXpForLevel(10))
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
        assertEquals(200, midTwo.xpToNext)
    }

    @Test
    fun progressFromTotalXp_capsAtMaxLevel() {
        val maxed = LevelCurve.progressFromTotalXp(LevelCurve.maxTotalXp() + 9999)
        assertEquals(LevelCurve.MAX_LEVEL, maxed.level)
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
        // 100 XP is exactly level 2 with 0 into the 200 bar → fraction 0 → index 0
        assertEquals(0, LevelCurve.progressFromTotalXp(100).xpBarIndex)
        val almost = LevelCurve.progressFromTotalXp(99)
        assertEquals(1, almost.level)
        assertEquals(4, almost.xpBarIndex)
        val maxed = LevelCurve.progressFromTotalXp(LevelCurve.maxTotalXp())
        assertEquals(5, maxed.xpBarIndex)
    }
}
