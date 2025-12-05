package com.pixelfitquest.model
import com.pixelfitquest.R
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val requiredWorkouts: Int,
    val lockedIcon: Int,
    val unlockedIcon: Int
)

val achievementsList = listOf(
    Achievement(
        id = "bronze_workout_1",
        name = "Rookie",
        description = "Complete 1 workout to unlock",
        requiredWorkouts = 1,
        lockedIcon = R.drawable.locked_achievement,
        unlockedIcon = R.drawable.achievement_bronze_workout_1_time
    ),
    Achievement(
        id = "silver_workout_10",
        name = "Veteran",
        description = "Complete 10 workouts to unlock",
        requiredWorkouts = 10,
        lockedIcon = R.drawable.locked_achievement,
        unlockedIcon = R.drawable.achievement_silver_workout_10_times
    ),
    Achievement(
        id = "gold_workout_50",
        name = "Legend",
        description = "Complete 50 workouts to unlock",
        requiredWorkouts = 50,
        lockedIcon = R.drawable.locked_achievement,
        unlockedIcon = R.drawable.achievement_gold_workout_50_times
    )

)