package com.pixelfitquest.feature.workout.catalog

import com.pixelfitquest.R
import com.pixelfitquest.feature.workout.model.enums.ExerciseType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class HeroPoseVisualsTest {
    @Test
    fun mapsCoreLifts() {
        assertEquals(R.drawable.hero_pose_squat, HeroPoseVisuals.poseRes(ExerciseType.SQUAT))
        assertEquals(R.drawable.hero_pose_bench, HeroPoseVisuals.poseRes(ExerciseType.BENCH_PRESS))
        assertEquals(R.drawable.hero_pose_deadlift, HeroPoseVisuals.poseRes(ExerciseType.DEADLIFT))
        assertEquals(R.drawable.hero_pose_overhead_press, HeroPoseVisuals.poseRes(ExerciseType.OVERHEAD_PRESS))
        assertEquals(R.drawable.hero_pose_row, HeroPoseVisuals.poseRes(ExerciseType.BARBELL_ROW))
    }

    @Test
    fun unknownReturnsNull() {
        assertNull(HeroPoseVisuals.poseRes(ExerciseType.PUSH_UP))
        assertNotNull(HeroPoseVisuals.poseResOrFallback(ExerciseType.PUSH_UP))
    }
}
