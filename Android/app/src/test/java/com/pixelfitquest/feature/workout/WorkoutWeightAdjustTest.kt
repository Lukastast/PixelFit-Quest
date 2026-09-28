package com.pixelfitquest.feature.workout

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutWeightAdjustTest {

    @Test
    fun weightAdjustIncrementsCorrectly() {
        val initialWeight = 60f
        val delta = 2.5f
        val newWeight = Math.round((initialWeight + delta).coerceAtLeast(0f) * 100f) / 100f
        assertEquals(62.5f, newWeight)
    }

    @Test
    fun weightAdjustDecrementsCorrectly() {
        val initialWeight = 60f
        val delta = -5f
        val newWeight = Math.round((initialWeight + delta).coerceAtLeast(0f) * 100f) / 100f
        assertEquals(55f, newWeight)
    }

    @Test
    fun weightAdjustCannotDropBelowZero() {
        val initialWeight = 2.5f
        val delta = -5f
        val newWeight = Math.round((initialWeight + delta).coerceAtLeast(0f) * 100f) / 100f
        assertEquals(0f, newWeight)
    }

    @Test
    fun weightAdjustAvoidsFloatingPointImprecision() {
        var weight = 20f
        // Add 1.25 multiple times
        repeat(3) {
            weight = Math.round((weight + 1.25f).coerceAtLeast(0f) * 100f) / 100f
        }
        assertEquals(23.75f, weight)
    }

    @Test
    fun progressiveOverloadSuggestedWhenRepThresholdMet() {
        val repThreshold = 10
        val lastReps = 10
        val isWeightSuggestionEnabled = true

        val shouldSuggest = isWeightSuggestionEnabled && lastReps >= repThreshold
        org.junit.Assert.assertTrue(shouldSuggest)
    }

    @Test
    fun progressiveOverloadSuggestedWhenRepThresholdExceeded() {
        val repThreshold = 10
        val lastReps = 12
        val isWeightSuggestionEnabled = true

        val shouldSuggest = isWeightSuggestionEnabled && lastReps >= repThreshold
        org.junit.Assert.assertTrue(shouldSuggest)
    }

    @Test
    fun progressiveOverloadNotSuggestedWhenRepThresholdNotMet() {
        val repThreshold = 10
        val lastReps = 8
        val isWeightSuggestionEnabled = true

        val shouldSuggest = isWeightSuggestionEnabled && lastReps >= repThreshold
        org.junit.Assert.assertFalse(shouldSuggest)
    }

    @Test
    fun progressiveOverloadNotSuggestedWhenSuggestionsDisabled() {
        val repThreshold = 10
        val lastReps = 12
        val isWeightSuggestionEnabled = false

        val shouldSuggest = isWeightSuggestionEnabled && lastReps >= repThreshold
        org.junit.Assert.assertFalse(shouldSuggest)
    }

    @Test
    fun customRepThresholdHonoredForProgression() {
        val customThreshold = 8
        val lastReps = 8
        val isWeightSuggestionEnabled = true

        val shouldSuggest = isWeightSuggestionEnabled && lastReps >= customThreshold
        org.junit.Assert.assertTrue(shouldSuggest)
    }

    @Test
    fun applyingProgressionSuggestionAddsTwoPointFiveKg() {
        val currentWeight = 80f
        val suggestionDelta = 2.5f
        val newWeight = Math.round((currentWeight + suggestionDelta) * 10f) / 10f
        assertEquals(82.5f, newWeight)
    }
}
