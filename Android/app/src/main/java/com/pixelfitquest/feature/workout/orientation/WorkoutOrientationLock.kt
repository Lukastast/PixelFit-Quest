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

    LaunchedEffect(mode) {
        activity?.requestedOrientation = mode.toRequestedOrientation()
    }

    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
}
