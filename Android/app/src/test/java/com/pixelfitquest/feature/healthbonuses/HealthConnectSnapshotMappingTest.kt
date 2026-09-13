package com.pixelfitquest.feature.healthbonuses

import com.pixelfitquest.feature.healthbonuses.model.BonusKind
import com.pixelfitquest.feature.healthbonuses.model.HealthDataOrigin
import com.pixelfitquest.feature.healthbonuses.model.HealthSnapshot
import com.pixelfitquest.feature.healthbonuses.model.SessionBonusRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Documents the Health Connect → HealthSnapshot contract used by
 * [HealthConnectMetricsSource]: steps/goal only; sleep/energy/running unset.
 */
class HealthConnectSnapshotMappingTest {

    @Test
    fun stepsAtGoal_grantsStepBonusOnly() {
        val snapshot = HealthSnapshot(
            steps = SessionBonusRules.DEFAULT_STEP_GOAL.toLong(),
            stepGoal = SessionBonusRules.DEFAULT_STEP_GOAL,
            sleepMinutes = null,
            sleepScore = null,
            energyScore = null,
            distanceMeters = null,
            hadRunningSession = false,
            origin = HealthDataOrigin.HEALTH_CONNECT,
        )
        val kinds = SessionBonusEvaluator.evaluate(snapshot).map { it.kind }
        assertEquals(listOf(BonusKind.STEP_GOAL), kinds)
    }

    @Test
    fun unsetSleepEnergyRunning_doNotInventBonuses() {
        val snapshot = HealthSnapshot(
            steps = 100,
            stepGoal = SessionBonusRules.DEFAULT_STEP_GOAL,
            origin = HealthDataOrigin.HEALTH_CONNECT,
        )
        assertTrue(SessionBonusEvaluator.evaluate(snapshot).isEmpty())
    }
}
