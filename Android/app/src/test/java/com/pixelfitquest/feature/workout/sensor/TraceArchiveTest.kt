package com.pixelfitquest.feature.workout.sensor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

class TraceArchiveTest {

    @Test
    fun writeThenParse_valuesMatch() {
        val trace = RawTrace(
            metadata = TraceMetadata(
                deviceModel = "Pixel 8",
                apiLevel = 34,
                sensors = listOf(
                    TraceSensorInfo("accel", 1, "BMI260 Accelerometer", "Bosch"),
                    TraceSensorInfo("game_rotation_vector", 15, "Game Rotation Vector", "AOSP"),
                ),
                exerciseId = "bench-press",
                weightKg = 62.5f,
                mountSide = MountSide.RIGHT.name,
                label = TraceLabel(
                    trueRepCount = 5,
                    failedRepIndices = listOf(2, 4),
                    notes = "paused rep, note with comma, and = sign",
                ),
            ),
            accel = listOf(
                TimedVec3(1_000L, 0.1f, -9.81f, 0.25f, accuracy = 3),
                TimedVec3(11_000_000L, 0.2f, -9.7f, 1.5f, accuracy = 3),
            ),
            gyro = listOf(TimedVec3(1_000L, 0.01f, -0.02f, 0.03f, accuracy = 3)),
            gyroUncalibrated = listOf(
                TimedGyroUncal(
                    tNanos = 1_000L,
                    x = 0.05f,
                    y = -0.02f,
                    z = 0.03f,
                    biasX = 0.04f,
                    biasY = 0.0f,
                    biasZ = -0.001f,
                    accuracy = 2,
                ),
            ),
            rotationVector = listOf(TimedQuat(1_000L, 0f, 0f, 0f, 1f, accuracy = 3)),
            gameRotationVector = listOf(
                TimedQuat(2_000L, 0.1f, -0.2f, 0.3f, 0.9f, accuracy = 1),
            ),
            accuracy = listOf(
                AccuracyEvent(tNanos = 500L, sensorType = 1, accuracy = 3),
                AccuracyEvent(tNanos = 800L, sensorType = 15, accuracy = 1),
            ),
        )
        val file = File.createTempFile("imu-trace", ".zip")
        try {
            TraceArchive.write(file, trace)
            val read = TraceArchive.read(file)
            assertEquals(trace.metadata, read.metadata)
            assertEquals(trace.accel, read.accel)
            assertEquals(trace.gyro, read.gyro)
            assertEquals(trace.gyroUncalibrated, read.gyroUncalibrated)
            assertEquals(trace.rotationVector, read.rotationVector)
            assertEquals(trace.gameRotationVector, read.gameRotationVector)
            assertEquals(trace.accuracy, read.accuracy)
        } finally {
            file.delete()
        }
    }

    @Test
    fun unsetWeightAndLabel_roundTripAsNull() {
        val trace = RawTrace(
            metadata = TraceMetadata(
                deviceModel = "",
                apiLevel = 29,
                sensors = emptyList(),
                exerciseId = "squat",
                weightKg = null,
                mountSide = MountSide.UNKNOWN.name,
                label = TraceLabel(trueRepCount = null, failedRepIndices = emptyList(), notes = ""),
            ),
            accel = emptyList(),
            gyro = emptyList(),
            gyroUncalibrated = emptyList(),
            rotationVector = emptyList(),
            gameRotationVector = emptyList(),
            accuracy = emptyList(),
        )
        val file = File.createTempFile("imu-trace-empty", ".zip")
        try {
            TraceArchive.write(file, trace)
            val read = TraceArchive.read(file)
            assertNull(read.metadata.weightKg)
            assertNull(read.metadata.label.trueRepCount)
            assertEquals(emptyList<Int>(), read.metadata.label.failedRepIndices)
            assertEquals(emptyList<TimedVec3>(), read.accel)
            assertEquals(MountSide.UNKNOWN.name, read.metadata.mountSide)
        } finally {
            file.delete()
        }
    }
}
