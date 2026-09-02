package com.pixelfitquest.feature.workout.analysis

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
    val still: BooleanArray,
    val usedRotationVector: Boolean,
)

internal object Signal {
    fun dtSeconds(prevNanos: Long, nanos: Long): Float {
        val dt = (nanos - prevNanos) / 1_000_000_000f
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

    fun integrateZupt(
        accel: FloatArray,
        dt: FloatArray,
        still: BooleanArray,
    ): FloatArray {
        val n = accel.size
        val s = FloatArray(n)
        var v = 0f
        var pos = 0f
        for (i in 0 until n) {
            if (still[i]) {
                v = 0f
            } else {
                val a0 = if (i == 0) accel[i] else accel[i - 1]
                v += 0.5f * (a0 + accel[i]) * dt[i]
            }
            pos += v * dt[i]
            s[i] = pos
        }
        return s
    }

    fun prepare(samples: List<ImuSample>, motion: PrimaryMotion): PreparedSignals {
        val n = samples.size
        val tNanos = LongArray(n) { samples[it].tNanos }
        val dt = FloatArray(n)
        dt[0] = 0.02f
        for (i in 1 until n) dt[i] = dtSeconds(samples[i - 1].tNanos, samples[i].tNanos)

        val usedRv = samples.count { it.hasRotationVector } > n / 2
        val linUp = FloatArray(n)
        val linX = FloatArray(n)
        val linY = FloatArray(n)
        val tiltX = FloatArray(n)
        val tiltZ = FloatArray(n)
        val pitch = FloatArray(n)
        val still = BooleanArray(n)
        val accelMagDev = FloatArray(n)

        var gx = 0f
        var gy = 0f
        var gz = GRAVITY
        val gAlpha = 0.02f

        for (i in 0 until n) {
            val s = samples[i]
            val accel = floatArrayOf(s.ax, s.ay, s.az)
            val mag = sqrt(s.ax * s.ax + s.ay * s.ay + s.az * s.az)
            accelMagDev[i] = abs(mag - GRAVITY)

            val gyroMag = s.gyroMagnitude
            val linWorld: FloatArray
            if (s.hasRotationVector) {
                val r = rotationMatrix(s.qx!!, s.qy!!, s.qz!!, s.qw!!)
                val gDevice = transposeMul(r, floatArrayOf(0f, 0f, GRAVITY))
                val linDevice = floatArrayOf(
                    s.ax - gDevice[0],
                    s.ay - gDevice[1],
                    s.az - gDevice[2],
                )
                linWorld = mulMatVec(r, linDevice)
                tiltX[i] = atan2(gDevice[0], gDevice[2])
                tiltZ[i] = atan2(gDevice[1], gDevice[2])
                pitch[i] = atan2(-gDevice[0], gDevice[2])
            } else {
                if (s.hasGyro) {
                    val ox = s.gx!!
                    val oy = s.gy!!
                    val oz = s.gz!!
                    val cx = oy * gz - oz * gy
                    val cy = oz * gx - ox * gz
                    val cz = ox * gy - oy * gx
                    gx -= cx * dt[i]
                    gy -= cy * dt[i]
                    gz -= cz * dt[i]
                }
                gx = gx * (1f - gAlpha) + accel[0] * gAlpha
                gy = gy * (1f - gAlpha) + accel[1] * gAlpha
                gz = gz * (1f - gAlpha) + accel[2] * gAlpha
                val gMag = sqrt(gx * gx + gy * gy + gz * gz).coerceAtLeast(1e-3f)
                val ux = gx / gMag
                val uy = gy / gMag
                val uz = gz / gMag
                val linDevX = s.ax - gx
                val linDevY = s.ay - gy
                val linDevZ = s.az - gz
                val up = linDevX * ux + linDevY * uy + linDevZ * uz
                linWorld = floatArrayOf(linDevX, linDevY, up)
                tiltX[i] = atan2(gx, gz)
                tiltZ[i] = atan2(gy, gz)
                pitch[i] = atan2(-gx, gz)
            }

            linX[i] = linWorld[0]
            linY[i] = linWorld[1]
            linUp[i] = linWorld[2]

            val accelStill = accelMagDev[i] < 0.45f
            val gyroStill = !s.hasGyro || gyroMag < 0.22f
            still[i] = accelStill && gyroStill
        }

        val vertical = integrateZupt(linUp, dt, still)
        val hx = integrateZupt(linX, dt, still)
        val hy = integrateZupt(linY, dt, still)
        val horizontal = principalProjection(hx, hy)
        val smoothedPitch = smooth(pitch, 7)

        val primary = when (motion) {
            PrimaryMotion.VERTICAL_VS_GRAVITY -> smooth(vertical, 7)
            PrimaryMotion.HORIZONTAL_IN_BAR_FRAME -> smooth(horizontal, 7)
            PrimaryMotion.PITCH_ABOUT_ELBOW -> smoothedPitch
        }

        return PreparedSignals(
            tNanos = tNanos,
            dt = dt,
            primary = primary,
            verticalAccel = linUp,
            tiltX = tiltX,
            tiltZ = tiltZ,
            still = still,
            usedRotationVector = usedRv,
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
                if (downTravel >= profile.minAmplitude * 0.7f &&
                    returnTravel < profile.minAmplitude * 0.35f
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
}
