package com.pixelfitquest.feature.home.model

import com.pixelfitquest.feature.workout.model.Workout
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlan
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutTemplate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

class HomeDisplayStateTest {

    private val cph = ZoneId.of("Europe/Copenhagen")
    private val today = LocalDate.of(2026, 10, 15) // Thursday; week is Mon 12 – Sun 18

    private fun workout(id: String, date: String, name: String = "Push Day", sets: Int = 10, minutes: Long = 45) =
        Workout(id = id, date = date, name = name, totalSets = sets, totalExercises = 3,
            totalDurationMillis = minutes * 60_000L)

    @Test
    fun weeklyStats_countsOnlyThisWeekAndSumsSets() {
        val workouts = listOf(
            workout("w1", "2026-10-12T08:00:00Z", sets = 15),
            workout("w2", "2026-10-14T17:00:00Z", sets = 18),
            workout("w3", "2026-10-05T08:00:00Z", sets = 20), // last week
            workout("w4", "2026-10-19T08:00:00Z", sets = 12), // next week
        )
        val stats = HomeStatsCalculator.computeWeeklyStats(workouts, today, cph)
        assertEquals(2, stats.workoutsCount)
        assertEquals(33, stats.setsCount)
        assertNull(stats.kmRun)
    }

    @Test
    fun weeklyStats_mondayJustAfterMidnightInCopenhagen_countsThisWeek() {
        // Monday 12 Oct 00:30 CEST is stored as Sunday 11 Oct 22:30Z.
        val workouts = listOf(workout("w1", "2026-10-11T22:30:00Z", sets = 9))
        val stats = HomeStatsCalculator.computeWeeklyStats(workouts, today, cph)
        assertEquals(1, stats.workoutsCount)
        assertEquals(9, stats.setsCount)
    }

    @Test
    fun weeklyStats_sundayLateLocal_countsLastWeek() {
        // Sunday 11 Oct 23:30 CEST = 21:30Z, belongs to last week.
        val stats = HomeStatsCalculator.computeWeeklyStats(listOf(workout("w1", "2026-10-11T21:30:00Z")), today, cph)
        assertEquals(0, stats.workoutsCount)
    }

    @Test
    fun weeklyStats_legacyPlainDate_isAccepted() {
        val stats = HomeStatsCalculator.computeWeeklyStats(listOf(workout("w1", "2026-10-12")), today, cph)
        assertEquals(1, stats.workoutsCount)
    }

    @Test
    fun weeklyStats_empty_returnsZeros() {
        val stats = HomeStatsCalculator.computeWeeklyStats(emptyList(), today, cph)
        assertEquals(0, stats.workoutsCount)
        assertEquals(0, stats.setsCount)
    }

    @Test
    fun resolveNextUpTemplate_returnsFirstOrFallback() {
        val t1 = WorkoutTemplate(id = "t1", name = "Heavy Bench", plan = WorkoutPlan(emptyList()))
        assertEquals("t1", HomeStatsCalculator.resolveNextUpTemplate(listOf(t1)).id)
        assertEquals(HomeDisplayState.DEFAULT_TEMPLATE.id, HomeStatsCalculator.resolveNextUpTemplate(emptyList()).id)
    }

    @Test
    fun lastDone_picksMostRecentMatchingByTimestamp_notListOrder() {
        val workouts = listOf(
            workout("old", "2026-10-08T10:00:00Z", minutes = 30),
            workout("new", "2026-10-13T10:00:00Z", minutes = 48),
            workout("legs", "2026-10-15T10:00:00Z", name = "Leg Day", minutes = 60),
        )
        val result = HomeStatsCalculator.lastDone(workouts, "push day", today, cph)
        assertEquals(LastDone.Done(LastDoneDay.Weekday(DayOfWeek.TUESDAY), 48), result)
    }

    @Test
    fun lastDone_templateNeverDone_isNone_evenWithOtherWorkouts() {
        val workouts = listOf(workout("legs", "2026-10-14T10:00:00Z", name = "Leg Day"))
        assertEquals(LastDone.None, HomeStatsCalculator.lastDone(workouts, "Push Day", today, cph))
    }

    @Test
    fun lastDone_todayAndYesterday_useLocalDate() {
        // 15 Oct 00:30 CEST = 14 Oct 22:30Z -> Today in Copenhagen.
        assertEquals(
            LastDone.Done(LastDoneDay.Today, 45),
            HomeStatsCalculator.lastDone(listOf(workout("a", "2026-10-14T22:30:00Z")), "Push Day", today, cph)
        )
        assertEquals(
            LastDone.Done(LastDoneDay.Yesterday, 45),
            HomeStatsCalculator.lastDone(listOf(workout("a", "2026-10-14T10:00:00Z")), "Push Day", today, cph)
        )
        assertEquals(
            LastDone.Done(LastDoneDay.DaysAgo(10), 45),
            HomeStatsCalculator.lastDone(listOf(workout("a", "2026-10-05T10:00:00Z")), "Push Day", today, cph)
        )
    }

    @Test
    fun localDate_handlesFormats() {
        assertEquals(LocalDate.of(2026, 10, 15), HomeStatsCalculator.localDate("2026-10-15", cph))
        assertEquals(LocalDate.of(2026, 10, 16), HomeStatsCalculator.localDate("2026-10-15T22:30:00Z", cph))
        assertNull(HomeStatsCalculator.localDate("", cph))
        assertNull(HomeStatsCalculator.localDate("invalid-date", cph))
    }
}
