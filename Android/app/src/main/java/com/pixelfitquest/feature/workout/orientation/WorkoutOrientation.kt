package com.pixelfitquest.feature.workout.orientation

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import com.pixelfitquest.feature.workout.model.WorkoutPhase

/** Device-local lay-flat preference. Not synced; does not require an account. */
object WorkoutOrientationPrefs {
    const val PREFS_NAME = "pixelfitquest_prefs"
    const val KEY_ENABLED = "workout_landscape_enabled"
    const val DEFAULT_ENABLED = true

    fun isEnabled(prefs: SharedPreferences): Boolean =
        prefs.getBoolean(KEY_ENABLED, DEFAULT_ENABLED)

    fun setEnabled(prefs: SharedPreferences, enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }
}

enum class WorkoutOrientationMode {
    /** Free rotation — sensor-driven, no lock. Used when lay-flat is OFF. */
    AllowRotation,
    /** Force landscape. Used when lay-flat is ON and a set is NOT being recorded. */
    Landscape,
    /** Freeze the current rotation. Used during an active recording to stabilise IMU axes. */
    Locked,
}

fun workoutOrientationMode(
    phase: WorkoutPhase,
    landscapeEnabled: Boolean,
): WorkoutOrientationMode {
    if (!landscapeEnabled) return WorkoutOrientationMode.AllowRotation
    return when (phase) {
        // Freeze rotation during IMU sampling so device axes cannot jump mid-set.
        WorkoutPhase.Recording -> WorkoutOrientationMode.Locked
        // All other phases: stay landscape (device was already set to landscape above).
        WorkoutPhase.Idle,
        WorkoutPhase.Countdown,
        WorkoutPhase.Reviewing -> WorkoutOrientationMode.Landscape
    }
}

fun WorkoutOrientationMode.toRequestedOrientation(): Int = when (this) {
    WorkoutOrientationMode.AllowRotation -> ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
    WorkoutOrientationMode.Landscape -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    WorkoutOrientationMode.Locked -> ActivityInfo.SCREEN_ORIENTATION_LOCKED
}
