package com.pixelfitquest.feature.achievements

import com.pixelfitquest.feature.achievements.model.AchievementCategory
import com.pixelfitquest.feature.achievements.model.AchievementDefinition
import com.pixelfitquest.feature.achievements.model.AchievementMetric
import com.pixelfitquest.feature.achievements.model.AchievementProgress
import com.pixelfitquest.feature.achievements.model.AchievementReward
import com.pixelfitquest.feature.achievements.model.AchievementTier
import com.pixelfitquest.feature.achievements.model.LocalFitnessSnapshot
import com.pixelfitquest.feature.achievements.unlock.AchievementUnlocker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementUnlockerTest {

    private val rookie = definition(
        id = "bronze_workout_1",
        metric = AchievementMetric.WORKOUTS_COMPLETED,
        threshold = 1,
    )
    private val veteran = definition(
        id = "silver_workout_10",
        metric = AchievementMetric.WORKOUTS_COMPLETED,
        threshold = 10,
    )
    private val onFire = definition(
        id = "streak_3",
        metric = AchievementMetric.CURRENT_STREAK,
        threshold = 3,
    )

    @Test
    fun unlocksWhenThresholdReached() {
        val result = AchievementUnlocker.evaluate(
            existing = emptyMap(),
            snapshot = LocalFitnessSnapshot(workoutsCompleted = 1),
            nowEpochMs = 1_000L,
            definitions = listOf(rookie, veteran),
        )
        assertEquals(listOf("bronze_workout_1"), result.newlyUnlockedIds)
        val unlocked = result.updated.first { it.achievementId == "bronze_workout_1" }
        assertEquals(1_000L, unlocked.unlockedAtEpochMs)
        assertEquals(1L, unlocked.currentValue)
        assertNull(result.updated.first { it.achievementId == "silver_workout_10" }.unlockedAtEpochMs)
    }

    @Test
    fun doesNotRelockWhenStreakDrops() {
        val existing = mapOf(
            "streak_3" to AchievementProgress(
                achievementId = "streak_3",
                currentValue = 3,
                unlockedAtEpochMs = 500L,
            ),
        )
        val result = AchievementUnlocker.evaluate(
            existing = existing,
            snapshot = LocalFitnessSnapshot(currentStreak = 0),
            nowEpochMs = 2_000L,
            definitions = listOf(onFire),
        )
        assertTrue(result.newlyUnlockedIds.isEmpty())
        val progress = result.updated.single()
        assertEquals(500L, progress.unlockedAtEpochMs)
        assertEquals(0L, progress.currentValue)
    }

    @Test
    fun cumulativeMetricKeepsHighWaterMark() {
        val existing = mapOf(
            "bronze_workout_1" to AchievementProgress(
                achievementId = "bronze_workout_1",
                currentValue = 4,
                unlockedAtEpochMs = 100L,
            ),
        )
        val result = AchievementUnlocker.evaluate(
            existing = existing,
            snapshot = LocalFitnessSnapshot(workoutsCompleted = 2),
            nowEpochMs = 3_000L,
            definitions = listOf(rookie),
        )
        assertEquals(4L, result.updated.single().currentValue)
        assertEquals(100L, result.updated.single().unlockedAtEpochMs)
        assertTrue(result.newlyUnlockedIds.isEmpty())
    }

    @Test
    fun alreadyUnlockedNotReportedAgain() {
        val existing = mapOf(
            "bronze_workout_1" to AchievementProgress(
                achievementId = "bronze_workout_1",
                currentValue = 1,
                unlockedAtEpochMs = 50L,
                rewardGranted = false,
            ),
        )
        val result = AchievementUnlocker.evaluate(
            existing = existing,
            snapshot = LocalFitnessSnapshot(workoutsCompleted = 12),
            nowEpochMs = 9_000L,
            definitions = listOf(rookie, veteran),
        )
        assertEquals(listOf("silver_workout_10"), result.newlyUnlockedIds)
        assertEquals(false, result.updated.first { it.achievementId == "bronze_workout_1" }.rewardGranted)
    }

    private fun definition(
        id: String,
        metric: AchievementMetric,
        threshold: Long,
    ) = AchievementDefinition(
        id = id,
        name = id,
        description = id,
        category = AchievementCategory.WORKOUTS,
        tier = AchievementTier.BRONZE,
        metric = metric,
        threshold = threshold,
        reward = AchievementReward(coins = 10, xp = 25),
    )
}
