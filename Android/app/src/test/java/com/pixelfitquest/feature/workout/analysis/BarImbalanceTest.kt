package com.pixelfitquest.feature.workout.analysis

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BarImbalanceTest {

    @Test
    fun qualityIgnoresPlayInsideDeadband() {
        assertEquals(100f, BarImbalance.qualityFromDeg(4f), 0.01f)
        assertEquals(100f, BarImbalance.qualityFromDeg(5f), 0.01f)
    }

    @Test
    fun qualityFallsGentlyAfterDeadband() {
        val quality = BarImbalance.qualityFromDeg(15f)
        // 10° past the 5° deadband × 2.5 pts/° = 25 off → 75
        assertEquals(75f, quality, 0.5f)
        assertTrue("15° should still cost something", quality < 90f)
        assertTrue("15° must not zero the score", quality > 50f)
    }
}
