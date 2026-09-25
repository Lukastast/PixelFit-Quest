package com.pixelfitquest.feature.workout.analysis

enum class Mount {
    BAR_CENTER,
    BAR_SLEEVE,
    CABLE_HANDLE,
    DUMBBELL,
    MACHINE_PAD,
}

enum class PrimaryMotion {
    VERTICAL_VS_GRAVITY,
    HORIZONTAL_IN_BAR_FRAME,
    PITCH_ABOUT_ELBOW,
}

enum class CycleShape {
    /** Top → bottom → top (bench, squat, pulldown). */
    HIGH_LOW_HIGH,

    /** Bottom → top → bottom (curl, many rows). */
    LOW_HIGH_LOW,
}

enum class QualityMetric {
    ROM,
    PATH_TILT,
    TEMPO,
}

enum class RomUnit {
    METERS,
    RADIANS,
}

/**
 * Physics + quality config for one lift. Adding an exercise is a new catalog
 * entry, not a ViewModel branch.
 */
data class ExerciseProfile(
    val id: String,
    val displayName: String,
    val mount: Mount,
    val primaryMotion: PrimaryMotion,
    val cycleShape: CycleShape,
    val eccentricFirst: Boolean,
    val minRepDurationMs: Long,
    val maxRepDurationMs: Long,
    val minAmplitude: Float,
    val typicalAmplitude: Float,
    val romFactor: Float,
    val usesArmLength: Boolean = false,
    val quality: Set<QualityMetric>,
) {
    val romUnit: RomUnit
        get() = when (primaryMotion) {
            PrimaryMotion.PITCH_ABOUT_ELBOW -> RomUnit.RADIANS
            else -> RomUnit.METERS
        }
}
