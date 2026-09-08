package com.pixelfitquest.feature.workout.analysis

import com.pixelfitquest.feature.workout.sensor.BarCalibration
import com.pixelfitquest.feature.workout.sensor.ImuSample
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

internal const val GRAVITY = 9.81f

internal data class Extremum(
    val index: Int,
    val tNanos: Long,
    val value: Float,
    val isPeak: Boolean,
)

internal data class RawCycle(
    val startIndex: Int,
    val extremumIndex: Int,
    val endIndex: Int,
    val tStartNanos: Long,
    val tMidNanos: Long,
    val tEndNanos: Long,
    val amplitude: Float,
    val truncated: Boolean,
)

internal data class PreparedSignals(
    val tNanos: LongArray,
    val dt: FloatArray,
    val primary: FloatArray,
    val verticalAccel: FloatArray,
    val tiltX: FloatArray,
    val tiltZ: FloatArray,
    val roll: FloatArray,
    val yaw: FloatArray,
    val still: BooleanArray,
    val usedRotationVector: Boolean,
    val usedGyro: Boolean,
)

internal object Signal {
    fun dtSeconds(prevNanos: Long, nanos: Long): Float {
        val dt = abs(nanos - prevNanos) / 1_000_000_000f
        return dt.coerceIn(0.001f, 0.08f)
    }

    fun rotationMatrix(qx: Float, qy: Float, qz: Float, qw: Float): FloatArray {
        val x = qx
        val y = qy
        val z = qz
        val w = qw
        return floatArrayOf(
            1f - 2f * y * y - 2f * z * z, 2f * x * y - 2f * z * w, 2f * x * z + 2f * y * w,
            2f * x * y + 2f * z * w, 1f - 2f * x * x - 2f * z * z, 2f * y * z - 2f * x * w,
            2f * x * z - 2f * y * w, 2f * y * z + 2f * x * w, 1f - 2f * x * x - 2f * y * y,
        )
    }

    fun mulMatVec(r: FloatArray, v: FloatArray): FloatArray = floatArrayOf(
        r[0] * v[0] + r[1] * v[1] + r[2] * v[2],
        r[3] * v[0] + r[4] * v[1] + r[5] * v[2],
        r[6] * v[0] + r[7] * v[1] + r[8] * v[2],
    )

    fun transposeMul(r: FloatArray, v: FloatArray): FloatArray = floatArrayOf(
        r[0] * v[0] + r[3] * v[1] + r[6] * v[2],
        r[1] * v[0] + r[4] * v[1] + r[7] * v[2],
        r[2] * v[0] + r[5] * v[1] + r[8] * v[2],
    )

    fun smooth(values: FloatArray, window: Int): FloatArray {
        if (values.isEmpty()) return values
        val w = window.coerceAtLeast(1)
        val half = w / 2
        val out = FloatArray(values.size)
        for (i in values.indices) {
            val from = (i - half).coerceAtLeast(0)
            val to = (i + half).coerceAtMost(values.lastIndex)
            var sum = 0f
            for (j in from..to) sum += values[j]
            out[i] = sum / (to - from + 1)
        }
        return out
    }

    fun filtfilt(values: FloatArray, window: Int): FloatArray {
        if (values.size < 3) return values.copyOf()
        val fwd = causalMa(values, window)
        return reverseInPlace(causalMa(reverseCopy(fwd), window))
    }

    private fun causalMa(values: FloatArray, window: Int): FloatArray {
        val w = window.coerceAtLeast(1)
        val out = FloatArray(values.size)
        var sum = 0f
        for (i in values.indices) {
            sum += values[i]
            if (i >= w) sum -= values[i - w]
            val n = if (i + 1 < w) i + 1 else w
            out[i] = sum / n
        }
        return out
    }

    private fun reverseCopy(values: FloatArray): FloatArray {
        val out = FloatArray(values.size)
        for (i in values.indices) out[i] = values[values.lastIndex - i]
        return out
    }

