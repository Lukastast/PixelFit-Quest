package com.pixelfitquest.feature.workoutResume.model

import androidx.compose.ui.graphics.Color
import com.pixelfitquest.R
import com.pixelfitquest.feature.workout.analysis.BAR_COACH_DEG
import com.pixelfitquest.feature.workout.analysis.CoachingTags
import com.pixelfitquest.feature.workout.analysis.TAG_CLIP_POSE
import com.pixelfitquest.feature.workout.model.WorkoutSet
import com.pixelfitquest.ui.theme.HeartRuby
import com.pixelfitquest.ui.theme.TorchAmber
import com.pixelfitquest.ui.theme.VitalGreen

/**
 * Visual metadata for form coaching clips and feedback icons.
 */
data class CoachingVisualInfo(
    val tag: String,
    val titleRes: Int,
    val cueRes: Int,
    val accentColor: Color,
    val isMistake: Boolean,
    val badgeTextRes: Int,
)

object CoachingVisuals {
    const val TAG_FORM_EVEN = "form_even"
    const val TAG_TILT_RIGHT = "tilt_right"
    const val TAG_TILT_LEFT = "tilt_left"
    const val TAG_TWIST_HEAD = "twist_head"
    const val TAG_TWIST_HIP = "twist_hip"
    const val TAG_SHORT_ROM = "short_rom"
    const val TAG_DROPPED = "dropped"

    fun infoFor(tag: String): CoachingVisualInfo = when (tag) {
        TAG_TILT_RIGHT -> CoachingVisualInfo(
            tag = tag,
            titleRes = R.string.tag_tilt_right,
            cueRes = R.string.feedback_tilt_right,
            accentColor = HeartRuby,
            isMistake = true,
            badgeTextRes = R.string.hero_stage_form_check,
        )
        TAG_TILT_LEFT -> CoachingVisualInfo(
            tag = tag,
            titleRes = R.string.tag_tilt_left,
            cueRes = R.string.feedback_tilt_left,
            accentColor = HeartRuby,
            isMistake = true,
            badgeTextRes = R.string.hero_stage_form_check,
        )
        TAG_TWIST_HEAD -> CoachingVisualInfo(
            tag = tag,
            titleRes = R.string.tag_twist_head,
            cueRes = R.string.feedback_x_tilt_right,
            accentColor = TorchAmber,
            isMistake = true,
            badgeTextRes = R.string.hero_stage_form_check,
        )
        TAG_TWIST_HIP -> CoachingVisualInfo(
            tag = tag,
            titleRes = R.string.tag_twist_hip,
            cueRes = R.string.feedback_x_tilt_left,
            accentColor = TorchAmber,
            isMistake = true,
            badgeTextRes = R.string.hero_stage_form_check,
        )
        TAG_SHORT_ROM -> CoachingVisualInfo(
            tag = tag,
            titleRes = R.string.tag_short_rom,
            cueRes = R.string.feedback_rom_low,
            accentColor = TorchAmber,
            isMistake = true,
            badgeTextRes = R.string.hero_stage_form_check,
        )
        TAG_DROPPED -> CoachingVisualInfo(
            tag = tag,
            titleRes = R.string.tag_dropped,
            cueRes = R.string.feedback_dropped_cue,
            accentColor = HeartRuby,
            isMistake = true,
            badgeTextRes = R.string.hero_stage_form_check,
        )
        TAG_CLIP_POSE, "clip_pose_unclear" -> CoachingVisualInfo(
            tag = TAG_CLIP_POSE,
            titleRes = R.string.tag_clip_pose,
            cueRes = R.string.feedback_clip_pose_cue,
            accentColor = TorchAmber,
            isMistake = true,
            badgeTextRes = R.string.hero_stage_form_check,
        )
        else -> CoachingVisualInfo(
            tag = TAG_FORM_EVEN,
            titleRes = R.string.form_even_title,
            cueRes = R.string.form_even_cue,
            accentColor = VitalGreen,
            isMistake = false,
            badgeTextRes = R.string.hero_stage_clean_form,
        )
    }

    /**
     * Resolves the primary visual coaching tag for a single WorkoutSet.
     */
    fun resolveVisualForSet(set: WorkoutSet): CoachingVisualInfo {
        if (TAG_CLIP_POSE in set.flags || "clip_pose_unclear" in set.flags) {
            return infoFor(TAG_CLIP_POSE)
        }

        // 1. Check rep records for explicit coaching tags
        val repTags = set.repRecords.flatMap { it.tags }
        val visibleRepTags = CoachingTags.visible(repTags)
        if (visibleRepTags.isNotEmpty()) {
            val mostFrequent = visibleRepTags.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
            if (mostFrequent != null) {
                return infoFor(mostFrequent)
            }
        }

        // 2. Check set-level continuous signals against coaching thresholds
        val level = set.levelDeg
        if (level > BAR_COACH_DEG) return infoFor(TAG_TILT_RIGHT)
        if (level < -BAR_COACH_DEG) return infoFor(TAG_TILT_LEFT)

        val twist = set.twistDeg
        if (twist > BAR_COACH_DEG) return infoFor(TAG_TWIST_HEAD)
        if (twist < -BAR_COACH_DEG) return infoFor(TAG_TWIST_HIP)

        if (set.romScore > 0f && set.romScore < 70f) {
            return infoFor(TAG_SHORT_ROM)
        }

        // 3. Fallback: form is clean / level
        return infoFor(TAG_FORM_EVEN)
    }
}
