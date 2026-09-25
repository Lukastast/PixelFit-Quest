package com.pixelfitquest.feature.progression

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SkillTreeTest {

    @Test
    fun unspentPointsFollowLevelMinusSpentRanks() {
        assertEquals(0, SkillTree.unspent(1, 0, 0, 0))
        assertEquals(11, SkillTree.unspent(12, 0, 0, 0))
        assertEquals(8, SkillTree.unspent(12, 1, 1, 1))
        assertEquals(0, SkillTree.unspent(4, 10, 0, 0))
    }

    @Test
    fun percentsCapAtRankTen() {
        assertEquals(30, SkillTree.formPercent(10))
        assertEquals(30, SkillTree.formPercent(40))
        assertEquals(40, SkillTree.ironPercent(10))
        assertEquals(50, SkillTree.vitalityPercent(10))
        assertEquals(15, SkillTree.applyVitalityCoins(10, 10))
    }

    @Test
    fun respecWaitsSevenDays() {
        val today = LocalDate.parse("2026-09-24")
        assertTrue(SkillTree.respecAllowed(today, ""))
        assertFalse(SkillTree.respecAllowed(today, "2026-09-24"))
        assertEquals(7, SkillTree.daysUntilRespec(today, "2026-09-24"))
        assertTrue(SkillTree.respecAllowed(today, "2026-09-17"))
        assertEquals(0, SkillTree.daysUntilRespec(today, "2026-09-17"))
    }
}
