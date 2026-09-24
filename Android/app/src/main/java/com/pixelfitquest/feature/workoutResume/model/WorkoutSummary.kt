package com.pixelfitquest.feature.workoutResume.model

data class WorkoutSummary(
    val totalXp: Int,
    val totalCoins: Int,
    val avgScore: Float = 0f,
    val rewardClipped: Boolean = false,
)