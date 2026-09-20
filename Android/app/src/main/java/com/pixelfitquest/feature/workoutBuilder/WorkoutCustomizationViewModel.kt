package com.pixelfitquest.feature.workoutBuilder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelfitquest.feature.customization.model.CustomizationUiState
import com.pixelfitquest.feature.workout.model.enums.ExerciseType
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlan
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlanItem
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutTemplate
import com.pixelfitquest.firebase.repository.WorkoutTemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutCustomizationViewModel @Inject constructor(
    private val templateRepository: WorkoutTemplateRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val initialTemplateId: String? = savedStateHandle.get<String>("templateId")
    private val isTemplateArg: Boolean = savedStateHandle.get<Boolean>("isTemplate") ?: false

    private val _uiState = MutableStateFlow(
        CustomizationUiState(
            isTemplateMode = isTemplateArg || !initialTemplateId.isNullOrBlank()
        )
    )
    val uiState: StateFlow<CustomizationUiState> = _uiState.asStateFlow()

    private val _templates = MutableStateFlow<List<WorkoutTemplate>>(emptyList())
    val templates: StateFlow<List<WorkoutTemplate>> = _templates.asStateFlow()

    private val _startWorkoutEvent = MutableSharedFlow<Pair<WorkoutPlan, String>>(extraBufferCapacity = 1)
    val startWorkoutEvent: SharedFlow<Pair<WorkoutPlan, String>> = _startWorkoutEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            templateRepository.getTemplates().collectLatest { templatesList ->
                _templates.value = templatesList
                if (!initialTemplateId.isNullOrBlank() && _uiState.value.editingTemplateId == null) {
                    val template = templatesList.firstOrNull { it.id == initialTemplateId }
                    if (template != null) {
                        loadTemplate(template)
                    }
                }
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

    fun saveTemplate(onSuccess: (() -> Unit)? = null) {
        val state = _uiState.value
        if (state.selections.isEmpty()) {
            _uiState.value = state.copy(error = "Please select at least one exercise")
            return
        }

        var name = state.templateName.trim()

        viewModelScope.launch {
            // Clear error and set saving true at the start of each save attempt
            _uiState.value = state.copy(isSaving = true, error = null)

            if (name.isBlank()) {
                var candidate = "Custom Routine"
                var counter = 2
                while (templateRepository.fetchTemplateByName(candidate) != null) {
                    candidate = "Custom Routine $counter"
                    counter++
                }
                name = candidate
            } else {
                // Check for duplicate name
                val existing = templateRepository.fetchTemplateByName(name)
                if (existing != null && existing.id != state.editingTemplateId) {
                    _uiState.value = state.copy(
                        isSaving = false,
                        error = "A template with this name already exists"
                    )
                    return@launch
                }
            }

            val plan = WorkoutPlan(state.selections.values.toList())

            val id = if (state.editMode && state.editingTemplateId != null) {
                state.editingTemplateId
            } else {
                generateId()
            }

            val template = WorkoutTemplate(
                id = id,
                name = name,
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
                    templateName = "",
                    saveSuccess = true
                )
                onSuccess?.invoke()
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isSaving = false,
                    error = e.message,
                    saveSuccess = false
                )
            }
        }
    }

    fun resetSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }

    fun loadTemplateById(id: String) {
        viewModelScope.launch {
            val template = _templates.value.firstOrNull { it.id == id }
                ?: templateRepository.fetchTemplatesOnce().firstOrNull { it.id == id }
            if (template != null) {
                loadTemplate(template)
            }
        }
    }

    fun loadTemplate(template: WorkoutTemplate) {
        val selections = template.plan.items.associateBy { it.exercise }
        _uiState.value = _uiState.value.copy(
            selections = selections,
            templateName = template.name,
            editMode = true,
            editingTemplateId = template.id,
            isTemplateMode = true
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

    /**
     * Starts the workout, auto-saving as a template first when a name has been entered.
     * This ensures "Save as Template → Start Workout" always works even after the form
     * has been cleared by [saveTemplate].
     */
    fun startWorkout() {
        val state = _uiState.value
        val plan = getWorkoutPlan() ?: return
        val name = state.templateName.ifBlank { "Workout" }

        if (state.templateName.isNotBlank() && state.selections.isNotEmpty()) {
            // Auto-save the template, then start. If the name already exists, upsert it.
            viewModelScope.launch {
                _uiState.value = state.copy(isSaving = true, error = null)
                try {
                    val existing = templateRepository.fetchTemplateByName(state.templateName)
                    val id = when {
                        state.editMode && state.editingTemplateId != null -> state.editingTemplateId
                        existing != null -> existing.id
                        else -> generateId()
                    }
                    val template = WorkoutTemplate(id = id, name = name, plan = plan)
                    templateRepository.saveTemplate(template)
                    _uiState.value = state.copy(isSaving = false)
                } catch (e: Exception) {
                    // Save failed — still start the workout so the user isn't blocked.
                    _uiState.value = state.copy(isSaving = false)
                }
                _startWorkoutEvent.emit(plan to name)
            }
        } else {
            // No name entered — start without saving.
            viewModelScope.launch {
                _startWorkoutEvent.emit(plan to name)
            }
        }
    }

    private fun generateId(): String = "template_${System.currentTimeMillis()}"
}