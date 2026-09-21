package com.pixelfitquest.feature.workout.analysis

import com.pixelfitquest.feature.workout.model.enums.ExerciseType

object ExerciseProfiles {
    private val barbellQuality = setOf(
        QualityMetric.ROM,
        QualityMetric.PATH_TILT,
        QualityMetric.TEMPO,
    )

    private val cableQuality = setOf(
        QualityMetric.ROM,
        QualityMetric.TEMPO,
    )

    val benchPress = ExerciseProfile(
        id = ExerciseType.BENCH_PRESS.type,
        displayName = "Bench Press",
        mount = Mount.BAR_SLEEVE,
        primaryMotion = PrimaryMotion.VERTICAL_VS_GRAVITY,
        cycleShape = CycleShape.HIGH_LOW_HIGH,
        eccentricFirst = true,
        minRepDurationMs = 700,
        maxRepDurationMs = 5_000,
        minAmplitude = 0.15f,
        typicalAmplitude = 0.45f,
        romFactor = 0.25f,
        quality = barbellQuality,
    )

    val squat = ExerciseProfile(
        id = ExerciseType.SQUAT.type,
        displayName = "Squat",
        mount = Mount.BAR_SLEEVE,
        primaryMotion = PrimaryMotion.VERTICAL_VS_GRAVITY,
        cycleShape = CycleShape.HIGH_LOW_HIGH,
        eccentricFirst = true,
        minRepDurationMs = 800,
        maxRepDurationMs = 6_000,
        minAmplitude = 0.20f,
        typicalAmplitude = 0.80f,
        romFactor = 0.45f,
        quality = barbellQuality,
    )

    val latPulldown = ExerciseProfile(
        id = ExerciseType.LAT_PULLDOWN.type,
        displayName = "Lat Pulldown",
        mount = Mount.CABLE_HANDLE,
        primaryMotion = PrimaryMotion.VERTICAL_VS_GRAVITY,
        cycleShape = CycleShape.HIGH_LOW_HIGH,
        eccentricFirst = false,
        minRepDurationMs = 700,
        maxRepDurationMs = 5_000,
        minAmplitude = 0.25f,
        typicalAmplitude = 0.90f,
        romFactor = 0.50f,
        usesArmLength = true,
        quality = cableQuality,
    )

    val seatedRows = ExerciseProfile(
        id = ExerciseType.SEATED_ROWS.type,
        displayName = "Seated Rows",
        mount = Mount.CABLE_HANDLE,
        primaryMotion = PrimaryMotion.HORIZONTAL_IN_BAR_FRAME,
        cycleShape = CycleShape.LOW_HIGH_LOW,
        eccentricFirst = false,
        minRepDurationMs = 700,
        maxRepDurationMs = 5_000,
        minAmplitude = 0.15f,
        typicalAmplitude = 0.60f,
        romFactor = 0.35f,
        quality = cableQuality,
    )

    val bicepCurl = ExerciseProfile(
        id = ExerciseType.BICEP_CURL.type,
        displayName = "Bicep Curl",
        mount = Mount.BAR_SLEEVE,
        primaryMotion = PrimaryMotion.PITCH_ABOUT_ELBOW,
        cycleShape = CycleShape.LOW_HIGH_LOW,
        eccentricFirst = false,
        minRepDurationMs = 600,
        maxRepDurationMs = 4_500,
        minAmplitude = 0.50f,
        typicalAmplitude = 2.0f,
        romFactor = 0.18f,
        usesArmLength = true,
        quality = barbellQuality,
    )

    val tricepExtension = ExerciseProfile(
        id = ExerciseType.TRICEP_EXTENSION.type,
        displayName = "Tricep Extension",
        mount = Mount.CABLE_HANDLE,
        primaryMotion = PrimaryMotion.PITCH_ABOUT_ELBOW,
        cycleShape = CycleShape.LOW_HIGH_LOW,
        eccentricFirst = false,
        minRepDurationMs = 600,
        maxRepDurationMs = 4_500,
        minAmplitude = 0.40f,
        typicalAmplitude = 1.6f,
        romFactor = 0.20f,
        usesArmLength = true,
        quality = cableQuality,
    )

    private val byId: Map<String, ExerciseProfile> = listOf(
        benchPress,
        squat,
        latPulldown,
        seatedRows,
        bicepCurl,
        tricepExtension,
    ).associateBy { it.id }

    fun forType(type: ExerciseType): ExerciseProfile =
        byId[type.type] ?: benchPress

    fun forId(id: String): ExerciseProfile =
        byId[id] ?: benchPress
}
