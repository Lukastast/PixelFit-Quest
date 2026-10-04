package com.pixelfitquest.feature.workout.analysis

const val ANALYSIS_VERSION = 4

data class DetectedRep(
    val index: Int,
    val tStartNanos: Long,
    val tEndNanos: Long,
    val durationMs: Long,
    val romEstimate: Float,
    val romUnit: RomUnit,
    val concentricMs: Long,
    val eccentricMs: Long,
    val pathDeviation: Float,
    val romScore: Float,
    val stabilityScore: Float? = null,
    val tempoScore: Float? = null,
    /** Signed degrees. Positive = pushing more to the right. */
    val levelDeg: Float? = null,
    /** Signed degrees. Positive = right hand closer to the head. */
    val twistDeg: Float? = null,
    val formScore: Float,
    val tags: List<String>,
    val confidence: Float,
    val accepted: Boolean,
    val assisted: Boolean = false,
) {
    val isManual: Boolean get() = "manual" in tags

    /** Score this measured rep against a full-range amplitude. Does not mark a manual override. */
    fun scoredAgainst(baseline: Float): DetectedRep {
        if (isManual || baseline <= 1e-4f || romEstimate <= 1e-4f) return this
        val percent = ((romEstimate / baseline) * 100f).coerceIn(0f, 100f)
        val form = formScoreFrom(romScore = percent, tempoScore = tempoScore, barQuality = stabilityScore)
        val next = buildList {
            addAll(tags.filter { it != "short_rom" })
            if (percent < 70f) add("short_rom")
        }.distinct()
        return copy(romScore = percent, formScore = form, tags = next)
    }

    fun withRomPercent(percent: Float): DetectedRep {
        val rom = percent.coerceIn(0f, 100f)
        val form = formScoreFrom(romScore = rom, tempoScore = tempoScore, barQuality = stabilityScore)
        val nextTags = buildList {
            addAll(if (isManual) tags else tags + "rom_override")
            if (rom < 70f) add("short_rom")
        }.distinct().let { list ->
            if (rom >= 70f) list - "short_rom" else list
        }
        return copy(romScore = rom, formScore = form, tags = nextTags)
    }
}

/** Range of motion is the main quality of the rep; bar control next; tempo last. */
const val FORM_ROM_WEIGHT = 0.50f
const val FORM_BAR_WEIGHT = 0.30f
const val FORM_TEMPO_WEIGHT = 0.20f

fun formScoreFrom(
    romScore: Float? = null,
    tempoScore: Float? = null,
    barQuality: Float? = null,
): Float {
    var total = 0f
    var weight = 0f
    if (romScore != null) {
        total += romScore * FORM_ROM_WEIGHT
        weight += FORM_ROM_WEIGHT
    }
    if (barQuality != null) {
        total += barQuality * FORM_BAR_WEIGHT
        weight += FORM_BAR_WEIGHT
    }
    if (tempoScore != null) {
        total += tempoScore * FORM_TEMPO_WEIGHT
        weight += FORM_TEMPO_WEIGHT
    }
    return if (weight <= 0f) 0f else total / weight
}

data class SetAnalysis(
    val analysisVersion: Int = ANALYSIS_VERSION,
    val reps: List<DetectedRep>,
    val meanFormScore: Float,
    val flags: List<String> = emptyList(),
    val usedRotationVector: Boolean = false,
) {
    val acceptedReps: List<DetectedRep> get() = reps.filter { it.accepted }
    val candidateReps: List<DetectedRep> get() = reps.filter { !it.accepted }
}

/**
 * Physical range for one set: mean amplitude of accepted, measured reps.
 * This is not [DetectedRep.romScore] (percent of the learned full-ROM baseline).
 * Null when the set has no measured rep, so older logs stay distinguishable.
 */
data class PerSetRom(
    val estimate: Float,
    val unit: RomUnit,
)

fun perSetRom(reps: List<DetectedRep>): PerSetRom? {
    val measured = reps.filter { it.accepted && !it.isManual && it.romEstimate > 1e-4f }
    if (measured.isEmpty()) return null
    val unit = measured.first().romUnit
    val sameUnit = measured.filter { it.romUnit == unit }
    if (sameUnit.isEmpty()) return null
    return PerSetRom(
        estimate = sameUnit.map { it.romEstimate }.average().toFloat(),
        unit = unit,
    )
}
