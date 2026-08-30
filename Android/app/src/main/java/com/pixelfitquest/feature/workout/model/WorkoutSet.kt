package com.pixelfitquest.feature.workout.model

data class RepPerformance(
    val repNumber: Int,
    val romScore: Float,
    val stabilityScore: Float,
    val durationMillis: Long,
    val velocity: Float = 0f
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "repNumber" to repNumber,
        "romScore" to romScore,
        "stabilityScore" to stabilityScore,
        "durationMillis" to durationMillis,
        "velocity" to velocity
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): RepPerformance = RepPerformance(
            repNumber = (map["repNumber"] as? Number)?.toInt() ?: 0,
            romScore = (map["romScore"] as? Number)?.toFloat() ?: 0f,
            stabilityScore = (map["stabilityScore"] as? Number)?.toFloat() ?: 0f,
            durationMillis = (map["durationMillis"] as? Number)?.toLong() ?: 0L,
            velocity = (map["velocity"] as? Number)?.toFloat() ?: 0f
        )
    }
}

data class WorkoutSet(
    val id: String,
    val exerciseId: String,
    val workoutId: String,
    val setNumber: Int,
    val timestamp: Long = System.currentTimeMillis(),

    // Quantity Metrics
    val reps: Int,
    val weight: Float = 0f,
    val rpe: Int? = null, // Rate of Perceived Exertion (1-10)

    // Quality Summary (0-100)
    val romScore: Float = 0f,
    val stabilityScore: Float = 0f, // Composite of X and Z tilts
    val velocityScore: Float = 0f,  // Consistency/Speed metric
    val formScore: Float = 0f,      // Aggregate quality score

    // Technical Details
    val avgRepTime: Float = 0f,
    val totalDurationMillis: Long = 0,
    val xTiltScore: Float = 0f,
    val zTiltScore: Float = 0f,
    val verticalAccel: Float = 0f,

    // Per-Rep Data
    val repDetails: List<RepPerformance> = emptyList(),

    val notes: String? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "exerciseId" to exerciseId,
        "workoutId" to workoutId,
        "setNumber" to setNumber,
        "timestamp" to timestamp,
        "reps" to reps,
        "weight" to weight,
        "rpe" to rpe,
        "romScore" to romScore,
        "stabilityScore" to stabilityScore,
        "velocityScore" to velocityScore,
        "formScore" to formScore,
        "avgRepTime" to avgRepTime,
        "totalDurationMillis" to totalDurationMillis,
        "xTiltScore" to xTiltScore,
        "zTiltScore" to zTiltScore,
        "verticalAccel" to verticalAccel,
        "repDetails" to repDetails.map { it.toMap() },
        "notes" to notes
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): WorkoutSet? {
            val workoutId = map["workoutId"] as? String ?: return null
            val exerciseId = map["exerciseId"] as? String ?: return null

            val repDetailsRaw = map["repDetails"] as? List<Map<String, Any?>> ?: emptyList()

            return WorkoutSet(
                id = map["id"] as? String ?: "",
                workoutId = workoutId,
                exerciseId = exerciseId,
                setNumber = (map["setNumber"] as? Number)?.toInt() ?: 0,
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                reps = (map["reps"] as? Number)?.toInt() ?: 0,
                weight = (map["weight"] as? Number)?.toFloat() ?: 0f,
                rpe = (map["rpe"] as? Number)?.toInt(),
                romScore = (map["romScore"] as? Number)?.toFloat() ?: 0f,
                stabilityScore = (map["stabilityScore"] as? Number)?.toFloat() ?: 0f,
                velocityScore = (map["velocityScore"] as? Number)?.toFloat() ?: 0f,
                formScore = (map["formScore"] as? Number ?: map["workoutScore"] as? Number)?.toFloat() ?: 0f,
                avgRepTime = (map["avgRepTime"] as? Number)?.toFloat() ?: 0f,
                totalDurationMillis = (map["totalDurationMillis"] as? Number)?.toLong() ?: 0L,
                xTiltScore = (map["xTiltScore"] as? Number)?.toFloat() ?: 0f,
                zTiltScore = (map["zTiltScore"] as? Number)?.toFloat() ?: 0f,
                verticalAccel = (map["verticalAccel"] as? Number)?.toFloat() ?: 0f,
                repDetails = repDetailsRaw.map { RepPerformance.fromMap(it) },
                notes = map["notes"] as? String
            )
        }
    }
}
