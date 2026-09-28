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
    val est1RmKg: Float = workingWeightKg,
    val totalReps: Int = 0,
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
    val maxWeightKg: Float get() = sessions.maxOfOrNull { it.workingWeightKg } ?: 0f
    val totalVolumeKg: Float get() = sessions.sumOf { it.volumeKg.toDouble() }.toFloat()
    val maxVolumeKg: Float get() = sessions.maxOfOrNull { it.volumeKg } ?: 0f
    val bestEst1RmKg: Float get() = sessions.maxOfOrNull { it.est1RmKg } ?: 0f
    val firstEst1RmKg: Float? get() = sessions.firstOrNull()?.est1RmKg
    val latestEst1RmKg: Float? get() = sessions.lastOrNull()?.est1RmKg
    val est1RmDeltaKg: Float?
        get() {
            val first = firstEst1RmKg
            val latest = latestEst1RmKg
            return if (first != null && latest != null) latest - first else null
        }
    val avgRomScore: Float
        get() {
            val scores = sessions.map { it.avgRomScore }.filter { it > 0f }
            return if (scores.isEmpty()) 0f else scores.average().toFloat()
        }
    val avgStabilityScore: Float
        get() {
            val scores = sessions.map { it.avgStabilityScore }.filter { it > 0f }
            return if (scores.isEmpty()) 0f else scores.average().toFloat()
        }
    val totalSets: Int get() = sessions.sumOf { it.setCount }
    val totalReps: Int get() = sessions.sumOf { it.totalReps }
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
    val primarySeries: ExerciseProgressSeries?
        get() = series.maxByOrNull { it.sessions.size } ?: series.firstOrNull()
}

data class ProgressLoad(
    val records: List<LiftSetRecord>,
    val source: ProgressDataSource,
)
