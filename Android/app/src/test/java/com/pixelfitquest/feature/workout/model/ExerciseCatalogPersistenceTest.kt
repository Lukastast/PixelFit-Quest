package com.pixelfitquest.feature.workout.model

import com.pixelfitquest.feature.workout.model.enums.ExerciseType
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlan
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlanItem
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutTemplate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExerciseCatalogPersistenceTest {

    @Test
    fun exerciseRoundTripKeepsLogOnlyType() {
        val original = Exercise(
            id = "ex-1",
            workoutId = "w-1",
            type = ExerciseType.DEADLIFT,
            profileId = ExerciseType.DEADLIFT.type,
            totalSets = 3,
            weight = 140f,
        )
        val parsed = Exercise.fromMap(original.toMap())
        assertNotNull(parsed)
        assertEquals(ExerciseType.DEADLIFT, parsed!!.type)
        assertEquals("deadlift", parsed.profileId)
    }

    @Test
    fun exerciseFromMapAcceptsKebabType() {
        val parsed = Exercise.fromMap(
            mapOf(
                "id" to "ex-2",
                "workoutId" to "w-2",
                "type" to "overhead-press",
                "profileId" to "overhead-press",
                "totalSets" to 4,
                "weight" to 50,
            ),
        )
        assertEquals(ExerciseType.OVERHEAD_PRESS, parsed!!.type)
    }

    @Test
    fun templateRoundTripKeepsMixedImuAndLogOnly() {
        val template = WorkoutTemplate(
            id = "t1",
            name = "Push",
            plan = WorkoutPlan(
                listOf(
                    WorkoutPlanItem(ExerciseType.BENCH_PRESS, 3, 80f),
                    WorkoutPlanItem(ExerciseType.OVERHEAD_PRESS, 3, 50f),
                    WorkoutPlanItem(ExerciseType.LATERAL_RAISE, 3, 10f),
                ),
            ),
        )
        val restored = WorkoutTemplate.fromMap(template.toMap())
        assertEquals(
            listOf(
                ExerciseType.BENCH_PRESS,
                ExerciseType.OVERHEAD_PRESS,
                ExerciseType.LATERAL_RAISE,
            ),
            restored.plan.items.map { it.exercise },
        )
    }

    @Test
    fun workoutSetRoundTripKeepsSignedImbalance() {
        val original = WorkoutSet(
            id = "s1",
            exerciseId = "e1",
            workoutId = "w1",
            setNumber = 2,
            reps = 8,
            formScore = 81f,
            twistDeg = 12f,
            levelDeg = -8f,
            repRecords = listOf(
                RepRecord(
                    index = 0,
                    tStartNanos = 0L,
                    tEndNanos = 1L,
                    durationMs = 1L,
                    romEstimate = 0.4f,
                    romUnit = "METERS",
                    concentricMs = 500L,
                    eccentricMs = 500L,
                    pathDeviation = 0.1f,
                    romScore = 90f,
                    levelDeg = -8f,
                    twistDeg = 12f,
                    formScore = 81f,
                ),
            ),
        )
        val parsed = WorkoutSet.fromMap(original.toMap())
        assertNotNull(parsed)
        assertEquals(12f, parsed!!.twistDeg, 0.01f)
        assertEquals(-8f, parsed.levelDeg, 0.01f)
        assertEquals(12f, parsed.repRecords.first().twistDeg!!, 0.01f)
        assertEquals(-8f, parsed.repRecords.first().levelDeg!!, 0.01f)
    }
}
