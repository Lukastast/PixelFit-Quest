package com.pixelfitquest.feature.workout.analysis

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupTrimTest {

    @Test
    fun emptyBuffer_staysEmpty() {
        listOf(
            ExerciseProfiles.deadlift,
            ExerciseProfiles.squat,
            ExerciseProfiles.benchPress,
            ExerciseProfiles.bicepCurl,
        ).forEach { profile ->
            assertTrue(SetupTrim.trim(emptyList(), profile).isEmpty())
        }
    }

    @Test
    fun deadlift_keepsTheFirstPull() {
        val pull = cycle(amplitude = 0.50f, durationMs = 1800L)
        val second = cycle(amplitude = 0.48f, durationMs = 1700L)
        // Motion that would be dropped on a squat is the floor pickup here.
        val leading = cycle(amplitude = 0.10f, durationMs = 900L, truncated = true)
        val cycles = listOf(leading, pull, second)

        val trimmed = SetupTrim.trim(cycles, ExerciseProfiles.deadlift)

        assertEquals(cycles, trimmed)
        assertEquals(leading.amplitude, trimmed.first().amplitude, 0.0001f)
        assertEquals(SetupFamily.DEADLIFT, SetupTrim.family(ExerciseProfiles.sumoDeadlift))
    }

    @Test
    fun squatBenchCurl_dropMotionBeforeTheFirstFullCycle() {
        val walkIn = cycle(amplitude = 0.08f, durationMs = 1200L, truncated = true)
        val unrack = cycle(amplitude = 0.70f, durationMs = 1500L, setup = true)
        val first = cycle(amplitude = 1.20f, durationMs = 2000L)
        val second = cycle(amplitude = 1.10f, durationMs = 1900L)
        val cycles = listOf(walkIn, unrack, first, second)

        listOf(
            ExerciseProfiles.squat,
            ExerciseProfiles.benchPress,
            ExerciseProfiles.bicepCurl,
            ExerciseProfiles.inclineBenchPress,
            ExerciseProfiles.frontSquat,
            ExerciseProfiles.preacherCurl,
        ).forEach { profile ->
            val trimmed = SetupTrim.trim(cycles, profile)
            assertEquals(
                profile.id,
                listOf(first.amplitude, second.amplitude),
                trimmed.map { it.amplitude },
            )
        }
    }

    @Test
    fun setupOnly_doesNotInventARep() {
        val setup = cycle(amplitude = 0.55f, durationMs = 1600L, setup = true)
        val partial = cycle(amplitude = 0.05f, durationMs = 400L, truncated = true)
        val onlySetup = listOf(setup, partial)

        listOf(
            ExerciseProfiles.squat,
            ExerciseProfiles.benchPress,
            ExerciseProfiles.bicepCurl,
        ).forEach { profile ->
            val trimmed = SetupTrim.trim(onlySetup, profile)
            assertTrue("${profile.id} invented ${trimmed.size}", trimmed.isEmpty())
        }

        val deadlift = SetupTrim.trim(onlySetup, ExerciseProfiles.deadlift)
        assertEquals(onlySetup, deadlift)
        assertTrue(deadlift.all { it.setup || it.truncated })
    }

    @Test
    fun firstCycleAlreadyFull_isKept() {
        val first = cycle(amplitude = 0.35f, durationMs = 2000L)
        val second = cycle(amplitude = 0.34f, durationMs = 2000L)
        val cycles = listOf(first, second)
        assertEquals(cycles, SetupTrim.trim(cycles, ExerciseProfiles.benchPress))
        assertEquals(cycles, SetupTrim.trim(cycles, ExerciseProfiles.deadlift))
    }

    @Test
    fun perSetRom_isMeanOfMeasuredReps_andNullWithoutThem() {
        val a = rep(0.40f)
        val b = rep(0.50f)
        val manual = rep(0.90f).copy(tags = listOf("manual"), accepted = true)
        val metric = perSetRom(listOf(a, b, manual, rep(0f).copy(accepted = false)))
        assertEquals(0.45f, metric!!.estimate, 0.0001f)
        assertEquals(RomUnit.METERS, metric.unit)
        assertNull(perSetRom(listOf(manual)))
        assertNull(perSetRom(emptyList()))
    }

    private fun rep(amplitude: Float): DetectedRep = DetectedRep(
        index = 0,
        tStartNanos = 0L,
        tEndNanos = 1_000_000_000L,
        durationMs = 1000L,
        romEstimate = amplitude,
        romUnit = RomUnit.METERS,
        concentricMs = 500L,
        eccentricMs = 500L,
        pathDeviation = 0f,
        romScore = 100f,
        formScore = 100f,
        tags = emptyList(),
        confidence = 1f,
        accepted = true,
    )

    private fun cycle(
        amplitude: Float,
        durationMs: Long,
        truncated: Boolean = false,
        setup: Boolean = false,
    ): RawCycle = RawCycle(
        startIndex = 0,
        extremumIndex = 1,
        endIndex = 2,
        tStartNanos = 0L,
        tMidNanos = durationMs * 500_000L,
        tEndNanos = durationMs * 1_000_000L,
        amplitude = amplitude,
        truncated = truncated,
        setup = setup,
    )
}
