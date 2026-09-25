package com.pixelfitquest.debug

import android.content.Context
import android.content.SharedPreferences
import com.pixelfitquest.feature.workout.orientation.WorkoutOrientationPrefs

/**
 * Debug-only preference helper for God Mode.
 * When enabled, all customization items, cosmetics, skins, themes, and dwellings
 * are treated as unlocked regardless of level or coins.
 *
 * Shares the same prefs file as other local device preferences so no extra I/O is needed.
 */
object GodModePrefs {
    const val KEY_GOD_MODE = "debug_god_mode_enabled"

    @Volatile
    var isGodModeActive: Boolean = false
        private set

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(WorkoutOrientationPrefs.PREFS_NAME, Context.MODE_PRIVATE)
        isGodModeActive = prefs.getBoolean(KEY_GOD_MODE, false)
    }

    fun isEnabled(prefs: SharedPreferences): Boolean {
        val enabled = prefs.getBoolean(KEY_GOD_MODE, false)
        isGodModeActive = enabled
        return enabled
    }

    fun setEnabled(prefs: SharedPreferences, enabled: Boolean) {
        prefs.edit().putBoolean(KEY_GOD_MODE, enabled).apply()
        isGodModeActive = enabled
    }
}
