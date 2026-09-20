package com.pixelfitquest.feature.workout.sensor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Documents the ≤200 Hz fallback used when SENSOR_DELAY_FASTEST (0 µs) is
 * rejected without HIGH_SAMPLING_RATE_SENSORS on API 31+ (see #149).
 */
class SensorSessionSamplingTest {

    @Test
    fun maxRateWithoutHighSamplingIs200Hz() {
        assertEquals(5_000, SensorSession.MAX_RATE_WITHOUT_HIGH_SAMPLING_US)
        assertTrue(SensorSession.MAX_RATE_WITHOUT_HIGH_SAMPLING_US >= 5_000)
    }
}
