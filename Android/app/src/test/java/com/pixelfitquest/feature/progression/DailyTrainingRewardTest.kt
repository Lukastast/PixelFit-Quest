package com.pixelfitquest.feature.progression

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyTrainingRewardTest {

    @Test
    fun setPaysAtMostThirtyReps() {
        val clip = DailyTrainingReward.clip(
            listOf(RewardSet(reps = 50, formScore = 100f)),
            setsAlreadyRewarded = 0,
        )
        assertEquals(60, clip.xp)
        assertEquals(6, clip.coins)
        assertEquals(1, clip.setsConsumed)
        assertTrue(clip.clipped)
        assertEquals(60, clip.perfectSetXp)
    }

    @Test
    fun fortyFirstSetPaysNothing() {
        val clip = DailyTrainingReward.clip(
            listOf(RewardSet(reps = 10, formScore = 70f)),
            setsAlreadyRewarded = DailyTrainingReward.MAX_SETS_PER_DAY,
        )
        assertEquals(0, clip.xp)
        assertEquals(0, clip.coins)
        assertEquals(0, clip.setsConsumed)
        assertTrue(clip.clipped)
    }

    @Test
    fun freshDayPaysTheFirstSets() {
        val sets = List(3) { RewardSet(reps = 10, formScore = 70f) }
        val clip = DailyTrainingReward.clip(sets, setsAlreadyRewarded = 0)
        assertEquals(30, clip.xp)
        assertEquals(6, clip.coins)
        assertEquals(3, clip.setsConsumed)
        assertFalse(clip.clipped)
    }

    @Test
    fun emptySetsDoNotConsumeTheDay() {
        val clip = DailyTrainingReward.clip(
            listOf(RewardSet(reps = 0, formScore = 100f), RewardSet(reps = 8, formScore = 80f)),
            setsAlreadyRewarded = 39,
        )
        assertEquals(1, clip.setsConsumed)
        assertEquals(12, clip.xp)
        assertFalse(clip.clipped)
    }
}
