package com.pixelfitquest.feature.workout.ui

import com.pixelfitquest.feature.workout.analysis.CycleShape
import com.pixelfitquest.feature.workout.analysis.DetectedRep
import com.pixelfitquest.feature.workout.analysis.ExerciseProfile
import com.pixelfitquest.feature.workout.analysis.Mount
import com.pixelfitquest.feature.workout.analysis.PrimaryMotion
import com.pixelfitquest.feature.workout.analysis.QualityMetric
import com.pixelfitquest.feature.workout.analysis.RomUnit
import com.pixelfitquest.feature.workout.analysis.SetAnalysis
import com.pixelfitquest.feature.workout.sensor.MountSide
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutDisplayRulesTest {

    private fun rep(
        rom: Float = 88f, ecc: Long = 2100L, con: Long = 900L, stab: Float? = 85f,
        lvl: Float? = 1.5f, twst: Float? = 2.0f, tags: List<String> = emptyList(), acc: Boolean = true,
    ) = DetectedRep(
        index = 0, tStartNanos = 0L, tEndNanos = 3_000_000_000L, durationMs = 3000L,
        romEstimate = 0.5f, romUnit = RomUnit.METERS, concentricMs = con, eccentricMs = ecc,
        pathDeviation = 0.02f, romScore = rom, stabilityScore = stab, tempoScore = 80f,
        levelDeg = lvl, twistDeg = twst, formScore = 85f, tags = tags, confidence = 0.95f, accepted = acc,
    )

    private fun profile(hasTilt: Boolean = true) = ExerciseProfile(
        id = "bench", displayName = "Bench", mount = Mount.BAR_SLEEVE,
        primaryMotion = PrimaryMotion.VERTICAL_VS_GRAVITY, cycleShape = CycleShape.HIGH_LOW_HIGH,
        eccentricFirst = true, minRepDurationMs = 800L, maxRepDurationMs = 8000L, minAmplitude = 0.15f,
        typicalAmplitude = 0.40f, romFactor = 1.0f,
        quality = if (hasTilt) setOf(QualityMetric.ROM, QualityMetric.TEMPO, QualityMetric.PATH_TILT)
                  else setOf(QualityMetric.ROM, QualityMetric.TEMPO),
    )

    @Test
    fun tableCells_tierA_allSleeveAndTiltCombinations() {
        val testTags = listOf("dropped", "short_rom", "tilt_right", "uncertain")
        for (side in listOf(MountSide.LEFT, MountSide.RIGHT, MountSide.UNKNOWN)) {
            for (hasTilt in listOf(true, false)) {
                val rep = rep(tags = testTags)
                val set = WorkoutDisplayRules.createSetDisplay(
                    analysis = SetAnalysis(reps = listOf(rep, rep(acc = false)), meanFormScore = 85f),
                    exerciseProfile = profile(hasTilt), mountSide = side, deviceTier = MeasurementTier.TIER_A,
                )
                val r = set.reps.first()
                val sleeveKnown = side != MountSide.UNKNOWN
                val tiltExpected = sleeveKnown && hasTilt

                assertTrue(set.showRepsCounted && set.showNotCountedReps && !set.showTierBExplainer && set.showMountChip)
                assertEquals(1, set.repsCounted)
                assertEquals(1, set.notCountedCount)
                assertFalse(set.showFormScore)
                assertEquals(tiltExpected, set.showBarLevel)
                assertEquals(tiltExpected, set.showBarTwist)

                assertTrue(r.isUncertain && r.showRom && r.showRomEditChips && r.showTempo && r.showBarQuality)
                assertEquals(88, r.romPercent)
                assertEquals(2.1f, r.eccentricSec!!, 0.01f)
                assertEquals(tiltExpected, r.showBarLevel)
                assertEquals(if (tiltExpected) 1.5f else null, r.levelDeg)
                assertEquals(tiltExpected, r.showBarTwist)
                assertEquals(if (tiltExpected) 2.0f else null, r.twistDeg)
                assertTrue("dropped" in r.visibleTags && "short_rom" in r.visibleTags)
                assertEquals(tiltExpected, "tilt_right" in r.visibleTags)
            }
        }
    }

    @Test
    fun tableCells_tierB_allSleeveAndTiltCombinations() {
        for (side in listOf(MountSide.LEFT, MountSide.RIGHT, MountSide.UNKNOWN)) {
            for (hasTilt in listOf(true, false)) {
                val rep = rep(tags = listOf("dropped", "short_rom", "tilt_right", "uncertain"))
                val set = WorkoutDisplayRules.createSetDisplay(
                    analysis = SetAnalysis(reps = listOf(rep), meanFormScore = 85f),
                    exerciseProfile = profile(hasTilt), mountSide = side, deviceTier = MeasurementTier.TIER_B,
                )
                val r = set.reps.first()
                assertTrue(set.showRepsCounted && set.showTierBExplainer && set.showMountChip && set.showLiveCountSlot)
                assertFalse(set.showBarLevel || set.showBarTwist || set.showFormScore)
                assertTrue(r.isUncertain)
                listOf(r.romPercent, r.eccentricSec, r.stabilityScore, r.levelDeg, r.twistDeg).forEach { assertNull(it) }
                assertTrue(r.visibleTags.isEmpty())
            }
        }
    }

    @Test
    fun tableCell_logOnlyExercise() {
        val rep = rep(tags = listOf("dropped", "short_rom", "tilt_right", "uncertain"))
        val set = WorkoutDisplayRules.createSetDisplay(
            reps = listOf(rep), exerciseProfile = null, mountSide = MountSide.LEFT,
            deviceTier = MeasurementTier.TIER_A, isLogOnly = true,
        )
        val r = set.reps.first()
        assertTrue(set.showRepsCounted)
        assertFalse(set.showNotCountedReps || set.showTierBExplainer || set.showMountChip || set.showLiveCountSlot || set.showBarLevel || set.showBarTwist)
        assertFalse(r.isUncertain || r.showRom || r.showTempo || r.showBarQuality || r.showBarLevel || r.showBarTwist)
        listOf(r.romPercent, r.levelDeg, r.twistDeg).forEach { assertNull(it) }
        assertTrue(r.visibleTags.isEmpty())
    }

    @Test
    fun r4Regression_levelNull_twistNonNull_showsTwistOnly() {
        val r = WorkoutDisplayRules.createRepDisplay(rep(lvl = null, twst = 3f), MeasurementTier.TIER_A, MountSide.LEFT, true)
        assertFalse(r.showBarLevel); assertNull(r.levelDeg)
        assertTrue(r.showBarTwist); assertEquals(3f, r.twistDeg!!, 0.001f)

        val r2 = WorkoutDisplayRules.createRepDisplay(rep(lvl = 2.5f, twst = null), MeasurementTier.TIER_A, MountSide.RIGHT, true)
        assertTrue(r2.showBarLevel); assertEquals(2.5f, r2.levelDeg!!, 0.001f)
        assertFalse(r2.showBarTwist); assertNull(r2.twistDeg)
    }

    @Test
    fun noGyroFlag_beatsDeviceTier_appliesTierBRules() {
        val set = WorkoutDisplayRules.createSetDisplay(
            analysis = SetAnalysis(reps = listOf(rep()), meanFormScore = 90f, flags = listOf("no_gyro")),
            exerciseProfile = profile(), mountSide = MountSide.LEFT, deviceTier = MeasurementTier.TIER_A,
        )
        assertTrue("Tier B explainer must show when no_gyro flag is present", set.showTierBExplainer)
        assertFalse(set.reps.first().showRom)
        assertNull(set.reps.first().romPercent)
    }

    @Test
    fun uncertainTag_triggersUncertainBadge() {
        val withTag = WorkoutDisplayRules.createRepDisplay(rep(tags = listOf("uncertain")), MeasurementTier.TIER_A, MountSide.LEFT, true)
        val withoutTag = WorkoutDisplayRules.createRepDisplay(rep(tags = listOf("clean")), MeasurementTier.TIER_A, MountSide.LEFT, true)
        assertTrue(withTag.isUncertain)
        assertFalse(withoutTag.isUncertain)
        assertFalse("uncertain tag should be consumed by badge", "uncertain" in withTag.visibleTags)
    }

    @Test
    fun notCounted_excludesSetupTaggedReps_inBothCountAndList() {
        val setupRep = rep(tags = listOf("setup"), acc = false)
        val shortRomRep = rep(tags = listOf("short_rom"), acc = false)
        val acceptedRep = rep(acc = true)
        val set = WorkoutDisplayRules.createSetDisplay(
            analysis = SetAnalysis(reps = listOf(setupRep, shortRomRep, acceptedRep), meanFormScore = 80f),
            exerciseProfile = profile(), mountSide = MountSide.LEFT, deviceTier = MeasurementTier.TIER_A,
        )
        assertEquals(1, set.notCountedCount)
        assertEquals(listOf(shortRomRep.index), set.notCountedReps.map { it.index })
        assertFalse(WorkoutDisplayRules.isNotCounted(setupRep))
        assertTrue(WorkoutDisplayRules.isNotCounted(shortRomRep))
        assertFalse(WorkoutDisplayRules.isNotCounted(acceptedRep))
    }

    @Test
    fun logOnlySet_repsCounted_countsOnlyAcceptedReps() {
        val set = WorkoutDisplayRules.createSetDisplay(
            reps = listOf(rep(acc = true), rep(acc = false), rep(acc = true)),
            exerciseProfile = null, mountSide = MountSide.LEFT,
            deviceTier = MeasurementTier.TIER_A, isLogOnly = true,
        )
        assertEquals(2, set.repsCounted)
        assertEquals(0, set.notCountedCount)
        assertTrue(set.notCountedReps.isEmpty())
    }

    @Test
    fun romPercent_rounding_roundsHalfUp() {
        fun romPercent(rom: Float) = WorkoutDisplayRules.createRepDisplay(
            rep(rom = rom), MeasurementTier.TIER_A, MountSide.LEFT, true,
        ).romPercent
        assertEquals(90, romPercent(89.5f))
        assertEquals(90, romPercent(89.9f))
        assertEquals(89, romPercent(89.4f))
    }

    @Test
    fun showRom_hiddenWhenProfileLacksRomMetric() {
        val noRomProfile = profile().copy(quality = setOf(QualityMetric.TEMPO, QualityMetric.PATH_TILT))
        val set = WorkoutDisplayRules.createSetDisplay(
            analysis = SetAnalysis(reps = listOf(rep(rom = 95f)), meanFormScore = 85f),
            exerciseProfile = noRomProfile, mountSide = MountSide.LEFT, deviceTier = MeasurementTier.TIER_A,
        )
        val r = set.reps.first()
        assertFalse(r.showRom || r.showRomEditChips)
        assertNull(r.romPercent)
    }

    @Test
    fun uncertainPrefixedTags_triggerBadgeAndHiddenFromVisibleTags() {
        val r = WorkoutDisplayRules.createRepDisplay(
            rep(tags = listOf("uncertain_speed", "uncertain_leg_amplitude")),
            MeasurementTier.TIER_A, MountSide.LEFT, true,
        )
        assertTrue(r.isUncertain)
        assertFalse("uncertain_speed" in r.visibleTags || "uncertain_leg_amplitude" in r.visibleTags)
        assertTrue(r.visibleTags.isEmpty())
    }
}
