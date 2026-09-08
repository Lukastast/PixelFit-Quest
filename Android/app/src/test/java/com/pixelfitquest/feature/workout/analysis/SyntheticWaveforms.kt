package com.pixelfitquest.feature.workout.analysis

import com.pixelfitquest.feature.workout.sensor.ImuSample
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object SyntheticWaveforms {
    const val HZ = 50
    private const val G = GRAVITY

    fun cleanBench8(hz: Int = HZ): List<ImuSample> = verticalReps(repCount = 8, hz = hz)

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
        hz: Int = HZ,
        gravityLeak: Float = 0f,
        tiltRad: Float = 0f,
        includeRv: Boolean = true,
        includeGyro: Boolean = true,
    ): List<ImuSample> {
        val samples = ArrayList<ImuSample>()
        var tNanos = 0L
        val dtNs = 1_000_000_000L / hz
        val dt = 1.0 / hz

        fun still(seconds: Float, azLin: Float = 0f) {
            val n = (seconds * hz).toInt().coerceAtLeast(1)
            repeat(n) {
                samples += sample(tNanos, azLin, gravityLeak, tiltRad, includeRv, includeGyro)
                tNanos += dtNs
            }
        }

        fun cosineRep(amp: Float, period: Float, halfOnly: Boolean = false) {
            val steps = (period * hz).toInt()
            val limit = if (halfOnly) steps / 2 else steps
            val omega = 2.0 * PI / period
            for (i in 0 until limit) {
                val localT = i * dt
                val az = (-amp / 2f) * (omega * omega).toFloat() * cos(omega * localT).toFloat()
                samples += sample(tNanos, az, gravityLeak, tiltRad, includeRv, includeGyro)
                tNanos += dtNs
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
            still(0.5f)
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
        hz: Int = HZ,
    ): List<ImuSample> {
        val samples = ArrayList<ImuSample>()
        var tNanos = 0L
        val dtNs = 1_000_000_000L / hz
        val dt = 1.0 / hz

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
            tNanos += dtNs
        }

        fun still(seconds: Float) {
            val n = (seconds * hz).toInt().coerceAtLeast(1)
            repeat(n) { atAngle(0f, 0f) }
        }

        still(0.7f)
        val omega = 2.0 * PI / periodSec
        val steps = (periodSec * hz).toInt()
        repeat(repCount) {
            for (i in 0 until steps) {
                val localT = i * dt
                val theta = (amplitudeRad / 2f) * (1.0 - cos(omega * localT)).toFloat()
                val omegaY = (amplitudeRad / 2f) * omega.toFloat() * sin(omega * localT).toFloat()
                atAngle(theta, omegaY)
            }
            still(pauseSec)
        }
        still(0.7f)
        return samples
    }

    private fun sample(
        tNanos: Long,
        azLin: Float,
        gravityLeak: Float,
        tiltRad: Float,
        includeRv: Boolean,
        includeGyro: Boolean,
    ): ImuSample {
        val half = tiltRad / 2f
        val qx = sin(half)
        val qw = cos(half)
        val r = Signal.rotationMatrix(qx, 0f, 0f, qw)
        val worldA = floatArrayOf(0f, 0f, G + azLin + gravityLeak)
        val deviceA = Signal.transposeMul(r, worldA)
        return ImuSample(
            tNanos = tNanos,
            ax = deviceA[0],
            ay = deviceA[1],
            az = deviceA[2],
            gx = if (includeGyro) 0f else null,
            gy = if (includeGyro) 0f else null,
            gz = if (includeGyro) 0f else null,
            qx = if (includeRv) qx else null,
            qy = if (includeRv) 0f else null,
            qz = if (includeRv) 0f else null,
            qw = if (includeRv) qw else null,
        )
    }
}
