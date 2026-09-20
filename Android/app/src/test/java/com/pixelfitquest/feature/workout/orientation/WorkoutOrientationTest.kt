package com.pixelfitquest.feature.workout.orientation

import android.content.pm.ActivityInfo
import com.pixelfitquest.feature.workout.model.WorkoutPhase
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutOrientationTest {

    @Test
    fun disabledAllowsFreeRotationInEveryPhase() {
        WorkoutPhase.entries.forEach { phase ->
            assertEquals(
                "phase=$phase",
                WorkoutOrientationMode.AllowRotation,
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
    fun idleCountdownAndReviewForceLandscapeWhenEnabled() {
        listOf(
            WorkoutPhase.Idle,
            WorkoutPhase.Countdown,
            WorkoutPhase.Reviewing,
        ).forEach { phase ->
            assertEquals(
                "phase=$phase",
                WorkoutOrientationMode.Landscape,
                workoutOrientationMode(phase, landscapeEnabled = true),
            )
        }
    }

    @Test
    fun mapsModesToActivityRequestedOrientation() {
        assertEquals(
            ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR,
            WorkoutOrientationMode.AllowRotation.toRequestedOrientation(),
        )
        assertEquals(
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
            WorkoutOrientationMode.Landscape.toRequestedOrientation(),
        )
        assertEquals(
            ActivityInfo.SCREEN_ORIENTATION_LOCKED,
            WorkoutOrientationMode.Locked.toRequestedOrientation(),
        )
    }
}
