package com.pixelfitquest.feature.workout.model.enums

import com.pixelfitquest.feature.workout.catalog.ExerciseCatalog

/**
 * Stable ids for logging. IMU math lives in ExerciseProfiles; a lift is tracked
 * only when the catalog sets imuSupported and a matching profile exists.
 */
enum class ExerciseType {
    // IMU-backed (ExerciseProfiles)
    BENCH_PRESS,
    SQUAT,
    BICEP_CURL,
    LAT_PULLDOWN,
    SEATED_ROWS,
    TRICEP_EXTENSION,

    // Chest
    INCLINE_BENCH_PRESS,
    DECLINE_BENCH_PRESS,
    DUMBBELL_BENCH_PRESS,
    INCLINE_DUMBBELL_PRESS,
    DECLINE_DUMBBELL_PRESS,
    FLOOR_PRESS,
    DUMBBELL_FLY,
    INCLINE_DUMBBELL_FLY,
    CABLE_FLY,
    CHEST_PRESS_MACHINE,
    PEC_DECK,
    PUSH_UP,
    CHEST_DIP,
    LANDMINE_PRESS,

    // Back
    DEADLIFT,
    SUMO_DEADLIFT,
    ROMANIAN_DEADLIFT,
    RACK_PULL,
    BARBELL_ROW,
    PENDLAY_ROW,
    DUMBBELL_ROW,
    T_BAR_ROW,
    MACHINE_ROW,
    PULL_UP,
    CHIN_UP,
    FACE_PULL,
    REVERSE_FLY,
    STRAIGHT_ARM_PULLDOWN,
    GOOD_MORNING,
    BACK_EXTENSION,
    SHRUG,

    // Legs
    FRONT_SQUAT,
    GOBLET_SQUAT,
    HACK_SQUAT,
    SMITH_SQUAT,
    LEG_PRESS,
    LEG_EXTENSION,
    SEATED_LEG_CURL,
    LYING_LEG_CURL,
    WALKING_LUNGE,
    BULGARIAN_SPLIT_SQUAT,
    STEP_UP,
    HIP_THRUST,
    GLUTE_BRIDGE,
    CABLE_KICKBACK,
    STANDING_CALF_RAISE,
    SEATED_CALF_RAISE,
    LEG_ADDUCTION,
    LEG_ABDUCTION,
    NORDIC_HAMSTRING_CURL,
    WALL_SIT,

    // Shoulders
    OVERHEAD_PRESS,
    DUMBBELL_SHOULDER_PRESS,
    ARNOLD_PRESS,
    SEATED_OVERHEAD_PRESS,
    PUSH_PRESS,
    LATERAL_RAISE,
    FRONT_RAISE,
    REAR_DELT_FLY,
    UPRIGHT_ROW,
    CABLE_LATERAL_RAISE,

    // Arms
    HAMMER_CURL,
    PREACHER_CURL,
    CONCENTRATION_CURL,
    CABLE_CURL,
    INCLINE_CURL,
    EZ_BAR_CURL,
    REVERSE_CURL,
    SKULL_CRUSHER,
    TRICEP_PUSHDOWN,
    OVERHEAD_TRICEP_EXTENSION,
    CLOSE_GRIP_BENCH_PRESS,
    TRICEP_DIP,
    BENCH_DIP,
    WRIST_CURL,
    CABLE_TRICEP_KICKBACK,

    // Core
    PLANK,
    SIDE_PLANK,
    CRUNCH,
    SIT_UP,
    HANGING_LEG_RAISE,
    CABLE_CRUNCH,
    RUSSIAN_TWIST,
    AB_WHEEL,
    DEAD_BUG,
    WOODCHOP,
    MOUNTAIN_CLIMBER,
    PALLOF_PRESS,

    // Olympic
    BARBELL_CLEAN,
    POWER_CLEAN,
    HANG_CLEAN,
    SNATCH,

    // Conditioning
    FARMER_CARRY,
    KETTLEBELL_SWING,
    BOX_JUMP,
    BURPEE,
    JUMP_ROPE,
    BATTLE_ROPE,
    SLED_PUSH,
    ;

    val type: String get() = name.lowercase().replace("_", "-")

    companion object {
        fun fromStored(value: String): ExerciseType? {
            val trimmed = value.trim()
            if (trimmed.isEmpty()) return null
            val normalized = trimmed.uppercase().replace('-', '_')
            return entries.find { it.name == normalized }
        }
    }
}

fun ExerciseType.displayName(): String =
    ExerciseCatalog.definitionOrNull(this)?.displayName
        ?: name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
