package com.pixelfitquest.feature.achievements

import com.pixelfitquest.feature.achievements.data.DefaultAchievementsRepository
import com.pixelfitquest.feature.achievements.data.InMemoryAchievementProgressStore
import com.pixelfitquest.feature.achievements.model.AchievementMetric
import com.pixelfitquest.feature.achievements.model.AchievementReward
import com.pixelfitquest.feature.achievements.model.LocalFitnessSnapshot
import com.pixelfitquest.feature.achievements.rewards.AchievementRewardSink
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultAchievementsRepositoryTest {

    @Test
    fun applySnapshotUnlocksRookieAndLeavesRewardPending() = runBlocking {
        val sink = RecordingRewardSink(grantResult = false)
        val repo = DefaultAchievementsRepository(
            store = InMemoryAchievementProgressStore(),
            rewardSink = sink,
        )

        val newly = repo.applySnapshot(LocalFitnessSnapshot(workoutsCompleted = 1))
        val items = repo.observeItems().first()
        val rookie = items.first { it.definition.id == "bronze_workout_1" }

        assertTrue(newly.contains("bronze_workout_1"))
        assertTrue(rookie.isUnlocked)
        assertFalse(rookie.progress.rewardGranted)
        assertEquals(1, sink.grants.size)
        assertEquals("bronze_workout_1", sink.grants.first().first)
        assertEquals(10, sink.grants.first().second.coins)
        assertEquals(25, sink.grants.first().second.xp)
        assertFalse(items.first { it.definition.id == "gold_workout_50" }.isUnlocked)
    }

    @Test
    fun incrementMetricUnlocksAndGrantedSinkMarksReward() = runBlocking {
        val sink = RecordingRewardSink(grantResult = true)
        val repo = DefaultAchievementsRepository(
            store = InMemoryAchievementProgressStore(),
            rewardSink = sink,
        )

        repo.incrementMetric(AchievementMetric.WORKOUTS_COMPLETED, 1)
        val rookie = repo.observeItems().first().first { it.definition.id == "bronze_workout_1" }
        assertTrue(rookie.isUnlocked)
        assertTrue(rookie.progress.rewardGranted)
    }

    @Test
    fun setMetricUnlocksStreakWithoutFirebase() = runBlocking {
        val repo = DefaultAchievementsRepository(
            store = InMemoryAchievementProgressStore(),
            rewardSink = RecordingRewardSink(grantResult = false),
        )
        repo.setMetric(AchievementMetric.CURRENT_STREAK, 7)
        val items = repo.observeItems().first()
        assertTrue(items.first { it.definition.id == "streak_3" }.isUnlocked)
        assertTrue(items.first { it.definition.id == "streak_7" }.isUnlocked)
        assertFalse(items.first { it.definition.id == "streak_14" }.isUnlocked)
    }

    @Test
    fun subsequentApplySnapshotGrantsPreviouslyUnrewardedUnlockedAchievement() = runBlocking {
        val failingSink = RecordingRewardSink(grantResult = false)
        val store = InMemoryAchievementProgressStore()
        val repo1 = DefaultAchievementsRepository(store = store, rewardSink = failingSink)

        repo1.applySnapshot(LocalFitnessSnapshot(workoutsCompleted = 1))
        val itemBefore = repo1.observeItems().first().first { it.definition.id == "bronze_workout_1" }
        assertTrue(itemBefore.isUnlocked)
        assertFalse(itemBefore.progress.rewardGranted)

        val workingSink = RecordingRewardSink(grantResult = true)
        val repo2 = DefaultAchievementsRepository(store = store, rewardSink = workingSink)
        repo2.applySnapshot(LocalFitnessSnapshot(workoutsCompleted = 1))

        val itemAfter = repo2.observeItems().first().first { it.definition.id == "bronze_workout_1" }
        assertTrue(itemAfter.isUnlocked)
        assertTrue(itemAfter.progress.rewardGranted)
        assertEquals(1, workingSink.grants.size)
        assertEquals("bronze_workout_1", workingSink.grants.first().first)
    }

    private class RecordingRewardSink(
        private val grantResult: Boolean,
    ) : AchievementRewardSink {
        val grants = mutableListOf<Pair<String, AchievementReward>>()

        override suspend fun grant(achievementId: String, reward: AchievementReward): Boolean {
            grants += achievementId to reward
            return grantResult
        }
    }
}
