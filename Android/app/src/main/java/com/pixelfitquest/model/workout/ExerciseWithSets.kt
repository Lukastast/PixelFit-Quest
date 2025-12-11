package com.pixelfitquest.model.workout

data class ExerciseWithSets(
    val exercise: Exercise,
    val sets: List<WorkoutSet>,
    val avgWorkoutScore: Int
)