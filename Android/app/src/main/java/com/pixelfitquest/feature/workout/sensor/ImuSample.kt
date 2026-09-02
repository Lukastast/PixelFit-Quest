package com.pixelfitquest.feature.workout.sensor

/**
 * One fused IMU tick. Accel is always present (includes gravity).
 * Gyro and rotation-vector quaternion are optional depending on hardware.
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
) {
    val hasRotationVector: Boolean
        get() = qx != null && qy != null && qz != null && qw != null

    val hasGyro: Boolean
        get() = gx != null && gy != null && gz != null

    val gyroMagnitude: Float
        get() = if (hasGyro) kotlin.math.sqrt(gx!! * gx + gy!! * gy + gz!! * gz) else 0f
}
