package com.pixelfitquest.feature.workout.analysis

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetAnalyzerTest {

    private val analyzer = SetAnalyzer()
    private val user = AnalyzerUser(heightCm = 178)

    @Test
    fun cleanEightRepBench_acceptsEight() {
        val analysis = analyzer.analyzeSet(
            SyntheticWaveforms.cleanBench8(),
            ExerciseProfiles.benchPress,
            user,
        )
        assertEquals(
            "accepted=${analysis.acceptedReps.size} all=${analysis.reps.map { "${it.accepted}:${"%.2f".format(it.romEstimate)}" }}",
            8,
            analysis.acceptedReps.size,
        )
        assertTrue(
            "unexpected candidates ${analysis.candidateReps.map { it.tags to it.romEstimate }}",
            analysis.candidateReps.isEmpty(),
        )
        analysis.acceptedReps.forEach { rep ->
            assertTrue("ROM percent must be 0–100, was ${rep.romScore}", rep.romScore in 0f..100f)
        }
    }

    @Test
    fun noisyFalseDip_doesNotAddARep() {
        val analysis = analyzer.analyzeSet(
            SyntheticWaveforms.benchWithFalseDip(),
            ExerciseProfiles.benchPress,
            user,
        )
        assertEquals(
            "accepted=${analysis.acceptedReps.size} candidates=${analysis.candidateReps.size} " +
                analysis.reps.map { "${it.accepted}:${"%.3f".format(it.romEstimate)}" }.toString(),
            8,
            analysis.acceptedReps.size,
        )
        assertTrue(
            "false dip should not be accepted",
            analysis.acceptedReps.none { it.romEstimate < 0.12f },
        )
    }

    @Test
    fun truncatedLastRep_isNotAccepted() {
        val analysis = analyzer.analyzeSet(
            SyntheticWaveforms.benchTruncatedLast(),
            ExerciseProfiles.benchPress,
            user,
        )
        assertEquals(8, analysis.acceptedReps.size)
        assertTrue(
            "truncated leftover must not be accepted: ${analysis.reps.map { it.tags to it.accepted }}",
            analysis.acceptedReps.none { "truncated" in it.tags },
        )
    }

    @Test
    fun curlPitchProfile_segmentsOnAngleNotWorldUp() {
        val curlSamples = SyntheticWaveforms.cleanCurl8()
        val curl = analyzer.analyzeSet(curlSamples, ExerciseProfiles.bicepCurl, user)
        val asBench = analyzer.analyzeSet(curlSamples, ExerciseProfiles.benchPress, user)

        assertEquals(
            "curl accepted=${curl.acceptedReps.size} amps=${curl.reps.map { "%.2f".format(it.romEstimate) }}",
            8,
            curl.acceptedReps.size,
        )
        assertEquals(RomUnit.RADIANS, curl.acceptedReps.first().romUnit)
        assertTrue(
            "bench profile on curl IMU should not count vertical reps, got ${asBench.acceptedReps.size}",
            asBench.acceptedReps.size <= 1,
        )
        curl.acceptedReps.forEach { rep ->
            assertTrue("ROM percent must be 0–100, was ${rep.romScore}", rep.romScore in 0f..100f)
        }
    }

    @Test
    fun gravityLeak_stillCountsEightBench() {
        val analysis = analyzer.analyzeSet(
            SyntheticWaveforms.verticalReps(repCount = 8, gravityLeak = 0.25f),
            ExerciseProfiles.benchPress,
            user,
        )
        assertEquals(
            "accepted=${analysis.acceptedReps.size} ${analysis.reps.map { it.romScore.toInt() }}",
            8,
            analysis.acceptedReps.size,
        )
    }

    @Test
    fun oneDegreeTilt_stillCountsEight() {
        val deg = (Math.PI / 180.0).toFloat()
        val analysis = analyzer.analyzeSet(
            SyntheticWaveforms.verticalReps(repCount = 8, tiltRad = deg),
            ExerciseProfiles.benchPress,
            user,
        )
        assertEquals(8, analysis.acceptedReps.size)
        analysis.acceptedReps.forEach {
            assertTrue("1° tilt should not tank stability: ${it.stabilityScore}", (it.stabilityScore ?: 0f) >= 85f)
        }
    }

    @Test
    fun fiftyAndTwoHundredHz_sameRepCount() {
        val at50 = analyzer.analyzeSet(SyntheticWaveforms.cleanBench8(hz = 50), ExerciseProfiles.benchPress, user)
        val at200 = analyzer.analyzeSet(SyntheticWaveforms.cleanBench8(hz = 200), ExerciseProfiles.benchPress, user)
        assertEquals(8, at50.acceptedReps.size)
        assertEquals(
            "200 Hz accepted=${at200.acceptedReps.size}",
            8,
            at200.acceptedReps.size,
        )
    }

    @Test
    fun missingRotationVector_stillCountsEight() {
        val analysis = analyzer.analyzeSet(
            SyntheticWaveforms.verticalReps(repCount = 8, includeRv = false),
            ExerciseProfiles.benchPress,
            user,
        )
        assertEquals(
            "accepted=${analysis.acceptedReps.size} flags=${analysis.flags}",
            8,
            analysis.acceptedReps.size,
        )
        assertTrue(analysis.flags.contains("no_rotation_vector"))
    }

    @Test
    fun missingGyro_stillCountsEight() {
        val analysis = analyzer.analyzeSet(
            SyntheticWaveforms.verticalReps(repCount = 8, includeGyro = false),
            ExerciseProfiles.benchPress,
            user,
        )
        assertEquals(8, analysis.acceptedReps.size)
        assertTrue(analysis.flags.contains("no_gyro"))
    }
}
