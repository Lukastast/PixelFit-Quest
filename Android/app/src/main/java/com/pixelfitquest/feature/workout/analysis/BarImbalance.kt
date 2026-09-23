package com.pixelfitquest.feature.workout.analysis

import kotlin.math.abs

/** Degrees of free-weight play that do not cut form. */
const val BAR_DEADBAND_DEG = 5f
/** Coaching copy starts here. */
const val BAR_COACH_DEG = 8f
/** Review/resume treat smaller angles as even. */
const val BAR_EVEN_DEG = 2f
/** Tilt meter saturates at this many degrees from center. */
const val BAR_METER_SPAN_DEG = 25f

/**
 * Bar level and twist from one phone on the **right sleeve**.
 *
 * Identity mount: device X along the bar (toward the left sleeve), Z up.
 * Positive [AxisImbalance.meanDeg] on the level axis = pushing right (right sleeve high).
 * Positive twist = right hand closer to the head.
 */
internal data class AxisImbalance(
    val meanDeg: Float,
    val quality: Float,
)

internal object BarImbalance {
    const val LATERAL_SIGN = -1f
    const val TWIST_SIGN = 1f
    private const val QUALITY_PER_DEG = 2.5f

    const val TAG_TILT_RIGHT = "tilt_right"
    const val TAG_TILT_LEFT = "tilt_left"
    const val TAG_TWIST_HEAD = "twist_head"
    const val TAG_TWIST_HIP = "twist_hip"
    const val TAG_BAR_TILT = "bar_tilt"

    fun scoreAxis(
        values: FloatArray,
        range: IntRange,
        ref: Float,
        sign: Float,
    ): AxisImbalance {
        if (values.isEmpty()) return AxisImbalance(0f, 100f)
        val start = range.first.coerceIn(0, values.lastIndex)
        val end = range.last.coerceIn(0, values.lastIndex)
        if (end < start) return AxisImbalance(0f, 100f)
        var sum = 0.0
        var n = 0
        for (i in start..end) {
            sum += values[i] - ref
            n++
        }
        val meanDeg = sign * (sum / n).toFloat() * 180f / Math.PI.toFloat()
        return AxisImbalance(
            meanDeg = meanDeg,
            quality = qualityFromDeg(abs(meanDeg)),
        )
    }

    fun qualityFromDeg(absDeg: Float): Float {
        val excess = (absDeg - BAR_DEADBAND_DEG).coerceAtLeast(0f)
        return (100f - QUALITY_PER_DEG * excess).coerceIn(0f, 100f)
    }

    fun combinedQuality(level: Float?, twist: Float?): Float? {
        val parts = listOfNotNull(level, twist)
        return if (parts.isEmpty()) null else parts.average().toFloat()
    }

    fun tags(levelDeg: Float?, twistDeg: Float?, barQuality: Float?): List<String> {
        val out = ArrayList<String>(4)
        if (barQuality != null && barQuality < 70f) out += TAG_BAR_TILT
        if (levelDeg != null) {
            when {
                levelDeg > BAR_COACH_DEG -> out += TAG_TILT_RIGHT
                levelDeg < -BAR_COACH_DEG -> out += TAG_TILT_LEFT
            }
        }
        if (twistDeg != null) {
            when {
                twistDeg > BAR_COACH_DEG -> out += TAG_TWIST_HEAD
                twistDeg < -BAR_COACH_DEG -> out += TAG_TWIST_HIP
            }
        }
        return out
    }
}
