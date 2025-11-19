package com.pixelfitquest.model

import android.util.Log

// Enum at the top
enum class ExerciseType {
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
    val date: String,  // ISO
    val name: String,  // e.g., "Leg Day"
    val totalExercises: Int = 0,
    val totalSets: Int = 0,
    val overallScore: Float = 0f,  // Aggregate from exercises
    val notes: String? = null,
    val rewardsAwarded: Boolean = false
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "date" to date,
        "name" to name,
        "totalExercises" to totalExercises,
        "totalSets" to totalSets,
        "overallScore" to overallScore,
        "notes" to notes,
        "rewardsAwarded" to rewardsAwarded

    )

    companion object {
        fun fromMap(map: Map<String, Any?>): Workout = Workout(
            id = map["id"] as? String ?: "",
            date = map["date"] as? String ?: "",
            name = map["name"] as? String ?: "",
            totalExercises = map["totalExercises"] as? Int ?: 0,
            totalSets = map["totalSets"] as? Int ?: 0,
            overallScore = map["overallScore"] as? Float ?: 0f,
            notes = map["notes"] as? String,
            rewardsAwarded = map["rewardsAwarded"] as? Boolean ?: false
        )
    }
}

data class Exercise(
    val id: String,
    val workoutId: String,  // Parent reference
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
        fun fromMap(map: Map<String, Any?>): Exercise {
            val parsedType = ExerciseType.entries.find { it.type == (map["type"] as? String) }
            if (parsedType == null) {
                Log.d("Exercise", "Unknown type '${map["type"]}', defaulting to BENCH_PRESS")
            }
            return Exercise(
                id = map["id"] as? String ?: "",
                workoutId = map["workoutId"] as? String ?: "",
                type = parsedType ?: ExerciseType.BENCH_PRESS,
                totalSets = map["totalSets"] as? Int ?: 0,
                weight = map["weight"] as? Float ?: 0f,
                notes = map["notes"] as? String
            )
        }
    }
}

data class WorkoutSet(
    val id: String,
    val exerciseId: String,
    val workoutId: String,
    val setNumber: Int,
    val reps: Int,
    val romScore: Float = 0f,
    val xTiltScore: Float = 0f,
    val zTiltScore: Float = 0f,
    val workoutScore: Float = 0f,
    val avgRepTime: Float = 0f,
    val verticalAccel: Float = 0f,
    val weight: Float = 0f,
    val notes: String? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "exerciseId" to exerciseId,
        "workoutId" to workoutId,
        "setNumber" to setNumber,
        "reps" to reps,
        "romScore" to romScore,
        "xTiltScore" to xTiltScore,
        "zTiltScore" to zTiltScore,
        "workoutScore" to workoutScore,
        "avgRepTime" to avgRepTime,
        "verticalAccel" to verticalAccel,
        "weight" to weight,
        "notes" to notes
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): WorkoutSet {
            return WorkoutSet(
                id = map["id"] as? String ?: "",
                exerciseId = map["exerciseId"] as? String ?: "",
                workoutId = map["workoutId"] as? String ?: "",
                setNumber = (map["setNumber"] as? Long)?.toInt() ?: (map["setNumber"] as? Double)?.toInt() ?: (map["setNumber"] as? Int) ?: 0,
                reps = (map["reps"] as? Long)?.toInt() ?: (map["reps"] as? Double)?.toInt() ?: (map["reps"] as? Int) ?: 0,
                romScore = (map["romScore"] as? Double)?.toFloat() ?: (map["romScore"] as? Float) ?: (map["romScore"] as? Long)?.toFloat() ?: 0f,
                xTiltScore = (map["xTiltScore"] as? Double)?.toFloat() ?: (map["xTiltScore"] as? Float) ?: (map["xTiltScore"] as? Long)?.toFloat() ?: 0f,
                zTiltScore = (map["zTiltScore"] as? Double)?.toFloat() ?: (map["zTiltScore"] as? Float) ?: (map["zTiltScore"] as? Long)?.toFloat() ?: 0f,
                workoutScore = (map["workoutScore"] as? Double)?.toFloat() ?: (map["workoutScore"] as? Float) ?: (map["workoutScore"] as? Long)?.toFloat() ?: 0f,
                avgRepTime = (map["avgRepTime"] as? Double)?.toFloat() ?: (map["avgRepTime"] as? Float) ?: (map["avgRepTime"] as? Long)?.toFloat() ?: 0f,
                verticalAccel = (map["verticalAccel"] as? Double)?.toFloat() ?: (map["verticalAccel"] as? Float) ?: (map["verticalAccel"] as? Long)?.toFloat() ?: 0f,
                weight = (map["weight"] as? Double)?.toFloat() ?: (map["weight"] as? Float) ?: (map["weight"] as? Long)?.toFloat() ?: 0f,
                notes = map["notes"] as? String
            )
        }
    }
}