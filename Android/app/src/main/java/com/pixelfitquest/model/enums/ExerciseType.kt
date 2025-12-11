package com.pixelfitquest.model.enums

enum class ExerciseType {
    BENCH_PRESS,
    SQUAT,
    BICEP_CURL,
    LAT_PULLDOWN,
    SEATED_ROWS,
    TRICEP_EXTENSION,
    ;

    val romFactor: Float
        get() = when (this) {
            BENCH_PRESS -> 0.28f
            SQUAT -> 0.53f
            BICEP_CURL -> 0.15f
            LAT_PULLDOWN -> 0.60f
            SEATED_ROWS -> 0.40f
            TRICEP_EXTENSION -> 0.18f
        }

    val type: String get() = name.lowercase().replace("_", "-")
}

fun ExerciseType.displayName(): String = this.name
    .replace("_", " ")
    .lowercase()
    .replaceFirstChar { it.uppercase() }
