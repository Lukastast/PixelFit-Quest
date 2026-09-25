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
        eccentricSec: Float? = null,
        concentricSec: Float? = null,
        pauseSec: Float = 0.35f,
        extraDipAmplitude: Float? = null,
        extraDipAfterRep: Int = 3,
        truncateLast: Boolean = false,
        hz: Int = HZ,
        gravityLeak: Float = 0f,
        tiltRad: Float = 0f,
        pitchRad: Float = 0f,
        restPitchRad: Float = 0f,
        yawRad: Float = 0f,
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
                samples += sample(
                    tNanos = tNanos,
                    azLin = azLin,
                    gravityLeak = gravityLeak,
                    rollRad = 0f,
                    pitchRad = restPitchRad,
                    yawRad = 0f,
                    includeRv = includeRv,
                    includeGyro = includeGyro,
                )
                tNanos += dtNs
            }
        }

        fun halfCycle(amp: Float, durationSec: Float, phase0: Double) {
            val steps = (durationSec * hz).toInt().coerceAtLeast(1)
            val omega = PI / durationSec
            for (i in 0 until steps) {
                val localT = i * dt
                val phase = phase0 + omega * localT
                val az = (-amp / 2f) * (omega * omega).toFloat() * cos(phase).toFloat()
                samples += sample(
                    tNanos = tNanos,
                    azLin = az,
                    gravityLeak = gravityLeak,
                    rollRad = tiltRad,
                    pitchRad = restPitchRad + pitchRad,
                    yawRad = yawRad,
                    includeRv = includeRv,
                    includeGyro = includeGyro,
                )
                tNanos += dtNs
            }
        }

        fun cosineRep(amp: Float, eccSec: Float, conSec: Float, halfOnly: Boolean = false) {
            halfCycle(amp, eccSec, 0.0)
            if (!halfOnly) halfCycle(amp, conSec, PI)
        }

        val eccSec = eccentricSec ?: (periodSec / 2f)
        val conSec = concentricSec ?: (periodSec / 2f)

        still(0.7f)
        for (r in 1..repCount) {
            cosineRep(amplitude, eccSec, conSec)
            still(pauseSec)
            if (extraDipAmplitude != null && r == extraDipAfterRep) {
                cosineRep(extraDipAmplitude, 0.2f, 0.2f)
                still(pauseSec)
            }
        }
        if (truncateLast) {
            cosineRep(amplitude, eccSec, conSec, halfOnly = true)
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
        rollRad: Float,
        pitchRad: Float,
        yawRad: Float,
        includeRv: Boolean,
        includeGyro: Boolean,
    ): ImuSample {
        val q = quatYawPitchRoll(yawRad, pitchRad, rollRad)
        val r = Signal.rotationMatrix(q[0], q[1], q[2], q[3])
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
            qx = if (includeRv) q[0] else null,
            qy = if (includeRv) q[1] else null,
            qz = if (includeRv) q[2] else null,
            qw = if (includeRv) q[3] else null,
        )
    }

    /** q = q_yaw * q_pitch * q_roll, stored as (x, y, z, w). */
    private fun quatYawPitchRoll(yaw: Float, pitch: Float, roll: Float): FloatArray {
        val qRoll = quatAxis(1f, 0f, 0f, roll)
        val qPitch = quatAxis(0f, 1f, 0f, pitch)
        val qYaw = quatAxis(0f, 0f, 1f, yaw)
        return quatMul(quatMul(qYaw, qPitch), qRoll)
    }

    private fun quatAxis(ax: Float, ay: Float, az: Float, angle: Float): FloatArray {
        val half = angle / 2f
        val s = sin(half)
        return floatArrayOf(ax * s, ay * s, az * s, cos(half))
    }

    private fun quatMul(a: FloatArray, b: FloatArray): FloatArray {
        val ax = a[0]
        val ay = a[1]
        val az = a[2]
        val aw = a[3]
        val bx = b[0]
        val by = b[1]
        val bz = b[2]
        val bw = b[3]
        return floatArrayOf(
            aw * bx + ax * bw + ay * bz - az * by,
            aw * by - ax * bz + ay * bw + az * bx,
            aw * bz + ax * by - ay * bx + az * bw,
            aw * bw - ax * bx - ay * by - az * bz,
        )
    }
}
