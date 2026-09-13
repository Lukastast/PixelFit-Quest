package com.pixelfitquest.feature.healthbonuses.model

/**
 * On-device health metrics used only for motivational session extras.
 * Not a medical reading and not a diagnosis.
 *
 * Sleep / energy / distance / running stay null/0 until Health Connect
 * exposes them with existing read permissions — do not invent values.
 */
data class HealthSnapshot(
    val steps: Long = 0L,
    val stepGoal: Int = 0,
    val sleepMinutes: Int? = null,
    val sleepScore: Int? = null,
    val energyScore: Float? = null,
    val distanceMeters: Float? = null,
    val hadRunningSession: Boolean = false,
    val origin: HealthDataOrigin = HealthDataOrigin.NONE,
) {
    val effectiveStepGoal: Int
        get() = if (stepGoal > 0) stepGoal else SessionBonusRules.DEFAULT_STEP_GOAL

    fun hasAnyMetric(): Boolean {
        return steps > 0L ||
            sleepMinutes != null ||
            sleepScore != null ||
            energyScore != null ||
            (distanceMeters != null && distanceMeters > 0f) ||
            hadRunningSession
    }

    fun mergePreferringThis(other: HealthSnapshot): HealthSnapshot {
        return HealthSnapshot(
            steps = if (steps > 0L) steps else other.steps,
            stepGoal = if (stepGoal > 0) stepGoal else other.stepGoal,
            sleepMinutes = sleepMinutes ?: other.sleepMinutes,
            sleepScore = sleepScore ?: other.sleepScore,
            energyScore = energyScore ?: other.energyScore,
            distanceMeters = distanceMeters ?: other.distanceMeters,
            hadRunningSession = hadRunningSession || other.hadRunningSession,
            origin = when {
                hasAnyMetric() && other.hasAnyMetric() && origin != other.origin ->
                    HealthDataOrigin.MIXED
                hasAnyMetric() -> origin
                other.hasAnyMetric() -> other.origin
                origin != HealthDataOrigin.NONE -> origin
                else -> other.origin
            },
        )
    }

    companion object {
        val EMPTY = HealthSnapshot()
    }
}

enum class HealthDataOrigin {
    NONE,
    HEALTH_CONNECT,
    MIXED,
}
