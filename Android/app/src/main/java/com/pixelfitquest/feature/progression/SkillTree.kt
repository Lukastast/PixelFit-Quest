package com.pixelfitquest.feature.progression

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class SkillBranch {
    FORM,
    IRON,
    VITALITY,
}

data class SkillLoadout(
    val form: Int = 0,
    val iron: Int = 0,
    val vitality: Int = 0,
    val lastRespecDate: String = "",
) {
    fun rank(branch: SkillBranch): Int = when (branch) {
        SkillBranch.FORM -> form
        SkillBranch.IRON -> iron
        SkillBranch.VITALITY -> vitality
    }

    fun spent(): Int = form + iron + vitality

    fun unspent(level: Int): Int = SkillTree.unspent(level, form, iron, vitality)
}

enum class RespecOutcome {
    RESET,
    COOLDOWN,
    CANT_AFFORD,
    NOTHING_SPENT,
}

/**
 * One point per level above 1. Each branch stops at [MAX_RANK].
 * Form boosts XP from sets scored 90 or better. Iron boosts workout coins.
 * Vitality boosts step and sleep coins.
 */
object SkillTree {
    const val MAX_RANK = 10
    const val RESPEC_COST = 150
    const val RESPEC_COOLDOWN_DAYS = 7

    fun totalPoints(level: Int): Int = (level - 1).coerceAtLeast(0)

    fun unspent(level: Int, form: Int, iron: Int, vitality: Int): Int {
        val spent = form.coerceAtLeast(0) + iron.coerceAtLeast(0) + vitality.coerceAtLeast(0)
        return (totalPoints(level) - spent).coerceAtLeast(0)
    }

    fun formPercent(rank: Int): Int = rank.coerceIn(0, MAX_RANK) * 3

    fun ironPercent(rank: Int): Int = rank.coerceIn(0, MAX_RANK) * 4

    fun vitalityPercent(rank: Int): Int = rank.coerceIn(0, MAX_RANK) * 5

    fun applyVitalityCoins(baseCoins: Int, rank: Int): Int {
        if (baseCoins <= 0) return 0
        val percent = vitalityPercent(rank)
        if (percent <= 0) return baseCoins
        return baseCoins + baseCoins * percent / 100
    }

    fun respecAllowed(today: LocalDate, lastRespecDate: String): Boolean {
        return daysUntilRespec(today, lastRespecDate) == 0
    }

    fun daysUntilRespec(today: LocalDate, lastRespecDate: String): Int {
        if (lastRespecDate.isBlank()) return 0
        val last = runCatching { LocalDate.parse(lastRespecDate) }.getOrNull() ?: return 0
        val ready = last.plusDays(RESPEC_COOLDOWN_DAYS.toLong())
        return ChronoUnit.DAYS.between(today, ready).toInt().coerceAtLeast(0)
    }
}
