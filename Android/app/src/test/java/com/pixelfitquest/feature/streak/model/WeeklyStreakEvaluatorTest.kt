package com.pixelfitquest.feature.streak.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime

class WeeklyStreakEvaluatorTest {

    private val monday = LocalDate.of(2026, 1, 5)
    private val previousMonday = LocalDate.of(2025, 12, 29)

    @Test
    fun weekStartsMondayEvenOnSunday() {
        val sunday = ZonedDateTime.of(2026, 1, 4, 18, 0, 0, 0, ZoneOffset.UTC)
        val start = WeeklyStreakEvaluator.weekStartDate(sunday.toInstant().toEpochMilli(), ZoneOffset.UTC)
        assertEquals(DayOfWeek.MONDAY, start.dayOfWeek)
        assertEquals(LocalDate.of(2025, 12, 29), start)
    }

    @Test
    fun mondayIsItsOwnWeekStart() {
        val start = WeeklyStreakEvaluator.weekStartDate(
            monday.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            ZoneOffset.UTC,
        )
        assertEquals(monday, start)
    }

    @Test
    fun firstSessionsDoNotCompleteWeek() {
        val mutation = WeeklyStreakEvaluator.apply(WeeklyStreakState(targetSessionsPerWeek = 3), 2, monday)
        assertFalse(mutation.weekJustCompleted)
        assertEquals(0, mutation.state.currentStreakWeeks)
        assertEquals(0, mutation.xpAwarded)
        assertEquals(null, mutation.state.lastCompletedWeekStart)
    }

    @Test
    fun hittingTargetStartsStreakAndAwardsXp() {
        val mutation = WeeklyStreakEvaluator.apply(WeeklyStreakState(targetSessionsPerWeek = 3), 3, monday)
        assertTrue(mutation.weekJustCompleted)
        assertEquals(1, mutation.state.currentStreakWeeks)
        assertEquals(1, mutation.state.longestStreakWeeks)
        assertEquals(monday, mutation.state.lastCompletedWeekStart)
        assertEquals(WeeklyStreakRewards.xpForCompletedWeek(1), mutation.xpAwarded)
        assertEquals(mutation.xpAwarded, mutation.state.pendingXp)
    }

    @Test
    fun extraSessionsSameWeekDoNotReaward() {
        val completed = WeeklyStreakEvaluator.apply(WeeklyStreakState(targetSessionsPerWeek = 3), 3, monday).state
        val again = WeeklyStreakEvaluator.apply(completed, 4, monday)
        assertFalse(again.weekJustCompleted)
        assertEquals(1, again.state.currentStreakWeeks)
        assertEquals(0, again.xpAwarded)
        assertEquals(completed.pendingXp, again.state.pendingXp)
    }

    @Test
    fun consecutiveWeekIncrementsStreak() {
        val week1 = WeeklyStreakEvaluator.apply(WeeklyStreakState(targetSessionsPerWeek = 3), 3, previousMonday).state
        val week2 = WeeklyStreakEvaluator.apply(week1.copy(pendingXp = 0), 3, monday)
        assertTrue(week2.weekJustCompleted)
        assertEquals(2, week2.state.currentStreakWeeks)
        assertEquals(2, week2.state.longestStreakWeeks)
        assertEquals(WeeklyStreakRewards.xpForCompletedWeek(2), week2.xpAwarded)
    }

    @Test
    fun missedWeekResetsThenRestartIsOne() {
        val oldWeek = LocalDate.of(2025, 12, 15)
        val broken = WeeklyStreakState(
            targetSessionsPerWeek = 3,
            currentStreakWeeks = 5,
            longestStreakWeeks = 5,
            lastCompletedWeekStart = oldWeek,
        )
        val live = WeeklyStreakEvaluator.apply(broken, 1, monday)
        assertEquals(0, live.state.currentStreakWeeks)
        assertEquals(5, live.state.longestStreakWeeks)
        assertFalse(live.weekJustCompleted)

        val restart = WeeklyStreakEvaluator.apply(live.state, 3, monday)
        assertTrue(restart.weekJustCompleted)
        assertEquals(1, restart.state.currentStreakWeeks)
        assertEquals(5, restart.state.longestStreakWeeks)
    }

    @Test
    fun gracePeriodKeepsStreakWhileCurrentWeekInProgress() {
        val lastWeekDone = WeeklyStreakState(
            targetSessionsPerWeek = 3,
            currentStreakWeeks = 4,
            longestStreakWeeks = 4,
            lastCompletedWeekStart = previousMonday,
        )
        val during = WeeklyStreakEvaluator.apply(lastWeekDone, 1, monday)
        assertEquals(4, during.state.currentStreakWeeks)
        assertFalse(during.weekJustCompleted)
    }

