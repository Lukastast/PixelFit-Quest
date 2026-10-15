package com.pixelfitquest.feature.workout

import android.content.SharedPreferences

/**
 * Preference helper for pre-workout weight check and smart weight progression suggestions.
 * - When pre-workout check is enabled (default: true), starting a workout prompts the loadout page.
 * - When weight suggestion is enabled (default: true), the loadout page suggests +2.5 kg when the user
 *   achieved the rep target in their latest session.
 * - The rep target threshold defaults to 10 reps ("make 10 the standard"), and is adjustable in Settings.
 * - The suggestion is only removable in Settings ("make the suggestion only removable in settings").
 */
object WorkoutWeightPrefs {
    const val PREFS_NAME = "pixelfitquest_prefs"

    const val KEY_PRE_WORKOUT_CHECK_ENABLED = "pre_workout_weight_check_enabled"
    const val DEFAULT_PRE_WORKOUT_CHECK_ENABLED = true

    const val KEY_WEIGHT_SUGGESTION_ENABLED = "weight_suggestion_enabled"
    const val DEFAULT_WEIGHT_SUGGESTION_ENABLED = true

    const val KEY_WEIGHT_SUGGESTION_REP_THRESHOLD = "weight_suggestion_rep_threshold"
    const val DEFAULT_WEIGHT_SUGGESTION_REP_THRESHOLD = 10

    const val MIN_REP_THRESHOLD = 5
    const val MAX_REP_THRESHOLD = 30

    const val PRESET_STRENGTH_REPS = 5
    const val PRESET_HYPERTROPHY_DEFAULT_REPS = 10
    const val PRESET_HYPERTROPHY_HIGH_REPS = 12

    enum class ProgressionPreset {
        STRENGTH,
        HYPERTROPHY,
        CUSTOM;

        companion object {
            fun fromThreshold(reps: Int): ProgressionPreset = when {
                reps == PRESET_STRENGTH_REPS -> STRENGTH
                reps in 10..12 -> HYPERTROPHY
                else -> CUSTOM
            }
        }
    }

    fun isPreWorkoutCheckEnabled(prefs: SharedPreferences): Boolean =
        prefs.getBoolean(KEY_PRE_WORKOUT_CHECK_ENABLED, DEFAULT_PRE_WORKOUT_CHECK_ENABLED)

    fun setPreWorkoutCheckEnabled(prefs: SharedPreferences, enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PRE_WORKOUT_CHECK_ENABLED, enabled).apply()
    }

    fun isEnabled(prefs: SharedPreferences): Boolean = isPreWorkoutCheckEnabled(prefs)
    fun setEnabled(prefs: SharedPreferences, enabled: Boolean) = setPreWorkoutCheckEnabled(prefs, enabled)

    fun isWeightSuggestionEnabled(prefs: SharedPreferences): Boolean =
        prefs.getBoolean(KEY_WEIGHT_SUGGESTION_ENABLED, DEFAULT_WEIGHT_SUGGESTION_ENABLED)

    fun setWeightSuggestionEnabled(prefs: SharedPreferences, enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WEIGHT_SUGGESTION_ENABLED, enabled).apply()
    }

    fun getRepThreshold(prefs: SharedPreferences): Int =
        prefs.getInt(KEY_WEIGHT_SUGGESTION_REP_THRESHOLD, DEFAULT_WEIGHT_SUGGESTION_REP_THRESHOLD)

    fun setRepThreshold(prefs: SharedPreferences, threshold: Int) {
        prefs.edit().putInt(KEY_WEIGHT_SUGGESTION_REP_THRESHOLD, threshold.coerceIn(MIN_REP_THRESHOLD, MAX_REP_THRESHOLD)).apply()
    }

    fun getProgressionPreset(prefs: SharedPreferences): ProgressionPreset =
        ProgressionPreset.fromThreshold(getRepThreshold(prefs))

    fun applyStrengthPreset(prefs: SharedPreferences) {
        setRepThreshold(prefs, PRESET_STRENGTH_REPS)
    }

    fun applyHypertrophyPreset(prefs: SharedPreferences) {
        val current = getRepThreshold(prefs)
        val target = if (current == PRESET_HYPERTROPHY_DEFAULT_REPS) {
            PRESET_HYPERTROPHY_HIGH_REPS
        } else {
            PRESET_HYPERTROPHY_DEFAULT_REPS
        }
        setRepThreshold(prefs, target)
    }
}
