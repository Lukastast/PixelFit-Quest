package com.pixelfitquest.feature.workout.sensor

/** Hardware limits recorded with a raw capture. Delays are microseconds. */
data class TraceSensorInfo(
    val stream: String,
    val type: Int,
    val name: String,
    val vendor: String,
    val minDelayUs: Int,
    val maxDelayUs: Int,
    val resolution: Float,
    val maxRange: Float,
)

/** onAccuracyChanged event. Timestamp is elapsed-realtime nanoseconds. */
data class AccuracyEvent(
    val tNanos: Long,
    val sensorType: Int,
    val accuracy: Int,
)

/** Uncalibrated gyroscope sample, including the bias estimate. */
data class TimedGyroUncal(
    val tNanos: Long,
    val x: Float,
    val y: Float,
    val z: Float,
    val biasX: Float,
    val biasY: Float,
    val biasZ: Float,
    val accuracy: Int,
)

/**
 * In-memory capture. Both rotation streams and gyro bias stay here.
 * This type does not write a file. Persisting a dump is debug-only (slice 1b).
 */
data class RawSensorDump(
    val accel: List<TimedVec3> = emptyList(),
    val gyro: List<TimedVec3> = emptyList(),
    val gyroUncalibrated: List<TimedGyroUncal> = emptyList(),
    val rotationVector: List<TimedQuat> = emptyList(),
    val gameRotationVector: List<TimedQuat> = emptyList(),
    val accuracy: List<AccuracyEvent> = emptyList(),
    val sensors: List<TraceSensorInfo> = emptyList(),
    /** Period passed to registerListener. 0 is SENSOR_DELAY_FASTEST. Null if never registered. */
    val requestedSamplingPeriodUs: Int? = null,
)
