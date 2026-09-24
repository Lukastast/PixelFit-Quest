package com.pixelfitquest.feature.progression

import com.pixelfitquest.feature.home.model.DwellingTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RewardBonusTest {

    @Test
    fun tarpAddsNothing() {
        val payout = RewardBonus.workoutPayout(100, 20, DwellingTier.TARP, "basic")
        assertEquals(100, payout.xp)
        assertEquals(20, payout.coins)
        assertFalse(payout.bonusApplied)
    }

    @Test
    fun shackAddsWorkoutXpOnly() {
        val payout = RewardBonus.workoutPayout(100, 20, DwellingTier.SHACK, "basic")
        assertEquals(108, payout.xp)
        assertEquals(20, payout.coins)
        assertTrue(payout.homeApplied)
    }

    @Test
    fun fitnessFlatStacksOnTheHomeBonus() {
        val payout = RewardBonus.workoutPayout(100, 20, DwellingTier.TENT, "male_fitness")
        assertEquals(102, payout.xp)
        assertEquals(23, payout.coins)
        assertTrue(payout.gearApplied)
        assertTrue(payout.homeApplied)
    }

    @Test
    fun formBonusUsesOnlyPerfectSetXp() {
        val payout = RewardBonus.workoutPayout(
            baseXp = 100,
            baseCoins = 20,
            dwelling = DwellingTier.TARP,
            variant = "basic",
            perfectSetXp = 40,
            formRank = 10,
            ironRank = 10,
        )
        assertEquals(112, payout.xp)
        assertEquals(28, payout.coins)
    }

    @Test
    fun fitnessFlatSkipsEmptyRewards() {
        val payout = RewardBonus.withFitnessFlat(0, 0, "female_fitness")
        assertEquals(0, payout.xp)
        assertEquals(0, payout.coins)
        assertFalse(payout.gearApplied)
    }
}
