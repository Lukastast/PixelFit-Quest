package com.pixelfitquest.model

data class BaseWorkoutState(
        val reps: Int = 0,
        val avgRepTime: Float = 0f,
        val estimatedROM: Float = 0f,
        val workoutScore: Float = 0f,
        val romScore: Float = 0f,
        val timingScore: Float = 0f,
        val tiltScore: Float = 0f,
    )
