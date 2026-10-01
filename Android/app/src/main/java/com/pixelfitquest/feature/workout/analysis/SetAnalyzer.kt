package com.pixelfitquest.feature.workout.analysis

import com.pixelfitquest.feature.workout.sensor.BarCalibration
import com.pixelfitquest.feature.workout.sensor.ImuSample
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.sqrt

const val TAG_CLIP_POSE = "clip_pose_unclear"

class SetAnalyzer @Inject constructor() {

    fun analyzeSet(
        samples: List<ImuSample>,
        profile: ExerciseProfile,
        calibration: BarCalibration? = null,
        fullRom: Float? = null,
    ): SetAnalysis {
        if (samples.size < 15) {
            return SetAnalysis(
                reps = emptyList(),
                meanFormScore = 0f,
                flags = listOf("too_few_samples"),
            )
        }

        val prepared = Signal.prepare(samples, profile.primaryMotion, calibration)
        val cycles = Signal.segmentCycles(prepared.primary, prepared.tNanos, profile)
        val clipOk = clipPoseOk(samples, prepared.still)
        val yawRef = openingStillMean(prepared.yaw, prepared.still)

        val reps = applyClusterGate(
            cycles.map { cycle ->
                classifyAndScore(0, cycle, prepared, profile, fullRom, clipOk, yawRef)
            },
            profile,
        ).filter { it.accepted || "candidate" in it.tags || "truncated" in it.tags || "setup" in it.tags }
            .mapIndexed { index, rep -> rep.copy(index = index) }

        val accepted = reps.filter { it.accepted }
        val mean = if (accepted.isEmpty()) 0f else accepted.map { it.formScore }.average().toFloat()
        val flags = mutableListOf<String>()
        if (!prepared.usedRotationVector) flags += "no_rotation_vector"
        if (!prepared.usedGyro) flags += "no_gyro"
        if (!clipOk) flags += TAG_CLIP_POSE
        if (accepted.size >= 4) {
            val first = accepted.take(accepted.size / 2).map { it.formScore }.average()
            val lastTwo = accepted.takeLast(2).map { it.formScore }.average()
            if (lastTwo < first - 15.0) flags += "last_reps_degraded"
        }

        return SetAnalysis(
            analysisVersion = ANALYSIS_VERSION,
            reps = reps,
            meanFormScore = mean,
            flags = flags,
            usedRotationVector = prepared.usedRotationVector,
        )
    }

    private fun classifyAndScore(
        index: Int,
        cycle: RawCycle,
        prepared: PreparedSignals,
        profile: ExerciseProfile,
        fullRom: Float?,
        clipOk: Boolean,
        yawRef: Float,
    ): DetectedRep {
        val durationMs = ((cycle.tEndNanos - cycle.tStartNanos) / 1_000_000L).coerceAtLeast(1L)
        val firstMs = ((cycle.tMidNanos - cycle.tStartNanos) / 1_000_000L).coerceAtLeast(1L)
        val secondMs = ((cycle.tEndNanos - cycle.tMidNanos) / 1_000_000L).coerceAtLeast(1L)
        val (eccentricMs, concentricMs) = if (profile.eccentricFirst) {
            firstMs to secondMs
        } else {
            secondMs to firstMs
        }

        val durationOk = durationMs in profile.minRepDurationMs..profile.maxRepDurationMs
        val fullAmp = cycle.amplitude >= profile.minAmplitude && !cycle.truncated && durationOk && !cycle.setup
        val nearAmp = cycle.amplitude >= profile.minAmplitude * 0.4f
        val accepted = fullAmp
        val candidate = !accepted && nearAmp
        val confidence = when {
            accepted && cycle.amplitude >= profile.minAmplitude * 1.2f -> 0.9f
            accepted -> 0.75f
            candidate && cycle.truncated -> 0.4f
            candidate -> 0.45f
            else -> 0.15f
        }

        val baseline = fullRom?.takeIf { it > 1e-4f }
        val romScore = if (baseline != null) {
            ((cycle.amplitude / baseline) * 100f).coerceIn(0f, 100f)
        } else {
            100f
        }

        val cycleRange = cycle.startIndex..cycle.endIndex
        val wantsLevel = clipOk &&
            profile.mount == Mount.BAR_SLEEVE &&
            QualityMetric.PATH_TILT in profile.quality
        val wantsTwist = wantsLevel &&
            profile.primaryMotion == PrimaryMotion.VERTICAL_VS_GRAVITY
        val headingOk = prepared.usedGyro || prepared.usedRotationVector
        val level = if (wantsLevel) {
            BarImbalance.scoreAxis(
                values = prepared.barLevel,
                range = cycleRange,
                ref = 0f,
                sign = BarImbalance.LATERAL_SIGN,
            )
        } else null
        val twist = if (wantsTwist && headingOk) {
            BarImbalance.scoreAxis(
                values = prepared.yaw,
                range = cycleRange,
                ref = yawRef,
                sign = BarImbalance.TWIST_SIGN,
            )
        } else null
        val barQuality = BarImbalance.combinedQuality(level?.quality, twist?.quality)
        val pathDeviation = listOfNotNull(level?.meanDeg, twist?.meanDeg)
            .map { abs(it) }
            .takeIf { it.isNotEmpty() }
            ?.average()
            ?.toFloat()
            ?: 0f

        val tempoScore = Tempo.score(eccentricMs, concentricMs, durationOk)

        val tags = mutableListOf<String>()
        if (cycle.truncated) tags += "truncated"
        if (cycle.setup) tags += "setup"
        if (baseline != null && romScore < 70f) tags += "short_rom"
        if (Tempo.dropped(eccentricMs)) tags += Tempo.TAG_DROPPED
        if (wantsLevel) {
            tags += BarImbalance.tags(
                levelDeg = level?.meanDeg,
                twistDeg = twist?.meanDeg,
                barQuality = barQuality,
            )
        }
        if (!accepted && (candidate || cycle.setup)) tags += "candidate"

        val includeTempo = QualityMetric.TEMPO in profile.quality
        val formScore = formScoreFrom(
            romScore = if (baseline != null) romScore else null,
            tempoScore = if (includeTempo) tempoScore else null,
            barQuality = barQuality,
        )

        return DetectedRep(
            index = index,
            tStartNanos = cycle.tStartNanos,
            tEndNanos = cycle.tEndNanos,
            durationMs = durationMs,
            romEstimate = cycle.amplitude,
            romUnit = profile.romUnit,
            concentricMs = concentricMs,
            eccentricMs = eccentricMs,
            pathDeviation = pathDeviation,
            romScore = romScore,
            stabilityScore = barQuality,
            tempoScore = tempoScore,
            levelDeg = level?.meanDeg,
            twistDeg = twist?.meanDeg,
            formScore = formScore,
            tags = tags,
            confidence = confidence,
            accepted = accepted,
        )
    }

