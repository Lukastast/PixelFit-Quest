package com.pixelfitquest.feature.workout.model

import com.pixelfitquest.feature.workout.model.enums.ExerciseType

data class Exercise(
    val id: String,
    val workoutId: String,
    val type: ExerciseType,
    val totalSets: Int,
    val weight: Float, // This can be the target weight or avg weight
    val avgFormScore: Float = 0f,
    val avgRomScore: Float = 0f,
    val totalVolume: Float = 0f,
    val notes: String? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "workoutId" to workoutId,
        "type" to type.type,
        "totalSets" to totalSets,
        "weight" to weight,
        "avgFormScore" to avgFormScore,
        "avgRomScore" to avgRomScore,
        "totalVolume" to totalVolume,
        "notes" to notes
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): Exercise? {
            val id = map["id"] as? String ?: return null
            val workoutId = map["workoutId"] as? String ?: return null

            val typeStr = map["type"] as? String
            val parsedType = ExerciseType.entries.find { it.type == typeStr } ?: ExerciseType.BENCH_PRESS

            return Exercise(
                id = id,
                workoutId = workoutId,
                type = parsedType,
                totalSets = (map["totalSets"] as? Number)?.toInt() ?: 0,
                weight = (map["weight"] as? Number)?.toFloat() ?: 0f,
                avgFormScore = (map["avgFormScore"] as? Number)?.toFloat() ?: 0f,
                avgRomScore = (map["avgRomScore"] as? Number)?.toFloat() ?: 0f,
                totalVolume = (map["totalVolume"] as? Number)?.toFloat() ?: 0f,
                notes = map["notes"] as? String
            )
        }
    }
}
