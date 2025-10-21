package com.pixelfitquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelfitquest.model.WorkoutPlan
import com.pixelfitquest.model.WorkoutPlanItem
import com.pixelfitquest.model.WorkoutTemplate
import com.pixelfitquest.model.WorkoutType
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

    fun toggleExercise(exercise: WorkoutType, sets: Int) {
        val currentSelections = _uiState.value.selections
        val updated = if (currentSelections.containsKey(exercise)) {
            currentSelections - exercise
        } else {
            currentSelections + (exercise to sets.coerceIn(1, 10))
        }
        _uiState.value = _uiState.value.copy(selections = updated)
    }

    fun setTemplateName(name: String) {
        _uiState.value = _uiState.value.copy(templateName = name)
    }

    fun saveTemplate() {
        val state = _uiState.value
        if (state.selections.isEmpty() || state.templateName.isBlank()) return

        val plan = WorkoutPlan(state.selections.map { (exercise, sets) -> WorkoutPlanItem(exercise, sets) })
        val template = WorkoutTemplate(
            id = generateId(),
            name = state.templateName,
            plan = plan
        )

        viewModelScope.launch {
            try {
                templateRepository.saveTemplate(template)
                // FIXED: No loadTemplates() call—real-time Flow handles refresh
                _uiState.value = state.copy(isSaving = false, error = null)
            } catch (e: Exception) {
                _uiState.value = state.copy(error = e.message)
            }
        }
    }

    fun loadTemplate(template: WorkoutTemplate) {
        val selections = template.plan.items.associate { it.exercise to it.sets }
        _uiState.value = _uiState.value.copy(
            selections = selections,
            templateName = template.name,
            editMode = true,
            editingTemplateId = template.id
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
            WorkoutPlan(state.selections.map { (exercise, sets) -> WorkoutPlanItem(exercise, sets) })
        } else null
    }

    private fun generateId(): String = "template_${System.currentTimeMillis()}"

    data class CustomizationUiState(
        val selections: Map<WorkoutType, Int> = emptyMap(),
        val templateName: String = "",
        val isSaving: Boolean = false,
        val error: String? = null,
        val editMode: Boolean = false,
        val editingTemplateId: String? = null
    )
}