    private fun reverseInPlace(values: FloatArray): FloatArray {
        var i = 0
        var j = values.lastIndex
        while (i < j) {
            val tmp = values[i]
            values[i] = values[j]
            values[j] = tmp
            i++
            j--
        }
        return values
    }

    fun extrema(values: FloatArray, tNanos: LongArray): List<Extremum> {
        if (values.size < 3) return emptyList()
        val raw = ArrayList<Extremum>()
        for (i in 1 until values.lastIndex) {
            val prev = values[i - 1]
            val cur = values[i]
            val next = values[i + 1]
            val peak = cur >= prev && cur >= next && (cur > prev || cur > next)
            val valley = cur <= prev && cur <= next && (cur < prev || cur < next)
            if (peak) {
                raw += Extremum(i, tNanos[i], cur, isPeak = true)
            } else if (valley) {
                raw += Extremum(i, tNanos[i], cur, isPeak = false)
            }
        }
        if (raw.size < 2) return raw
        val collapsed = ArrayList<Extremum>()
        for (ext in raw) {
            val last = collapsed.lastOrNull()
            if (last == null) {
                collapsed += ext
            } else if (last.isPeak == ext.isPeak) {
                val keep = if (ext.isPeak) {
                    if (ext.value >= last.value) ext else last
                } else {
                    if (ext.value <= last.value) ext else last
                }
                collapsed[collapsed.lastIndex] = keep
            } else {
                collapsed += ext
            }
        }
        return collapsed
    }

    fun principalProjection(x: FloatArray, y: FloatArray): FloatArray {
        val n = x.size
        if (n == 0) return floatArrayOf()
        var mx = 0.0
        var my = 0.0
        for (i in 0 until n) {
            mx += x[i]
            my += y[i]
        }
        mx /= n
        my /= n
        var cxx = 0.0
        var cxy = 0.0
        var cyy = 0.0
        for (i in 0 until n) {
            val dx = x[i] - mx
            val dy = y[i] - my
            cxx += dx * dx
            cxy += dx * dy
            cyy += dy * dy
        }
        val theta = 0.5 * atan2(2.0 * cxy, cxx - cyy)
        val c = cos(theta)
        val s = sin(theta)
        return FloatArray(n) { i -> ((x[i] - mx) * c + (y[i] - my) * s).toFloat() }
    }

    fun stillMask(
        samples: List<ImuSample>,
        linUp: FloatArray,
        tNanos: LongArray,
    ): BooleanArray {
        val n = samples.size
        val still = BooleanArray(n)
        if (n == 0) return still
        val medianDt = if (n < 2) 0.02f else dtSeconds(tNanos[0], tNanos[1])
        val win = ((0.22f / medianDt.coerceAtLeast(0.002f)).toInt()).coerceIn(7, 61)
        val magDev = FloatArray(n)
        val gyroMag = FloatArray(n)
        for (i in 0 until n) {
            val s = samples[i]
            val mag = sqrt(s.ax * s.ax + s.ay * s.ay + s.az * s.az)
            magDev[i] = abs(mag - GRAVITY)
            gyroMag[i] = s.gyroMagnitude
        }
        val rmsLin = rollingRms(linUp, win)
        val meanDev = smooth(magDev, win)
        val meanGyro = smooth(gyroMag, win)
        val hasGyro = samples.any { it.hasGyro && it.gyroMagnitude > 1e-4f }
        for (i in 0 until n) {
            val accelQuiet = rmsLin[i] < 0.22f && meanDev[i] < 0.50f
            val gyroQuiet = !hasGyro || meanGyro[i] < 0.18f
            still[i] = accelQuiet && gyroQuiet
        }
        return still
    }

