package com.pixelfitquest.feature.workout.sensor

import android.content.SharedPreferences

/**
 * Which sleeve the phone is clipped to. One setting for the device, not per set.
 * Unset means the bar-axis sign is unknown: reps still count, but lateral tilt
 * and twist stay hidden.
 */
object MountSidePrefs {
    const val KEY = "bar_sleeve_side"

    fun get(prefs: SharedPreferences): MountSide {
        val raw = prefs.getString(KEY, null) ?: return MountSide.UNKNOWN
        return runCatching { MountSide.valueOf(raw) }.getOrDefault(MountSide.UNKNOWN)
    }

    fun set(prefs: SharedPreferences, side: MountSide) {
        prefs.edit().putString(KEY, side.name).apply()
    }
}

/** Debug/developer switch for sharing a raw IMU zip from the set review screen. */
object TraceExportPrefs {
    const val KEY = "developer_trace_export"

    fun isEnabled(prefs: SharedPreferences): Boolean = prefs.getBoolean(KEY, false)

    fun setEnabled(prefs: SharedPreferences, enabled: Boolean) {
        prefs.edit().putBoolean(KEY, enabled).apply()
    }
}
