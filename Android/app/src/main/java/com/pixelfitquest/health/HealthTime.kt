package com.pixelfitquest.health

import java.time.Clock
import java.time.Instant
import java.time.LocalDate

object HealthTime {
    fun todayRange(clock: Clock = Clock.systemDefaultZone()): Pair<Instant, Instant> {
        val zone = clock.zone
        val today = LocalDate.now(clock)
        val start = today.atStartOfDay(zone).toInstant()
        val end = Instant.now(clock)
        return start to end
    }
}
