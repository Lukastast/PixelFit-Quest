package com.pixelfitquest.model

data class BaseWorkoutState(
        val isTracking: Boolean = false,
        val reps: Int = 0,
        val lastRepTime: Long = 0L,
        val avgRepTime: Float = 0f,
        val estimatedROM: Float = 0f,
        val downROM: Float = 0f,
        val upROM: Float = 0f,
        val workoutStartTime: Long = 0L,
        val verticalAccel: Float = 0f,
        val xAccel: Float = 0f,
        val yAccel: Float = 0f,
        val failedReps: Int = 0
    )
