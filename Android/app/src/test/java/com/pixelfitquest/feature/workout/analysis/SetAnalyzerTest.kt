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
    fun fourDegreeTilt_doesNotDropForm() {
        val deg = (4.0 * Math.PI / 180.0).toFloat()
        val clean = analyzer.analyzeSet(SyntheticWaveforms.cleanBench8(), ExerciseProfiles.benchPress, user)
        val small = analyzer.analyzeSet(
            SyntheticWaveforms.verticalReps(repCount = 8, pitchRad = deg, yawRad = deg),
            ExerciseProfiles.benchPress,
            user,
        )
        assertEquals(8, small.acceptedReps.size)
        small.acceptedReps.forEach { rep ->
            assertTrue("4° is free-weight play: ${rep.stabilityScore}", (rep.stabilityScore ?: 0f) >= 95f)
            assertTrue("no coach tags for 4°: ${rep.tags}", rep.tags.none { it.startsWith("tilt_") || it.startsWith("twist_") })
        }
        assertTrue(
            "small wobble must not tank form: clean=${clean.meanFormScore} small=${small.meanFormScore}",
            small.meanFormScore >= clean.meanFormScore - 2f,
        )
    }

    @Test
    fun oneDegreeTilt_stillCountsEight() {
        val deg = (Math.PI / 180.0).toFloat()
        val analysis = analyzer.analyzeSet(
            SyntheticWaveforms.verticalReps(repCount = 8, pitchRad = deg),
            ExerciseProfiles.benchPress,
            user,
        )
        assertEquals(8, analysis.acceptedReps.size)
        analysis.acceptedReps.forEach {
            assertTrue("1° tilt should not tank stability: ${it.stabilityScore}", (it.stabilityScore ?: 0f) >= 95f)
            assertTrue("1° should not coach left/right: ${it.tags}", "tilt_right" !in it.tags && "tilt_left" !in it.tags)
        }
    }

    @Test
    fun fifteenDegreeRightPitch_tagsTiltRightAndLowersForm() {
        val deg = (15.0 * Math.PI / 180.0).toFloat()
        val clean = analyzer.analyzeSet(SyntheticWaveforms.cleanBench8(), ExerciseProfiles.benchPress, user)
        val tilted = analyzer.analyzeSet(
            SyntheticWaveforms.verticalReps(repCount = 8, pitchRad = deg),
            ExerciseProfiles.benchPress,
            user,
        )
        assertEquals(8, tilted.acceptedReps.size)
        tilted.acceptedReps.forEach { rep ->
            assertTrue(
                "right-high pitch should score positive (right): ${rep.levelDeg}",
                (rep.levelDeg ?: 0f) > BAR_COACH_DEG,
            )
            assertTrue("expected tilt_right in ${rep.tags}", BarImbalance.TAG_TILT_RIGHT in rep.tags)
            assertTrue("twist should stay near even: ${rep.twistDeg}", kotlin.math.abs(rep.twistDeg ?: 0f) < BAR_EVEN_DEG + 6f)
        }
        assertTrue(
            "uneven press must lower form: clean=${clean.meanFormScore} tilted=${tilted.meanFormScore}",
            tilted.meanFormScore < clean.meanFormScore - 1.5f,
        )
    }

    @Test
    fun fifteenDegreeYaw_tagsTwistHeadAndLowersForm() {
        val deg = (15.0 * Math.PI / 180.0).toFloat()
        val clean = analyzer.analyzeSet(SyntheticWaveforms.cleanBench8(), ExerciseProfiles.benchPress, user)
        val twisted = analyzer.analyzeSet(
            SyntheticWaveforms.verticalReps(repCount = 8, yawRad = deg),
            ExerciseProfiles.benchPress,
            user,
        )
        assertEquals(8, twisted.acceptedReps.size)
        twisted.acceptedReps.forEach { rep ->
            assertTrue(
                "positive yaw should mean right hand closer to head: ${rep.twistDeg}",
                (rep.twistDeg ?: 0f) > BAR_COACH_DEG,
            )
            assertTrue("expected twist_head in ${rep.tags}", BarImbalance.TAG_TWIST_HEAD in rep.tags)
        }
        assertTrue(
            "twist must lower form: clean=${clean.meanFormScore} twisted=${twisted.meanFormScore}",
            twisted.meanFormScore < clean.meanFormScore - 1.5f,
        )
    }

    @Test
    fun yawTowardHip_tagsTwistHip() {
        val deg = (-15.0 * Math.PI / 180.0).toFloat()
        val analysis = analyzer.analyzeSet(
            SyntheticWaveforms.verticalReps(repCount = 8, yawRad = deg),
            ExerciseProfiles.benchPress,
            user,
        )
        analysis.acceptedReps.forEach { rep ->
            assertTrue("negative yaw → right hand toward hip: ${rep.twistDeg}", (rep.twistDeg ?: 0f) < -BAR_COACH_DEG)
            assertTrue("expected twist_hip in ${rep.tags}", BarImbalance.TAG_TWIST_HIP in rep.tags)
        }
    }

    @Test
    fun pitchAndYawTogether_tagsBothAndLowestForm() {
        val deg = (15.0 * Math.PI / 180.0).toFloat()
        val pitchOnly = analyzer.analyzeSet(
            SyntheticWaveforms.verticalReps(repCount = 8, pitchRad = deg),
            ExerciseProfiles.benchPress,
            user,
        )
        val both = analyzer.analyzeSet(
            SyntheticWaveforms.verticalReps(repCount = 8, pitchRad = deg, yawRad = deg),
            ExerciseProfiles.benchPress,
            user,
        )
        both.acceptedReps.forEach { rep ->
            assertTrue(BarImbalance.TAG_TILT_RIGHT in rep.tags)
            assertTrue(BarImbalance.TAG_TWIST_HEAD in rep.tags)
        }
        assertTrue(
            "both axes should score worse than pitch alone: pitch=${pitchOnly.meanFormScore} both=${both.meanFormScore}",
            both.meanFormScore < pitchOnly.meanFormScore - 2f,
        )
    }

    @Test
    fun withRomPercent_keepsTiltInForm() {
        val deg = (15.0 * Math.PI / 180.0).toFloat()
        val analysis = analyzer.analyzeSet(
            SyntheticWaveforms.verticalReps(repCount = 8, pitchRad = deg),
            ExerciseProfiles.benchPress,
            user,
        )
        val rep = analysis.acceptedReps.first()
        val overridden = rep.withRomPercent(100f)
        val expected = formScoreFrom(
            romScore = 100f,
            tempoScore = rep.tempoScore,
            barQuality = rep.stabilityScore,
        )
        assertEquals(expected, overridden.formScore, 0.01f)
        assertTrue(
            "overriding ROM to 100 must still penalize tilt: ${overridden.formScore} bar=${rep.stabilityScore}",
            overridden.formScore < 99f && (rep.stabilityScore ?: 100f) < 100f,
        )
    }

    @Test
    fun cleanBench_levelAndTwistNearZero() {
        val analysis = analyzer.analyzeSet(
            SyntheticWaveforms.cleanBench8(),
            ExerciseProfiles.benchPress,
            user,
        )
        analysis.acceptedReps.forEach { rep ->
            assertTrue("level ${rep.levelDeg}", kotlin.math.abs(rep.levelDeg ?: 99f) < 8f)
            assertTrue("twist ${rep.twistDeg}", kotlin.math.abs(rep.twistDeg ?: 99f) < 8f)
            assertTrue("quality ${rep.stabilityScore}", (rep.stabilityScore ?: 0f) >= 85f)
            assertTrue(rep.tags.none { it.startsWith("tilt_") || it.startsWith("twist_") })
        }
    }

    @Test
    fun noBouncedTag() {
        val analysis = analyzer.analyzeSet(SyntheticWaveforms.cleanBench8(), ExerciseProfiles.benchPress, user)
        analysis.reps.forEach { rep ->
            assertTrue("bounced" !in rep.tags)
        }
    }

    @Test
    fun cableLift_hasNoLevelOrTwist() {
        val analysis = analyzer.analyzeSet(
            SyntheticWaveforms.verticalReps(repCount = 8, amplitude = 0.50f),
            ExerciseProfiles.latPulldown,
            user,
        )
        assertTrue(analysis.acceptedReps.isNotEmpty())
        analysis.acceptedReps.forEach { rep ->
            assertEquals(null, rep.levelDeg)
            assertEquals(null, rep.twistDeg)
        }
    }

    @Test
    fun curl_hasLevelButNoTwist() {
        val analysis = analyzer.analyzeSet(
            SyntheticWaveforms.cleanCurl8(),
            ExerciseProfiles.bicepCurl,
            user,
        )
        assertEquals(8, analysis.acceptedReps.size)
        analysis.acceptedReps.forEach { rep ->
            assertEquals("curl should not score head/hip twist", null, rep.twistDeg)
        }
    }

    @Test
    fun personalFullRom_gradesAgainstBaseline() {
        val samples = SyntheticWaveforms.verticalReps(repCount = 8, amplitude = 0.20f)
        val ungraded = analyzer.analyzeSet(samples, ExerciseProfiles.benchPress, user)
        ungraded.acceptedReps.forEach { rep ->
            assertTrue("short_rom" !in rep.tags)
            assertEquals(100f, rep.romScore, 0.01f)
        }
        val graded = analyzer.analyzeSet(
            samples,
            ExerciseProfiles.benchPress,
            user,
            fullRom = 0.40f,
        )
        graded.acceptedReps.forEach { rep ->
            assertTrue("expected ~50 ROM vs 40cm baseline, was ${rep.romScore}", rep.romScore in 40f..60f)
            assertTrue("short_rom" in rep.tags)
        }
    }

    @Test
    fun clipPoseUnclear_skipsLevelAndTwist() {
        val deg = (80.0 * Math.PI / 180.0).toFloat()
        val analysis = analyzer.analyzeSet(
            SyntheticWaveforms.verticalReps(repCount = 8, restPitchRad = deg),
            ExerciseProfiles.benchPress,
            user,
        )
        assertTrue("flags=${analysis.flags}", TAG_CLIP_POSE in analysis.flags)
        analysis.reps.forEach { rep ->
            assertEquals(null, rep.levelDeg)
            assertEquals(null, rep.twistDeg)
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
