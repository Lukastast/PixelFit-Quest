package com.pixelfitquest.feature.progress.model

import com.pixelfitquest.feature.workout.model.enums.ExerciseType

data class LiftSetRecord(
    val id: String,
    val workoutId: String,
    val exerciseType: String,
    val timestampMillis: Long,
    val weightKg: Float,
    val reps: Int,
    val romScore: Float = 0f,
    val stabilityScore: Float = 0f,
) {
    val volumeKg: Float get() = weightKg * reps.coerceAtLeast(0)
}

data class SessionProgressPoint(
    val workoutId: String,
    val timestampMillis: Long,
    val workingWeightKg: Float,
    val volumeKg: Float,
    val avgRomScore: Float,
    val avgStabilityScore: Float,
    val setCount: Int,
)

data class ExerciseProgressSeries(
    val exerciseType: ExerciseType,
    val sessions: List<SessionProgressPoint>,
) {
    val latestWeightKg: Float? get() = sessions.lastOrNull()?.workingWeightKg
    val firstWeightKg: Float? get() = sessions.firstOrNull()?.workingWeightKg
    val weightDeltaKg: Float?
        get() {
            val first = firstWeightKg
            val latest = latestWeightKg
            return if (first != null && latest != null) latest - first else null
        }
}

enum class ProgressDataSource {
    LOCAL,
    WORKOUT_LOG,
    SAMPLE,
}

data class ProgressOverview(
    val series: List<ExerciseProgressSeries>,
    val source: ProgressDataSource,
) {
    val isSample: Boolean get() = source == ProgressDataSource.SAMPLE
}

data class ProgressLoad(
    val records: List<LiftSetRecord>,
    val source: ProgressDataSource,
)
