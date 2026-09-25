package com.pixelfitquest.feature.levels.progression

import com.pixelfitquest.feature.levels.model.LevelProgress

/**
 * Local XP curve through level 100.
 *
 * Cost to leave level L is `88 + 12L + L²/50` (integer math). Level 1 still
 * costs 100 XP, about one workout. Later levels rise slowly enough that a
 * steady training week still moves the bar at level 90. Phone storage is the
 * source of truth; this object has no Firebase.
 */
object LevelCurve {
    const val MAX_LEVEL = 100

    private val costByLevel = IntArray(MAX_LEVEL + 1)
    private val totalToReach = IntArray(MAX_LEVEL + 1)

    init {
        var sum = 0
        for (level in 1..MAX_LEVEL) {
            costByLevel[level] = 88 + 12 * level + (level * level) / 50
            totalToReach[level] = sum
            sum += costByLevel[level]
        }
    }

    fun xpToAdvance(fromLevel: Int): Int = costByLevel[fromLevel.coerceIn(1, MAX_LEVEL)]

    /** Total XP required to *reach* [level] (xp into that level is 0). */
    fun totalXpForLevel(level: Int): Int = totalToReach[level.coerceIn(1, MAX_LEVEL)]

    fun maxTotalXp(): Int = totalXpForLevel(MAX_LEVEL) + xpToAdvance(MAX_LEVEL)

    /** Coins paid once, when the hero arrives on [level]. */
    fun coinsForReaching(level: Int): Int {
        if (level <= 1) return 0
        return 10 + (level / 5) * 5
    }

    fun coinsForLevels(previousLevel: Int, newLevel: Int): Int {
        if (newLevel <= previousLevel) return 0
        var sum = 0
        val last = newLevel.coerceAtMost(MAX_LEVEL)
        for (level in (previousLevel + 1)..last) {
            sum += coinsForReaching(level)
        }
        return sum
    }

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
     * Convert a stored (level, xp-into-level) pair into total XP.
     * Used to seed the phone store and to draw the bar. Not an ongoing sync.
     */
    fun totalXpFromRemote(level: Int, xpIntoLevel: Int): Int {
        val safeLevel = level.coerceIn(1, MAX_LEVEL)
        val into = xpIntoLevel.coerceAtLeast(0).coerceAtMost(xpToAdvance(safeLevel))
        return (totalXpForLevel(safeLevel) + into).coerceAtMost(maxTotalXp())
    }
}
