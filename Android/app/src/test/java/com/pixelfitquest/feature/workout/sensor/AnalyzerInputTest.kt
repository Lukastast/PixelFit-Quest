package com.pixelfitquest.feature.workout.sensor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sin

class AnalyzerInputTest {

    @Test
    fun gameRotationNeverReachesAnalyzerWhenRvExists() {
        val rv = TimedQuat(0L, 0f, 0f, 0f, 1f, accuracy = 3)
        val game = quatAboutX(23.0, accuracy = 1)
        val selected = AnalyzerInput.select(
            dump(rotationVector = listOf(rv), gameRotationVector = listOf(game)),
        )
        assertEquals(listOf(rv), selected.rotation)
        assertTrue(
            "ImuTrace must not carry a game-rotation field",
            ImuTrace::class.java.declaredFields.none { it.name.contains("game", ignoreCase = true) },
        )
        assertTrue(ImuSample::class.java.declaredFields.none { it.name.startsWith("gq") })
    }

    @Test
    fun fallsBackToGameRvOnlyWhenRvEmpty() {
        val game = quatAboutX(23.0)
        val selected = AnalyzerInput.select(dump(gameRotationVector = listOf(game)))
        assertEquals(listOf(game), selected.rotation)
    }

    @Test
    fun calibratedGyroWins_uncalibratedOnlyAsFallback() {
        val calibrated = TimedVec3(10L, 1f, 2f, 3f, accuracy = 3)
        val uncal = TimedGyroUncal(
            tNanos = 10L,
            x = 4f,
            y = 5f,
            z = 6f,
            biasX = 0.11f,
            biasY = -0.22f,
            biasZ = 0.33f,
            accuracy = 1,
        )
        val both = AnalyzerInput.select(
            dump(gyro = listOf(calibrated), gyroUncalibrated = listOf(uncal)),
        )
        assertEquals(listOf(calibrated), both.gyro)

        val fallback = AnalyzerInput.select(dump(gyroUncalibrated = listOf(uncal)))
        assertEquals(listOf(TimedVec3(10L, 4f, 5f, 6f, accuracy = 1)), fallback.gyro)
    }

    @Test
    fun rawDumpKeepsBothRotationStreamsAndBias() {
        val rv = TimedQuat(1L, 0f, 0f, 0f, 1f, accuracy = 3)
        val game = quatAboutX(23.0, tNanos = 2L, accuracy = 2)
        val uncal = TimedGyroUncal(3L, 1f, 0f, 0f, biasX = 0.1f, biasY = 0.2f, biasZ = 0.3f, accuracy = 1)
        val events = listOf(AccuracyEvent(tNanos = 4L, sensorType = 4, accuracy = 2))
        val captured = dump(
            rotationVector = listOf(rv),
            gameRotationVector = listOf(game),
            gyroUncalibrated = listOf(uncal),
            accuracy = events,
            requestedSamplingPeriodUs = 0,
        )
        AnalyzerInput.select(captured)
        assertEquals(listOf(rv), captured.rotationVector)
        assertEquals(listOf(game), captured.gameRotationVector)
        assertEquals(0.1f, captured.gyroUncalibrated.single().biasX, 0f)
        assertEquals(0.2f, captured.gyroUncalibrated.single().biasY, 0f)
        assertEquals(0.3f, captured.gyroUncalibrated.single().biasZ, 0f)
        assertEquals(events, captured.accuracy)
        assertEquals(0, captured.requestedSamplingPeriodUs)
    }

    private fun quatAboutX(degrees: Double, tNanos: Long = 0L, accuracy: Int = 0): TimedQuat {
        val half = Math.toRadians(degrees).toFloat() / 2f
        return TimedQuat(tNanos, sin(half), 0f, 0f, cos(half), accuracy)
    }

    private fun dump(
        gyro: List<TimedVec3> = emptyList(),
        gyroUncalibrated: List<TimedGyroUncal> = emptyList(),
        rotationVector: List<TimedQuat> = emptyList(),
        gameRotationVector: List<TimedQuat> = emptyList(),
        accuracy: List<AccuracyEvent> = emptyList(),
        requestedSamplingPeriodUs: Int? = null,
    ) = RawSensorDump(
        gyro = gyro,
        gyroUncalibrated = gyroUncalibrated,
        rotationVector = rotationVector,
        gameRotationVector = gameRotationVector,
        accuracy = accuracy,
        requestedSamplingPeriodUs = requestedSamplingPeriodUs,
    )
}
