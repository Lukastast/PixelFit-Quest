package com.pixelfitquest.feature.workout.orientation

import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.pixelfitquest.feature.workout.model.WorkoutPhase

@Composable
fun WorkoutOrientationLock(
    phase: WorkoutPhase,
    landscapeEnabled: Boolean,
) {
    val activity = LocalActivity.current
    val mode = workoutOrientationMode(phase, landscapeEnabled)
    val keepScreenOn = phase == WorkoutPhase.Recording || phase == WorkoutPhase.Resting

    LaunchedEffect(mode) {
        activity?.requestedOrientation = mode.toRequestedOrientation()
    }

    // P1-4: only keep the screen awake while Recording or Resting (not Review/Idle).
    DisposableEffect(keepScreenOn) {
        val window = activity?.window
        if (keepScreenOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
}
