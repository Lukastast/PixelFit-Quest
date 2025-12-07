package com.pixelfitquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelfitquest.model.CustomizationUiState
import com.pixelfitquest.model.enums.ExerciseType
import com.pixelfitquest.model.workout.WorkoutPlan
import com.pixelfitquest.model.workout.WorkoutPlanItem
import com.pixelfitquest.model.workout.WorkoutTemplate
import com.pixelfitquest.repository.WorkoutTemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutCustomizationViewModel @Inject constructor(
    private val templateRepository: WorkoutTemplateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomizationUiState())
    val uiState: StateFlow<CustomizationUiState> = _uiState.asStateFlow()

    private val _templates = MutableStateFlow<List<WorkoutTemplate>>(emptyList())
    val templates: StateFlow<List<WorkoutTemplate>> = _templates.asStateFlow()

    init {
        viewModelScope.launch {
            templateRepository.getTemplates().collectLatest { templatesList ->
                _templates.value = templatesList
            }
        }
    }

    fun toggleExercise(exercise: ExerciseType, sets: Int = 3, weight: Float = 0f) {
        val currentSelections = _uiState.value.selections
        val updated = if (currentSelections.containsKey(exercise)) {
            currentSelections - exercise
        } else {
            val item = WorkoutPlanItem(exercise, sets.coerceIn(1, 10), weight.coerceIn(0f, 500f))
            currentSelections + (exercise to item)
        }
        _uiState.value = _uiState.value.copy(selections = updated)
    }

    fun updateSets(exercise: ExerciseType, sets: Int) {
        if (sets < 1) return
        val currentSelections = _uiState.value.selections
        if (currentSelections.containsKey(exercise)) {
            val currentItem = currentSelections[exercise]!!
            val updatedItem = currentItem.copy(sets = sets.coerceIn(1, 10))
            val updated = currentSelections + (exercise to updatedItem)
            _uiState.value = _uiState.value.copy(selections = updated)
        }
    }

    fun updateWeight(exercise: ExerciseType, weight: Float) {
        if (weight < 0) return
        val currentSelections = _uiState.value.selections
        if (currentSelections.containsKey(exercise)) {
            val currentItem = currentSelections[exercise]!!
            val updatedItem = currentItem.copy(weight = weight.coerceIn(0f, 500f))
            val updated = currentSelections + (exercise to updatedItem)
            _uiState.value = _uiState.value.copy(selections = updated)
        }
    }

    fun setTemplateName(name: String) {
        _uiState.value = _uiState.value.copy(templateName = name)
    }

    fun saveTemplate() {
        val state = _uiState.value
        if (state.selections.isEmpty() || state.templateName.isBlank()) return

        viewModelScope.launch {
            // Clear error and set saving true at the start of each save attempt
            _uiState.value = state.copy(isSaving = true, error = null)

            // Check for duplicate name
            val existing = templateRepository.fetchTemplateByName(state.templateName)
            if (existing != null && existing.id != state.editingTemplateId) {
                _uiState.value = state.copy(
                    isSaving = false,
                    error = "A template with this name already exists"
                )
                return@launch
            }

            val plan = WorkoutPlan(state.selections.values.toList())  // Direct toList() of items

            val id = if (state.editMode && state.editingTemplateId != null) {
                state.editingTemplateId
            } else {
                generateId()
            }

            val template = WorkoutTemplate(
                id = id,
                name = state.templateName,
                plan = plan
            )

            try {
                templateRepository.saveTemplate(template)
                _uiState.value = state.copy(
                    isSaving = false,
                    error = null,
                    editMode = false,
                    editingTemplateId = null,
                    selections = emptyMap(),
                    templateName = ""
                )
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isSaving = false,
                    error = e.message
                )
            }
        }
    }

    fun loadTemplate(template: WorkoutTemplate) {
        val selections = template.plan.items.associateBy { it.exercise }
        _uiState.value = _uiState.value.copy(
            selections = selections,
            templateName = template.name,
            editMode = true,
            editingTemplateId = template.id
        )
    }

    fun clearTemplate() {
        _uiState.value = _uiState.value.copy(
            selections = emptyMap(),
            templateName = "",
            editMode = false,
            editingTemplateId = null
        )
    }

    fun deleteTemplate(templateId: String) {
        viewModelScope.launch {
            templateRepository.deleteTemplate(templateId)
        }
    }

    fun getWorkoutPlan(): WorkoutPlan? {
        val state = _uiState.value
        return if (state.selections.isNotEmpty()) {
            WorkoutPlan(state.selections.values.toList())
        } else null
    }

    private fun generateId(): String = "template_${System.currentTimeMillis()}"
}