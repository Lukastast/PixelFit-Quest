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
                        SessionProgressPoint(
                            workoutId = workoutId,
                            timestampMillis = sets.minOf { it.timestampMillis },
                            workingWeightKg = working,
                            volumeKg = sets.sumOf { it.volumeKg.toDouble() }.toFloat(),
                            avgRomScore = if (rom.isEmpty()) 0f else rom.average().toFloat(),
                            avgStabilityScore = if (stability.isEmpty()) 0f else stability.average().toFloat(),
                            setCount = sets.size,
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
        remote: List<LiftSetRecord>,
    ): ProgressLoad {
        if (local.isNotEmpty()) {
            return ProgressLoad(local, ProgressDataSource.LOCAL)
        }
        if (remote.isNotEmpty()) {
            return ProgressLoad(remote, ProgressDataSource.WORKOUT_LOG)
        }
        return ProgressLoad(SampleProgressData.records(), ProgressDataSource.SAMPLE)
    }
}
