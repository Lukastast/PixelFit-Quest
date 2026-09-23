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

    val overheadPress = benchPress.copy(
        id = ExerciseType.OVERHEAD_PRESS.type,
        displayName = "Overhead Press",
        minAmplitude = 0.12f,
        typicalAmplitude = 0.40f,
        romFactor = 0.22f,
    )

    val seatedOverheadPress = overheadPress.copy(
        id = ExerciseType.SEATED_OVERHEAD_PRESS.type,
        displayName = "Seated Overhead Press",
    )

    val closeGripBenchPress = benchPress.copy(
        id = ExerciseType.CLOSE_GRIP_BENCH_PRESS.type,
        displayName = "Close-Grip Bench Press",
        typicalAmplitude = 0.48f,
        romFactor = 0.27f,
    )

    val floorPress = benchPress.copy(
        id = ExerciseType.FLOOR_PRESS.type,
        displayName = "Floor Press",
        maxRepDurationMs = 4_500,
        minAmplitude = 0.10f,
        typicalAmplitude = 0.28f,
        romFactor = 0.16f,
    )

    val inclineBenchPress = benchPress.copy(
        id = ExerciseType.INCLINE_BENCH_PRESS.type,
        displayName = "Incline Bench Press",
        minAmplitude = 0.12f,
        typicalAmplitude = 0.35f,
        romFactor = 0.20f,
    )

    val declineBenchPress = benchPress.copy(
        id = ExerciseType.DECLINE_BENCH_PRESS.type,
        displayName = "Decline Bench Press",
        minAmplitude = 0.12f,
        typicalAmplitude = 0.32f,
        romFactor = 0.18f,
    )

    val frontSquat = squat.copy(
        id = ExerciseType.FRONT_SQUAT.type,
        displayName = "Front Squat",
        typicalAmplitude = 0.75f,
        romFactor = 0.42f,
    )

    val romanianDeadlift = benchPress.copy(
        id = ExerciseType.ROMANIAN_DEADLIFT.type,
        displayName = "Romanian Deadlift",
        minRepDurationMs = 800,
        maxRepDurationMs = 6_000,
        minAmplitude = 0.18f,
        typicalAmplitude = 0.40f,
        romFactor = 0.22f,
    )

    val goodMorning = romanianDeadlift.copy(
        id = ExerciseType.GOOD_MORNING.type,
        displayName = "Good Morning",
        minAmplitude = 0.15f,
        typicalAmplitude = 0.35f,
    )

    val deadlift = ExerciseProfile(
        id = ExerciseType.DEADLIFT.type,
        displayName = "Deadlift",
        mount = Mount.BAR_SLEEVE,
        primaryMotion = PrimaryMotion.VERTICAL_VS_GRAVITY,
        cycleShape = CycleShape.LOW_HIGH_LOW,
        eccentricFirst = false,
        minRepDurationMs = 800,
        maxRepDurationMs = 6_000,
        minAmplitude = 0.25f,
        typicalAmplitude = 0.55f,
        romFactor = 0.30f,
        quality = barbellQuality,
    )

    val sumoDeadlift = deadlift.copy(
        id = ExerciseType.SUMO_DEADLIFT.type,
        displayName = "Sumo Deadlift",
        typicalAmplitude = 0.50f,
        romFactor = 0.28f,
    )

    val rackPull = deadlift.copy(
        id = ExerciseType.RACK_PULL.type,
        displayName = "Rack Pull",
        minAmplitude = 0.12f,
        typicalAmplitude = 0.28f,
        romFactor = 0.16f,
    )

    val hipThrust = deadlift.copy(
        id = ExerciseType.HIP_THRUST.type,
        displayName = "Hip Thrust",
        minRepDurationMs = 700,
        maxRepDurationMs = 5_000,
        minAmplitude = 0.12f,
        typicalAmplitude = 0.30f,
        romFactor = 0.17f,
    )

    val ezBarCurl = bicepCurl.copy(
        id = ExerciseType.EZ_BAR_CURL.type,
        displayName = "EZ-Bar Curl",
    )

    val preacherCurl = bicepCurl.copy(
        id = ExerciseType.PREACHER_CURL.type,
        displayName = "Preacher Curl",
        typicalAmplitude = 1.7f,
    )

    val reverseCurl = bicepCurl.copy(
        id = ExerciseType.REVERSE_CURL.type,
        displayName = "Reverse Curl",
        typicalAmplitude = 1.6f,
    )

    val skullCrusher = ExerciseProfile(
        id = ExerciseType.SKULL_CRUSHER.type,
        displayName = "Skull Crusher",
        mount = Mount.BAR_SLEEVE,
        primaryMotion = PrimaryMotion.PITCH_ABOUT_ELBOW,
        cycleShape = CycleShape.HIGH_LOW_HIGH,
        eccentricFirst = true,
        minRepDurationMs = 600,
        maxRepDurationMs = 4_500,
        minAmplitude = 0.40f,
        typicalAmplitude = 1.5f,
        romFactor = 0.16f,
        quality = barbellQuality,
    )

    val barbellRow = ExerciseProfile(
        id = ExerciseType.BARBELL_ROW.type,
        displayName = "Barbell Row",
        mount = Mount.BAR_SLEEVE,
        primaryMotion = PrimaryMotion.HORIZONTAL_IN_BAR_FRAME,
        cycleShape = CycleShape.LOW_HIGH_LOW,
        eccentricFirst = false,
        minRepDurationMs = 700,
        maxRepDurationMs = 5_000,
        minAmplitude = 0.15f,
        typicalAmplitude = 0.45f,
        romFactor = 0.25f,
        quality = barbellQuality,
    )

    val pendlayRow = barbellRow.copy(
        id = ExerciseType.PENDLAY_ROW.type,
        displayName = "Pendlay Row",
    )

    val tBarRow = barbellRow.copy(
        id = ExerciseType.T_BAR_ROW.type,
        displayName = "T-Bar Row",
        typicalAmplitude = 0.40f,
    )

    private val byId: Map<String, ExerciseProfile> = listOf(
        benchPress,
        overheadPress,
        seatedOverheadPress,
        closeGripBenchPress,
        floorPress,
        inclineBenchPress,
        declineBenchPress,
        squat,
        frontSquat,
        romanianDeadlift,
        goodMorning,
        deadlift,
        sumoDeadlift,
        rackPull,
        hipThrust,
        latPulldown,
        seatedRows,
        barbellRow,
        pendlayRow,
        tBarRow,
        bicepCurl,
        ezBarCurl,
        preacherCurl,
        reverseCurl,
        skullCrusher,
        tricepExtension,
    ).associateBy { it.id }

    fun forType(type: ExerciseType): ExerciseProfile =
        byId[type.type] ?: benchPress

    fun forId(id: String): ExerciseProfile =
        byId[id] ?: benchPress
}
