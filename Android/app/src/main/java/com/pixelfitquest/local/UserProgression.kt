package com.pixelfitquest.local

data class ProgressionResult(val level: Int, val exp: Int)

data class StreakResult(val streak: Int, val lastActivityDate: String)

object UserProgression {
    fun applyExp(
        level: Int,
        exp: Int,
        amount: Int,
        maxLevel: Int,
        expRequiredForLevel: (Int) -> Int,
    ): ProgressionResult {
        if (amount <= 0) return ProgressionResult(level, exp)

        var currentLevel = level.coerceAtMost(maxLevel)
        var newExp = exp + amount
        while (true) {
            if (currentLevel >= maxLevel) {
                val maxLevelExp = expRequiredForLevel(maxLevel)
                newExp = newExp.coerceAtMost(maxLevelExp)
                break
            }
            val expRequiredForNext = expRequiredForLevel(currentLevel)
            if (newExp >= expRequiredForNext) {
                newExp -= expRequiredForNext
                currentLevel++
            } else {
                break
            }
        }
        currentLevel = currentLevel.coerceAtMost(maxLevel)
        return ProgressionResult(currentLevel, newExp)
    }

    fun applyStreak(
        currentStreak: Int,
        lastActivityDate: String,
        today: String,
        yesterday: String,
        increment: Boolean,
        reset: Boolean,
    ): StreakResult {
        var newStreak = currentStreak
        var newLastActivityDate = today

        if (reset) {
            newStreak = 0
        } else if (increment) {
            newStreak = when {
                lastActivityDate.isEmpty() -> 1
                lastActivityDate == today -> currentStreak
                lastActivityDate == yesterday -> currentStreak + 1
                else -> 1
            }
            newLastActivityDate = today
        }

        return StreakResult(newStreak, newLastActivityDate)
    }
}
