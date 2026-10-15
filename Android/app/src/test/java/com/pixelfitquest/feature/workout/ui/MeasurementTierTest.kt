package com.pixelfitquest.feature.workout.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class MeasurementTierTest {
    @Test
    fun fromDevice_returnsCorrectTier() {
        assertEquals(MeasurementTier.TIER_A, MeasurementTier.fromDevice(hasGyroscope = true))
        assertEquals(MeasurementTier.TIER_B, MeasurementTier.fromDevice(hasGyroscope = false))
    }

    @Test
    fun noGyroFlag_beatsDeviceTier() {
        assertEquals(MeasurementTier.TIER_B, MeasurementTier.resolve(MeasurementTier.TIER_A, listOf("no_gyro")))
        assertEquals(MeasurementTier.TIER_B, MeasurementTier.resolve(deviceHasGyro = true, analysisFlags = listOf("no_gyro")))
    }

    @Test
    fun resolve_preservesDeviceTierWhenNoGyroFlagAbsent() {
        assertEquals(MeasurementTier.TIER_A, MeasurementTier.resolve(MeasurementTier.TIER_A, emptyList()))
        assertEquals(MeasurementTier.TIER_A, MeasurementTier.resolve(MeasurementTier.TIER_A, listOf("clip_pose")))
        assertEquals(MeasurementTier.TIER_B, MeasurementTier.resolve(MeasurementTier.TIER_B, emptyList()))
        assertEquals(MeasurementTier.TIER_B, MeasurementTier.resolve(deviceHasGyro = false, analysisFlags = emptyList()))
    }
}
