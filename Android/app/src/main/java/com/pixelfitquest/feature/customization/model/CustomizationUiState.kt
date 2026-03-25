package com.pixelfitquest.feature.customization.model


import com.pixelfitquest.feature.workout.model.enums.ExerciseType
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlanItem

data class CustomizationUiState(
    val selections: Map<ExerciseType, WorkoutPlanItem> = emptyMap(),
    val templateName: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val editMode: Boolean = false,
    val editingTemplateId: String? = null
)
