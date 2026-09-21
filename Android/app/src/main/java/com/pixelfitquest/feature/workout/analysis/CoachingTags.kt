package com.pixelfitquest.feature.workout.analysis

import com.pixelfitquest.R

object CoachingTags {
    private val hidden = setOf(
        "candidate",
        "truncated",
        "merged",
        "rom_override",
        "manual",
        "bar_tilt",
        "no_rotation_vector",
        "no_gyro",
        "too_few_samples",
        "last_reps_degraded",
    )

    fun labelRes(tag: String): Int? = when (tag) {
        "tilt_right" -> R.string.tag_tilt_right
        "tilt_left" -> R.string.tag_tilt_left
        "twist_head" -> R.string.tag_twist_head
        "twist_hip" -> R.string.tag_twist_hip
        "dropped" -> R.string.tag_dropped
        "short_rom" -> R.string.tag_short_rom
        TAG_CLIP_POSE -> R.string.tag_clip_pose
        else -> null
    }

    fun visible(tags: List<String>): List<String> =
        tags.filter { it !in hidden && labelRes(it) != null }
}
