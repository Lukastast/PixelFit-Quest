package com.pixelfitquest.feature.workout.analysis

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

/** Per-exercise high-water mark of confirmed IMU amplitude. Only ever rises. */
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

    private fun key(exerciseId: String) = "full_rom_$exerciseId"
}
