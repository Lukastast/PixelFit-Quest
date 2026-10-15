package com.pixelfitquest.feature.workout.ui

enum class MeasurementTier {
    TIER_A,
    TIER_B;

    companion object {
        fun fromDevice(hasGyroscope: Boolean): MeasurementTier =
            if (hasGyroscope) TIER_A else TIER_B

        fun resolve(deviceHasGyro: Boolean, analysisFlags: Collection<String> = emptyList()): MeasurementTier =
            if ("no_gyro" in analysisFlags) TIER_B else fromDevice(deviceHasGyro)

        fun resolve(deviceTier: MeasurementTier, analysisFlags: Collection<String> = emptyList()): MeasurementTier =
            if ("no_gyro" in analysisFlags) TIER_B else deviceTier
    }
}
