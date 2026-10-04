package com.pixelfitquest.feature.workout.sensor

import kotlin.math.sqrt

data class TimedVec3(
    val tNanos: Long,
    val x: Float,
    val y: Float,
    val z: Float,
    val accuracy: Int = 0,
)

data class TimedQuat(
    val tNanos: Long,
    val x: Float,
    val y: Float,
    val z: Float,
    val w: Float,
    val accuracy: Int = 0,
)

/**
 * Raw phone-IMU streams, sensor timestamps in ns (elapsed realtime).
 * [gameRotation] is recorded separately from the magnetometer rotation vector.
 * [linearAccel] is unused by analysis and is no longer registered; the field
 * stays so older interpolated snapshots still parse.
 */
data class ImuTrace(
    val accel: List<TimedVec3>,
    val gyro: List<TimedVec3>,
    val rotation: List<TimedQuat>,
    val linearAccel: List<TimedVec3> = emptyList(),
    val gameRotation: List<TimedQuat> = emptyList(),
    val gyroUncalibrated: List<TimedGyroUncal> = emptyList(),
    val accuracy: List<AccuracyEvent> = emptyList(),
)

enum class MountSide {
    LEFT,
    RIGHT,
    UNKNOWN,
}

/**
 * Sleeve the phone is clipped to. Screen faces the floor: device +Y lies along
 * the bar, device +Z points down, device +X is horizontal and perpendicular to
 * the bar. [qx]..[qw] is an optional extra clip rotation (identity = axes already
 * match that mount). Y can point either way along the bar; [mountSide] picks the
 * lateral/twist sign. [UNKNOWN] does not guess.
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
    /** Game rotation vector (no magnetometer). Null when that sensor did not report. */
    val gqx: Float? = null,
    val gqy: Float? = null,
    val gqz: Float? = null,
    val gqw: Float? = null,
) {
    val hasRotationVector: Boolean
        get() = qx != null && qy != null && qz != null && qw != null

    val hasGameRotationVector: Boolean
        get() = gqx != null && gqy != null && gqz != null && gqw != null

    val hasGyro: Boolean
        get() = gx != null && gy != null && gz != null

    val hasLinearAccel: Boolean
        get() = lx != null && ly != null && lz != null

    val gyroMagnitude: Float
        get() = if (hasGyro) sqrt(gx!! * gx + gy!! * gy + gz!! * gz) else 0f
}