    fun integrateZuptSmoothed(
        accel: FloatArray,
        dt: FloatArray,
        tNanos: LongArray,
        still: BooleanArray,
    ): FloatArray {
        val n = accel.size
        if (n == 0) return floatArrayOf()
        val nodes = stillNodes(still, tNanos)
        val v = FloatArray(n)
        val p = FloatArray(n)
        val ends = if (nodes.size >= 2) nodes else intArrayOf(0, n - 1)
        for (k in 0 until ends.size - 1) {
            val i0 = ends[k]
            val i1 = ends[k + 1]
            v[i0] = 0f
            for (i in i0 + 1..i1) {
                v[i] = v[i - 1] + 0.5f * (accel[i - 1] + accel[i]) * dt[i]
            }
            val vErr = v[i1]
            val span = (tNanos[i1] - tNanos[i0]).coerceAtLeast(1L)
            for (i in i0..i1) {
                val alpha = (tNanos[i] - tNanos[i0]).toFloat() / span
                v[i] -= alpha * vErr
            }
            v[i1] = 0f
            for (i in i0 + 1..i1) {
                p[i] = p[i - 1] + 0.5f * (v[i - 1] + v[i]) * dt[i]
            }
        }
        if (ends.last() < n - 1) {
            val i0 = ends.last()
            v[i0] = 0f
            for (i in i0 + 1 until n) {
                v[i] = v[i - 1] + 0.5f * (accel[i - 1] + accel[i]) * dt[i]
                p[i] = p[i - 1] + 0.5f * (v[i - 1] + v[i]) * dt[i]
            }
        }
        val medianDt = if (n < 2) 0.02f else dtSeconds(tNanos[0], tNanos[n - 1]) / (n - 1)
        val win = ((0.12f / medianDt.coerceAtLeast(0.002f)).toInt()).coerceIn(5, 51)
        return filtfilt(p, win)
    }

    fun prepare(
        samples: List<ImuSample>,
        motion: PrimaryMotion,
        calibration: BarCalibration? = null,
    ): PreparedSignals {
        val n = samples.size
        val tNanos = LongArray(n) { samples[it].tNanos }
        val dt = FloatArray(n)
        dt[0] = if (n > 1) dtSeconds(tNanos[0], tNanos[1]) else 0.02f
        for (i in 1 until n) dt[i] = dtSeconds(tNanos[i - 1], tNanos[i])

        val attitude = Attitude.estimate(samples, calibration)
        val linUp = FloatArray(n) { attitude.linWorld[it][2] }
        val linX = FloatArray(n) { attitude.linWorld[it][0] }
        val linY = FloatArray(n) { attitude.linWorld[it][1] }

        val still = stillMask(samples, linUp, tNanos)
        val vertical = integrateZuptSmoothed(linUp, dt, tNanos, still)
        val hx = integrateZuptSmoothed(linX, dt, tNanos, still)
        val hy = integrateZuptSmoothed(linY, dt, tNanos, still)
        val horizontal = principalProjection(hx, hy)
        val medianDt = dt.average().toFloat().coerceAtLeast(0.002f)
        val win = ((0.12f / medianDt).toInt()).coerceIn(5, 51)

        val primary = when (motion) {
            PrimaryMotion.VERTICAL_VS_GRAVITY -> vertical
            PrimaryMotion.HORIZONTAL_IN_BAR_FRAME -> filtfilt(horizontal, win)
            PrimaryMotion.PITCH_ABOUT_ELBOW -> smooth(attitude.pitch, (win / 2).coerceAtLeast(3))
        }

        return PreparedSignals(
            tNanos = tNanos,
            dt = dt,
            primary = primary,
            verticalAccel = linUp,
            tiltX = attitude.roll,
            tiltZ = attitude.pitch,
            roll = attitude.roll,
            yaw = attitude.yaw,
            still = still,
            usedRotationVector = attitude.usedRotationVector,
            usedGyro = attitude.usedGyro,
        )
    }

