package com.pixelfitquest.feature.workout.analysis

import com.pixelfitquest.feature.workout.sensor.BarCalibration
import com.pixelfitquest.feature.workout.sensor.ImuSample
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

internal data class AttitudeSeries(
    val q: Array<FloatArray>,
    val linWorld: Array<FloatArray>,
    val roll: FloatArray,
    val pitch: FloatArray,
    val yaw: FloatArray,
    val usedRotationVector: Boolean,
    val usedGyro: Boolean,
)

/**
 * Batch attitude + gravity-free linear accel. Prefers rotation vector,
 * else Madgwick on gyro+accel, else accel-only gravity. Smooths the whole set.
 */
internal object Attitude {
    fun estimate(samples: List<ImuSample>, calibration: BarCalibration?): AttitudeSeries {
        val n = samples.size
        val q = Array(n) { floatArrayOf(0f, 0f, 0f, 1f) }
        val usedRv = samples.count { it.hasRotationVector } > n / 2
        val usedGyro = samples.count { it.hasGyro } > n / 4

        if (usedRv) {
            for (i in 0 until n) {
                val s = samples[i]
                q[i] = if (s.hasRotationVector) {
                    floatArrayOf(s.qx!!, s.qy!!, s.qz!!, s.qw!!)
                } else {
                    q[i.coerceAtLeast(1) - 1].copyOf()
                }
            }
        } else if (usedGyro) {
            madgwickForwardBackward(samples, q)
        } else {
            accelOnlyGravity(samples, q)
        }

        smoothQuaternions(q)

        val cal = calibration?.let { floatArrayOf(it.qx, it.qy, it.qz, it.qw) }
        val linWorld = Array(n) { FloatArray(3) }
        val roll = FloatArray(n)
        val pitch = FloatArray(n)
        val yaw = FloatArray(n)
        val q0inv = invert(q[0])

        for (i in 0 until n) {
            val s = samples[i]
            val qi = if (cal != null) mul(q[i], cal) else q[i]
            val r = Signal.rotationMatrix(qi[0], qi[1], qi[2], qi[3])
            val gDevice = Signal.transposeMul(r, floatArrayOf(0f, 0f, GRAVITY))
            val linDevice = floatArrayOf(s.ax - gDevice[0], s.ay - gDevice[1], s.az - gDevice[2])
            linWorld[i] = Signal.mulMatVec(r, linDevice)
            val rel = mul(q0inv, qi)
            val rv = rotationVector(rel)
            roll[i] = rv[0]
            pitch[i] = rv[1]
            yaw[i] = rv[2]
        }

        return AttitudeSeries(
            q = q,
            linWorld = linWorld,
            roll = roll,
            pitch = pitch,
            yaw = yaw,
            usedRotationVector = usedRv,
            usedGyro = usedGyro,
        )
    }

    private fun accelOnlyGravity(samples: List<ImuSample>, q: Array<FloatArray>) {
        for (i in samples.indices) {
            val s = samples[i]
            q[i] = quatFromAccel(s.ax, s.ay, s.az)
        }
    }

    private fun quatFromAccel(ax: Float, ay: Float, az: Float): FloatArray {
        val mag = sqrt(ax * ax + ay * ay + az * az).coerceAtLeast(1e-6f)
        val ux = ax / mag
        val uy = ay / mag
        val uz = az / mag
        val gx = 0f
        val gy = 0f
        val gz = 1f
        val dx = gy * uz - gz * uy
        val dy = gz * ux - gx * uz
        val dz = gx * uy - gy * ux
        val dmag = sqrt(dx * dx + dy * dy + dz * dz)
        if (dmag < 1e-5f) return floatArrayOf(0f, 0f, 0f, 1f)
        val angle = kotlin.math.acos((ux * gx + uy * gy + uz * gz).coerceIn(-1f, 1f))
        val half = angle / 2f
        val s = sin(half) / dmag
        return floatArrayOf(dx * s, dy * s, dz * s, cos(half))
    }

    private fun madgwickForwardBackward(samples: List<ImuSample>, out: Array<FloatArray>) {
        val fwd = madgwickPass(samples, reverse = false)
        val bwd = madgwickPass(samples, reverse = true)
        for (i in out.indices) {
            out[i] = slerpHalf(fwd[i], bwd[i])
        }
    }

