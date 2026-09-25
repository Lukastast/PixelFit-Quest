package com.pixelfitquest.feature.achievements

import com.pixelfitquest.feature.achievements.data.DefaultAchievementsRepository
import com.pixelfitquest.feature.achievements.data.InMemoryAchievementProgressStore
import com.pixelfitquest.feature.achievements.model.AchievementReward
import com.pixelfitquest.feature.achievements.rewards.AchievementRewardSink
import com.pixelfitquest.feature.workout.model.Exercise
import com.pixelfitquest.feature.workout.model.Workout
import com.pixelfitquest.feature.workout.model.enums.ExerciseType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementSnapshotBuilderTest {

    @Test
    fun buildIgnoresWorkoutsWithZeroExercises() {
        val workouts = listOf(
            Workout(id = "w1", date = "2026-09-20", name = "Chest Day", totalExercises = 2, totalSets = 6, totalVolume = 1200f),
            Workout(id = "w2", date = "2026-09-21", name = "Empty Day", totalExercises = 0, totalSets = 3, totalVolume = 500f),
        )
        val snapshot = AchievementSnapshotBuilder.build(
            workouts = workouts,
            exercises = emptyList(),
            streak = 4,
            level = 2,
            steps = 5000L,
        )

        assertEquals(1, snapshot.workoutsCompleted)
        assertEquals(6, snapshot.setsCompleted)
        assertEquals(1200L, snapshot.totalVolumeKg)
        assertEquals(4, snapshot.currentStreak)
        assertEquals(2, snapshot.level)
        assertEquals(5000L, snapshot.lifetimeSteps)
    }

    @Test
    fun buildCountsUniqueExerciseTypes() {
        val exercises = listOf(
            exercise("e1", "w1", ExerciseType.BENCH_PRESS),
            exercise("e2", "w1", ExerciseType.SQUAT),
            exercise("e3", "w2", ExerciseType.BENCH_PRESS),
            exercise("e4", "w2", ExerciseType.LAT_PULLDOWN),
        )
        val snapshot = AchievementSnapshotBuilder.build(
            workouts = emptyList(),
            exercises = exercises,
            streak = 0,
            level = 1,
        )

        assertEquals(3, snapshot.uniqueExercises)
    }

    @Test
    fun snapshotUnlocksAchievementsAndAwardsInRepository() = runBlocking {
        val sink = TestRewardSink()
        val repository = DefaultAchievementsRepository(
            store = InMemoryAchievementProgressStore(),
            rewardSink = sink,
        )

        val workouts = listOf(
            Workout(id = "w1", date = "2026-09-20", name = "Full Body", totalExercises = 3, totalSets = 10, totalVolume = 1500f),
        )
        val exercises = listOf(
            exercise("e1", "w1", ExerciseType.BENCH_PRESS),
            exercise("e2", "w1", ExerciseType.SQUAT),
            exercise("e3", "w1", ExerciseType.BICEP_CURL),
        )
        val snapshot = AchievementSnapshotBuilder.build(
            workouts = workouts,
            exercises = exercises,
            streak = 3,
            level = 5,
            steps = 10000L,
        )

        val newlyUnlocked = repository.applySnapshot(snapshot)

        // Verifications:
        // 1 workout completed -> bronze_workout_1 (Rookie)
        assertTrue(newlyUnlocked.contains("bronze_workout_1"))
        // 3-day streak -> streak_3 (On Fire)
        assertTrue(newlyUnlocked.contains("streak_3"))
        // 10 sets completed -> sets_10 (Set Starter)
        assertTrue(newlyUnlocked.contains("sets_10"))
        // 1000+ kg volume -> volume_1000 (Iron Apprentice)
        assertTrue(newlyUnlocked.contains("volume_1000"))
        // 3 unique exercises -> unique_3 (Variety Pack)
        assertTrue(newlyUnlocked.contains("unique_3"))
        // Level 5 -> level_5 (Apprentice)
        assertTrue(newlyUnlocked.contains("level_5"))
        // 10,000 steps -> steps_5000, steps_10000
        assertTrue(newlyUnlocked.contains("steps_5000"))
        assertTrue(newlyUnlocked.contains("steps_10000"))

        val items = repository.observeItems().first()
        val rookie = items.first { it.definition.id == "bronze_workout_1" }
        assertTrue(rookie.isUnlocked)
        assertTrue(rookie.progress.rewardGranted)

        // All unlocked achievements had rewards granted
        assertTrue(sink.grantedAchievements.contains("bronze_workout_1"))
        assertTrue(sink.grantedAchievements.contains("unique_3"))
    }

    private fun exercise(id: String, workoutId: String, type: ExerciseType) = Exercise(
        id = id,
        workoutId = workoutId,
        type = type,
        profileId = type.type,
        totalSets = 3,
        weight = 50f,
    )

    private class TestRewardSink : AchievementRewardSink {
        val grantedAchievements = mutableListOf<String>()

        override suspend fun grant(achievementId: String, reward: AchievementReward): Boolean {
            grantedAchievements += achievementId
            return true
        }
    }
}
