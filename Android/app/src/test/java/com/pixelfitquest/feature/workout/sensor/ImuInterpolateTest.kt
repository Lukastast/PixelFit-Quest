package com.pixelfitquest.feature.workout.sensor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ImuInterpolateTest {

    @Test
    fun interpolatesGyroOntoAccelTimeline() {
        val accel = listOf(
            TimedVec3(0L, 0f, 0f, 9.81f),
            TimedVec3(20_000_000L, 0f, 0f, 9.81f),
            TimedVec3(40_000_000L, 0f, 0f, 9.81f),
        )
        val gyro = listOf(
            TimedVec3(0L, 0f, 0f, 0f),
            TimedVec3(40_000_000L, 2f, 0f, 0f),
        )
        val fused = ImuInterpolate.ontoAccel(
            ImuTrace(accel, gyro, emptyList(), emptyList()),
        )
        assertEquals(3, fused.size)
        assertEquals(0f, fused[0].gx!!, 1e-4f)
        assertEquals(1f, fused[1].gx!!, 1e-3f)
        assertEquals(2f, fused[2].gx!!, 1e-4f)
    }

    @Test
    fun doesNotHoldLastGyroAcrossAGap() {
        val accel = listOf(TimedVec3(100_000_000L, 0f, 0f, 9.81f))
        val gyro = listOf(TimedVec3(0L, 3f, 0f, 0f))
        val fused = ImuInterpolate.ontoAccel(
            ImuTrace(accel, gyro, emptyList(), emptyList()),
        )
        assertEquals(1, fused.size)
        assertNull(fused[0].gx)
    }

    @Test
    fun slerpsRotationVector() {
        val accel = listOf(TimedVec3(10_000_000L, 0f, 0f, 9.81f))
        val rot = listOf(
            TimedQuat(0L, 0f, 0f, 0f, 1f),
            TimedQuat(20_000_000L, 0f, 0.70710677f, 0f, 0.70710677f),
        )
        val fused = ImuInterpolate.ontoAccel(
            ImuTrace(accel, emptyList(), rot, emptyList()),
        )
        assertNotNull(fused[0].qw)
        assertEquals(0.9239f, fused[0].qw!!, 0.02f)
    }

    @Test
    fun gameRotationStaysOffTheAnalyzerQuaternion() {
        val accel = listOf(TimedVec3(10_000_000L, 0f, 0f, 9.81f))
        val mag = listOf(
            TimedQuat(0L, 0f, 0f, 0f, 1f),
            TimedQuat(20_000_000L, 0f, 0f, 0f, 1f),
        )
        val half = Math.toRadians(23.0).toFloat() / 2f
        val game = TimedQuat(10_000_000L, kotlin.math.sin(half), 0f, 0f, kotlin.math.cos(half))
        val fused = ImuInterpolate.ontoAccel(
            AnalyzerInput.select(
                RawSensorDump(
                    accel = accel,
                    rotationVector = mag,
                    gameRotationVector = listOf(game),
                ),
            ),
        )
        assertEquals(1f, fused[0].qw!!, 1e-4f)
        assertEquals(0f, fused[0].qx!!, 1e-4f)
    }
}
