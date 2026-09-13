package com.pixelfitquest.feature.progress.model

import com.pixelfitquest.feature.workout.model.enums.ExerciseType
import java.util.concurrent.TimeUnit

/**
 * Clearly labeled demo lifts so the progress screen is usable before any
 * sessions exist on-device. Never persisted to Room.
 */
object SampleProgressData {
    private val weekMs = TimeUnit.DAYS.toMillis(7)

    fun records(nowMillis: Long = System.currentTimeMillis()): List<LiftSetRecord> {
        val bench = sampleLift(
            type = ExerciseType.BENCH_PRESS,
            nowMillis = nowMillis,
            weights = floatArrayOf(40f, 42.5f, 45f, 47.5f, 50f, 52.5f, 55f),
            reps = intArrayOf(8, 8, 6, 6, 5, 5, 5),
        )
        val squat = sampleLift(
            type = ExerciseType.SQUAT,
            nowMillis = nowMillis,
            weights = floatArrayOf(60f, 65f, 70f, 72.5f, 75f, 80f, 85f),
            reps = intArrayOf(6, 6, 5, 5, 5, 4, 4),
        )
        val curl = sampleLift(
            type = ExerciseType.BICEP_CURL,
            nowMillis = nowMillis,
            weights = floatArrayOf(10f, 10f, 12f, 12f, 14f, 14f, 16f),
            reps = intArrayOf(10, 10, 10, 8, 8, 8, 8),
        )
        return bench + squat + curl
    }

    private fun sampleLift(
        type: ExerciseType,
        nowMillis: Long,
        weights: FloatArray,
        reps: IntArray,
    ): List<LiftSetRecord> {
        val weeks = weights.size
        return weights.indices.map { index ->
            val weekIndex = weeks - 1 - index
            val timestamp = nowMillis - weekIndex * weekMs
            val workoutId = "sample-${type.type}-$index"
            LiftSetRecord(
                id = "$workoutId-set",
                workoutId = workoutId,
                exerciseType = type.type,
                timestampMillis = timestamp,
                weightKg = weights[index],
                reps = reps[index],
            )
        }
    }
}
