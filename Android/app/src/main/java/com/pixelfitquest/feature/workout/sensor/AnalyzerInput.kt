package com.pixelfitquest.feature.workout.sensor

/**
 * Streams the analyzer is allowed to see.
 * Calibrated gyro wins; uncalibrated contributes x/y/z only, and its bias stays
 * on the raw dump. Magnetometer rotation wins when it has samples. Game rotation
 * is the fallback and is never attached beside it.
 */
object AnalyzerInput {
    fun select(dump: RawSensorDump): ImuTrace {
        val gyro = if (dump.gyro.isNotEmpty()) {
            dump.gyro
        } else {
            dump.gyroUncalibrated.map { sample ->
                TimedVec3(sample.tNanos, sample.x, sample.y, sample.z, sample.accuracy)
            }
        }
        val rotation = if (dump.rotationVector.isNotEmpty()) {
            dump.rotationVector
        } else {
            dump.gameRotationVector
        }
        return ImuTrace(accel = dump.accel, gyro = gyro, rotation = rotation)
    }
}
