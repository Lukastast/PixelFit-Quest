package com.pixelfitquest.feature.workout.analysis

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Per-exercise learned full-range amplitude.
 * Confirming a set only raises it. Marking a rep as 100% replaces it, and
 * Settings can clear it. Deleting workouts does not touch this store.
 */
@Singleton
class FullRomStore @Inject constructor(
    private val prefs: SharedPreferences,
) {
    fun get(exerciseId: String): Float? {
        val key = key(exerciseId)
        if (!prefs.contains(key)) return null
        return prefs.getFloat(key, 0f).takeIf { it > 1e-4f }
    }

    fun raise(exerciseId: String, amplitude: Float) {
        if (amplitude <= 1e-4f) return
        val current = get(exerciseId) ?: 0f
        if (amplitude > current) {
            prefs.edit().putFloat(key(exerciseId), amplitude).apply()
        }
    }

    /**
     * Confirm path: raise from accepted amps unless the user already marked 100% ROM
     * in this review ([userRecalibrated]). In that case [replace] already wrote their
     * baseline — taller peer amps must not push full-ROM above it.
     */
    fun raiseFromConfirmedSet(
        exerciseId: String,
        acceptedAmplitudes: List<Float>,
        userRecalibrated: Boolean,
    ) {
        if (userRecalibrated) return
        raise(exerciseId, credibleFullRom(acceptedAmplitudes))
    }

    /** User said this amplitude is full range, including when the stored mark is too high. */
    fun replace(exerciseId: String, amplitude: Float) {
        if (amplitude <= 1e-4f) return
        prefs.edit().putFloat(key(exerciseId), amplitude).apply()
    }

    fun clearAll() {
        val editor = prefs.edit()
        prefs.all.keys.filter { it.startsWith(PREFIX) }.forEach { editor.remove(it) }
        editor.apply()
    }

    private fun key(exerciseId: String) = "$PREFIX$exerciseId"

    private companion object {
        const val PREFIX = "full_rom_"
    }
}

/** Deepest rep in the set, ignoring one spike so a bad rep cannot poison the learned range. */
fun credibleFullRom(amplitudes: List<Float>): Float {
    val positive = amplitudes.filter { it > 1e-4f }
    if (positive.isEmpty()) return 0f
    val sorted = positive.sorted()
    val median = sorted[sorted.size / 2]
    val cap = median * 1.45f
    return positive.filter { it <= cap }.maxOrNull() ?: median
}
