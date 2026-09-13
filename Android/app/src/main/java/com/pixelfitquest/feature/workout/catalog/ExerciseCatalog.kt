package com.pixelfitquest.feature.workout.catalog

import com.pixelfitquest.feature.workout.model.enums.ExerciseType

/**
 * Muscle-group grouping for the workout builder list. Bundled on-device;
 * not fetched from Firebase.
 */
enum class ExerciseCategory(val label: String) {
    CHEST("Chest"),
    BACK("Back"),
    LEGS("Legs"),
    SHOULDERS("Shoulders"),
    ARMS("Arms"),
    CORE("Core"),
    OLYMPIC("Olympic"),
    CONDITIONING("Conditioning"),
}

enum class ExerciseEquipment(val label: String) {
    BARBELL("Barbell"),
    DUMBBELL("Dumbbell"),
    CABLE("Cable"),
    MACHINE("Machine"),
    BODYWEIGHT("Bodyweight"),
    KETTLEBELL("Kettlebell"),
    OTHER("Other"),
}

/**
 * Logging metadata for one lift. [imuSupported] is true only when an existing
 * [com.pixelfitquest.feature.workout.analysis.ExerciseProfile] ships with the app.
 * Log-only entries are still selectable; they do not invent analyzer math.
 */
data class ExerciseDefinition(
    val type: ExerciseType,
    val displayName: String,
    val category: ExerciseCategory,
    val equipment: ExerciseEquipment,
    val imuSupported: Boolean = false,
) {
    val id: String get() = type.type
    val subtitle: String get() = "${category.label} · ${equipment.label}"
}

/**
 * On-device exercise library for planning and logging. IMU profiles may lag
 * this list; missing profiles never block selecting or starting a workout.
 */
object ExerciseCatalog {

