package com.pixelfitquest.feature.workout.model

import com.pixelfitquest.feature.workout.analysis.ANALYSIS_VERSION

const val WORKOUT_SCHEMA_VERSION = 2

data class WorkoutSet(
    val id: String,
    val exerciseId: String,
    val workoutId: String,
    val setNumber: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val schemaVersion: Int = WORKOUT_SCHEMA_VERSION,
    val analysisVersion: Int = ANALYSIS_VERSION,
    val reps: Int,
    val userCorrected: Boolean = false,
    val weight: Float = 0f,
    val rpe: Int? = null,
    val romScore: Float = 0f,
    val stabilityScore: Float = 0f,
    val tempoScore: Float = 0f,
    val formScore: Float = 0f,
    val avgRepTime: Float = 0f,
    val totalDurationMillis: Long = 0,
    val xTiltScore: Float = 0f,
    val zTiltScore: Float = 0f,
    val flags: List<String> = emptyList(),
    val repRecords: List<RepRecord> = emptyList(),
    val notes: String? = null,
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "exerciseId" to exerciseId,
        "workoutId" to workoutId,
        "setNumber" to setNumber,
        "timestamp" to timestamp,
        "schemaVersion" to schemaVersion,
        "analysisVersion" to analysisVersion,
        "reps" to reps,
        "userCorrected" to userCorrected,
        "weight" to weight,
        "rpe" to rpe,
        "romScore" to romScore,
        "stabilityScore" to stabilityScore,
        "tempoScore" to tempoScore,
        "formScore" to formScore,
        "avgRepTime" to avgRepTime,
        "totalDurationMillis" to totalDurationMillis,
        "xTiltScore" to xTiltScore,
        "zTiltScore" to zTiltScore,
        "flags" to flags,
        "repRecords" to repRecords.map { it.toMap() },
        "notes" to notes,
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): WorkoutSet? {
            val workoutId = map["workoutId"] as? String ?: return null
            val exerciseId = map["exerciseId"] as? String ?: return null
            val repRaw = map["repRecords"] as? List<*> ?: emptyList<Any>()
            val records = repRaw.mapNotNull { raw ->
                val item = raw as? Map<*, *> ?: return@mapNotNull null
                RepRecord.fromMap(item.entries.associate { it.key.toString() to it.value })
            }
            return WorkoutSet(
                id = map["id"] as? String ?: "",
                workoutId = workoutId,
                exerciseId = exerciseId,
                setNumber = (map["setNumber"] as? Number)?.toInt() ?: 0,
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                schemaVersion = (map["schemaVersion"] as? Number)?.toInt() ?: WORKOUT_SCHEMA_VERSION,
                analysisVersion = (map["analysisVersion"] as? Number)?.toInt() ?: ANALYSIS_VERSION,
                reps = (map["reps"] as? Number)?.toInt() ?: 0,
                userCorrected = map["userCorrected"] as? Boolean ?: false,
                weight = (map["weight"] as? Number)?.toFloat() ?: 0f,
                rpe = (map["rpe"] as? Number)?.toInt(),
                romScore = (map["romScore"] as? Number)?.toFloat() ?: 0f,
                stabilityScore = (map["stabilityScore"] as? Number)?.toFloat() ?: 0f,
                tempoScore = (map["tempoScore"] as? Number)?.toFloat() ?: 0f,
                formScore = (map["formScore"] as? Number)?.toFloat() ?: 0f,
                avgRepTime = (map["avgRepTime"] as? Number)?.toFloat() ?: 0f,
                totalDurationMillis = (map["totalDurationMillis"] as? Number)?.toLong() ?: 0L,
                xTiltScore = (map["xTiltScore"] as? Number)?.toFloat() ?: 0f,
                zTiltScore = (map["zTiltScore"] as? Number)?.toFloat() ?: 0f,
                flags = (map["flags"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                repRecords = records,
                notes = map["notes"] as? String,
            )
        }
    }
}
