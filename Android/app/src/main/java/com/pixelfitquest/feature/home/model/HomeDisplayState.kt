package com.pixelfitquest.feature.home.model

import com.pixelfitquest.feature.workout.model.Workout
import com.pixelfitquest.feature.workout.model.enums.ExerciseType
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlan
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlanItem
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutTemplate
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

data class WeeklyStats(
    val workoutsCount: Int = 0,
    val setsCount: Int = 0,
    val kmRun: Float? = null,
)

/** When the next-up workout was last done. Formatted with string resources in the UI. */
sealed interface LastDone {
    data object None : LastDone

    data class Done(
        val day: LastDoneDay,
        val durationMinutes: Long,
    ) : LastDone
}

sealed interface LastDoneDay {
    data object Today : LastDoneDay
    data object Yesterday : LastDoneDay
    data class Weekday(val dayOfWeek: DayOfWeek) : LastDoneDay
    data class DaysAgo(val days: Long) : LastDoneDay
}

data class HomeDisplayState(
    val nextUpTemplate: WorkoutTemplate = DEFAULT_TEMPLATE,
    val lastDone: LastDone = LastDone.None,
    val weeklyStats: WeeklyStats = WeeklyStats(),
) {
    companion object {
        // Starter template shown before the user saves one; exercise names are localized elsewhere.
        val DEFAULT_TEMPLATE = WorkoutTemplate(
            id = "starter_push_day",
            name = "Push Day",
            plan = WorkoutPlan(
                items = listOf(
                    WorkoutPlanItem(exercise = ExerciseType.BENCH_PRESS, sets = 3, weight = 60f),
                    WorkoutPlanItem(exercise = ExerciseType.OVERHEAD_PRESS, sets = 3, weight = 40f),
                    WorkoutPlanItem(exercise = ExerciseType.INCLINE_DUMBBELL_PRESS, sets = 3, weight = 22f),
                )
            )
        )
    }
}

object HomeStatsCalculator {

    /**
     * Aggregates completed workouts and sets for the Monday–Sunday week containing [today].
     * Stored timestamps are UTC instants, so each one is converted to [zone] before comparing.
     */
    fun computeWeeklyStats(
        workouts: List<Workout>,
        today: LocalDate,
        zone: ZoneId,
    ): WeeklyStats {
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        var workoutCount = 0
        var setCount = 0
        for (workout in workouts) {
            val date = localDate(workout.date, zone) ?: continue
            if (!date.isBefore(startOfWeek) && !date.isAfter(endOfWeek)) {
                workoutCount++
                setCount += workout.totalSets
            }
        }
        // Distance from Health Connect is deferred to H3.
        return WeeklyStats(workoutsCount = workoutCount, setsCount = setCount, kmRun = null)
    }

    /**
     * Next-up template: the first template the repository returns, else the starter template.
     * This is not a rotation yet.
     */
    fun resolveNextUpTemplate(
        templates: List<WorkoutTemplate>,
        fallback: WorkoutTemplate = HomeDisplayState.DEFAULT_TEMPLATE,
    ): WorkoutTemplate = templates.firstOrNull() ?: fallback

    /**
     * The most recent completed workout with the template's name, by timestamp.
     * Returns [LastDone.None] when that template has never been done; never falls back to another workout.
     */
    fun lastDone(
        workouts: List<Workout>,
        templateName: String,
        today: LocalDate,
        zone: ZoneId,
    ): LastDone {
        val latest = workouts
            .filter { it.name.equals(templateName, ignoreCase = true) }
            .mapNotNull { w -> instant(w.date, zone)?.let { w to it } }
            .maxByOrNull { it.second }
            ?: return LastDone.None
        val (workout, at) = latest
        val date = at.atZone(zone).toLocalDate()
        val dayDiff = ChronoUnit.DAYS.between(date, today)
        val day = when {
            dayDiff <= 0L -> LastDoneDay.Today
            dayDiff == 1L -> LastDoneDay.Yesterday
            dayDiff in 2L..6L -> LastDoneDay.Weekday(date.dayOfWeek)
            else -> LastDoneDay.DaysAgo(dayDiff)
        }
        val minutes = (workout.totalDurationMillis / 60_000L).coerceAtLeast(1)
        return LastDone.Done(day, minutes)
    }

    /** Local calendar date of a stored workout timestamp in [zone]. */
    fun localDate(dateStr: String, zone: ZoneId): LocalDate? = instant(dateStr, zone)?.atZone(zone)?.toLocalDate()

    /**
     * Parses a stored timestamp. New rows are ISO instants (`Instant.now().toString()`, UTC).
     * Legacy rows may be a plain `yyyy-MM-dd`; those are treated as local midnight in [zone].
     */
    internal fun instant(dateStr: String, zone: ZoneId): Instant? {
        if (dateStr.isBlank()) return null
        runCatching { return Instant.parse(dateStr) }
        runCatching { return OffsetDateTime.parse(dateStr).toInstant() }
        return runCatching { LocalDate.parse(dateStr).atStartOfDay(zone).toInstant() }.getOrNull()
    }
}
