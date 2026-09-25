package com.pixelfitquest.feature.achievements

import android.util.Log
import com.pixelfitquest.feature.achievements.data.AchievementsRepository
import com.pixelfitquest.feature.achievements.model.LocalFitnessSnapshot
import com.pixelfitquest.feature.workout.model.Exercise
import com.pixelfitquest.feature.workout.model.Workout
import com.pixelfitquest.local.LocalPixelFitStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

object AchievementSnapshotBuilder {
    fun build(
        workouts: List<Workout>,
        exercises: List<Exercise>,
        streak: Int,
        level: Int,
        steps: Long = 0L,
    ): LocalFitnessSnapshot {
        val validWorkouts = workouts.filter { it.totalExercises > 0 }
        val uniqueExercises = exercises.map { it.type }.distinct().size
        return LocalFitnessSnapshot(
            workoutsCompleted = validWorkouts.size,
            currentStreak = streak.coerceAtLeast(0),
            lifetimeSteps = steps.coerceAtLeast(0L),
            totalVolumeKg = validWorkouts.sumOf { it.totalVolume.toDouble() }.toLong().coerceAtLeast(0L),
            uniqueExercises = uniqueExercises,
            setsCompleted = validWorkouts.sumOf { it.totalSets.toLong() }.toInt().coerceAtLeast(0),
            level = level.coerceAtLeast(1),
        )
    }
}

@Singleton
class AchievementSyncService @Inject constructor(
    private val achievementsRepository: AchievementsRepository,
    private val localStore: LocalPixelFitStore,
) {
    private val mutex = Mutex()

    suspend fun sync(
        workouts: List<Workout>? = null,
        steps: Long? = null,
    ): List<String> = mutex.withLock {
        try {
            val validWorkouts = workouts ?: localStore.getAllWorkouts()
            val profile = localStore.ensureProfile()
            val allExercises = localStore.getAllExercises()

            val snapshot = AchievementSnapshotBuilder.build(
                workouts = validWorkouts,
                exercises = allExercises,
                streak = profile.streak,
                level = profile.level,
                steps = steps ?: 0L,
            )

            val newlyUnlocked = achievementsRepository.applySnapshot(snapshot)
            if (newlyUnlocked.isNotEmpty()) {
                Log.d(TAG, "Unlocked achievements: $newlyUnlocked")
            }
            newlyUnlocked
        } catch (e: Exception) {
            Log.e(TAG, "Achievement sync failed", e)
            emptyList()
        }
    }

    private companion object {
        private const val TAG = "AchievementSyncService"
    }
}
