package com.pixelfitquest.feature.workout.ui

import com.pixelfitquest.feature.workout.analysis.CoachingTags
import com.pixelfitquest.feature.workout.analysis.DetectedRep
import com.pixelfitquest.feature.workout.analysis.ExerciseProfile
import com.pixelfitquest.feature.workout.analysis.Mount
import com.pixelfitquest.feature.workout.analysis.QualityMetric
import com.pixelfitquest.feature.workout.analysis.SetAnalysis
import com.pixelfitquest.feature.workout.sensor.MountSide

data class RepDisplay(
    val index: Int, val accepted: Boolean, val isUncertain: Boolean,
    val showRom: Boolean, val romPercent: Int?, val showRomEditChips: Boolean,
    val showTempo: Boolean, val eccentricSec: Float?, val concentricSec: Float?,
    val showBarQuality: Boolean, val stabilityScore: Float?,
    val showBarLevel: Boolean, val levelDeg: Float?,
    val showBarTwist: Boolean, val twistDeg: Float?,
    val visibleTags: List<String>,
)

data class SetDisplay(
    val repsCounted: Int, val showRepsCounted: Boolean,
    val notCountedCount: Int, val showNotCountedReps: Boolean,
    val showTierBExplainer: Boolean, val showMountChip: Boolean,
    val showLiveCountSlot: Boolean, val showFormScore: Boolean = false,
    val showBarLevel: Boolean, val showBarTwist: Boolean,
    val reps: List<RepDisplay>,
) {
    val notCountedReps: List<RepDisplay> get() = reps.filter { !it.accepted }
}

object WorkoutDisplayRules {
    const val TAG_UNCERTAIN = "uncertain"
    const val TAG_CLIP_POSE = "clip_pose"

    fun isUncertain(tags: List<String>): Boolean =
        TAG_UNCERTAIN in tags || tags.any { it.startsWith("uncertain_") }

    fun isSleeveKnown(mountSide: MountSide): Boolean =
        mountSide == MountSide.LEFT || mountSide == MountSide.RIGHT

    fun createRepDisplay(
        rep: DetectedRep,
        tier: MeasurementTier,
        mountSide: MountSide,
        hasPathTilt: Boolean,
        isLogOnly: Boolean = false,
    ): RepDisplay {
        if (isLogOnly) {
            return RepDisplay(
                index = rep.index, accepted = rep.accepted, isUncertain = false,
                showRom = false, romPercent = null, showRomEditChips = false,
                showTempo = false, eccentricSec = null, concentricSec = null,
                showBarQuality = false, stabilityScore = null,
                showBarLevel = false, levelDeg = null, showBarTwist = false, twistDeg = null,
                visibleTags = emptyList(),
            )
        }

        val isTierA = tier == MeasurementTier.TIER_A
        val sleeveKnown = isSleeveKnown(mountSide)
        val canShowTiltTwist = isTierA && sleeveKnown && hasPathTilt

        val showRom = isTierA
        val showTempo = isTierA && (rep.eccentricMs > 0 || rep.concentricMs > 0 || rep.tempoScore != null)
        val showBarQuality = isTierA && rep.stabilityScore != null
        val showBarLevel = canShowTiltTwist && rep.levelDeg != null
        val showBarTwist = canShowTiltTwist && rep.twistDeg != null

        val visibleTags = filterVisibleTags(rep.tags, isTierA, sleeveKnown, hasPathTilt)

        return RepDisplay(
            index = rep.index,
            accepted = rep.accepted,
            isUncertain = isUncertain(rep.tags),
            showRom = showRom,
            romPercent = if (showRom) rep.romScore.toInt() else null,
            showRomEditChips = showRom,
            showTempo = showTempo,
            eccentricSec = if (showTempo && rep.eccentricMs > 0) ((rep.eccentricMs / 100) / 10f) else null,
            concentricSec = if (showTempo && rep.concentricMs > 0) ((rep.concentricMs / 100) / 10f) else null,
            showBarQuality = showBarQuality,
            stabilityScore = if (showBarQuality) rep.stabilityScore else null,
            showBarLevel = showBarLevel,
            levelDeg = if (showBarLevel) rep.levelDeg else null,
            showBarTwist = showBarTwist,
            twistDeg = if (showBarTwist) rep.twistDeg else null,
            visibleTags = visibleTags,
        )
    }

    fun createSetDisplay(
        reps: List<DetectedRep>,
        flags: List<String> = emptyList(),
        exerciseProfile: ExerciseProfile?,
        mountSide: MountSide = MountSide.UNKNOWN,
        deviceTier: MeasurementTier = MeasurementTier.TIER_A,
        isLogOnly: Boolean = exerciseProfile == null,
    ): SetDisplay {
        val effectiveTier = MeasurementTier.resolve(deviceTier, flags)
        val hasPathTilt = exerciseProfile?.quality?.contains(QualityMetric.PATH_TILT) == true
        val isBarSleeve = exerciseProfile?.mount == Mount.BAR_SLEEVE

        val repDisplays = reps.map { rep ->
            createRepDisplay(rep, effectiveTier, mountSide, hasPathTilt, isLogOnly)
        }

        val notCounted = if (isLogOnly) 0 else reps.count { !it.accepted && "setup" !in it.tags }
        return SetDisplay(
            repsCounted = if (isLogOnly) reps.size else reps.count { it.accepted },
            showRepsCounted = true,
            notCountedCount = notCounted,
            showNotCountedReps = !isLogOnly,
            showTierBExplainer = !isLogOnly && effectiveTier == MeasurementTier.TIER_B,
            showMountChip = !isLogOnly && isBarSleeve,
            showLiveCountSlot = !isLogOnly,
            showFormScore = false,
            showBarLevel = repDisplays.any { it.showBarLevel },
            showBarTwist = repDisplays.any { it.showBarTwist },
            reps = repDisplays,
        )
    }

    fun createSetDisplay(
        analysis: SetAnalysis?,
        exerciseProfile: ExerciseProfile?,
        mountSide: MountSide = MountSide.UNKNOWN,
        deviceTier: MeasurementTier = MeasurementTier.TIER_A,
        isLogOnly: Boolean = exerciseProfile == null,
    ): SetDisplay = createSetDisplay(
        reps = analysis?.reps ?: emptyList(),
        flags = analysis?.flags ?: emptyList(),
        exerciseProfile = exerciseProfile,
        mountSide = mountSide,
        deviceTier = deviceTier,
        isLogOnly = isLogOnly,
    )

    private fun filterVisibleTags(
        tags: List<String>, isTierA: Boolean, sleeveKnown: Boolean, hasPathTilt: Boolean,
    ): List<String> = tags.filter { tag ->
        when (tag) {
            "dropped", "short_rom" -> isTierA
            "tilt_right", "tilt_left", "twist_head", "twist_hip" -> isTierA && sleeveKnown && hasPathTilt
            TAG_UNCERTAIN -> false
            TAG_CLIP_POSE -> true
            else -> CoachingTags.labelRes(tag) != null
        }
    }
}
