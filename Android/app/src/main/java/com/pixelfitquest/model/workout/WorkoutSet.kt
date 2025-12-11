package com.pixelfitquest.model.workout

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