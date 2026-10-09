package com.pixelfitquest.feature.workout

import android.content.SharedPreferences

/**
 * Rest clock between sets. Off until turned on in Settings.
 * Reaching zero starts the next set only when autostart is also on.
 */
object RestTimerPrefs {
    const val KEY_ENABLED = "rest_timer_enabled"
    const val DEFAULT_ENABLED = false

    const val KEY_SECONDS = "rest_timer_seconds"
    const val DEFAULT_SECONDS = 90
    const val MIN_SECONDS = 30
    const val MAX_SECONDS = 300
    const val STEP_SECONDS = 15

    const val KEY_AUTOSTART = "rest_timer_autostart"
    const val DEFAULT_AUTOSTART = false

    fun isEnabled(prefs: SharedPreferences): Boolean =
        prefs.getBoolean(KEY_ENABLED, DEFAULT_ENABLED)

    fun setEnabled(prefs: SharedPreferences, enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun getSeconds(prefs: SharedPreferences): Int =
        coerceSeconds(prefs.getInt(KEY_SECONDS, DEFAULT_SECONDS))

    fun setSeconds(prefs: SharedPreferences, seconds: Int) {
        prefs.edit().putInt(KEY_SECONDS, coerceSeconds(seconds)).apply()
    }

    fun isAutostartEnabled(prefs: SharedPreferences): Boolean =
        prefs.getBoolean(KEY_AUTOSTART, DEFAULT_AUTOSTART)

    fun setAutostartEnabled(prefs: SharedPreferences, enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTOSTART, enabled).apply()
    }

    fun coerceSeconds(seconds: Int): Int {
        val stepped = (seconds / STEP_SECONDS) * STEP_SECONDS
        return stepped.coerceIn(MIN_SECONDS, MAX_SECONDS)
    }
}
