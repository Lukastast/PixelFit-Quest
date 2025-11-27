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
        fun fromMap(map: Map<String, Any?>): WorkoutSet? {
            // VI MÅ IKKE KASTE EXCEPTION – vi skal returnere null hvis krævede felter mangler
            val workoutId = map["workoutId"] as? String ?: return null
            val exerciseId = map["exerciseId"] as? String ?: return null

            return WorkoutSet(
                id = map["id"] as? String ?: "",
                workoutId = workoutId,
                exerciseId = exerciseId,
                setNumber = map["setNumber"]?.toString()?.toIntOrNull() ?: 0,
                reps = map["reps"]?.toString()?.toIntOrNull() ?: 0,
                romScore = map["romScore"]?.toString()?.toFloatOrNull() ?: 0f,
                xTiltScore = map["xTiltScore"]?.toString()?.toFloatOrNull() ?: 0f,
                zTiltScore = map["zTiltScore"]?.toString()?.toFloatOrNull() ?: 0f,
                workoutScore = map["workoutScore"]?.toString()?.toFloatOrNull() ?: 0f,
                avgRepTime = map["avgRepTime"]?.toString()?.toFloatOrNull() ?: 0f,
                verticalAccel = map["verticalAccel"]?.toString()?.toFloatOrNull() ?: 0f,
                weight = map["weight"]?.toString()?.toFloatOrNull() ?: 0f,
                notes = map["notes"] as? String
            )
        }
    }
}