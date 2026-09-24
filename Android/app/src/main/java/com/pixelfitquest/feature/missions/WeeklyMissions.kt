package com.pixelfitquest.feature.missions

import com.pixelfitquest.feature.workout.model.Workout
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale
import kotlin.math.round
import kotlin.random.Random

enum class MissionMetric {
    WORKOUTS,
    SETS,
    VOLUME_KG,
    WEEKLY_STEPS,
}

data class MissionDefinition(
    val id: String,
    val title: String,
    val metric: MissionMetric,
    val target: Long,
    val xp: Int,
    val coins: Int,
)

data class MissionProgress(
    val definition: MissionDefinition,
    val current: Long,
    val claimed: Boolean,
) {
    val isComplete: Boolean
        get() = definition.target > 0L && current >= definition.target

    val fraction: Float
        get() {
            val target = definition.target
            if (target <= 0L) return 0f
            return (current.toFloat() / target.toFloat()).coerceIn(0f, 1f)
        }

    /** Progress to show. A paid mission stays full even if this week's count later drops. */
    fun displayedCurrent(): Long {
        val target = definition.target.coerceAtLeast(0L)
        if (claimed || isComplete) return target
        return current.coerceAtLeast(0L).coerceAtMost(target)
    }
}

fun groupedCount(value: Long): String = "%,d".format(Locale.getDefault(), value)

data class WeeklyMissionStats(
    val workouts: Long = 0L,
    val sets: Long = 0L,
    val volumeKg: Long = 0L,
    val weeklySteps: Long = 0L,
) {
    fun valueOf(metric: MissionMetric): Long = when (metric) {
        MissionMetric.WORKOUTS -> workouts
        MissionMetric.SETS -> sets
        MissionMetric.VOLUME_KG -> volumeKg
        MissionMetric.WEEKLY_STEPS -> weeklySteps
    }
}

data class WeeklyMissionBoard(
    val weekKey: String,
    val missions: List<MissionProgress>,
) {
    val completedCount: Int get() = missions.count { it.isComplete }

    companion object {
        val EMPTY = WeeklyMissionBoard(weekKey = "", missions = emptyList())
    }
}

/**
 * Weekly board. Each ISO week gets one mission from each metric, plus a second
 * workout mission so the week always has a reachable training goal.
 * Rewards are fixed on the mission, not shuffled independently.
 */
object WeeklyMissions {
    val catalog: List<MissionDefinition> = listOf(
        mission("workouts_2", "Finish 2 workouts", MissionMetric.WORKOUTS, 2, xp = 80, coins = 10),
        mission("workouts_4", "Finish 4 workouts", MissionMetric.WORKOUTS, 4, xp = 150, coins = 25),
        mission("workouts_6", "Finish 6 workouts", MissionMetric.WORKOUTS, 6, xp = 250, coins = 40),
        mission("sets_12", "Log 12 sets", MissionMetric.SETS, 12, xp = 70, coins = 10),
        mission("sets_30", "Log 30 sets", MissionMetric.SETS, 30, xp = 140, coins = 20),
        mission("sets_50", "Log 50 sets", MissionMetric.SETS, 50, xp = 220, coins = 35),
        mission("volume_1000", "Lift 1,000 kg", MissionMetric.VOLUME_KG, 1_000, xp = 90, coins = 10),
        mission("volume_5000", "Lift 5,000 kg", MissionMetric.VOLUME_KG, 5_000, xp = 180, coins = 25),
        mission("volume_15000", "Lift 15,000 kg", MissionMetric.VOLUME_KG, 15_000, xp = 300, coins = 50),
        mission("steps_15000", "Walk 15,000 steps", MissionMetric.WEEKLY_STEPS, 15_000, xp = 80, coins = 10),
        mission("steps_35000", "Walk 35,000 steps", MissionMetric.WEEKLY_STEPS, 35_000, xp = 160, coins = 25),
        mission("steps_60000", "Walk 60,000 steps", MissionMetric.WEEKLY_STEPS, 60_000, xp = 260, coins = 40),
    )

    fun boardForWeek(weekKey: String): List<MissionDefinition> {
        val random = Random(weekSeed(weekKey))
        val byMetric = catalog.groupBy { it.metric }
            .mapValues { (_, definitions) -> definitions.sortedBy { it.target } }
        val picked = mutableListOf<MissionDefinition>()
        for (metric in MissionMetric.entries) {
            val tiers = byMetric[metric].orEmpty()
            if (tiers.isEmpty()) continue
            picked += tiers[random.nextInt(tiers.size)]
        }
        val bonus = byMetric[MissionMetric.WORKOUTS].orEmpty()
            .firstOrNull { candidate -> picked.none { it.id == candidate.id } }
        if (bonus != null) picked += bonus
        return picked
    }

    fun stats(
        workouts: List<Workout>,
        weeklySteps: Long,
        weekStart: Instant,
        weekEnd: Instant,
    ): WeeklyMissionStats {
        if (weekEnd.isBefore(weekStart)) {
            return WeeklyMissionStats(weeklySteps = weeklySteps.coerceAtLeast(0L))
        }
        var workoutCount = 0L
        var sets = 0L
        var volume = 0.0
        for (workout in workouts) {
            if (workout.totalExercises <= 0) continue
            val instant = parseWorkoutInstant(workout.date) ?: continue
            if (instant.isBefore(weekStart) || instant.isAfter(weekEnd)) continue
            workoutCount += 1
            sets += workout.totalSets.coerceAtLeast(0).toLong()
            val lifted = workout.totalVolume.toDouble()
            if (lifted > 0.0) volume += lifted
        }
        return WeeklyMissionStats(
            workouts = workoutCount,
            sets = sets,
            volumeKg = round(volume).toLong(),
            weeklySteps = weeklySteps.coerceAtLeast(0L),
        )
    }

    fun evaluate(
        weekKey: String,
        workouts: List<Workout>,
        weeklySteps: Long,
        weekStart: Instant,
        weekEnd: Instant,
        claimedIds: Set<String>,
    ): WeeklyMissionBoard {
        val totals = stats(workouts, weeklySteps, weekStart, weekEnd)
        val missions = boardForWeek(weekKey).map { definition ->
            MissionProgress(
                definition = definition,
                current = totals.valueOf(definition.metric),
                claimed = definition.id in claimedIds,
            )
        }
        return WeeklyMissionBoard(weekKey = weekKey, missions = missions)
    }

    fun newlyClaimable(missions: List<MissionProgress>): List<MissionDefinition> {
        return missions.filter { it.isComplete && !it.claimed }.map { it.definition }
    }
}

internal fun parseWorkoutInstant(raw: String): Instant? {
    val date = raw.trim()
    if (date.isEmpty()) return null
    runCatching { Instant.parse(date) }.getOrNull()?.let { return it }
    val day = date.take(10)
    return runCatching { LocalDate.parse(day).atStartOfDay(ZoneOffset.UTC).toInstant() }.getOrNull()
}

internal fun weekSeed(weekKey: String): Long {
    var hash = 1125899906842597L
    for (char in weekKey) {
        hash = 31L * hash + char.code
    }
    return hash
}

private fun mission(
    id: String,
    title: String,
    metric: MissionMetric,
    target: Long,
    xp: Int,
    coins: Int,
): MissionDefinition = MissionDefinition(
    id = id,
    title = title,
    metric = metric,
    target = target,
    xp = xp,
    coins = coins,
)