    fun segmentCycles(
        primary: FloatArray,
        tNanos: LongArray,
        profile: ExerciseProfile,
    ): List<RawCycle> {
        val wantStartPeak = profile.cycleShape == CycleShape.HIGH_LOW_HIGH
        val ext = completeExtrema(extrema(primary, tNanos), primary, tNanos, wantStartPeak)
        if (ext.isEmpty()) return emptyList()

        val boundaries = ext.filter { it.isPeak == wantStartPeak }
        val mids = ext.filter { it.isPeak != wantStartPeak }

        val cycles = ArrayList<RawCycle>()
        for (i in 0 until boundaries.size - 1) {
            val a = boundaries[i]
            val b = boundaries[i + 1]
            val mid = mids
                .filter { it.index > a.index && it.index < b.index }
                .maxByOrNull { abs(it.value - a.value) }
                ?: continue
            val amp = (abs(a.value - mid.value) + abs(b.value - mid.value)) / 2f
            cycles += RawCycle(
                startIndex = a.index,
                extremumIndex = mid.index,
                endIndex = b.index,
                tStartNanos = a.tNanos,
                tMidNanos = mid.tNanos,
                tEndNanos = b.tNanos,
                amplitude = amp,
                truncated = false,
            )
        }

        val lastBoundary = boundaries.lastOrNull()
        if (lastBoundary != null) {
            val trailingMid = mids
                .filter { it.index > lastBoundary.index }
                .maxByOrNull { abs(it.value - lastBoundary.value) }
            if (trailingMid != null) {
                val returnTravel = abs(primary.last() - trailingMid.value)
                val downTravel = abs(trailingMid.value - lastBoundary.value)
                if (downTravel >= profile.minAmplitude * 0.5f &&
                    returnTravel < profile.minAmplitude * 0.5f
                ) {
                    cycles += RawCycle(
                        startIndex = lastBoundary.index,
                        extremumIndex = trailingMid.index,
                        endIndex = primary.lastIndex,
                        tStartNanos = lastBoundary.tNanos,
                        tMidNanos = trailingMid.tNanos,
                        tEndNanos = tNanos.last(),
                        amplitude = downTravel,
                        truncated = true,
                    )
                }
            }
        }
        return cycles
    }

    private fun completeExtrema(
        raw: List<Extremum>,
        primary: FloatArray,
        tNanos: LongArray,
        wantStartPeak: Boolean,
    ): List<Extremum> {
        if (raw.isEmpty()) return raw
        val out = ArrayList<Extremum>(raw.size + 2)
        if (raw.first().isPeak != wantStartPeak) {
            out += Extremum(0, tNanos[0], primary[0], isPeak = wantStartPeak)
        }
        out += raw
        val last = out.last()
        if (last.isPeak != wantStartPeak) {
            val lastBound = out.lastOrNull { it.isPeak == wantStartPeak }
            val endVal = primary.last()
            val returned = lastBound != null &&
                abs(endVal - lastBound.value) <= abs(endVal - last.value) * 0.6f
            if (returned) {
                out += Extremum(
                    primary.lastIndex,
                    tNanos.last(),
                    endVal,
                    isPeak = wantStartPeak,
                )
            }
        }
        return out
    }

    private fun stillNodes(still: BooleanArray, tNanos: LongArray): IntArray {
        val nodes = ArrayList<Int>()
        var i = 0
        val minNs = 150_000_000L
        while (i < still.size) {
            if (!still[i]) {
                i++
                continue
            }
            var j = i
            while (j < still.size && still[j]) j++
            val dur = tNanos[(j - 1).coerceAtLeast(0)] - tNanos[i]
            if (dur >= minNs && (j - i) >= 6) {
                nodes += (i + j - 1) / 2
            }
            i = j
        }
        return nodes.toIntArray()
    }

    private fun rollingRms(values: FloatArray, window: Int): FloatArray {
        val out = FloatArray(values.size)
        val half = window / 2
        for (i in values.indices) {
            val from = (i - half).coerceAtLeast(0)
            val to = (i + half).coerceAtMost(values.lastIndex)
            var acc = 0f
            val count = to - from + 1
            for (j in from..to) acc += values[j] * values[j]
            out[i] = sqrt(acc / count)
        }
        return out
    }

}
