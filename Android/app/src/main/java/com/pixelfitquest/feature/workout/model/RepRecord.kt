package com.pixelfitquest.feature.workout.model

import com.pixelfitquest.feature.workout.analysis.DetectedRep
import com.pixelfitquest.feature.workout.analysis.RomUnit

data class RepRecord(
    val index: Int,
    val tStartNanos: Long,
    val tEndNanos: Long,
    val durationMs: Long,
    val romEstimate: Float,
    val romUnit: String,
    val concentricMs: Long,
    val eccentricMs: Long,
    val pathDeviation: Float,
    val romScore: Float,
    val stabilityScore: Float? = null,
    val tempoScore: Float? = null,
    val formScore: Float,
    val tags: List<String> = emptyList(),
    val confidence: Float = 1f,
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "index" to index,
        "tStartNanos" to tStartNanos,
        "tEndNanos" to tEndNanos,
        "durationMs" to durationMs,
        "romEstimate" to romEstimate,
        "romUnit" to romUnit,
        "concentricMs" to concentricMs,
        "eccentricMs" to eccentricMs,
        "pathDeviation" to pathDeviation,
        "romScore" to romScore,
        "stabilityScore" to stabilityScore,
        "tempoScore" to tempoScore,
        "formScore" to formScore,
        "tags" to tags,
        "confidence" to confidence,
    )

    companion object {
        fun fromDetected(rep: DetectedRep): RepRecord = RepRecord(
            index = rep.index,
            tStartNanos = rep.tStartNanos,
            tEndNanos = rep.tEndNanos,
            durationMs = rep.durationMs,
            romEstimate = rep.romEstimate,
            romUnit = rep.romUnit.name,
            concentricMs = rep.concentricMs,
            eccentricMs = rep.eccentricMs,
            pathDeviation = rep.pathDeviation,
            romScore = rep.romScore,
            stabilityScore = rep.stabilityScore,
            tempoScore = rep.tempoScore,
            formScore = rep.formScore,
            tags = rep.tags,
            confidence = rep.confidence,
        )

        fun fromMap(map: Map<String, Any?>): RepRecord = RepRecord(
            index = (map["index"] as? Number)?.toInt() ?: 0,
            tStartNanos = (map["tStartNanos"] as? Number)?.toLong() ?: 0L,
            tEndNanos = (map["tEndNanos"] as? Number)?.toLong() ?: 0L,
            durationMs = (map["durationMs"] as? Number)?.toLong() ?: 0L,
            romEstimate = (map["romEstimate"] as? Number)?.toFloat() ?: 0f,
            romUnit = map["romUnit"] as? String ?: RomUnit.METERS.name,
            concentricMs = (map["concentricMs"] as? Number)?.toLong() ?: 0L,
            eccentricMs = (map["eccentricMs"] as? Number)?.toLong() ?: 0L,
            pathDeviation = (map["pathDeviation"] as? Number)?.toFloat() ?: 0f,
            romScore = (map["romScore"] as? Number)?.toFloat() ?: 0f,
            stabilityScore = (map["stabilityScore"] as? Number)?.toFloat(),
            tempoScore = (map["tempoScore"] as? Number)?.toFloat(),
            formScore = (map["formScore"] as? Number)?.toFloat() ?: 0f,
            tags = (map["tags"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            confidence = (map["confidence"] as? Number)?.toFloat() ?: 1f,
        )
    }
}
