package com.pixelfitquest.feature.workout.model

import com.pixelfitquest.feature.workout.analysis.DetectedRep
import com.pixelfitquest.feature.workout.analysis.SetAnalysis

data class SetReviewState(
    val analysis: SetAnalysis,
    val reps: List<DetectedRep>,
    val sampleCount: Int,
    val setNumber: Int,
    val exerciseName: String,
    val edited: Boolean = false,
) {
    val acceptedCount: Int get() = reps.count { it.accepted }
    val meanFormScore: Float
        get() {
            val accepted = reps.filter { it.accepted }
            return if (accepted.isEmpty()) 0f else accepted.map { it.formScore }.average().toFloat()
        }
}
