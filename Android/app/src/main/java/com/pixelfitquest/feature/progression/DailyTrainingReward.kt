package com.pixelfitquest.feature.progression

/**
 * Workout XP and coins stop after a full-body day: 30 reps in a set, 40 sets
 * in a local day. Logged reps and sets stay on the workout either way.
 */
object DailyTrainingReward {
    const val MAX_REPS_PER_SET = 30
    const val MAX_SETS_PER_DAY = 40

    fun clip(sets: List<RewardSet>, setsAlreadyRewarded: Int): DailyRewardClip {
        val start = setsAlreadyRewarded.coerceIn(0, MAX_SETS_PER_DAY)
        var used = start
        var rewardedReps = 0
        var xp = 0
        var perfectSetXp = 0
        var consumed = 0
        var clipped = false
        for (set in sets) {
            val reps = set.reps.coerceAtLeast(0)
            if (reps <= 0) continue
            if (reps > MAX_REPS_PER_SET) clipped = true
            if (used >= MAX_SETS_PER_DAY) {
                clipped = true
                continue
            }
            val paidReps = minOf(reps, MAX_REPS_PER_SET)
            val score = set.formScore.coerceIn(0f, 100f)
            val setXp = (paidReps * xpMultiplier(score)).toInt()
            xp += setXp
            if (score >= 90f) perfectSetXp += setXp
            rewardedReps += paidReps
            used += 1
            consumed += 1
        }
        return DailyRewardClip(
            xp = xp,
            coins = rewardedReps / 5,
            perfectSetXp = perfectSetXp,
            setsConsumed = consumed,
            clipped = clipped,
        )
    }

    fun xpMultiplier(formScore: Float): Double = when {
        formScore >= 90f -> 2.0
        formScore >= 80f -> 1.5
        else -> 1.0
    }
}

data class RewardSet(
    val reps: Int,
    val formScore: Float,
)

data class ClaimedWorkoutReward(
    val xp: Int,
    val coins: Int,
    val clipped: Boolean,
    val fresh: Boolean,
)

data class DailyRewardClip(
    val xp: Int,
    val coins: Int,
    val perfectSetXp: Int,
    val setsConsumed: Int,
    val clipped: Boolean,
)
