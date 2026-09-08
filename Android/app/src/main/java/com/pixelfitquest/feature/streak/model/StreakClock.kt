package com.pixelfitquest.feature.streak.model

import java.time.ZoneId

fun interface StreakClock {
    fun nowMillis(): Long

    fun zone(): ZoneId = ZoneId.systemDefault()
}

class SystemStreakClock : StreakClock {
    override fun nowMillis(): Long = System.currentTimeMillis()

    override fun zone(): ZoneId = ZoneId.systemDefault()
}
