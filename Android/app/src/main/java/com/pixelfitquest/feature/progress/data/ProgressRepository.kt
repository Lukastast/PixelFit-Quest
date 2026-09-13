package com.pixelfitquest.feature.progress.data

import android.util.Log
import com.pixelfitquest.feature.progress.model.LiftSetRecord
import com.pixelfitquest.feature.progress.model.ProgressAggregator
import com.pixelfitquest.feature.progress.model.ProgressDataSource
import com.pixelfitquest.feature.progress.model.ProgressOverview
import com.pixelfitquest.feature.progress.model.ProgressSourceResolver
import com.pixelfitquest.firebase.repository.WorkoutRepository
import dagger.hilt.android.scopes.ViewModelScoped
import java.time.Instant
import javax.inject.Inject

/**
 * Progress charts prefer lift_history in pixelfit.db. If that cache is empty,
 * derive/cache from local workouts via WorkoutRepository (LocalPixelFitStore /
 * WorkoutDao in the same Room DB). Sample data only when both are empty.
 */
@ViewModelScoped
class ProgressRepository @Inject constructor(
    private val liftHistoryDao: LiftHistoryDao,
    private val workoutRepository: WorkoutRepository,
) {
    suspend fun loadOverview(): ProgressOverview {
        val localHistory = try {
            liftHistoryDao.getAll().map { it.toRecord() }
        } catch (e: Exception) {
            Log.w(TAG, "Room lift history unavailable", e)
            emptyList()
        }

        // WorkoutRepository is offline-first LocalPixelFitStore (pixelfit.db), not Firebase.
        val workoutLog = if (localHistory.isEmpty()) fetchLocalWorkoutRecords() else emptyList()
        val load = ProgressSourceResolver.resolve(localHistory, workoutLog)

        if (load.source == ProgressDataSource.WORKOUT_LOG) {
            try {
                liftHistoryDao.insertAll(load.records.map { it.toEntity() })
            } catch (e: Exception) {
                Log.w(TAG, "Could not cache workout log into lift_history", e)
            }
        }

        return ProgressOverview(
            series = ProgressAggregator.aggregate(load.records),
            source = load.source,
        )
    }

    private suspend fun fetchLocalWorkoutRecords(): List<LiftSetRecord> {
        val workouts = try {
            workoutRepository.getAllCompletedWorkouts()
        } catch (e: Exception) {
            Log.i(TAG, "Local workout log not available")
            return emptyList()
        }
        if (workouts.isEmpty()) return emptyList()

        val records = mutableListOf<LiftSetRecord>()
        for (workout in workouts) {
            val workoutTs = parseTimestamp(workout.date)
            val exercises = try {
                workoutRepository.getExercisesByWorkoutId(workout.id)
            } catch (e: Exception) {
                emptyList()
            }
            val sets = try {
                workoutRepository.getSetsByWorkoutId(workout.id)
            } catch (e: Exception) {
                emptyList()
            }
            val typeByExerciseId = exercises.associate { it.id to it.type }
            if (sets.isNotEmpty()) {
                for (set in sets) {
                    val type = typeByExerciseId[set.exerciseId] ?: continue
                    val timestamp = if (set.timestamp > 0L) set.timestamp else workoutTs
                    records += LiftSetRecord(
                        id = set.id.ifBlank { "${workout.id}-${set.exerciseId}-${set.setNumber}" },
                        workoutId = workout.id,
                        exerciseType = type.type,
                        timestampMillis = timestamp,
                        weightKg = set.weight,
                        reps = set.reps,
                        romScore = set.romScore,
                        stabilityScore = set.stabilityScore,
                    )
                }
            } else {
                for (exercise in exercises) {
                    if (exercise.weight <= 0f) continue
                    records += LiftSetRecord(
                        id = "ex-${exercise.id}",
                        workoutId = workout.id,
                        exerciseType = exercise.type.type,
                        timestampMillis = workoutTs,
                        weightKg = exercise.weight,
                        reps = 0,
                        romScore = exercise.avgRomScore,
                    )
                }
            }
        }
        return records
    }

    private fun parseTimestamp(iso: String): Long {
        return try {
            Instant.parse(iso).toEpochMilli()
        } catch (e: Exception) {
            0L
        }
    }

    companion object {
        private const val TAG = "ProgressRepo"
    }
}
