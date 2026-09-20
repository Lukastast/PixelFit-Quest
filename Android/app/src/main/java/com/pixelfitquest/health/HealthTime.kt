package com.pixelfitquest.health

import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.IsoFields
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object HealthTime {
    fun todayRange(clock: Clock = Clock.systemDefaultZone()): Pair<Instant, Instant> {
        val zone = clock.zone
        val today = LocalDate.now(clock)
        val start = today.atStartOfDay(zone).toInstant()
        val end = Instant.now(clock)
        return start to end
    }

    fun weekRange(clock: Clock = Clock.systemDefaultZone()): Pair<Instant, Instant> {
        val zone = clock.zone
        val today = LocalDate.now(clock)
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .atStartOfDay(zone).toInstant()
        val end = Instant.now(clock)
        return startOfWeek to end
    }

    fun rolling7DaysRange(clock: Clock = Clock.systemDefaultZone()): Pair<Instant, Instant> {
        val end = Instant.now(clock)
        val start = end.minus(java.time.Duration.ofDays(7))
        return start to end
    }

    fun currentWeekIso(clock: Clock = Clock.systemDefaultZone()): String {
        val today = LocalDate.now(clock)
        val year = today.get(IsoFields.WEEK_BASED_YEAR)
        val week = today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
        return String.format(Locale.US, "%d-W%02d", year, week)
    }
}
