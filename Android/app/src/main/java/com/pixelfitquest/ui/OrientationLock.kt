package com.pixelfitquest.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun LockToPortrait() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context as? Activity ?: return@DisposableEffect onDispose {}
        val original = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            activity.requestedOrientation = original
        }
    }
}

@Composable
fun AllowAllOrientations() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context as? Activity ?: return@DisposableEffect onDispose {}
        val original = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose {
            activity.requestedOrientation = original
        }
    }
}
