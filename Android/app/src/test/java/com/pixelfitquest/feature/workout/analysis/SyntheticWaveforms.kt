package com.pixelfitquest.feature.workout.analysis

import com.pixelfitquest.feature.workout.sensor.ImuSample
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

object SyntheticWaveforms {
    const val HZ = 50
    private const val DT = 1.0 / HZ
    private const val DT_NS = 1_000_000_000L / HZ
    private const val G = GRAVITY

    fun cleanBench8(): List<ImuSample> = verticalReps(repCount = 8)

    fun benchWithFalseDip(): List<ImuSample> = verticalReps(
        repCount = 8,
        extraDipAmplitude = 0.05f,
        extraDipAfterRep = 3,
    )

    fun benchTruncatedLast(): List<ImuSample> = verticalReps(
        repCount = 8,
        truncateLast = true,
    )

    fun cleanCurl8(): List<ImuSample> = pitchReps(repCount = 8)

    fun verticalReps(
        repCount: Int,
        amplitude: Float = 0.35f,
        periodSec: Float = 2.5f,
        pauseSec: Float = 0.35f,
        extraDipAmplitude: Float? = null,
        extraDipAfterRep: Int = 3,
        truncateLast: Boolean = false,
    ): List<ImuSample> {
        val samples = ArrayList<ImuSample>()
        var tNanos = 0L

        fun still(seconds: Float, azLin: Float = 0f) {
            val n = (seconds * HZ).toInt().coerceAtLeast(1)
            repeat(n) {
                samples += identity(tNanos, azLin)
                tNanos += DT_NS
            }
        }

        fun cosineRep(amp: Float, period: Float, halfOnly: Boolean = false) {
            val steps = (period * HZ).toInt()
            val limit = if (halfOnly) steps / 2 else steps
            val omega = 2.0 * PI / period
            for (i in 0 until limit) {
                val localT = i * DT
                val az = (-amp / 2f) * (omega * omega).toFloat() * cos(omega * localT).toFloat()
                samples += identity(tNanos, az)
                tNanos += DT_NS
            }
        }

        still(0.7f)
        for (r in 1..repCount) {
            cosineRep(amplitude, periodSec)
            still(pauseSec)
            if (extraDipAmplitude != null && r == extraDipAfterRep) {
                cosineRep(extraDipAmplitude, period = 0.4f)
                still(pauseSec)
            }
        }
        if (truncateLast) {
            cosineRep(amplitude, periodSec, halfOnly = true)
            still(0.5f, azLin = 0f)
        } else {
            still(0.7f)
        }
        return samples
    }

    fun pitchReps(
        repCount: Int,
        amplitudeRad: Float = 1.8f,
        periodSec: Float = 2.5f,
        pauseSec: Float = 0.35f,
    ): List<ImuSample> {
        val samples = ArrayList<ImuSample>()
        var tNanos = 0L

        fun atAngle(theta: Float, omegaY: Float) {
            val half = theta / 2f
            val qy = sin(half)
            val qw = cos(half)
            val r = Signal.rotationMatrix(0f, qy, 0f, qw)
            val gDevice = Signal.transposeMul(r, floatArrayOf(0f, 0f, G))
            samples += ImuSample(
                tNanos = tNanos,
                ax = gDevice[0],
                ay = gDevice[1],
                az = gDevice[2],
                gx = 0f,
                gy = omegaY,
                gz = 0f,
                qx = 0f,
                qy = qy,
                qz = 0f,
                qw = qw,
            )
            tNanos += DT_NS
        }

        fun still(seconds: Float) {
            val n = (seconds * HZ).toInt().coerceAtLeast(1)
            repeat(n) { atAngle(0f, 0f) }
        }

        still(0.7f)
        val omega = 2.0 * PI / periodSec
        val steps = (periodSec * HZ).toInt()
        repeat(repCount) {
            for (i in 0 until steps) {
                val localT = i * DT
                val theta = (amplitudeRad / 2f) * (1.0 - cos(omega * localT)).toFloat()
                val omegaY = (amplitudeRad / 2f) * omega.toFloat() * sin(omega * localT).toFloat()
                atAngle(theta, omegaY)
            }
            still(pauseSec)
        }
        still(0.7f)
        return samples
    }

    private fun identity(tNanos: Long, azLin: Float): ImuSample = ImuSample(
        tNanos = tNanos,
        ax = 0f,
        ay = 0f,
        az = G + azLin,
        gx = 0f,
        gy = 0f,
        gz = 0f,
        qx = 0f,
        qy = 0f,
        qz = 0f,
        qw = 1f,
    )
}