    val all: List<ExerciseDefinition> = buildList {
        // Existing IMU-backed lifts (profiles live in ExerciseProfiles; do not duplicate math here).
        add(e(ExerciseType.BENCH_PRESS, "Bench Press", ExerciseCategory.CHEST, ExerciseEquipment.BARBELL, imu = true))
        add(e(ExerciseType.SQUAT, "Squat", ExerciseCategory.LEGS, ExerciseEquipment.BARBELL, imu = true))
        add(e(ExerciseType.BICEP_CURL, "Bicep Curl", ExerciseCategory.ARMS, ExerciseEquipment.BARBELL, imu = true))
        add(e(ExerciseType.LAT_PULLDOWN, "Lat Pulldown", ExerciseCategory.BACK, ExerciseEquipment.CABLE, imu = true))
        add(e(ExerciseType.SEATED_ROWS, "Seated Rows", ExerciseCategory.BACK, ExerciseEquipment.CABLE, imu = true))
        add(e(ExerciseType.TRICEP_EXTENSION, "Tricep Extension", ExerciseCategory.ARMS, ExerciseEquipment.CABLE, imu = true))

        // Chest — log-only until an IMU profile exists.
        add(e(ExerciseType.INCLINE_BENCH_PRESS, "Incline Bench Press", ExerciseCategory.CHEST, ExerciseEquipment.BARBELL))
        add(e(ExerciseType.DECLINE_BENCH_PRESS, "Decline Bench Press", ExerciseCategory.CHEST, ExerciseEquipment.BARBELL))
        add(e(ExerciseType.DUMBBELL_BENCH_PRESS, "Dumbbell Bench Press", ExerciseCategory.CHEST, ExerciseEquipment.DUMBBELL))
        add(e(ExerciseType.INCLINE_DUMBBELL_PRESS, "Incline Dumbbell Press", ExerciseCategory.CHEST, ExerciseEquipment.DUMBBELL))
        add(e(ExerciseType.DECLINE_DUMBBELL_PRESS, "Decline Dumbbell Press", ExerciseCategory.CHEST, ExerciseEquipment.DUMBBELL))
        add(e(ExerciseType.FLOOR_PRESS, "Floor Press", ExerciseCategory.CHEST, ExerciseEquipment.BARBELL))
        add(e(ExerciseType.DUMBBELL_FLY, "Dumbbell Fly", ExerciseCategory.CHEST, ExerciseEquipment.DUMBBELL))
        add(e(ExerciseType.INCLINE_DUMBBELL_FLY, "Incline Dumbbell Fly", ExerciseCategory.CHEST, ExerciseEquipment.DUMBBELL))
        add(e(ExerciseType.CABLE_FLY, "Cable Fly", ExerciseCategory.CHEST, ExerciseEquipment.CABLE))
        add(e(ExerciseType.CHEST_PRESS_MACHINE, "Chest Press Machine", ExerciseCategory.CHEST, ExerciseEquipment.MACHINE))
        add(e(ExerciseType.PEC_DECK, "Pec Deck", ExerciseCategory.CHEST, ExerciseEquipment.MACHINE))
        add(e(ExerciseType.PUSH_UP, "Push-Up", ExerciseCategory.CHEST, ExerciseEquipment.BODYWEIGHT))
        add(e(ExerciseType.CHEST_DIP, "Chest Dip", ExerciseCategory.CHEST, ExerciseEquipment.BODYWEIGHT))
        add(e(ExerciseType.LANDMINE_PRESS, "Landmine Press", ExerciseCategory.CHEST, ExerciseEquipment.BARBELL))

        // Back
        add(e(ExerciseType.DEADLIFT, "Deadlift", ExerciseCategory.BACK, ExerciseEquipment.BARBELL))
        add(e(ExerciseType.SUMO_DEADLIFT, "Sumo Deadlift", ExerciseCategory.BACK, ExerciseEquipment.BARBELL))
        add(e(ExerciseType.ROMANIAN_DEADLIFT, "Romanian Deadlift", ExerciseCategory.BACK, ExerciseEquipment.BARBELL))
        add(e(ExerciseType.RACK_PULL, "Rack Pull", ExerciseCategory.BACK, ExerciseEquipment.BARBELL))
        add(e(ExerciseType.BARBELL_ROW, "Barbell Row", ExerciseCategory.BACK, ExerciseEquipment.BARBELL))
        add(e(ExerciseType.PENDLAY_ROW, "Pendlay Row", ExerciseCategory.BACK, ExerciseEquipment.BARBELL))
        add(e(ExerciseType.DUMBBELL_ROW, "Dumbbell Row", ExerciseCategory.BACK, ExerciseEquipment.DUMBBELL))
        add(e(ExerciseType.T_BAR_ROW, "T-Bar Row", ExerciseCategory.BACK, ExerciseEquipment.BARBELL))
        add(e(ExerciseType.MACHINE_ROW, "Machine Row", ExerciseCategory.BACK, ExerciseEquipment.MACHINE))
        add(e(ExerciseType.PULL_UP, "Pull-Up", ExerciseCategory.BACK, ExerciseEquipment.BODYWEIGHT))
        add(e(ExerciseType.CHIN_UP, "Chin-Up", ExerciseCategory.BACK, ExerciseEquipment.BODYWEIGHT))
        add(e(ExerciseType.FACE_PULL, "Face Pull", ExerciseCategory.BACK, ExerciseEquipment.CABLE))
        add(e(ExerciseType.REVERSE_FLY, "Reverse Fly", ExerciseCategory.BACK, ExerciseEquipment.DUMBBELL))
        add(e(ExerciseType.STRAIGHT_ARM_PULLDOWN, "Straight-Arm Pulldown", ExerciseCategory.BACK, ExerciseEquipment.CABLE))
        add(e(ExerciseType.GOOD_MORNING, "Good Morning", ExerciseCategory.BACK, ExerciseEquipment.BARBELL))
        add(e(ExerciseType.BACK_EXTENSION, "Back Extension", ExerciseCategory.BACK, ExerciseEquipment.MACHINE))
        add(e(ExerciseType.SHRUG, "Shrug", ExerciseCategory.BACK, ExerciseEquipment.BARBELL))

        // Legs
        add(e(ExerciseType.FRONT_SQUAT, "Front Squat", ExerciseCategory.LEGS, ExerciseEquipment.BARBELL))
        add(e(ExerciseType.GOBLET_SQUAT, "Goblet Squat", ExerciseCategory.LEGS, ExerciseEquipment.DUMBBELL))
        add(e(ExerciseType.HACK_SQUAT, "Hack Squat", ExerciseCategory.LEGS, ExerciseEquipment.MACHINE))
        add(e(ExerciseType.SMITH_SQUAT, "Smith Squat", ExerciseCategory.LEGS, ExerciseEquipment.MACHINE))
        add(e(ExerciseType.LEG_PRESS, "Leg Press", ExerciseCategory.LEGS, ExerciseEquipment.MACHINE))
        add(e(ExerciseType.LEG_EXTENSION, "Leg Extension", ExerciseCategory.LEGS, ExerciseEquipment.MACHINE))
        add(e(ExerciseType.SEATED_LEG_CURL, "Seated Leg Curl", ExerciseCategory.LEGS, ExerciseEquipment.MACHINE))
        add(e(ExerciseType.LYING_LEG_CURL, "Lying Leg Curl", ExerciseCategory.LEGS, ExerciseEquipment.MACHINE))
        add(e(ExerciseType.WALKING_LUNGE, "Walking Lunge", ExerciseCategory.LEGS, ExerciseEquipment.DUMBBELL))
        add(e(ExerciseType.BULGARIAN_SPLIT_SQUAT, "Bulgarian Split Squat", ExerciseCategory.LEGS, ExerciseEquipment.DUMBBELL))
        add(e(ExerciseType.STEP_UP, "Step-Up", ExerciseCategory.LEGS, ExerciseEquipment.DUMBBELL))
        add(e(ExerciseType.HIP_THRUST, "Hip Thrust", ExerciseCategory.LEGS, ExerciseEquipment.BARBELL))
        add(e(ExerciseType.GLUTE_BRIDGE, "Glute Bridge", ExerciseCategory.LEGS, ExerciseEquipment.BODYWEIGHT))
        add(e(ExerciseType.CABLE_KICKBACK, "Cable Kickback", ExerciseCategory.LEGS, ExerciseEquipment.CABLE))
        add(e(ExerciseType.STANDING_CALF_RAISE, "Standing Calf Raise", ExerciseCategory.LEGS, ExerciseEquipment.MACHINE))
        add(e(ExerciseType.SEATED_CALF_RAISE, "Seated Calf Raise", ExerciseCategory.LEGS, ExerciseEquipment.MACHINE))
        add(e(ExerciseType.LEG_ADDUCTION, "Hip Adduction", ExerciseCategory.LEGS, ExerciseEquipment.MACHINE))
        add(e(ExerciseType.LEG_ABDUCTION, "Hip Abduction", ExerciseCategory.LEGS, ExerciseEquipment.MACHINE))
        add(e(ExerciseType.NORDIC_HAMSTRING_CURL, "Nordic Hamstring Curl", ExerciseCategory.LEGS, ExerciseEquipment.BODYWEIGHT))
        add(e(ExerciseType.WALL_SIT, "Wall Sit", ExerciseCategory.LEGS, ExerciseEquipment.BODYWEIGHT))

        // Shoulders
        add(e(ExerciseType.OVERHEAD_PRESS, "Overhead Press", ExerciseCategory.SHOULDERS, ExerciseEquipment.BARBELL))
        add(e(ExerciseType.DUMBBELL_SHOULDER_PRESS, "Dumbbell Shoulder Press", ExerciseCategory.SHOULDERS, ExerciseEquipment.DUMBBELL))
        add(e(ExerciseType.ARNOLD_PRESS, "Arnold Press", ExerciseCategory.SHOULDERS, ExerciseEquipment.DUMBBELL))
        add(e(ExerciseType.SEATED_OVERHEAD_PRESS, "Seated Overhead Press", ExerciseCategory.SHOULDERS, ExerciseEquipment.BARBELL))
        add(e(ExerciseType.PUSH_PRESS, "Push Press", ExerciseCategory.SHOULDERS, ExerciseEquipment.BARBELL))
        add(e(ExerciseType.LATERAL_RAISE, "Lateral Raise", ExerciseCategory.SHOULDERS, ExerciseEquipment.DUMBBELL))
        add(e(ExerciseType.FRONT_RAISE, "Front Raise", ExerciseCategory.SHOULDERS, ExerciseEquipment.DUMBBELL))
        add(e(ExerciseType.REAR_DELT_FLY, "Rear Delt Fly", ExerciseCategory.SHOULDERS, ExerciseEquipment.DUMBBELL))
        add(e(ExerciseType.UPRIGHT_ROW, "Upright Row", ExerciseCategory.SHOULDERS, ExerciseEquipment.BARBELL))
        add(e(ExerciseType.CABLE_LATERAL_RAISE, "Cable Lateral Raise", ExerciseCategory.SHOULDERS, ExerciseEquipment.CABLE))

        // Arms
        add(e(ExerciseType.HAMMER_CURL, "Hammer Curl", ExerciseCategory.ARMS, ExerciseEquipment.DUMBBELL))
        add(e(ExerciseType.PREACHER_CURL, "Preacher Curl", ExerciseCategory.ARMS, ExerciseEquipment.BARBELL))
        add(e(ExerciseType.CONCENTRATION_CURL, "Concentration Curl", ExerciseCategory.ARMS, ExerciseEquipment.DUMBBELL))
        add(e(ExerciseType.CABLE_CURL, "Cable Curl", ExerciseCategory.ARMS, ExerciseEquipment.CABLE))
        add(e(ExerciseType.INCLINE_CURL, "Incline Curl", ExerciseCategory.ARMS, ExerciseEquipment.DUMBBELL))
        add(e(ExerciseType.EZ_BAR_CURL, "EZ-Bar Curl", ExerciseCategory.ARMS, ExerciseEquipment.BARBELL))
        add(e(ExerciseType.REVERSE_CURL, "Reverse Curl", ExerciseCategory.ARMS, ExerciseEquipment.BARBELL))
        add(e(ExerciseType.SKULL_CRUSHER, "Skull Crusher", ExerciseCategory.ARMS, ExerciseEquipment.BARBELL))
        add(e(ExerciseType.TRICEP_PUSHDOWN, "Tricep Pushdown", ExerciseCategory.ARMS, ExerciseEquipment.CABLE))
        add(e(ExerciseType.OVERHEAD_TRICEP_EXTENSION, "Overhead Tricep Extension", ExerciseCategory.ARMS, ExerciseEquipment.DUMBBELL))
        add(e(ExerciseType.CLOSE_GRIP_BENCH_PRESS, "Close-Grip Bench Press", ExerciseCategory.ARMS, ExerciseEquipment.BARBELL))
        add(e(ExerciseType.TRICEP_DIP, "Tricep Dip", ExerciseCategory.ARMS, ExerciseEquipment.BODYWEIGHT))
        add(e(ExerciseType.BENCH_DIP, "Bench Dip", ExerciseCategory.ARMS, ExerciseEquipment.BODYWEIGHT))
        add(e(ExerciseType.WRIST_CURL, "Wrist Curl", ExerciseCategory.ARMS, ExerciseEquipment.BARBELL))
        add(e(ExerciseType.CABLE_TRICEP_KICKBACK, "Cable Tricep Kickback", ExerciseCategory.ARMS, ExerciseEquipment.CABLE))

        // Core
        add(e(ExerciseType.PLANK, "Plank", ExerciseCategory.CORE, ExerciseEquipment.BODYWEIGHT))
        add(e(ExerciseType.SIDE_PLANK, "Side Plank", ExerciseCategory.CORE, ExerciseEquipment.BODYWEIGHT))
        add(e(ExerciseType.CRUNCH, "Crunch", ExerciseCategory.CORE, ExerciseEquipment.BODYWEIGHT))
        add(e(ExerciseType.SIT_UP, "Sit-Up", ExerciseCategory.CORE, ExerciseEquipment.BODYWEIGHT))
        add(e(ExerciseType.HANGING_LEG_RAISE, "Hanging Leg Raise", ExerciseCategory.CORE, ExerciseEquipment.BODYWEIGHT))
        add(e(ExerciseType.CABLE_CRUNCH, "Cable Crunch", ExerciseCategory.CORE, ExerciseEquipment.CABLE))
        add(e(ExerciseType.RUSSIAN_TWIST, "Russian Twist", ExerciseCategory.CORE, ExerciseEquipment.BODYWEIGHT))
        add(e(ExerciseType.AB_WHEEL, "Ab Wheel", ExerciseCategory.CORE, ExerciseEquipment.OTHER))
        add(e(ExerciseType.DEAD_BUG, "Dead Bug", ExerciseCategory.CORE, ExerciseEquipment.BODYWEIGHT))
        add(e(ExerciseType.WOODCHOP, "Woodchop", ExerciseCategory.CORE, ExerciseEquipment.CABLE))
        add(e(ExerciseType.MOUNTAIN_CLIMBER, "Mountain Climber", ExerciseCategory.CORE, ExerciseEquipment.BODYWEIGHT))
        add(e(ExerciseType.PALLOF_PRESS, "Pallof Press", ExerciseCategory.CORE, ExerciseEquipment.CABLE))

        // Olympic
        add(e(ExerciseType.BARBELL_CLEAN, "Clean", ExerciseCategory.OLYMPIC, ExerciseEquipment.BARBELL))
        add(e(ExerciseType.POWER_CLEAN, "Power Clean", ExerciseCategory.OLYMPIC, ExerciseEquipment.BARBELL))
        add(e(ExerciseType.HANG_CLEAN, "Hang Clean", ExerciseCategory.OLYMPIC, ExerciseEquipment.BARBELL))
        add(e(ExerciseType.SNATCH, "Snatch", ExerciseCategory.OLYMPIC, ExerciseEquipment.BARBELL))

        // Conditioning
        add(e(ExerciseType.FARMER_CARRY, "Farmer Carry", ExerciseCategory.CONDITIONING, ExerciseEquipment.OTHER))
        add(e(ExerciseType.KETTLEBELL_SWING, "Kettlebell Swing", ExerciseCategory.CONDITIONING, ExerciseEquipment.KETTLEBELL))
        add(e(ExerciseType.BOX_JUMP, "Box Jump", ExerciseCategory.CONDITIONING, ExerciseEquipment.BODYWEIGHT))
        add(e(ExerciseType.BURPEE, "Burpee", ExerciseCategory.CONDITIONING, ExerciseEquipment.BODYWEIGHT))
        add(e(ExerciseType.JUMP_ROPE, "Jump Rope", ExerciseCategory.CONDITIONING, ExerciseEquipment.OTHER))
        add(e(ExerciseType.BATTLE_ROPE, "Battle Rope", ExerciseCategory.CONDITIONING, ExerciseEquipment.OTHER))
        add(e(ExerciseType.SLED_PUSH, "Sled Push", ExerciseCategory.CONDITIONING, ExerciseEquipment.OTHER))
    }

