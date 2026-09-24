package com.pixelfitquest.feature.progression

import com.pixelfitquest.feature.home.model.DwellingTier

/**
 * Gear and home bonuses applied on top of a base payout.
 * Dwelling percents affect workout reps only. Gym Fit's flat bonus applies
 * to workout, mission, and health rewards.
 */
object RewardBonus {
    const val FITNESS_FLAT_XP = 2
    const val FITNESS_FLAT_COINS = 2

    fun isFitnessGear(variant: String?): Boolean = variant?.contains("fitness") == true

    fun workoutPayout(
        baseXp: Int,
        baseCoins: Int,
        dwelling: DwellingTier,
        variant: String?,
        perfectSetXp: Int = 0,
        formRank: Int = 0,
        ironRank: Int = 0,
    ): RewardPayout {
        val safePerfect = perfectSetXp.coerceIn(0, baseXp.coerceAtLeast(0))
        val xp = percentBonus(baseXp, dwelling.xpBonusPercent) +
            percentOf(safePerfect, SkillTree.formPercent(formRank))
        val coins = percentBonus(baseCoins, dwelling.coinBonusPercent) +
            percentOf(baseCoins, SkillTree.ironPercent(ironRank))
        val withGear = withFitnessFlat(xp, coins, variant)
        val homeApplied = xp != baseXp.coerceAtLeast(0) || coins != baseCoins.coerceAtLeast(0)
        return withGear.copy(homeApplied = homeApplied)
    }

    fun withFitnessFlat(baseXp: Int, baseCoins: Int, variant: String?): RewardPayout {
        val gear = isFitnessGear(variant)
        val xp = baseXp.coerceAtLeast(0) + if (gear && baseXp > 0) FITNESS_FLAT_XP else 0
        val coins = baseCoins.coerceAtLeast(0) + if (gear && baseCoins > 0) FITNESS_FLAT_COINS else 0
        return RewardPayout(
            xp = xp,
            coins = coins,
            gearApplied = gear && (baseXp > 0 || baseCoins > 0),
        )
    }

    private fun percentBonus(base: Int, percent: Int): Int {
        if (base <= 0) return 0
        return base + percentOf(base, percent)
    }

    private fun percentOf(base: Int, percent: Int): Int {
        if (base <= 0 || percent <= 0) return 0
        return base * percent / 100
    }
}

data class RewardPayout(
    val xp: Int,
    val coins: Int,
    val gearApplied: Boolean = false,
    val homeApplied: Boolean = false,
) {
    val bonusApplied: Boolean get() = gearApplied || homeApplied
}
