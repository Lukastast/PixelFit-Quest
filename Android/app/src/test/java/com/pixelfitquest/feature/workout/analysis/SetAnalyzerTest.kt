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
        val truncated = analysis.reps.filter { "truncated" in it.tags || !it.accepted }
        assertTrue(
            "expected a truncated/candidate leftover, got ${analysis.reps.map { it.tags }}",
            truncated.isNotEmpty() && truncated.none { it.accepted && "truncated" in it.tags },
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
    }
}
