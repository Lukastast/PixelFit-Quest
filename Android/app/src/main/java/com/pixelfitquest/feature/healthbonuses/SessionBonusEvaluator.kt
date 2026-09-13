package com.pixelfitquest.feature.healthbonuses

import com.pixelfitquest.feature.healthbonuses.model.BonusKind
import com.pixelfitquest.feature.healthbonuses.model.HealthSnapshot
import com.pixelfitquest.feature.healthbonuses.model.SessionBonus
import com.pixelfitquest.feature.healthbonuses.model.SessionBonusRules

/**
 * Pure mapping from on-device metrics to motivational session extras.
 * Thresholds are game rules, not medical cutoffs.
 */
object SessionBonusEvaluator {

    fun evaluate(
        snapshot: HealthSnapshot,
        claimedToday: Set<BonusKind> = emptySet(),
    ): List<SessionBonus> {
        val bonuses = ArrayList<SessionBonus>(4)
        if (BonusKind.GOOD_SLEEP !in claimedToday && qualifiesSleep(snapshot)) {
            bonuses += SessionBonus(
                kind = BonusKind.GOOD_SLEEP,
                xp = SessionBonusRules.SLEEP_XP,
                coins = SessionBonusRules.SLEEP_COINS,
            )
        }
        if (BonusKind.STEP_GOAL !in claimedToday && qualifiesSteps(snapshot)) {
            bonuses += SessionBonus(
                kind = BonusKind.STEP_GOAL,
                xp = SessionBonusRules.STEP_XP,
                coins = SessionBonusRules.STEP_COINS,
            )
        }
        if (BonusKind.RUNNING !in claimedToday && qualifiesRunning(snapshot)) {
            bonuses += SessionBonus(
                kind = BonusKind.RUNNING,
                xp = SessionBonusRules.RUN_XP,
                coins = SessionBonusRules.RUN_COINS,
            )
        }
        if (BonusKind.ENERGY !in claimedToday && qualifiesEnergy(snapshot)) {
            bonuses += SessionBonus(
                kind = BonusKind.ENERGY,
                xp = SessionBonusRules.ENERGY_XP,
                coins = SessionBonusRules.ENERGY_COINS,
            )
        }
        return bonuses
    }

    fun qualifiesSleep(snapshot: HealthSnapshot): Boolean {
        val minutes = snapshot.sleepMinutes
        if (minutes != null && minutes >= SessionBonusRules.SLEEP_MINUTES_MIN) return true
        val score = snapshot.sleepScore
        return score != null && score >= SessionBonusRules.SLEEP_SCORE_MIN
    }

    fun qualifiesSteps(snapshot: HealthSnapshot): Boolean {
        val goal = snapshot.effectiveStepGoal
        if (goal <= 0) return false
        return snapshot.steps >= goal.toLong()
    }

    fun qualifiesRunning(snapshot: HealthSnapshot): Boolean {
        if (snapshot.hadRunningSession) return true
        val distance = snapshot.distanceMeters ?: return false
        return distance >= SessionBonusRules.RUNNING_DISTANCE_METERS
    }

    fun qualifiesEnergy(snapshot: HealthSnapshot): Boolean {
        val score = snapshot.energyScore ?: return false
        return score >= SessionBonusRules.ENERGY_SCORE_MIN
    }
}
