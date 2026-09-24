package com.pixelfitquest.feature.missions

import com.pixelfitquest.feature.workout.model.Workout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class WeeklyMissionEvaluatorTest {

    @Test
    fun catalogHasThreeTiersForEachMetric() {
        assertEquals(12, WeeklyMissions.catalog.size)
        assertEquals(12, WeeklyMissions.catalog.map { it.id }.distinct().size)
        MissionMetric.entries.forEach { metric ->
            val tiers = WeeklyMissions.catalog.filter { it.metric == metric }
            assertEquals(3, tiers.size)
            assertTrue(tiers.all { it.target > 0L && it.xp > 0 && it.coins > 0 })
        }
    }

    @Test
    fun boardIsStableAndAlwaysIncludesAnEasierWorkout() {
        val first = WeeklyMissions.boardForWeek("2026-W38").map { it.id }
        val second = WeeklyMissions.boardForWeek("2026-W38").map { it.id }
        assertEquals(first, second)
        assertEquals(5, first.distinct().size)

        val board = WeeklyMissions.boardForWeek("2026-W38")
        assertEquals(2, board.count { it.metric == MissionMetric.WORKOUTS })
        assertEquals(1, board.count { it.metric == MissionMetric.SETS })
        assertEquals(1, board.count { it.metric == MissionMetric.VOLUME_KG })
        assertEquals(1, board.count { it.metric == MissionMetric.WEEKLY_STEPS })
        assertTrue(board.any { it.metric == MissionMetric.WORKOUTS && it.target <= 4L })
    }

    @Test
    fun differentWeeksAreNotAllTheSameBoard() {
        val boards = (1..12).map { week ->
            WeeklyMissions.boardForWeek("2026-W%02d".format(week)).map { it.id }
        }
        assertTrue(boards.distinct().size > 1)
    }

    @Test
    fun statsCountOnlyWorkoutsInsideTheWeek() {
        val start = Instant.parse("2026-09-14T00:00:00Z")
        val end = Instant.parse("2026-09-20T12:00:00Z")
        val stats = WeeklyMissions.stats(
            workouts = listOf(
                workout("in", "2026-09-14T00:00:00Z", sets = 4, volume = 600f),
                workout("later", "2026-09-18T12:00:00Z", sets = 6, volume = 601f),
                workout("before", "2026-09-13T23:00:00Z", sets = 20, volume = 9_000f),
                workout("after", "2026-09-20T12:00:01Z", sets = 20, volume = 9_000f),
                workout("empty", "2026-09-16T08:00:00Z", sets = 8, volume = 400f, exercises = 0),
                workout("blank", "", sets = 8, volume = 400f),
                workout("day", "2026-09-15", sets = 2, volume = 50f),
            ),
            weeklySteps = 12_500,
            weekStart = start,
            weekEnd = end,
        )
        assertEquals(3L, stats.workouts)
        assertEquals(12L, stats.sets)
        assertEquals(1_251L, stats.volumeKg)
        assertEquals(12_500L, stats.weeklySteps)
    }

    @Test
    fun negativeStepsAndInvertedRangeDoNotInventTrainingProgress() {
        val stats = WeeklyMissions.stats(
            workouts = listOf(workout("in", "2026-09-18T12:00:00Z", sets = 4, volume = 100f)),
            weeklySteps = -5,
            weekStart = Instant.parse("2026-09-20T00:00:00Z"),
            weekEnd = Instant.parse("2026-09-14T00:00:00Z"),
        )
        assertEquals(0L, stats.workouts)
        assertEquals(0L, stats.sets)
        assertEquals(0L, stats.volumeKg)
        assertEquals(0L, stats.weeklySteps)
    }

    @Test
    fun evaluateUsesWeeklyStepsAndSkipsAlreadyClaimed() {
        val board = WeeklyMissions.evaluate(
            weekKey = "2026-W38",
            workouts = listOf(workout("in", "2026-09-18T12:00:00Z", sets = 40, volume = 6_000f)),
            weeklySteps = 40_000,
            weekStart = Instant.parse("2026-09-14T00:00:00Z"),
            weekEnd = Instant.parse("2026-09-20T12:00:00Z"),
            claimedIds = setOf("workouts_2"),
        )
        val workouts = board.missions.filter { it.definition.metric == MissionMetric.WORKOUTS }
        assertTrue(workouts.all { it.current == 1L })
        val easy = workouts.first { it.definition.id == "workouts_2" }
        assertFalse(easy.isComplete)
        assertTrue(easy.claimed)

        val steps = board.missions.first { it.definition.metric == MissionMetric.WEEKLY_STEPS }
        assertEquals(40_000L, steps.current)
        if (steps.definition.target <= 40_000L) {
            assertTrue(steps.isComplete)
        }
        val setsMission = board.missions.first { it.definition.metric == MissionMetric.SETS }
        assertEquals(
            (40f / setsMission.definition.target).coerceIn(0f, 1f),
            setsMission.fraction,
            0.001f,
        )

        val fresh = WeeklyMissions.newlyClaimable(board.missions)
        assertTrue(fresh.none { it.id == "workouts_2" })
        if (setsMission.isComplete) {
            assertTrue(fresh.any { it.id == setsMission.definition.id })
        }
    }
}

private fun workout(
    id: String,
    date: String,
    sets: Int,
    volume: Float,
    exercises: Int = 1,
) = Workout(
    id = id,
    date = date,
    name = "Test",
    totalExercises = exercises,
    totalSets = sets,
    totalVolume = volume,
)