    /**
     * Real reps in one set share a depth. Fragments much smaller than the deepest
     * reps (bar wobble, a bounce off the hooks) stay out of the accepted count.
     */
    private fun applyClusterGate(reps: List<DetectedRep>, profile: ExerciseProfile): List<DetectedRep> {
        val pool = reps.filter {
            "setup" !in it.tags && "truncated" !in it.tags && it.romEstimate > 1e-4f
        }
        if (pool.size < 3) return reps
        val top = pool.map { it.romEstimate }.sortedDescending().take(3)
        val reference = top.sorted()[top.size / 2]
        if (reference < profile.minAmplitude) return reps
        val keepFloor = reference * 0.62f
        val dropFloor = reference * 0.40f
        val promoteFloor = maxOf(profile.minAmplitude * 0.70f, reference * 0.55f)
        return reps.mapNotNull { rep ->
            if ("setup" in rep.tags || "truncated" in rep.tags || rep.isManual) return@mapNotNull rep
            val durationOk = rep.durationMs in profile.minRepDurationMs..profile.maxRepDurationMs
            when {
                rep.accepted && rep.romEstimate < keepFloor -> {
                    if (rep.romEstimate < dropFloor) null
                    else rep.copy(
                        accepted = false,
                        tags = (rep.tags + "candidate").distinct(),
                        confidence = 0.4f,
                    )
                }
                !rep.accepted && "candidate" in rep.tags && durationOk && rep.romEstimate >= promoteFloor ->
                    rep.copy(
                        accepted = true,
                        tags = rep.tags - "candidate",
                        confidence = 0.7f,
                    )
                !rep.accepted && "candidate" in rep.tags && rep.romEstimate < dropFloor -> null
                else -> rep
            }
        }
    }
}

internal fun openingStillMean(values: FloatArray, still: BooleanArray): Float {
    val range = openingStillRange(still) ?: return values.firstOrNull() ?: 0f
    var sum = 0.0
    var n = 0
    for (i in range) {
        if (i in values.indices) {
            sum += values[i]
            n++
        }
    }
    return if (n == 0) values.firstOrNull() ?: 0f else (sum / n).toFloat()
}

internal fun clipPoseOk(samples: List<ImuSample>, still: BooleanArray): Boolean {
    val range = openingStillRange(still) ?: (0 until minOf(10, samples.size))
    var ratioSum = 0.0
    var n = 0
    for (i in range) {
        if (i !in samples.indices) continue
        val s = samples[i]
        val mag = sqrt(s.ax * s.ax + s.ay * s.ay + s.az * s.az)
        if (mag < 1e-3f) continue
        ratioSum += abs(s.az) / mag
        n++
    }
    if (n == 0) return true
    return (ratioSum / n) >= 0.80
}

private fun openingStillRange(still: BooleanArray): IntRange? {
    var i = 0
    while (i < still.size && !still[i]) i++
    if (i >= still.size) return null
    val start = i
    while (i < still.size && still[i]) i++
    if (i - start < 3) return null
    return start until i
}