    private val byType: Map<ExerciseType, ExerciseDefinition> = all.associateBy { it.type }

    val imuSupportedCount: Int get() = all.count { it.imuSupported }

    fun definition(type: ExerciseType): ExerciseDefinition =
        byType[type] ?: ExerciseDefinition(
            type = type,
            displayName = fallbackDisplayName(type),
            category = ExerciseCategory.CONDITIONING,
            equipment = ExerciseEquipment.OTHER,
            imuSupported = false,
        )

    fun definitionOrNull(type: ExerciseType): ExerciseDefinition? = byType[type]

    fun hasImuSupport(type: ExerciseType): Boolean = byType[type]?.imuSupported == true

    fun grouped(definitions: List<ExerciseDefinition> = all): Map<ExerciseCategory, List<ExerciseDefinition>> {
        val grouped = definitions.groupBy { it.category }
        return ExerciseCategory.entries.associateWith { grouped[it].orEmpty() }
            .filterValues { it.isNotEmpty() }
    }

    fun search(
        query: String,
        category: ExerciseCategory? = null,
        imuOnly: Boolean = false,
        logOnly: Boolean = false,
        selectedTypes: Set<ExerciseType>? = null,
    ): List<ExerciseDefinition> {
        val needle = query.trim()
        return all.asSequence()
            .filter { category == null || it.category == category }
            .filter { !imuOnly || it.imuSupported }
            .filter { !logOnly || !it.imuSupported }
            .filter { selectedTypes == null || it.type in selectedTypes }
            .filter { def ->
                if (needle.isEmpty()) true
                else {
                    val haystack = buildString {
                        append(def.displayName)
                        append(' ')
                        append(def.category.label)
                        append(' ')
                        append(def.equipment.label)
                        append(' ')
                        append(def.type.name.replace('_', ' '))
                    }
                    haystack.contains(needle, ignoreCase = true)
                }
            }
            .toList()
    }

    private fun e(
        type: ExerciseType,
        displayName: String,
        category: ExerciseCategory,
        equipment: ExerciseEquipment,
        imu: Boolean = false,
    ) = ExerciseDefinition(
        type = type,
        displayName = displayName,
        category = category,
        equipment = equipment,
        imuSupported = imu,
    )

    private fun fallbackDisplayName(type: ExerciseType): String =
        type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
}
