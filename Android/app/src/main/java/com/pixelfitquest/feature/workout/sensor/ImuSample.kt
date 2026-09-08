package com.pixelfitquest.feature.workout.sensor

import kotlin.math.sqrt

data class TimedVec3(
    val tNanos: Long,
    val x: Float,
    val y: Float,
    val z: Float,
)

data class TimedQuat(
    val tNanos: Long,
    val x: Float,
    val y: Float,
    val z: Float,
    val w: Float,
)

/** Raw phone-IMU streams, sensor timestamps in ns (boot time). */
data class ImuTrace(
    val accel: List<TimedVec3>,
    val gyro: List<TimedVec3>,
    val rotation: List<TimedQuat>,
    val linearAccel: List<TimedVec3>,
)

enum class MountSide {
    LEFT,
    RIGHT,
    UNKNOWN,
}

/**
 * Optional clip pose: phone long-axis along the bar. Identity means
 * device axes already match bar (x along bar, z up).
 */
data class BarCalibration(
    val mountSide: MountSide = MountSide.UNKNOWN,
    val qx: Float = 0f,
    val qy: Float = 0f,
    val qz: Float = 0f,
    val qw: Float = 1f,
)

/**
 * One fused tick on the accelerometer timeline after interpolation.
 * Accel includes gravity. Gyro/RV/linear are interpolated, not held-last.
 */
data class ImuSample(
    val tNanos: Long,
    val ax: Float,
    val ay: Float,
    val az: Float,
    val gx: Float? = null,
    val gy: Float? = null,
    val gz: Float? = null,
    val qx: Float? = null,
    val qy: Float? = null,
    val qz: Float? = null,
    val qw: Float? = null,
    val lx: Float? = null,
    val ly: Float? = null,
    val lz: Float? = null,
) {
    val hasRotationVector: Boolean
        get() = qx != null && qy != null && qz != null && qw != null

    val hasGyro: Boolean
        get() = gx != null && gy != null && gz != null

    val hasLinearAccel: Boolean
        get() = lx != null && ly != null && lz != null

    val gyroMagnitude: Float
        get() = if (hasGyro) sqrt(gx!! * gx + gy!! * gy + gz!! * gz) else 0f
}
