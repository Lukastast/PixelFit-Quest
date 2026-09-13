package com.pixelfitquest.feature.streak.model

import java.time.LocalDate

const val MIN_WEEKLY_TARGET = 2
const val MAX_WEEKLY_TARGET = 7
const val DEFAULT_WEEKLY_TARGET = 3

data class WeeklyStreakState(
    val targetSessionsPerWeek: Int = DEFAULT_WEEKLY_TARGET,
    val currentStreakWeeks: Int = 0,
    val longestStreakWeeks: Int = 0,
    val lastCompletedWeekStart: LocalDate? = null,
    val lastXpAwardWeekStart: LocalDate? = null,
    val lastXpAwardAmount: Int = 0,
    val pendingXp: Int = 0,
)

data class WeeklyStreakSkin(
    val id: String,
    val requiredWeeks: Int,
    val displayName: String,
)

data class WeeklyStreakMutation(
    val state: WeeklyStreakState,
    val weekJustCompleted: Boolean,
    val xpAwarded: Int,
    val newlyUnlockedSkins: List<WeeklyStreakSkin>,
    val liveStreakBefore: Int,
)

data class WeeklyStreakSnapshot(
    val targetSessionsPerWeek: Int,
    val sessionsThisWeek: Int,
    val currentStreakWeeks: Int,
    val longestStreakWeeks: Int,
    val weekStart: LocalDate,
    val weekGoalMet: Boolean,
    val lastXpAwardAmount: Int,
    val lastXpAwardedThisWeek: Boolean,
    val pendingXp: Int,
    val unlockedSkins: List<WeeklyStreakSkin>,
    val nextSkin: WeeklyStreakSkin?,
    val xpForNextCompletion: Int,
) {
    val sessionsRemaining: Int
        get() = (targetSessionsPerWeek - sessionsThisWeek).coerceAtLeast(0)

    companion object {
        val Empty = WeeklyStreakSnapshot(
            targetSessionsPerWeek = DEFAULT_WEEKLY_TARGET,
            sessionsThisWeek = 0,
            currentStreakWeeks = 0,
            longestStreakWeeks = 0,
            weekStart = LocalDate.EPOCH,
            weekGoalMet = false,
            lastXpAwardAmount = 0,
            lastXpAwardedThisWeek = false,
            pendingXp = 0,
            unlockedSkins = emptyList(),
            nextSkin = WeeklyStreakRewards.skins.firstOrNull(),
            xpForNextCompletion = WeeklyStreakRewards.xpForCompletedWeek(1),
        )
    }
}

data class SessionRecordResult(
    val snapshot: WeeklyStreakSnapshot,
    val weekJustCompleted: Boolean,
    val xpAwarded: Int,
    val newlyUnlockedSkins: List<WeeklyStreakSkin>,
    val duplicateSession: Boolean,
)
