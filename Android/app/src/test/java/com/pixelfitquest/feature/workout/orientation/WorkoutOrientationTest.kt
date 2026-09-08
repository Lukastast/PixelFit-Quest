package com.pixelfitquest.feature.workout.orientation

import android.content.pm.ActivityInfo
import com.pixelfitquest.feature.workout.model.WorkoutPhase
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutOrientationTest {

    @Test
    fun disabledStaysPortraitInEveryPhase() {
        WorkoutPhase.entries.forEach { phase ->
            assertEquals(
                "phase=$phase",
                WorkoutOrientationMode.Portrait,
                workoutOrientationMode(phase, landscapeEnabled = false),
            )
        }
    }

    @Test
    fun recordingLocksWhenLandscapeEnabled() {
        assertEquals(
            WorkoutOrientationMode.Locked,
            workoutOrientationMode(WorkoutPhase.Recording, landscapeEnabled = true),
        )
    }

    @Test
    fun idleCountdownAndReviewAllowRotation() {
        listOf(
            WorkoutPhase.Idle,
            WorkoutPhase.Countdown,
            WorkoutPhase.Reviewing,
        ).forEach { phase ->
            assertEquals(
                "phase=$phase",
                WorkoutOrientationMode.AllowRotation,
                workoutOrientationMode(phase, landscapeEnabled = true),
            )
        }
    }

    @Test
    fun mapsModesToActivityRequestedOrientation() {
        assertEquals(
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
            WorkoutOrientationMode.Portrait.toRequestedOrientation(),
        )
        assertEquals(
            ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR,
            WorkoutOrientationMode.AllowRotation.toRequestedOrientation(),
        )
        assertEquals(
            ActivityInfo.SCREEN_ORIENTATION_LOCKED,
            WorkoutOrientationMode.Locked.toRequestedOrientation(),
        )
    }
}
