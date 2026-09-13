package com.pixelfitquest.feature.streak.model

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import kotlin.math.max

object WeeklyStreakEvaluator {

    fun weekStartDate(epochMillis: Long, zone: ZoneId): LocalDate {
        return Instant.ofEpochMilli(epochMillis)
            .atZone(zone)
            .toLocalDate()
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    fun clampTarget(target: Int): Int = target.coerceIn(MIN_WEEKLY_TARGET, MAX_WEEKLY_TARGET)

    fun liveStreakWeeks(state: WeeklyStreakState, nowWeekStart: LocalDate): Int {
        val last = state.lastCompletedWeekStart ?: return 0
        val previous = nowWeekStart.minusWeeks(1)
        return if (last == nowWeekStart || last == previous) {
            state.currentStreakWeeks
        } else {
            0
        }
    }

    fun apply(
        state: WeeklyStreakState,
        sessionsThisWeek: Int,
        nowWeekStart: LocalDate,
    ): WeeklyStreakMutation {
        val target = clampTarget(state.targetSessionsPerWeek)
        val last = state.lastCompletedWeekStart
        val previousWeek = nowWeekStart.minusWeeks(1)
        val liveBefore = liveStreakWeeks(state, nowWeekStart)
        val alreadyCompletedThisWeek = last == nowWeekStart
        val met = sessionsThisWeek >= target

        if (met && !alreadyCompletedThisWeek) {
            val continued = last == previousWeek
            val newStreak = if (continued) liveBefore + 1 else 1
            val awardXp = state.lastXpAwardWeekStart != nowWeekStart
            val xp = if (awardXp) WeeklyStreakRewards.xpForCompletedWeek(newStreak) else 0
            val newlyUnlocked = WeeklyStreakRewards.unlocked(newStreak) -
                WeeklyStreakRewards.unlocked(liveBefore).toSet()
            val next = state.copy(
                targetSessionsPerWeek = target,
                currentStreakWeeks = newStreak,
                longestStreakWeeks = max(state.longestStreakWeeks, newStreak),
                lastCompletedWeekStart = nowWeekStart,
                lastXpAwardWeekStart = if (xp > 0) nowWeekStart else state.lastXpAwardWeekStart,
                lastXpAwardAmount = if (xp > 0) xp else state.lastXpAwardAmount,
                pendingXp = state.pendingXp + xp,
            )
            return WeeklyStreakMutation(
                state = next,
                weekJustCompleted = true,
                xpAwarded = xp,
                newlyUnlockedSkins = newlyUnlocked,
                liveStreakBefore = liveBefore,
            )
        }

        val next = state.copy(
            targetSessionsPerWeek = target,
            currentStreakWeeks = liveBefore,
            longestStreakWeeks = max(state.longestStreakWeeks, liveBefore),
        )
        return WeeklyStreakMutation(
            state = next,
            weekJustCompleted = false,
            xpAwarded = 0,
            newlyUnlockedSkins = emptyList(),
            liveStreakBefore = liveBefore,
        )
    }

    fun toSnapshot(
        state: WeeklyStreakState,
        sessionsThisWeek: Int,
        nowWeekStart: LocalDate,
    ): WeeklyStreakSnapshot {
        val mutation = apply(state, sessionsThisWeek, nowWeekStart)
        val live = mutation.state
        val goalMet = sessionsThisWeek >= live.targetSessionsPerWeek ||
            live.lastCompletedWeekStart == nowWeekStart
        val previewStreak = when {
            goalMet && live.lastCompletedWeekStart == nowWeekStart -> live.currentStreakWeeks
            live.lastCompletedWeekStart == nowWeekStart.minusWeeks(1) -> live.currentStreakWeeks + 1
            else -> 1
        }
        return WeeklyStreakSnapshot(
            targetSessionsPerWeek = live.targetSessionsPerWeek,
            sessionsThisWeek = sessionsThisWeek,
            currentStreakWeeks = live.currentStreakWeeks,
            longestStreakWeeks = live.longestStreakWeeks,
            weekStart = nowWeekStart,
            weekGoalMet = goalMet,
            lastXpAwardAmount = live.lastXpAwardAmount,
            lastXpAwardedThisWeek = live.lastXpAwardWeekStart == nowWeekStart,
            pendingXp = live.pendingXp,
            unlockedSkins = WeeklyStreakRewards.unlocked(live.currentStreakWeeks),
            nextSkin = WeeklyStreakRewards.nextUnlock(live.currentStreakWeeks),
            xpForNextCompletion = if (goalMet && live.lastCompletedWeekStart == nowWeekStart) {
                0
            } else {
                WeeklyStreakRewards.xpForCompletedWeek(previewStreak)
            },
        )
    }
}
