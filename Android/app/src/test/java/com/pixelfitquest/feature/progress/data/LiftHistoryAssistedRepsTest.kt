package com.pixelfitquest.feature.progress.data

import com.pixelfitquest.feature.workout.model.RepRecord
import com.pixelfitquest.feature.workout.model.WorkoutSet
import com.pixelfitquest.feature.workout.model.enums.ExerciseType
import org.junit.Assert.assertEquals
import org.junit.Test

class LiftHistoryAssistedRepsTest {

    private fun rep(index: Int, assisted: Boolean) = RepRecord(
        index = index,
        tStartNanos = 0L,
        tEndNanos = 1_000_000_000L,
        durationMs = 1000L,
        romEstimate = 0.4f,
        romUnit = "METERS",
        concentricMs = 500L,
        eccentricMs = 500L,
        pathDeviation = 0f,
        romScore = 100f,
        formScore = 90f,
        assisted = assisted,
    )

    @Test
    fun toLiftHistoryEntity_excludesAssistedRepsFromProgressionCount() {
        val set = WorkoutSet(
            id = "s1",
            exerciseId = "e1",
            workoutId = "w1",
            setNumber = 1,
            reps = 8,
            weight = 60f,
            repRecords = listOf(
                rep(0, false),
                rep(1, false),
                rep(2, true),
                rep(3, true),
                rep(4, false),
                rep(5, false),
                rep(6, false),
                rep(7, false),
            ),
        )
        assertEquals(6, set.progressionRepCount())
        assertEquals(6, set.toLiftHistoryEntity(ExerciseType.BENCH_PRESS).reps)
    }

    @Test
    fun progressionRepCount_fallsBackToRepsWhenNoRecords() {
        val set = WorkoutSet(
            id = "s2",
            exerciseId = "e1",
            workoutId = "w1",
            setNumber = 1,
            reps = 5,
            weight = 40f,
        )
        assertEquals(5, set.progressionRepCount())
    }
}
