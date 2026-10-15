package com.pixelfitquest.feature.workout.catalog

import androidx.annotation.DrawableRes
import com.pixelfitquest.R
import com.pixelfitquest.feature.workout.model.enums.ExerciseType

/**
 * Static hero-pose art for the exercise picker (#172).
 * v1: single still frame, no animation. Original PixelFit chibi — not licensed IPs.
 */
object HeroPoseVisuals {
    @DrawableRes
    fun poseRes(type: ExerciseType): Int? = when (type) {
        ExerciseType.SQUAT,
        ExerciseType.FRONT_SQUAT,
        -> R.drawable.hero_pose_squat

        ExerciseType.BENCH_PRESS,
        ExerciseType.INCLINE_BENCH_PRESS,
        ExerciseType.DECLINE_BENCH_PRESS,
        ExerciseType.CLOSE_GRIP_BENCH_PRESS,
        ExerciseType.FLOOR_PRESS,
        -> R.drawable.hero_pose_bench

        ExerciseType.DEADLIFT,
        ExerciseType.SUMO_DEADLIFT,
        ExerciseType.ROMANIAN_DEADLIFT,
        ExerciseType.RACK_PULL,
        -> R.drawable.hero_pose_deadlift

        ExerciseType.OVERHEAD_PRESS,
        ExerciseType.SEATED_OVERHEAD_PRESS,
        -> R.drawable.hero_pose_overhead_press

        ExerciseType.BARBELL_ROW,
        ExerciseType.PENDLAY_ROW,
        ExerciseType.T_BAR_ROW,
        ExerciseType.SEATED_ROWS,
        -> R.drawable.hero_pose_row

        else -> null
    }

    @DrawableRes
    fun poseResOrFallback(type: ExerciseType): Int =
        poseRes(type) ?: R.drawable.hero_pose_overhead_press
}
