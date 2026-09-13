package com.pixelfitquest.feature.healthbonuses.model

enum class BonusKind {
    GOOD_SLEEP,
    STEP_GOAL,
    RUNNING,
    ENERGY,
}

data class SessionBonus(
    val kind: BonusKind,
    val xp: Int,
    val coins: Int,
)

data class SessionBonusResolution(
    val workoutId: String,
    val snapshot: HealthSnapshot,
    val bonuses: List<SessionBonus>,
    val isNewAward: Boolean,
) {
    val bonusXp: Int get() = bonuses.sumOf { it.xp }
    val bonusCoins: Int get() = bonuses.sumOf { it.coins }
}

data class SessionBonusUiState(
    val loaded: Boolean = false,
    val snapshot: HealthSnapshot = HealthSnapshot.EMPTY,
    val bonuses: List<SessionBonus> = emptyList(),
)

object SessionBonusRules {
    const val DEFAULT_STEP_GOAL = 10_000
    const val SLEEP_MINUTES_MIN = 7 * 60
    const val SLEEP_SCORE_MIN = 70
    const val ENERGY_SCORE_MIN = 70f
    const val RUNNING_DISTANCE_METERS = 1_000f

    const val SLEEP_XP = 25
    const val SLEEP_COINS = 5
    const val STEP_XP = 20
    const val STEP_COINS = 5
    const val RUN_XP = 15
    const val RUN_COINS = 5
    const val ENERGY_XP = 10
    const val ENERGY_COINS = 3
}
