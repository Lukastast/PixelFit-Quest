package com.pixelfitquest.feature.workout.analysis

import com.pixelfitquest.feature.workout.sensor.ImuSample
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max

class SetAnalyzer @Inject constructor() {

    fun analyzeSet(
        samples: List<ImuSample>,
        profile: ExerciseProfile,
        user: AnalyzerUser,
    ): SetAnalysis {
        if (samples.size < 15) {
            return SetAnalysis(
                reps = emptyList(),
                meanFormScore = 0f,
                flags = listOf("too_few_samples"),
            )
        }

        val prepared = Signal.prepare(samples, profile.primaryMotion)
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

        val tiltSlice = cycle.startIndex..cycle.endIndex
        var tiltRms = 0f
        var tiltXMean = 0f
        var tiltZMean = 0f
        var n = 0
        for (i in tiltSlice) {
            tiltXMean += prepared.tiltX[i]
            tiltZMean += prepared.tiltZ[i]
            n++
        }
        if (n > 0) {
            tiltXMean /= n
            tiltZMean /= n
            var acc = 0f
            for (i in tiltSlice) {
                val dx = prepared.tiltX[i] - tiltXMean
                val dz = prepared.tiltZ[i] - tiltZMean
                acc += dx * dx + dz * dz
            }
            tiltRms = kotlin.math.sqrt(acc / n)
        }
        val pathDeviation = tiltRms
        val stabilityScore = (100f - tiltRms * 180f / Math.PI.toFloat() * 3f).coerceIn(0f, 100f)

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
}
