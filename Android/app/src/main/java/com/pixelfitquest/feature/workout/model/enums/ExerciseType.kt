package com.pixelfitquest.feature.workout.model.enums

enum class ExerciseType {
    BENCH_PRESS,
    SQUAT,
    BICEP_CURL,
    LAT_PULLDOWN,
    SEATED_ROWS,
    TRICEP_EXTENSION,
    ;

    /**
     * Percentage of user height that represents a "perfect" 100-score ROM.
     */
    val romFactor: Float
        get() = when (this) {
            BENCH_PRESS -> 0.25f
            SQUAT -> 0.45f
            BICEP_CURL -> 0.18f
            LAT_PULLDOWN -> 0.50f
            SEATED_ROWS -> 0.35f
            TRICEP_EXTENSION -> 0.20f
        }

    /**
     * The minimum vertical travel (in meters) required to even consider a movement a rep.
     * This prevents jitter/noise from being counted.
     */
    val minRomMeters: Float
        get() = when (this) {
            SQUAT -> 0.20f
            LAT_PULLDOWN -> 0.25f
            BENCH_PRESS -> 0.15f
            else -> 0.08f
        }

    /**
     * Velocity threshold for zero-crossing detection.
     * Larger movements (Squats) can have a higher threshold to ignore wobbling.
     */
    val velocityHysteresis: Float
        get() = when (this) {
            SQUAT -> 0.15f
            else -> 0.08f
        }

    val type: String get() = name.lowercase().replace("_", "-")
}

fun ExerciseType.displayName(): String = this.name
    .replace("_", " ")
    .lowercase()
    .replaceFirstChar { it.uppercase() }
