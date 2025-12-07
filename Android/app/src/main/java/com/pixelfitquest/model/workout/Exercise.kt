package com.pixelfitquest.model.workout

import com.pixelfitquest.model.enums.ExerciseType

data class Exercise(
    val id: String,
    val workoutId: String,
    val type: ExerciseType,
    val totalSets: Int,
    val weight: Float,
    val notes: String? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "workoutId" to workoutId,
        "type" to type.type,
        "totalSets" to totalSets,
        "weight" to weight,
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
                totalSets = map["totalSets"] as? Int ?: 0,
                weight = (map["weight"] as? Number)?.toFloat() ?: 0f,
                notes = map["notes"] as? String
            )
        }
    }
}