package com.pixelfitquest.feature.workout.analysis

import com.pixelfitquest.feature.workout.model.enums.ExerciseType

/**
 * How the start of a set relates to rep 1. Not a user-facing delay: setup time
 * is not fixed, and the post-set buffer is trimmed after the fact.
 */
enum class SetupFamily {
    /**
     * Floor pickup is rep 1. Dropping the leading cycle deletes the set.
     * Conventional and sumo deadlift. Rack pulls and other dead-stop pulls
     * are left on [OTHER] so this rule stays limited to floor deadlifts.
     */
    DEADLIFT,

    /** Walk-out is not a rep. */
    SQUAT,

    /** Unrack is not a rep. */
    BENCH,

    /** Lifting the bar out of the rack is not a rep. */
    CURL,

    /** No leading-cycle trim. Existing setup tagging still applies. */
    OTHER,
}

/**
 * Post-set trim of already segmented cycles. Never invents a cycle.
 *
 * [SetupFamily.DEADLIFT] keeps the first pull.
 * Squat, bench, and curl drop every cycle before the first full one.
 * A full cycle is closed, not flagged setup, and inside the profile's
 * existing amplitude and duration gates. A buffer with no full cycle
 * (empty, or setup only) stays empty for those families.
 */
internal object SetupTrim {
    fun family(profile: ExerciseProfile): SetupFamily = when (profile.id) {
        ExerciseType.DEADLIFT.type,
        ExerciseType.SUMO_DEADLIFT.type,
        -> SetupFamily.DEADLIFT

        ExerciseType.SQUAT.type,
        ExerciseType.FRONT_SQUAT.type,
        -> SetupFamily.SQUAT

        ExerciseType.BENCH_PRESS.type,
        ExerciseType.INCLINE_BENCH_PRESS.type,
        ExerciseType.DECLINE_BENCH_PRESS.type,
        ExerciseType.CLOSE_GRIP_BENCH_PRESS.type,
        ExerciseType.FLOOR_PRESS.type,
        -> SetupFamily.BENCH

        ExerciseType.BICEP_CURL.type,
        ExerciseType.EZ_BAR_CURL.type,
        ExerciseType.PREACHER_CURL.type,
        ExerciseType.REVERSE_CURL.type,
        -> SetupFamily.CURL

        else -> SetupFamily.OTHER
    }

    fun trim(cycles: List<RawCycle>, profile: ExerciseProfile): List<RawCycle> {
        if (cycles.isEmpty()) return emptyList()
        return when (family(profile)) {
            SetupFamily.DEADLIFT,
            SetupFamily.OTHER,
            -> cycles

            SetupFamily.SQUAT,
            SetupFamily.BENCH,
            SetupFamily.CURL,
            -> dropBeforeFirstFull(cycles, profile)
        }
    }

    private fun dropBeforeFirstFull(cycles: List<RawCycle>, profile: ExerciseProfile): List<RawCycle> {
        val start = cycles.indexOfFirst { it.isFullCycle(profile) }
        if (start < 0) return emptyList()
        if (start == 0) return cycles
        return cycles.subList(start, cycles.size)
    }
}

/** A closed rep, using the profile gates that already decide acceptance. */
internal fun RawCycle.isFullCycle(profile: ExerciseProfile): Boolean {
    if (truncated || setup) return false
    if (amplitude < profile.minAmplitude) return false
    val durationMs = (tEndNanos - tStartNanos) / 1_000_000L
    return durationMs in profile.minRepDurationMs..profile.maxRepDurationMs
}
