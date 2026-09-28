package com.pixelfitquest.feature.progress.model

import com.pixelfitquest.feature.workout.model.enums.ExerciseType

object ProgressAggregator {

    fun parseExerciseType(raw: String): ExerciseType? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        return ExerciseType.entries.find { it.type.equals(trimmed, ignoreCase = true) }
            ?: ExerciseType.entries.find { it.name.equals(trimmed, ignoreCase = true) }
            ?: ExerciseType.entries.find {
                it.name.replace("_", " ").equals(trimmed.replace("-", " "), ignoreCase = true)
            }
    }

    fun aggregate(records: List<LiftSetRecord>): List<ExerciseProgressSeries> {
        if (records.isEmpty()) return emptyList()

        return records
            .mapNotNull { record ->
                val type = parseExerciseType(record.exerciseType) ?: return@mapNotNull null
                type to record
            }
            .groupBy({ it.first }, { it.second })
            .map { (type, typeRecords) ->
                val sessions = typeRecords
                    .groupBy { it.workoutId.ifBlank { "ts-${it.timestampMillis}" } }
                    .map { (workoutId, sets) ->
                        val weights = sets.map { it.weightKg }.filter { it > 0f }
                        val working = weights.maxOrNull() ?: 0f
                        val rom = sets.map { it.romScore }.filter { it > 0f }
                        val stability = sets.map { it.stabilityScore }.filter { it > 0f }
                        val est1Rm = sets.maxOfOrNull { set ->
                            if (set.reps > 0 && set.weightKg > 0f) {
                                set.weightKg * (1f + set.reps / 30f)
                            } else {
                                set.weightKg
                            }
                        } ?: working
                        val totalReps = sets.sumOf { it.reps }
                        SessionProgressPoint(
                            workoutId = workoutId,
                            timestampMillis = sets.minOf { it.timestampMillis },
                            workingWeightKg = working,
                            volumeKg = sets.sumOf { it.volumeKg.toDouble() }.toFloat(),
                            avgRomScore = if (rom.isEmpty()) 0f else rom.average().toFloat(),
                            avgStabilityScore = if (stability.isEmpty()) 0f else stability.average().toFloat(),
                            setCount = sets.size,
                            est1RmKg = est1Rm,
                            totalReps = totalReps,
                        )
                    }
                    .sortedBy { it.timestampMillis }
                ExerciseProgressSeries(exerciseType = type, sessions = sessions)
            }
            .filter { it.sessions.isNotEmpty() }
            .sortedBy { it.exerciseType.ordinal }
    }
}

object ProgressSourceResolver {
    fun resolve(
        local: List<LiftSetRecord>,
        workoutLog: List<LiftSetRecord>,
    ): ProgressLoad {
        if (local.isNotEmpty()) {
            return ProgressLoad(local, ProgressDataSource.LOCAL)
        }
        if (workoutLog.isNotEmpty()) {
            return ProgressLoad(workoutLog, ProgressDataSource.WORKOUT_LOG)
        }
        return ProgressLoad(SampleProgressData.records(), ProgressDataSource.SAMPLE)
    }
}
