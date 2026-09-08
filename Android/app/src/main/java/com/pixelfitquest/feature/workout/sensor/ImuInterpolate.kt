package com.pixelfitquest.feature.workout.sensor

import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.sin
import kotlin.math.sqrt

private const val EDGE_NS = 20_000_000L

/**
 * Interpolate gyro, rotation vector, and linear accel onto the accel timeline.
 * Does not hold the last sample across the set — only lerps/slerps between
 * bracketing timestamps (or a 20 ms edge).
 */
object ImuInterpolate {
    fun ontoAccel(trace: ImuTrace): List<ImuSample> {
        if (trace.accel.isEmpty()) return emptyList()
        val gyroT = LongArray(trace.gyro.size) { trace.gyro[it].tNanos }
        val rotT = LongArray(trace.rotation.size) { trace.rotation[it].tNanos }
        val linT = LongArray(trace.linearAccel.size) { trace.linearAccel[it].tNanos }
        return trace.accel.map { a ->
            val g = lerpVec(trace.gyro, gyroT, a.tNanos)
            val q = slerpQuat(trace.rotation, rotT, a.tNanos)
            val lin = lerpVec(trace.linearAccel, linT, a.tNanos)
            ImuSample(
                tNanos = a.tNanos,
                ax = a.x,
                ay = a.y,
                az = a.z,
                gx = g?.get(0),
                gy = g?.get(1),
                gz = g?.get(2),
                qx = q?.get(0),
                qy = q?.get(1),
                qz = q?.get(2),
                qw = q?.get(3),
                lx = lin?.get(0),
                ly = lin?.get(1),
                lz = lin?.get(2),
            )
        }
    }

    internal fun lerpVec(stream: List<TimedVec3>, times: LongArray, t: Long): FloatArray? {
        if (stream.isEmpty()) return null
        val i = bracket(times, t) ?: return edgeVec(stream, t)
        val t0 = times[i]
        val t1 = times[i + 1]
        val span = (t1 - t0).coerceAtLeast(1L)
        val alpha = ((t - t0).toDouble() / span).toFloat().coerceIn(0f, 1f)
        val a = stream[i]
        val b = stream[i + 1]
        return floatArrayOf(
            a.x + (b.x - a.x) * alpha,
            a.y + (b.y - a.y) * alpha,
            a.z + (b.z - a.z) * alpha,
        )
    }

    internal fun slerpQuat(stream: List<TimedQuat>, times: LongArray, t: Long): FloatArray? {
        if (stream.isEmpty()) return null
        val i = bracket(times, t) ?: return edgeQuat(stream, t)
        val t0 = times[i]
        val t1 = times[i + 1]
        val span = (t1 - t0).coerceAtLeast(1L)
        val alpha = ((t - t0).toDouble() / span).toFloat().coerceIn(0f, 1f)
        val a = stream[i]
        val b = stream[i + 1]
        return slerp(a.x, a.y, a.z, a.w, b.x, b.y, b.z, b.w, alpha)
    }

    private fun bracket(times: LongArray, t: Long): Int? {
        if (times.size < 2) return null
        if (t < times.first() || t > times.last()) return null
        var lo = 0
        var hi = times.lastIndex
        while (lo + 1 < hi) {
            val mid = (lo + hi) ushr 1
            if (times[mid] <= t) lo = mid else hi = mid
        }
        return lo.coerceAtMost(times.size - 2)
    }

    private fun edgeVec(stream: List<TimedVec3>, t: Long): FloatArray? {
        val first = stream.first()
        val last = stream.last()
        val nearest = if (abs(t - first.tNanos) <= abs(t - last.tNanos)) first else last
        if (abs(t - nearest.tNanos) > EDGE_NS) return null
        return floatArrayOf(nearest.x, nearest.y, nearest.z)
    }

    private fun edgeQuat(stream: List<TimedQuat>, t: Long): FloatArray? {
        val first = stream.first()
        val last = stream.last()
        val nearest = if (abs(t - first.tNanos) <= abs(t - last.tNanos)) first else last
        if (abs(t - nearest.tNanos) > EDGE_NS) return null
        return floatArrayOf(nearest.x, nearest.y, nearest.z, nearest.w)
    }

    private fun slerp(
        x0: Float, y0: Float, z0: Float, w0: Float,
        x1: Float, y1: Float, z1: Float, w1: Float,
        t: Float,
    ): FloatArray {
        var dot = x0 * x1 + y0 * y1 + z0 * z1 + w0 * w1
        var bx = x1
        var by = y1
        var bz = z1
        var bw = w1
        if (dot < 0f) {
            bx = -bx
            by = -by
            bz = -bz
            bw = -bw
            dot = -dot
        }
        if (dot > 0.9995f) {
            return normalizeQuat(
                x0 + t * (bx - x0),
                y0 + t * (by - y0),
                z0 + t * (bz - z0),
                w0 + t * (bw - w0),
            )
        }
        val theta = acos(dot.coerceIn(-1f, 1f))
        val s = sin(theta).coerceAtLeast(1e-6f)
        val a = sin((1f - t) * theta) / s
        val b = sin(t * theta) / s
        return floatArrayOf(a * x0 + b * bx, a * y0 + b * by, a * z0 + b * bz, a * w0 + b * bw)
    }

    private fun normalizeQuat(x: Float, y: Float, z: Float, w: Float): FloatArray {
        val mag = sqrt(x * x + y * y + z * z + w * w).coerceAtLeast(1e-9f)
        return floatArrayOf(x / mag, y / mag, z / mag, w / mag)
    }
}
