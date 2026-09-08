package com.pixelfitquest.feature.streak.model

/**
 * XP table and skin-id hooks for maintaining a weekly streak.
 *
 * Customization / cosmetics can call [unlockedSkinIds] or [isSkinUnlocked]
 * without depending on Room. Skin sprites themselves live outside this package.
 */
object WeeklyStreakRewards {
    const val XP_BASE_FOR_WEEK = 50
    const val XP_PER_STREAK_WEEK = 25

    const val SKIN_EMBER = "streak_ember"
    const val SKIN_PHOENIX = "streak_phoenix"
    const val SKIN_LEGEND = "streak_legend"

    val skins: List<WeeklyStreakSkin> = listOf(
        WeeklyStreakSkin(SKIN_EMBER, requiredWeeks = 4, displayName = "Ember outfit"),
        WeeklyStreakSkin(SKIN_PHOENIX, requiredWeeks = 8, displayName = "Phoenix outfit"),
        WeeklyStreakSkin(SKIN_LEGEND, requiredWeeks = 12, displayName = "Legend aura"),
    )

    fun xpForCompletedWeek(streakWeeks: Int): Int {
        val weeks = streakWeeks.coerceAtLeast(1)
        return XP_BASE_FOR_WEEK + XP_PER_STREAK_WEEK * weeks
    }

    fun unlocked(streakWeeks: Int): List<WeeklyStreakSkin> =
        skins.filter { streakWeeks >= it.requiredWeeks }

    fun nextUnlock(streakWeeks: Int): WeeklyStreakSkin? =
        skins.firstOrNull { streakWeeks < it.requiredWeeks }

    fun unlockedSkinIds(streakWeeks: Int): Set<String> =
        unlocked(streakWeeks).map { it.id }.toSet()

    fun isSkinUnlocked(skinId: String, streakWeeks: Int): Boolean =
        unlockedSkinIds(streakWeeks).contains(skinId)
}

object WeeklyStreakSkinHooks {
    fun unlockedIds(streakWeeks: Int): Set<String> =
        WeeklyStreakRewards.unlockedSkinIds(streakWeeks)

    fun isUnlocked(skinId: String, streakWeeks: Int): Boolean =
        WeeklyStreakRewards.isSkinUnlocked(skinId, streakWeeks)

    fun catalog(): List<WeeklyStreakSkin> = WeeklyStreakRewards.skins
}
