package com.pixelfitquest.model

// Enum at the top
enum class WorkoutType {
    BENCH_PRESS,
    SQUAT,
    BICEP_CURL,
    LAT_PULLDOWN,
    SEATED_ROWS,
    TRICEP_EXTENSION,
    ;

    val romFactor: Float
        get() = when (this) {
            BENCH_PRESS -> 0.28f
            SQUAT -> 0.53f
            BICEP_CURL -> 0.15f
            LAT_PULLDOWN -> 0.60f
            SEATED_ROWS -> 0.40f
            TRICEP_EXTENSION -> 0.18f
            // Default or add cases: else -> 0.4f
        }

    // Computed string for Firestore (reusable)
    val type: String get() = name.lowercase().replace("_", "-")
}

data class Workout(
    val id: String,
    val workoutType: WorkoutType,  // Enum for type safety
    val date: String,  // ISO format
    val reps: Int,
    val avgRepTime: Float,
    val workoutScore: Float,
    val romScore: Float,
    val timingScore: Float,
    val tiltScore: Float,
    val weight: Double,
    val notes: String? = null
) {
    // For Firestore serialization
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "type" to workoutType.type,  // Uses enum's getter
        "date" to date,
        "reps" to reps,
        "avgRepTime" to avgRepTime,
        "workoutScore" to workoutScore,
        "romScore" to romScore,
        "timingScore" to timingScore,
        "tiltScore" to tiltScore,
        "weight" to weight,
        "notes" to notes
    )

    companion object {
        // Factory for deserialization (polymorphic via type string)
        fun fromMap(map: Map<String, Any?>): Workout {
            val typeStr = map["type"] as? String ?: throw IllegalArgumentException("Missing type")
            val workoutType = try {
                val normalized = typeStr.uppercase().replace("-", "_")
                WorkoutType.valueOf(normalized)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Unknown type: $typeStr")
            }
            return Workout(
                id = map["id"] as? String ?: "",
                workoutType = workoutType,
                date = map["date"] as? String ?: "",
                reps = (map["reps"] as? Int ?: 0),
                avgRepTime = (map["avgRepTime"] as? Float ?: 0f),
                workoutScore = (map["workoutScore"] as? Float ?: 0f),
                romScore = (map["romScore"] as? Float ?: 0f),
                timingScore = (map["timingScore"] as? Float ?: 0f),
                tiltScore = (map["tiltScore"] as? Float ?: 0f),
                weight = (map["weight"] as? Double ?: 0.0),
                notes = map["notes"] as? String
            )
        }
    }
}