package com.pixelfitquest.feature.healthbonuses

import com.pixelfitquest.feature.healthbonuses.model.BonusKind
import com.pixelfitquest.feature.healthbonuses.model.HealthSnapshot
import com.pixelfitquest.feature.healthbonuses.model.SessionBonusRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionBonusEvaluatorTest {

    @Test
    fun emptySnapshot_grantsNothing() {
        val bonuses = SessionBonusEvaluator.evaluate(HealthSnapshot.EMPTY)
        assertTrue(bonuses.isEmpty())
    }

    @Test
    fun sleepDurationAtThreshold_grantsSleepBonus() {
        val snapshot = HealthSnapshot(sleepMinutes = SessionBonusRules.SLEEP_MINUTES_MIN)
        val bonuses = SessionBonusEvaluator.evaluate(snapshot)
        assertEquals(listOf(BonusKind.GOOD_SLEEP), bonuses.map { it.kind })
        assertEquals(SessionBonusRules.SLEEP_XP, bonuses.single().xp)
        assertEquals(SessionBonusRules.SLEEP_COINS, bonuses.single().coins)
    }

    @Test
    fun sleepDurationJustBelow_noBonusUnlessScore() {
        val snapshot = HealthSnapshot(sleepMinutes = SessionBonusRules.SLEEP_MINUTES_MIN - 1)
        assertTrue(SessionBonusEvaluator.evaluate(snapshot).isEmpty())
    }

    @Test
    fun sleepScoreAtThreshold_grantsSleepBonus() {
        val snapshot = HealthSnapshot(sleepScore = SessionBonusRules.SLEEP_SCORE_MIN)
        assertEquals(
            listOf(BonusKind.GOOD_SLEEP),
            SessionBonusEvaluator.evaluate(snapshot).map { it.kind },
        )
    }

    @Test
    fun sleepScoreBelow_noBonus() {
        val snapshot = HealthSnapshot(sleepScore = SessionBonusRules.SLEEP_SCORE_MIN - 1)
        assertTrue(SessionBonusEvaluator.evaluate(snapshot).isEmpty())
    }

    @Test
    fun sleepDurationAndScore_grantOnlyOneSleepBonus() {
        val snapshot = HealthSnapshot(
            sleepMinutes = SessionBonusRules.SLEEP_MINUTES_MIN,
            sleepScore = 95,
        )
        assertEquals(1, SessionBonusEvaluator.evaluate(snapshot).size)
    }

    @Test
    fun stepsAtGoal_grantsStepBonus() {
        val snapshot = HealthSnapshot(steps = 10_000, stepGoal = 10_000)
        assertTrue(SessionBonusEvaluator.qualifiesSteps(snapshot))
        assertEquals(
            listOf(BonusKind.STEP_GOAL),
            SessionBonusEvaluator.evaluate(snapshot).map { it.kind },
        )
    }

    @Test
    fun stepsJustBelowGoal_noBonus() {
        val snapshot = HealthSnapshot(steps = 9_999, stepGoal = 10_000)
        assertFalse(SessionBonusEvaluator.qualifiesSteps(snapshot))
    }

    @Test
    fun missingStepGoal_usesDefaultTenThousand() {
        val snapshot = HealthSnapshot(steps = 10_000, stepGoal = 0)
        assertEquals(SessionBonusRules.DEFAULT_STEP_GOAL, snapshot.effectiveStepGoal)
        assertTrue(SessionBonusEvaluator.qualifiesSteps(snapshot))
    }

    @Test
    fun runningSession_grantsRunBonus() {
        val snapshot = HealthSnapshot(hadRunningSession = true)
        assertEquals(
            listOf(BonusKind.RUNNING),
            SessionBonusEvaluator.evaluate(snapshot).map { it.kind },
        )
    }

    @Test
    fun distanceAtThresholdWithoutRunType_grantsRunBonus() {
        val snapshot = HealthSnapshot(distanceMeters = SessionBonusRules.RUNNING_DISTANCE_METERS)
        assertTrue(SessionBonusEvaluator.qualifiesRunning(snapshot))
    }

    @Test
    fun distanceBelowThreshold_noRunBonus() {
        val snapshot = HealthSnapshot(distanceMeters = SessionBonusRules.RUNNING_DISTANCE_METERS - 1f)
        assertFalse(SessionBonusEvaluator.qualifiesRunning(snapshot))
    }

    @Test
    fun energyScoreAtThreshold_grantsEnergyBonus() {
        val snapshot = HealthSnapshot(energyScore = SessionBonusRules.ENERGY_SCORE_MIN)
        assertEquals(
            listOf(BonusKind.ENERGY),
            SessionBonusEvaluator.evaluate(snapshot).map { it.kind },
        )
    }

    @Test
    fun energyScoreMissing_noEnergyBonus() {
        assertFalse(SessionBonusEvaluator.qualifiesEnergy(HealthSnapshot.EMPTY))
    }

    @Test
    fun claimedKinds_areSkipped() {
        val snapshot = HealthSnapshot(
            sleepMinutes = 480,
            steps = 12_000,
            stepGoal = 10_000,
            hadRunningSession = true,
            energyScore = 80f,
        )
        val bonuses = SessionBonusEvaluator.evaluate(
            snapshot,
            claimedToday = setOf(BonusKind.GOOD_SLEEP, BonusKind.STEP_GOAL),
        )
        assertEquals(listOf(BonusKind.RUNNING, BonusKind.ENERGY), bonuses.map { it.kind })
    }

    @Test
    fun allMetrics_grantFourBonusesInStableOrder() {
        val snapshot = HealthSnapshot(
            steps = 12_000,
            stepGoal = 8_000,
            sleepMinutes = 450,
            sleepScore = 80,
            energyScore = 90f,
            distanceMeters = 2_000f,
            hadRunningSession = true,
        )
        val kinds = SessionBonusEvaluator.evaluate(snapshot).map { it.kind }
        assertEquals(
            listOf(
                BonusKind.GOOD_SLEEP,
                BonusKind.STEP_GOAL,
                BonusKind.RUNNING,
                BonusKind.ENERGY,
            ),
            kinds,
        )
    }
}
