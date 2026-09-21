package com.pixelfitquest.feature.workout.analysis

/**
 * Tempo quality: a longer eccentric is normal lifting. Dumping the weight
 * on the way down is the only named tempo fault.
 */
internal object Tempo {
    const val TAG_DROPPED = "dropped"
    const val DROP_ECCENTRIC_MS = 500L

    fun score(eccentricMs: Long, concentricMs: Long, durationOk: Boolean): Float {
        val ecc = eccentricMs.coerceAtLeast(1L)
        val con = concentricMs.coerceAtLeast(1L)
        val ratio = ecc.toFloat() / con
        val base = when {
            dropped(ecc) -> 50f
            ratio >= 1f -> 95f
            else -> 85f
        }
        return if (durationOk) base else base * 0.7f
    }

    fun dropped(eccentricMs: Long): Boolean = eccentricMs < DROP_ECCENTRIC_MS
}
