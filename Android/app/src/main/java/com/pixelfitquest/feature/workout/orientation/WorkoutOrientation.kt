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
    Portrait,
    AllowRotation,
    Locked,
}

fun workoutOrientationMode(
    phase: WorkoutPhase,
    landscapeEnabled: Boolean,
): WorkoutOrientationMode {
    if (!landscapeEnabled) return WorkoutOrientationMode.Portrait
    return when (phase) {
        // Freeze the current rotation while IMU is sampled so device axes cannot jump.
        WorkoutPhase.Recording -> WorkoutOrientationMode.Locked
        WorkoutPhase.Idle,
        WorkoutPhase.Countdown,
        WorkoutPhase.Reviewing -> WorkoutOrientationMode.AllowRotation
    }
}

fun WorkoutOrientationMode.toRequestedOrientation(): Int = when (this) {
    WorkoutOrientationMode.Portrait -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    WorkoutOrientationMode.AllowRotation -> ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
    WorkoutOrientationMode.Locked -> ActivityInfo.SCREEN_ORIENTATION_LOCKED
}
