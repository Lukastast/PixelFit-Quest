package com.pixelfitquest.feature.workout.model

data class ExerciseWithSets(
    val exercise: Exercise,
    val sets: List<WorkoutSet>,
    val avgWorkoutScore: Int
)