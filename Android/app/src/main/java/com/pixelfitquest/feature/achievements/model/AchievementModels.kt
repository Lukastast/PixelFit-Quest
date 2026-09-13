package com.pixelfitquest.feature.achievements.model

enum class AchievementCategory {
    WORKOUTS,
    STREAK,
    STEPS,
    VOLUME,
    MILESTONES,
}

enum class AchievementTier {
    BRONZE,
    SILVER,
    GOLD,
    PLATINUM,
}

enum class AchievementMetric {
    WORKOUTS_COMPLETED,
    CURRENT_STREAK,
    LIFETIME_STEPS,
    TOTAL_VOLUME_KG,
    UNIQUE_EXERCISES,
    SETS_COMPLETED,
    LEVEL_REACHED,
}

data class AchievementReward(
    val coins: Int = 0,
    val xp: Int = 0,
) {
    val hasValue: Boolean get() = coins > 0 || xp > 0
}

data class AchievementDefinition(
    val id: String,
    val name: String,
    val description: String,
    val category: AchievementCategory,
    val tier: AchievementTier,
    val metric: AchievementMetric,
    val threshold: Long,
    val reward: AchievementReward,
)

data class AchievementProgress(
    val achievementId: String,
    val currentValue: Long = 0L,
    val unlockedAtEpochMs: Long? = null,
    val rewardGranted: Boolean = false,
) {
    val isUnlocked: Boolean get() = unlockedAtEpochMs != null
}

data class AchievementItem(
    val definition: AchievementDefinition,
    val progress: AchievementProgress,
) {
    val isUnlocked: Boolean get() = progress.isUnlocked

    val progressFraction: Float
        get() {
            if (isUnlocked) return 1f
            val threshold = definition.threshold.toFloat().coerceAtLeast(1f)
            return (progress.currentValue.toFloat() / threshold).coerceIn(0f, 1f)
        }
}

data class LocalFitnessSnapshot(
    val workoutsCompleted: Int = 0,
    val currentStreak: Int = 0,
    val lifetimeSteps: Long = 0L,
    val totalVolumeKg: Long = 0L,
    val uniqueExercises: Int = 0,
    val setsCompleted: Int = 0,
    val level: Int = 1,
) {
    fun valueOf(metric: AchievementMetric): Long = when (metric) {
        AchievementMetric.WORKOUTS_COMPLETED -> workoutsCompleted.toLong()
        AchievementMetric.CURRENT_STREAK -> currentStreak.toLong()
        AchievementMetric.LIFETIME_STEPS -> lifetimeSteps
        AchievementMetric.TOTAL_VOLUME_KG -> totalVolumeKg
        AchievementMetric.UNIQUE_EXERCISES -> uniqueExercises.toLong()
        AchievementMetric.SETS_COMPLETED -> setsCompleted.toLong()
        AchievementMetric.LEVEL_REACHED -> level.toLong()
    }

    fun copyFor(metric: AchievementMetric, value: Long): LocalFitnessSnapshot {
        val safe = value.coerceAtLeast(0L)
        return when (metric) {
            AchievementMetric.WORKOUTS_COMPLETED -> copy(workoutsCompleted = safe.toInt())
            AchievementMetric.CURRENT_STREAK -> copy(currentStreak = safe.toInt())
            AchievementMetric.LIFETIME_STEPS -> copy(lifetimeSteps = safe)
            AchievementMetric.TOTAL_VOLUME_KG -> copy(totalVolumeKg = safe)
            AchievementMetric.UNIQUE_EXERCISES -> copy(uniqueExercises = safe.toInt())
            AchievementMetric.SETS_COMPLETED -> copy(setsCompleted = safe.toInt())
            AchievementMetric.LEVEL_REACHED -> copy(level = safe.toInt().coerceAtLeast(1))
        }
    }
}

data class AchievementEvaluation(
    val updated: List<AchievementProgress>,
    val newlyUnlockedIds: List<String>,
)
