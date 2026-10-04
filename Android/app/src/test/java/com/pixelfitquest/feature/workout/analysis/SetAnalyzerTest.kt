package com.pixelfitquest.feature.workout.analysis

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetAnalyzerTest {

    private val analyzer = SetAnalyzer()
    @Test
    fun cleanEightRepBench_acceptsEight() {
        val analysis = analyzer.analyzeSet(
            SyntheticWaveforms.cleanBench8(),
            ExerciseProfiles.benchPress,
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
        )
        assertEquals(8, analysis.acceptedReps.size)
        assertTrue(
            "truncated leftover must not be accepted: ${analysis.reps.map { it.tags to it.accepted }}",
            analysis.acceptedReps.none { "truncated" in it.tags },
        )
    }

    @Test
    fun inclineUnrack_isNotCountedAsARep() {
        val analysis = analyzer.analyzeSet(
            SyntheticWaveforms.verticalReps(
                repCount = 7,
                amplitude = 0.32f,
                leadRepAmplitude = 0.72f,
            ),
            ExerciseProfiles.inclineBenchPress,
        )
        assertEquals(
            "accepted=${analysis.acceptedReps.size} reps=${analysis.reps.map { "${it.accepted}:${it.tags}:${"%.2f".format(it.romEstimate)}" }}",
            7,
            analysis.acceptedReps.size,
        )
        assertTrue(
            "unrack must not be an accepted rep: ${analysis.reps.map { it.tags to it.accepted }}",
            analysis.acceptedReps.none { "setup" in it.tags || it.romEstimate > 0.5f },
        )
    }

    @Test
    fun skullCrusherWobble_doesNotMultiplyReps() {
        val analysis = analyzer.analyzeSet(
            SyntheticWaveforms.pitchReps(
                repCount = 8,
                amplitudeRad = 1.5f,
                wobbleRad = 0.30f,
                wobbleHz = 3.2f,
            ),
            ExerciseProfiles.skullCrusher,
        )
        assertTrue(
            "accepted=${analysis.acceptedReps.size} " +
                analysis.reps.joinToString { "${it.accepted}/${it.durationMs}/${it.tags}/${it.romEstimate}" },
            analysis.acceptedReps.size in 7..8 && analysis.reps.size <= 10,
        )
        assertTrue(analysis.acceptedReps.all { it.romEstimate > 1f })
    }

    @Test
    fun skullCrusherLargerWobble_doesNotExplodeTheCount() {
        val analysis = analyzer.analyzeSet(
            SyntheticWaveforms.pitchReps(
                repCount = 8,
                amplitudeRad = 1.5f,
                wobbleRad = 0.50f,
                wobbleHz = 2.5f,
            ),
            ExerciseProfiles.skullCrusher,
        )
        assertTrue(
            "accepted=${analysis.acceptedReps.size} shown=${analysis.reps.size} " +
                analysis.reps.map { "${it.accepted}:${"%.2f".format(it.romEstimate)}" }.toString(),
            analysis.acceptedReps.size in 6..12 && analysis.reps.size <= 20,
        )
    }

    @Test
    fun credibleFullRom_ignoresOneSpike() {
        assertEquals(0.40f, credibleFullRom(listOf(0.36f, 0.40f, 1.8f)), 0.001f)
        assertEquals(0f, credibleFullRom(emptyList()), 0.001f)
    }

    @Test
    fun curlPitchProfile_segmentsOnAngleNotWorldUp() {
        val curlSamples = SyntheticWaveforms.cleanCurl8()
        val curl = analyzer.analyzeSet(curlSamples, ExerciseProfiles.bicepCurl)
        val asBench = analyzer.analyzeSet(curlSamples, ExerciseProfiles.benchPress)

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
        val clean = analyzer.analyzeSet(SyntheticWaveforms.cleanBench8(), ExerciseProfiles.benchPress)
        val small = analyzer.analyzeSet(
            SyntheticWaveforms.verticalReps(repCount = 8, pitchRad = deg, yawRad = deg),
            ExerciseProfiles.benchPress,
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
        val clean = analyzer.analyzeSet(SyntheticWaveforms.cleanBench8(), ExerciseProfiles.benchPress)
        val tilted = analyzer.analyzeSet(
            SyntheticWaveforms.verticalReps(repCount = 8, pitchRad = deg),
            ExerciseProfiles.benchPress,
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
        val clean = analyzer.analyzeSet(SyntheticWaveforms.cleanBench8(), ExerciseProfiles.benchPress)
        val twisted = analyzer.analyzeSet(
            SyntheticWaveforms.verticalReps(repCount = 8, yawRad = deg),
            ExerciseProfiles.benchPress,
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
        )
        val both = analyzer.analyzeSet(
            SyntheticWaveforms.verticalReps(repCount = 8, pitchRad = deg, yawRad = deg),
            ExerciseProfiles.benchPress,
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
        val analysis = analyzer.analyzeSet(SyntheticWaveforms.cleanBench8(), ExerciseProfiles.benchPress)
        analysis.reps.forEach { rep ->
            assertTrue("bounced" !in rep.tags)
        }
    }

    @Test
    fun cableLift_hasNoLevelOrTwist() {
        val analysis = analyzer.analyzeSet(
            SyntheticWaveforms.verticalReps(repCount = 8, amplitude = 0.50f),
            ExerciseProfiles.latPulldown,
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
        )
        assertEquals(8, analysis.acceptedReps.size)
        analysis.acceptedReps.forEach { rep ->
            assertEquals("curl should not score head/hip twist", null, rep.twistDeg)
        }
    }

    @Test
    fun personalFullRom_gradesAgainstBaseline() {
        val samples = SyntheticWaveforms.verticalReps(repCount = 8, amplitude = 0.20f)
        val ungraded = analyzer.analyzeSet(samples, ExerciseProfiles.benchPress)
        ungraded.acceptedReps.forEach { rep ->
            assertTrue("short_rom" !in rep.tags)
            assertEquals(100f, rep.romScore, 0.01f)
        }
        val graded = analyzer.analyzeSet(
            samples,
            ExerciseProfiles.benchPress,
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
        )
        assertTrue("flags=${analysis.flags}", TAG_CLIP_POSE in analysis.flags)
        analysis.reps.forEach { rep ->
            assertEquals(null, rep.levelDeg)
            assertEquals(null, rep.twistDeg)
        }
    }

    @Test
    fun fiftyAndTwoHundredHz_sameRepCount() {
        val at50 = analyzer.analyzeSet(SyntheticWaveforms.cleanBench8(hz = 50), ExerciseProfiles.benchPress)
        val at200 = analyzer.analyzeSet(SyntheticWaveforms.cleanBench8(hz = 200), ExerciseProfiles.benchPress)
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
        )
        assertEquals(8, analysis.acceptedReps.size)
        assertTrue(analysis.flags.contains("no_gyro"))
    }

    @Test
    fun clusterGate_demotesFragmentToCandidate_insteadOfDeleting() {
        fun rep(amp: Float, accepted: Boolean, vararg tags: String) = DetectedRep(
            index = 0,
            tStartNanos = 0L,
            tEndNanos = 1_500_000_000L,
            durationMs = 1500L,
            romEstimate = amp,
            romUnit = RomUnit.METERS,
            concentricMs = 750L,
            eccentricMs = 750L,
            pathDeviation = 0f,
            romScore = 100f,
            formScore = 90f,
            tags = tags.toList(),
            confidence = if (accepted) 0.8f else 0.45f,
            accepted = accepted,
        )
        // Reference from top-3 median ≈ 0.40 → dropFloor 0.16, keepFloor 0.248
        val input = listOf(
            rep(0.40f, true),
            rep(0.39f, true),
            rep(0.41f, true),
            rep(0.38f, true),
            rep(0.12f, true), // below dropFloor — must demote, not delete
            rep(0.10f, false, "candidate"), // below dropFloor candidate — keep
            rep(0.40f, true),
        )
        val out = analyzer.applyClusterGate(input, ExerciseProfiles.benchPress)
        assertEquals("must not silently drop fragments: ${out.map { it.romEstimate }}", 7, out.size)
        val demoted = out.first { kotlin.math.abs(it.romEstimate - 0.12f) < 1e-4f }
        assertTrue(!demoted.accepted)
        assertTrue("candidate" in demoted.tags)
        val keptCandidate = out.first { kotlin.math.abs(it.romEstimate - 0.10f) < 1e-4f }
        assertTrue(!keptCandidate.accepted)
        assertTrue("candidate" in keptCandidate.tags)
    }

    @Test
    fun clusterGate_keepsSetupAndTruncateUnchanged() {
        fun rep(amp: Float, vararg tags: String) = DetectedRep(
            index = 0,
            tStartNanos = 0L,
            tEndNanos = 1_500_000_000L,
            durationMs = 1500L,
            romEstimate = amp,
            romUnit = RomUnit.METERS,
            concentricMs = 750L,
            eccentricMs = 750L,
            pathDeviation = 0f,
            romScore = 100f,
            formScore = 90f,
            tags = tags.toList(),
            confidence = 0.4f,
            accepted = false,
        )
        val input = listOf(
            rep(0.40f, "setup", "candidate"),
            rep(0.39f),
            rep(0.41f),
            rep(0.38f),
            rep(0.08f, "truncated"),
        ).mapIndexed { i, r -> r.copy(index = i, accepted = i in 1..3) }
        val out = analyzer.applyClusterGate(input, ExerciseProfiles.benchPress)
        assertEquals(5, out.size)
        assertTrue("setup" in out[0].tags && !out[0].accepted)
        assertTrue("truncated" in out[4].tags)
    }
}
