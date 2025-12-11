package com.pixelfitquest.model


import com.pixelfitquest.model.enums.ExerciseType
import com.pixelfitquest.model.workout.WorkoutPlanItem

data class CustomizationUiState(
    val selections: Map<ExerciseType, WorkoutPlanItem> = emptyMap(),
    val templateName: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val editMode: Boolean = false,
    val editingTemplateId: String? = null
)
