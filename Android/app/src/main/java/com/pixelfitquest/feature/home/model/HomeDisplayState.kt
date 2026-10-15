package com.pixelfitquest.feature.home.model

import com.pixelfitquest.feature.workout.model.Workout
import com.pixelfitquest.feature.workout.model.enums.ExerciseType
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlan
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlanItem
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutTemplate
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

data class WeeklyStats(
    val workoutsCount: Int = 0,
    val setsCount: Int = 0,
    val kmRun: Float? = null,
)

data class HomeDisplayState(
    val nextUpTemplate: WorkoutTemplate = DEFAULT_TEMPLATE,
    val lastDoneFormatted: String = "No recent workouts",
    val weeklyStats: WeeklyStats = WeeklyStats(),
) {
    companion object {
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
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)

    /**
     * Aggregates completed workouts and sets for the calendar week containing [referenceDate].
     * Calendar week starts on Monday and ends on Sunday.
     */
    fun computeWeeklyStats(
        workouts: List<Workout>,
        referenceDate: LocalDate = LocalDate.now(),
    ): WeeklyStats {
        val startOfWeek = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endOfWeek = referenceDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        var workoutCount = 0
        var setCount = 0

        for (workout in workouts) {
            val workoutDate = parseWorkoutDate(workout.date) ?: continue
            if (!workoutDate.isBefore(startOfWeek) && !workoutDate.isAfter(endOfWeek)) {
                workoutCount++
                setCount += workout.totalSets
            }
        }

        return WeeklyStats(
            workoutsCount = workoutCount,
            setsCount = setCount,
            kmRun = null, // Distance from Health Connect deferred to H3
        )
    }

    /**
     * Resolves the next-up workout template.
     * Returns the first user template if available, otherwise falls back to [HomeDisplayState.DEFAULT_TEMPLATE].
     */
    fun resolveNextUpTemplate(
        templates: List<WorkoutTemplate>,
        fallback: WorkoutTemplate = HomeDisplayState.DEFAULT_TEMPLATE,
    ): WorkoutTemplate {
        return templates.firstOrNull() ?: fallback
    }

    /**
     * Formats relative or weekday time when the template or last workout was completed.
     */
    fun formatLastDone(
        workouts: List<Workout>,
        templateName: String,
        referenceDate: LocalDate = LocalDate.now(),
    ): String {
        val matchingWorkout = workouts.firstOrNull {
            it.name.equals(templateName, ignoreCase = true)
        } ?: workouts.firstOrNull() ?: return "No recent workouts"

        val workoutDate = parseWorkoutDate(matchingWorkout.date) ?: return "No recent workouts"
        val dayDiff = ChronoUnit.DAYS.between(workoutDate, referenceDate)

        val durationMinutes = (matchingWorkout.totalDurationMillis / 60000L).coerceAtLeast(1)

        val dayName = when {
            dayDiff == 0L -> "Today"
            dayDiff == 1L -> "Yesterday"
            dayDiff in 2L..6L -> workoutDate.dayOfWeek.name.take(3).lowercase()
                .replaceFirstChar { it.uppercase() }
            dayDiff >= 7L -> "${dayDiff}d ago"
            else -> "Today"
        }

        return "Last done $dayName, ${durationMinutes} min"
    }

    fun parseWorkoutDate(dateStr: String): LocalDate? {
        if (dateStr.isBlank()) return null
        return try {
            val datePrefix = if (dateStr.length >= 10) dateStr.substring(0, 10) else dateStr
            LocalDate.parse(datePrefix, DATE_FORMATTER)
        } catch (_: Exception) {
            null
        }
    }
}