    @Test
    fun loweringTargetMidWeekCanComplete() {
        val inProgress = WeeklyStreakState(targetSessionsPerWeek = 5)
        val afterLower = WeeklyStreakEvaluator.apply(
            inProgress.copy(targetSessionsPerWeek = 2),
            sessionsThisWeek = 2,
            nowWeekStart = monday,
        )
        assertTrue(afterLower.weekJustCompleted)
        assertEquals(1, afterLower.state.currentStreakWeeks)
    }

    @Test
    fun raisingTargetAfterCompleteDoesNotRevoke() {
        val done = WeeklyStreakEvaluator.apply(WeeklyStreakState(targetSessionsPerWeek = 2), 2, monday).state
        val raised = WeeklyStreakEvaluator.apply(done.copy(targetSessionsPerWeek = 5), 2, monday)
        assertFalse(raised.weekJustCompleted)
        assertEquals(1, raised.state.currentStreakWeeks)
        assertEquals(monday, raised.state.lastCompletedWeekStart)
    }

    @Test
    fun clampTargetBounds() {
        assertEquals(2, WeeklyStreakEvaluator.clampTarget(1))
        assertEquals(7, WeeklyStreakEvaluator.clampTarget(99))
        assertEquals(3, WeeklyStreakEvaluator.clampTarget(3))
    }

    @Test
    fun xpScalesWithStreakWeeks() {
        assertEquals(75, WeeklyStreakRewards.xpForCompletedWeek(1))
        assertEquals(100, WeeklyStreakRewards.xpForCompletedWeek(2))
        assertEquals(150, WeeklyStreakRewards.xpForCompletedWeek(4))
    }

    @Test
    fun skinHooksUnlockAtFourEightTwelve() {
        assertTrue(WeeklyStreakSkinHooks.unlockedIds(3).isEmpty())
        assertEquals(setOf(WeeklyStreakRewards.SKIN_EMBER), WeeklyStreakSkinHooks.unlockedIds(4))
        assertTrue(WeeklyStreakSkinHooks.isUnlocked(WeeklyStreakRewards.SKIN_PHOENIX, 8))
        assertFalse(WeeklyStreakSkinHooks.isUnlocked(WeeklyStreakRewards.SKIN_LEGEND, 8))
        assertEquals(
            setOf(
                WeeklyStreakRewards.SKIN_EMBER,
                WeeklyStreakRewards.SKIN_PHOENIX,
                WeeklyStreakRewards.SKIN_LEGEND,
            ),
            WeeklyStreakSkinHooks.unlockedIds(12),
        )
    }

    @Test
    fun completingFourthWeekUnlocksEmberHook() {
        val prior = WeeklyStreakState(
            targetSessionsPerWeek = 3,
            currentStreakWeeks = 3,
            longestStreakWeeks = 3,
            lastCompletedWeekStart = previousMonday,
        )
        val fourth = WeeklyStreakEvaluator.apply(prior, 3, monday)
        assertEquals(4, fourth.state.currentStreakWeeks)
        assertEquals(listOf(WeeklyStreakRewards.skins.first()), fourth.newlyUnlockedSkins)
        assertTrue(WeeklyStreakSkinHooks.isUnlocked(WeeklyStreakRewards.SKIN_EMBER, 4))
    }

    @Test
    fun snapshotShowsPreviewXpWhileWeekOpen() {
        val lastWeekDone = WeeklyStreakState(
            targetSessionsPerWeek = 3,
            currentStreakWeeks = 2,
            longestStreakWeeks = 2,
            lastCompletedWeekStart = previousMonday,
        )
        val snapshot = WeeklyStreakEvaluator.toSnapshot(lastWeekDone, 1, monday)
        assertEquals(2, snapshot.currentStreakWeeks)
        assertFalse(snapshot.weekGoalMet)
        assertEquals(WeeklyStreakRewards.xpForCompletedWeek(3), snapshot.xpForNextCompletion)
    }

    @Test
    fun snapshotMarksGoalMetAndZeroPreviewAfterComplete() {
        val done = WeeklyStreakEvaluator.apply(WeeklyStreakState(targetSessionsPerWeek = 3), 3, monday).state
        val snapshot = WeeklyStreakEvaluator.toSnapshot(done, 3, monday)
        assertTrue(snapshot.weekGoalMet)
        assertTrue(snapshot.lastXpAwardedThisWeek)
        assertEquals(0, snapshot.xpForNextCompletion)
    }
}
