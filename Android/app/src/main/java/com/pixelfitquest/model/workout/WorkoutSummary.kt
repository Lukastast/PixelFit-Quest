package com.pixelfitquest.model.workout

data class WorkoutSummary(
    val totalXp: Int,
    val totalCoins: Int,
    val avgScore: Float = 0f  // NEW: For UI display
)