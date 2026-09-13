package com.pixelfitquest.feature.healthbonuses

import com.pixelfitquest.feature.healthbonuses.model.HealthDataOrigin
import com.pixelfitquest.feature.healthbonuses.model.HealthSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthSnapshotMergeTest {

    @Test
    fun prefersPresentFieldsOverEmpty() {
        val primary = HealthSnapshot(
            steps = 4_000,
            stepGoal = 8_000,
            origin = HealthDataOrigin.HEALTH_CONNECT,
        )
        val other = HealthSnapshot.EMPTY
        val merged = primary.mergePreferringThis(other)
        assertEquals(4_000L, merged.steps)
        assertEquals(8_000, merged.stepGoal)
        assertEquals(HealthDataOrigin.HEALTH_CONNECT, merged.origin)
        assertTrue(merged.hasAnyMetric())
    }

    @Test
    fun fillsGapsFromOtherSource() {
        val primary = HealthSnapshot(
            steps = 1_000,
            origin = HealthDataOrigin.HEALTH_CONNECT,
        )
        val other = HealthSnapshot(
            sleepScore = 80,
            energyScore = 75f,
            origin = HealthDataOrigin.HEALTH_CONNECT,
        )
        val merged = primary.mergePreferringThis(other)
        assertEquals(1_000L, merged.steps)
        assertEquals(80, merged.sleepScore)
        assertEquals(75f, merged.energyScore)
        assertEquals(HealthDataOrigin.HEALTH_CONNECT, merged.origin)
    }
}
