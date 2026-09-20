package com.pixelfitquest.feature.workouts

import androidx.lifecycle.viewModelScope
import com.pixelfitquest.feature.workout.model.Workout
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutTemplate
import com.pixelfitquest.firebase.repository.WorkoutRepository
import com.pixelfitquest.firebase.repository.WorkoutTemplateRepository
import com.pixelfitquest.viewmodel.PixelFitViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutsHistoryViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val templateRepository: WorkoutTemplateRepository,
) : PixelFitViewModel() {

    private val _workouts = MutableStateFlow<List<Workout>>(emptyList())
    val workouts: StateFlow<List<Workout>> = _workouts.asStateFlow()

    private val _templates = MutableStateFlow<List<WorkoutTemplate>>(emptyList())
    val templates: StateFlow<List<WorkoutTemplate>> = _templates.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadWorkouts()
        loadTemplates()
    }

    fun loadWorkouts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                workoutRepository.getWorkouts().collect { list ->
                    _workouts.value = list.filter { it.totalExercises > 0 }.sortedByDescending { it.date }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    private fun loadTemplates() {
        viewModelScope.launch {
            try {
                templateRepository.getTemplates().collectLatest { list ->
                    _templates.value = list
                }
            } catch (_: Exception) {
            }
        }
    }

    fun deleteTemplate(templateId: String) {
        viewModelScope.launch {
            templateRepository.deleteTemplate(templateId)
        }
    }
}
