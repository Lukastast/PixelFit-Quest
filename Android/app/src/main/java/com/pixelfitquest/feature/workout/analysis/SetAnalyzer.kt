package com.pixelfitquest.feature.workout.analysis

import com.pixelfitquest.feature.workout.sensor.BarCalibration
import com.pixelfitquest.feature.workout.sensor.ImuSample
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

class SetAnalyzer @Inject constructor() {

    fun analyzeSet(
        samples: List<ImuSample>,
        profile: ExerciseProfile,
        user: AnalyzerUser,
        calibration: BarCalibration? = null,
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
        val typical = theoreticalAmplitude(profile, user)

        val reps = cycles.mapIndexed { index, cycle ->
            classifyAndScore(index, cycle, prepared, profile, typical)
        }.filter { it.accepted || "candidate" in it.tags || "truncated" in it.tags }
            .mapIndexed { index, rep -> rep.copy(index = index) }

        val accepted = reps.filter { it.accepted }
        val mean = if (accepted.isEmpty()) 0f else accepted.map { it.formScore }.average().toFloat()
        val flags = mutableListOf<String>()
        if (!prepared.usedRotationVector) flags += "no_rotation_vector"
        if (!prepared.usedGyro) flags += "no_gyro"
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

    private fun theoreticalAmplitude(profile: ExerciseProfile, user: AnalyzerUser): Float {
        if (profile.primaryMotion == PrimaryMotion.PITCH_ABOUT_ELBOW) {
            return profile.typicalAmplitude
        }
        val heightM = user.heightCm / 100f
        val fromHeight = heightM * profile.romFactor
        val armM = user.armLengthCm?.div(100f)
        return if (profile.usesArmLength && armM != null && armM > 0.1f) {
            max(fromHeight, armM * 0.85f)
        } else {
            fromHeight
        }
    }

    private fun classifyAndScore(
        index: Int,
        cycle: RawCycle,
        prepared: PreparedSignals,
        profile: ExerciseProfile,
        typical: Float,
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
        val fullAmp = cycle.amplitude >= profile.minAmplitude && !cycle.truncated && durationOk
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

        val romScore = if (typical > 1e-4f) {
            ((cycle.amplitude / typical) * 100f).coerceIn(0f, 100f)
        } else 0f

        val concentric = if (profile.eccentricFirst) {
            cycle.extremumIndex..cycle.endIndex
        } else {
            cycle.startIndex..cycle.extremumIndex
        }
        val (pathDeviation, stabilityScore) = stabilityDuring(
            prepared,
            concentric,
        )

        val ratio = eccentricMs.toFloat() / concentricMs.toFloat()
        val tempoScore = when {
            ratio in 0.6f..3.5f -> 90f
            ratio in 0.4f..5f -> 70f
            else -> 50f
        }.let { base ->
            if (durationOk) base else (base * 0.7f)
        }

        var peakBottomAccel = 0f
        val around = (cycle.extremumIndex - 2).coerceAtLeast(0)..
            (cycle.extremumIndex + 2).coerceAtMost(prepared.verticalAccel.lastIndex)
        for (i in around) {
            peakBottomAccel = max(peakBottomAccel, abs(prepared.verticalAccel[i]))
        }
        val bounced = firstMs < 80L || (peakBottomAccel > 6f && secondMs < 200L)

        val tags = mutableListOf<String>()
        if (cycle.truncated) tags += "truncated"
        if (romScore < 70f) tags += "short_rom"
        if (bounced && QualityMetric.BOTTOM_PAUSE in profile.quality) tags += "bounced"
        if (tempoScore < 65f) tags += "uneven_tempo"
        if (stabilityScore < 70f) tags += "bar_tilt"
        if (!accepted && candidate) tags += "candidate"

        val scores = mutableListOf<Float>()
        if (QualityMetric.ROM in profile.quality) scores += romScore
        if (QualityMetric.PATH_TILT in profile.quality) scores += stabilityScore
        if (QualityMetric.TEMPO in profile.quality) scores += tempoScore
        val formScore = if (scores.isEmpty()) romScore else scores.average().toFloat()

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
            stabilityScore = stabilityScore,
            tempoScore = tempoScore,
            formScore = formScore,
            tags = tags,
            confidence = confidence,
            accepted = accepted,
        )
    }

    private fun stabilityDuring(
        prepared: PreparedSignals,
        concentric: IntRange,
    ): Pair<Float, Float> {
        val start = concentric.first.coerceIn(0, prepared.roll.lastIndex)
        val end = concentric.last.coerceIn(0, prepared.roll.lastIndex)
        if (end < start) return 0f to 100f
        val refRoll = prepared.roll[0]
        val refYaw = prepared.yaw[0]
        var acc = 0.0
        var n = 0
        if (prepared.usedGyro || prepared.usedRotationVector) {
            for (i in start..end) {
                val dRoll = prepared.roll[i] - refRoll
                val dYaw = prepared.yaw[i] - refYaw
                acc += dRoll * dRoll + dYaw * dYaw
                n++
            }
            val rmsRad = if (n == 0) 0f else sqrt(acc / n).toFloat()
            val rmsDeg = rmsRad * 180f / Math.PI.toFloat()
            val score = (100f - 4f * rmsDeg).coerceIn(0f, 100f)
            return rmsRad to score
        }
        var tiltAcc = 0.0
        for (i in start..end) {
            val dx = prepared.tiltX[i] - prepared.tiltX[0]
            val dz = prepared.tiltZ[i] - prepared.tiltZ[0]
            tiltAcc += dx * dx + dz * dz
            n++
        }
        val rms = if (n == 0) 0f else sqrt(tiltAcc / n).toFloat()
        val score = (100f - rms * 180f / Math.PI.toFloat() * 3f).coerceIn(0f, 100f)
        return rms to score
    }
}
