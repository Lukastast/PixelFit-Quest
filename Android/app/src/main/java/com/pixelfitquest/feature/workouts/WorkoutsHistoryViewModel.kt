package com.pixelfitquest.feature.workouts

import androidx.lifecycle.viewModelScope
import com.pixelfitquest.feature.workout.model.Workout
import com.pixelfitquest.firebase.repository.WorkoutRepository
import com.pixelfitquest.viewmodel.PixelFitViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutsHistoryViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
) : PixelFitViewModel() {

    private val _workouts = MutableStateFlow<List<Workout>>(emptyList())
    val workouts: StateFlow<List<Workout>> = _workouts.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadWorkouts()
    }

    fun loadWorkouts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                workoutRepository.getWorkouts().collect { list ->
                    _workouts.value = list.sortedByDescending { it.date }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }
}
