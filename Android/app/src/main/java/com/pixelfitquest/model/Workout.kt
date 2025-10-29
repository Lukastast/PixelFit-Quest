package com.pixelfitquest.model

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
    val notes: String? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "date" to date,
        "name" to name,
        "totalExercises" to totalExercises,
        "totalSets" to totalSets,
        "overallScore" to overallScore,
        "notes" to notes
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): Workout = Workout(
            id = map["id"] as? String ?: "",
            date = map["date"] as? String ?: "",
            name = map["name"] as? String ?: "",
            totalExercises = map["totalExercises"] as? Int ?: 0,
            totalSets = map["totalSets"] as? Int ?: 0,
            overallScore = map["overallScore"] as? Float ?: 0f,
            notes = map["notes"] as? String
        )
    }
}

data class Exercise(
    val id: String,
    val workoutId: String,  // Parent reference
    val type: ExerciseType,
    val totalSets: Int,
    val exerciseScore: Float = 0f,  // Aggregate from sets
    val weight: Double = 0.0,  // Shared for exercise
    val notes: String? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "workoutId" to workoutId,
        "type" to type.type,
        "totalSets" to totalSets,
        "exerciseScore" to exerciseScore,
        "weight" to weight,
        "notes" to notes
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): Exercise = Exercise(
            id = map["id"] as? String ?: "",
            workoutId = map["workoutId"] as? String ?: "",
            type = ExerciseType.entries.find { it.type == (map["type"] as? String) } ?: ExerciseType.BENCH_PRESS,
            totalSets = map["totalSets"] as? Int ?: 0,
            exerciseScore = map["exerciseScore"] as? Float ?: 0f,
            weight = map["weight"] as? Double ?: 0.0,
            notes = map["notes"] as? String
        )
    }
}

data class WorkoutSet(
    val id: String,
    val exerciseId: String,
    val workoutId: String,
    val setNumber: Int,
    val reps: Int,
    val romScore: Float = 0f,
    val avgRepTime: Float = 0f,
    val verticalAccel: Float = 0f,  // Peak or avg
    val weight: Double = 0.0,  // Per set if varies
    val notes: String? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "exerciseId" to exerciseId,
        "workoutId" to workoutId,
        "setNumber" to setNumber,
        "reps" to reps,
        "romScore" to romScore,
        "avgRepTime" to avgRepTime,
        "verticalAccel" to verticalAccel,
        "weight" to weight,
        "notes" to notes
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): WorkoutSet = WorkoutSet(
            id = map["id"] as? String ?: "",
            exerciseId = map["exerciseId"] as? String ?: "",
            workoutId = map["workoutId"] as? String ?: "",
            setNumber = map["setNumber"] as? Int ?: 0,
            reps = map["reps"] as? Int ?: 0,
            romScore = map["romScore"] as? Float ?: 0f,
            avgRepTime = map["avgRepTime"] as? Float ?: 0f,
            verticalAccel = map["verticalAccel"] as? Float ?: 0f,
            weight = map["weight"] as? Double ?: 0.0,
            notes = map["notes"] as? String
        )
    }
}