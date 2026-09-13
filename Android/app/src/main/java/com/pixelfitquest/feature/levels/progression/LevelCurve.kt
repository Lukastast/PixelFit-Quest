package com.pixelfitquest.feature.levels.progression

import com.pixelfitquest.feature.levels.model.LevelProgress

/**
 * Local XP curve. Matches the existing on-device formula used by
 * [com.pixelfitquest.firebase.repository.UserRepository]: cost to leave
 * level L is `100 * L`, max level 30.
 *
 * Phone/Room is the source of truth. This object has no Firebase.
 */
object LevelCurve {
    const val MAX_LEVEL = 30
    const val BASE_XP = 100

    fun xpToAdvance(fromLevel: Int): Int {
        val level = fromLevel.coerceIn(1, MAX_LEVEL)
        return BASE_XP * level
    }

    /** Total XP required to *reach* [level] (xp into that level is 0). */
    fun totalXpForLevel(level: Int): Int {
        val l = level.coerceIn(1, MAX_LEVEL)
        return BASE_XP * (l - 1) * l / 2
    }

    fun maxTotalXp(): Int = totalXpForLevel(MAX_LEVEL) + xpToAdvance(MAX_LEVEL)

    fun progressFromTotalXp(totalXp: Int): LevelProgress {
        val capped = totalXp.coerceIn(0, maxTotalXp())
        var level = 1
        var remaining = capped
        while (level < MAX_LEVEL) {
            val cost = xpToAdvance(level)
            if (remaining < cost) break
            remaining -= cost
            level++
        }
        val xpToNext = xpToAdvance(level)
        if (level >= MAX_LEVEL) {
            remaining = remaining.coerceAtMost(xpToNext)
        }
        return LevelProgress(
            totalXp = capped,
            level = level,
            xpIntoLevel = remaining,
            xpToNext = xpToNext,
        )
    }

    /**
     * Convert a remote (level, xp-into-level) pair into local total XP.
     * Used once to seed the phone store; not an ongoing sync.
     */
    fun totalXpFromRemote(level: Int, xpIntoLevel: Int): Int {
        val safeLevel = level.coerceIn(1, MAX_LEVEL)
        val into = xpIntoLevel.coerceAtLeast(0).coerceAtMost(xpToAdvance(safeLevel))
        return (totalXpForLevel(safeLevel) + into).coerceAtMost(maxTotalXp())
    }
}
