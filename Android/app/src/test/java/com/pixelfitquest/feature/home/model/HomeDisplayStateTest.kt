package com.pixelfitquest.feature.home.model

import com.pixelfitquest.feature.workout.model.Workout
import com.pixelfitquest.feature.workout.model.enums.ExerciseType
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlan
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlanItem
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutTemplate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class HomeDisplayStateTest {

    private val referenceDate = LocalDate.of(2026, 10, 15) // A Thursday

    @Test
    fun computeWeeklyStats_countsOnlyCurrentWeekWorkoutsAndSumsSets() {
        // Current week of 2026-10-15: Mon 2026-10-12 to Sun 2026-10-18
        val workouts = listOf(
            Workout(
                id = "w1",
                date = "2026-10-12", // Mon this week
                name = "Push Day",
                totalSets = 15,
                totalExercises = 3,
            ),
            Workout(
                id = "w2",
                date = "2026-10-14", // Wed this week
                name = "Pull Day",
                totalSets = 18,
                totalExercises = 4,
            ),
            Workout(
                id = "w3",
                date = "2026-10-05", // Mon last week - excluded
                name = "Leg Day",
                totalSets = 20,
                totalExercises = 4,
            ),
            Workout(
                id = "w4",
                date = "2026-10-19", // Mon next week - excluded
                name = "Push Day",
                totalSets = 12,
                totalExercises = 3,
            ),
        )

        val stats = HomeStatsCalculator.computeWeeklyStats(workouts, referenceDate)

        assertEquals(2, stats.workoutsCount)
        assertEquals(33, stats.setsCount)
        assertNull(stats.kmRun) // H1 leaves distance null
    }

    @Test
    fun computeWeeklyStats_emptyWorkouts_returnsZeros() {
        val stats = HomeStatsCalculator.computeWeeklyStats(emptyList(), referenceDate)
        assertEquals(0, stats.workoutsCount)
        assertEquals(0, stats.setsCount)
        assertNull(stats.kmRun)
    }

    @Test
    fun resolveNextUpTemplate_withTemplates_returnsFirst() {
        val template1 = WorkoutTemplate(
            id = "t1",
            name = "Heavy Bench",
            plan = WorkoutPlan(emptyList())
        )
        val template2 = WorkoutTemplate(
            id = "t2",
            name = "Squat Session",
            plan = WorkoutPlan(emptyList())
        )

        val result = HomeStatsCalculator.resolveNextUpTemplate(listOf(template1, template2))
        assertEquals("t1", result.id)
        assertEquals("Heavy Bench", result.name)
    }

    @Test
    fun resolveNextUpTemplate_emptyTemplates_returnsFallback() {
        val result = HomeStatsCalculator.resolveNextUpTemplate(emptyList())
        assertEquals(HomeDisplayState.DEFAULT_TEMPLATE.id, result.id)
        assertEquals("Push Day", result.name)
        assertEquals(3, result.plan.items.size)
    }

    @Test
    fun formatLastDone_matchingTemplate_formatsDayAndDuration() {
        val workouts = listOf(
            Workout(
                id = "w1",
                date = "2026-10-15", // Today
                name = "Push Day",
                totalDurationMillis = 52 * 60 * 1000L,
            ),
            Workout(
                id = "w2",
                date = "2026-10-13",
                name = "Pull Day",
                totalDurationMillis = 45 * 60 * 1000L,
            )
        )

        val result = HomeStatsCalculator.formatLastDone(workouts, "Push Day", referenceDate)
        assertEquals("Last done Today, 52 min", result)
    }

    @Test
    fun formatLastDone_pastDay_formatsWeekdayName() {
        val workouts = listOf(
            Workout(
                id = "w1",
                date = "2026-10-13", // Tuesday (2 days ago from Thursday 15th)
                name = "Push Day",
                totalDurationMillis = 48 * 60 * 1000L,
            )
        )

        val result = HomeStatsCalculator.formatLastDone(workouts, "Push Day", referenceDate)
        assertEquals("Last done Tue, 48 min", result)
    }

    @Test
    fun formatLastDone_emptyWorkouts_returnsNoRecentWorkouts() {
        val result = HomeStatsCalculator.formatLastDone(emptyList(), "Push Day", referenceDate)
        assertEquals("No recent workouts", result)
    }

    @Test
    fun parseWorkoutDate_handlesVariousFormats() {
        assertEquals(LocalDate.of(2026, 10, 15), HomeStatsCalculator.parseWorkoutDate("2026-10-15"))
        assertEquals(LocalDate.of(2026, 10, 15), HomeStatsCalculator.parseWorkoutDate("2026-10-15T14:30:00Z"))
        assertNull(HomeStatsCalculator.parseWorkoutDate(""))
        assertNull(HomeStatsCalculator.parseWorkoutDate("invalid-date"))
    }
}
