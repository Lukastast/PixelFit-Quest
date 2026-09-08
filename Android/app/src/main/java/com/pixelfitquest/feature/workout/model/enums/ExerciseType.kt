package com.pixelfitquest.feature.workout.model.enums

enum class ExerciseType {
    BENCH_PRESS,
    SQUAT,
    BICEP_CURL,
    LAT_PULLDOWN,
    SEATED_ROWS,
    TRICEP_EXTENSION,
    ;

    val type: String get() = name.lowercase().replace("_", "-")
}

fun ExerciseType.displayName(): String = this.name
    .replace("_", " ")
    .lowercase()
    .replaceFirstChar { it.uppercase() }
