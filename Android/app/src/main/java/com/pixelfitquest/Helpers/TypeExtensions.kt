package com.pixelfitquest.Helpers

import com.pixelfitquest.model.ExerciseType

fun ExerciseType.displayName(): String = this.name
    .replace("_", " ")
    .lowercase()
    .replaceFirstChar { it.uppercase() }