    private fun madgwickPass(samples: List<ImuSample>, reverse: Boolean): Array<FloatArray> {
        val n = samples.size
        val q = Array(n) { floatArrayOf(0f, 0f, 0f, 1f) }
        var qw = 1f
        var qx = 0f
        var qy = 0f
        var qz = 0f
        val beta = 0.06f
        val indices = if (reverse) (n - 1 downTo 0) else (0 until n)
        var prevT = samples[indices.first()].tNanos
        val sign = if (reverse) -1f else 1f
        for (i in indices) {
            val s = samples[i]
            val dt = Signal.dtSeconds(prevT, s.tNanos).coerceIn(0.001f, 0.05f)
            prevT = s.tNanos
            val gx = (s.gx ?: 0f) * sign
            val gy = (s.gy ?: 0f) * sign
            val gz = (s.gz ?: 0f) * sign
            var ax = s.ax
            var ay = s.ay
            var az = s.az
            val an = sqrt(ax * ax + ay * ay + az * az)
            if (an > 1e-6f) {
                ax /= an
                ay /= an
                az /= an
                val f1 = 2f * (qx * qz - qw * qy) - ax
                val f2 = 2f * (qw * qx + qy * qz) - ay
                val f3 = 2f * (0.5f - qx * qx - qy * qy) - az
                val s0 = -2f * qy * f1 + 2f * qx * f2
                val s1 = 2f * qz * f1 + 2f * qw * f2 - 4f * qx * f3
                val s2 = -2f * qw * f1 + 2f * qz * f2 - 4f * qy * f3
                val s3 = 2f * qx * f1 + 2f * qy * f2
                val sn = sqrt(s0 * s0 + s1 * s1 + s2 * s2 + s3 * s3).coerceAtLeast(1e-9f)
                val b0 = s0 / sn
                val b1 = s1 / sn
                val b2 = s2 / sn
                val b3 = s3 / sn
                val qDotW = 0.5f * (-qx * gx - qy * gy - qz * gz) - beta * b0
                val qDotX = 0.5f * (qw * gx + qy * gz - qz * gy) - beta * b1
                val qDotY = 0.5f * (qw * gy - qx * gz + qz * gx) - beta * b2
                val qDotZ = 0.5f * (qw * gz + qx * gy - qy * gx) - beta * b3
                qw += qDotW * dt
                qx += qDotX * dt
                qy += qDotY * dt
                qz += qDotZ * dt
                val qn = sqrt(qw * qw + qx * qx + qy * qy + qz * qz).coerceAtLeast(1e-9f)
                qw /= qn
                qx /= qn
                qy /= qn
                qz /= qn
            } else {
                val qDotW = 0.5f * (-qx * gx - qy * gy - qz * gz)
                val qDotX = 0.5f * (qw * gx + qy * gz - qz * gy)
                val qDotY = 0.5f * (qw * gy - qx * gz + qz * gx)
                val qDotZ = 0.5f * (qw * gz + qx * gy - qy * gx)
                qw += qDotW * dt
                qx += qDotX * dt
                qy += qDotY * dt
                qz += qDotZ * dt
            }
            q[i] = floatArrayOf(qx, qy, qz, qw)
        }
        return q
    }

    private fun smoothQuaternions(q: Array<FloatArray>) {
        if (q.size < 5) return
        val refInv = invert(q[0])
        val n = q.size
        val lx = FloatArray(n)
        val ly = FloatArray(n)
        val lz = FloatArray(n)
        for (i in 0 until n) {
            val rel = mul(refInv, q[i])
            val v = quatLog(rel)
            lx[i] = v[0]
            ly[i] = v[1]
            lz[i] = v[2]
        }
        val sx = Signal.filtfilt(lx, 5)
        val sy = Signal.filtfilt(ly, 5)
        val sz = Signal.filtfilt(lz, 5)
        val ref = q[0]
        for (i in 0 until n) {
            q[i] = mul(ref, quatExp(sx[i], sy[i], sz[i]))
        }
    }

    private fun quatLog(q: FloatArray): FloatArray {
        var x = q[0]
        var y = q[1]
        var z = q[2]
        var w = q[3]
        if (w < 0f) {
            x = -x
            y = -y
            z = -z
            w = -w
        }
        val vmag = sqrt(x * x + y * y + z * z)
        if (vmag < 1e-8f) return floatArrayOf(0f, 0f, 0f)
        val theta = atan2(vmag, w)
        val s = theta / vmag
        return floatArrayOf(x * s, y * s, z * s)
    }

    private fun quatExp(x: Float, y: Float, z: Float): FloatArray {
        val theta = sqrt(x * x + y * y + z * z)
        if (theta < 1e-8f) return floatArrayOf(0f, 0f, 0f, 1f)
        val s = sin(theta) / theta
        return floatArrayOf(x * s, y * s, z * s, cos(theta))
    }

    /** Axis-angle vector (radians); does not fold at 90° like Euler pitch. */
    private fun rotationVector(q: FloatArray): FloatArray {
        val log = quatLog(q)
        return floatArrayOf(2f * log[0], 2f * log[1], 2f * log[2])
    }

    private fun invert(q: FloatArray): FloatArray = floatArrayOf(-q[0], -q[1], -q[2], q[3])

    private fun mul(a: FloatArray, b: FloatArray): FloatArray {
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

    private fun slerpHalf(a: FloatArray, b: FloatArray): FloatArray {
        var dot = a[0] * b[0] + a[1] * b[1] + a[2] * b[2] + a[3] * b[3]
        val bx = if (dot < 0f) -b[0] else b[0]
        val by = if (dot < 0f) -b[1] else b[1]
        val bz = if (dot < 0f) -b[2] else b[2]
        val bw = if (dot < 0f) -b[3] else b[3]
        if (dot < 0f) dot = -dot
        val x = a[0] + bx
        val y = a[1] + by
        val z = a[2] + bz
        val w = a[3] + bw
        val mag = sqrt(x * x + y * y + z * z + w * w).coerceAtLeast(1e-9f)
        return floatArrayOf(x / mag, y / mag, z / mag, w / mag)
    }
}
