package com.pixelfitquest.feature.workouts

import androidx.lifecycle.viewModelScope
import com.pixelfitquest.feature.progress.data.LiftHistoryDao
import com.pixelfitquest.feature.progress.data.ProgressRepository
import com.pixelfitquest.feature.progress.model.ProgressOverview
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
    private val progressRepository: ProgressRepository,
    private val liftHistoryDao: LiftHistoryDao,
) : PixelFitViewModel() {

    private val _workouts = MutableStateFlow<List<Workout>>(emptyList())
    val workouts: StateFlow<List<Workout>> = _workouts.asStateFlow()

    private val _templates = MutableStateFlow<List<WorkoutTemplate>>(emptyList())
    val templates: StateFlow<List<WorkoutTemplate>> = _templates.asStateFlow()

    private val _progressionOverview = MutableStateFlow<ProgressOverview?>(null)
    val progressionOverview: StateFlow<ProgressOverview?> = _progressionOverview.asStateFlow()

    private val _exerciseLastReps = MutableStateFlow<Map<String, Int>>(emptyMap())
    val exerciseLastReps: StateFlow<Map<String, Int>> = _exerciseLastReps.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadWorkouts()
        loadTemplates()
        loadProgression()
    }

    fun loadWorkouts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                workoutRepository.getWorkouts().collect { list ->
                    _workouts.value = list.filter { it.totalExercises > 0 }.sortedByDescending { it.date }
                    _isLoading.value = false
                    loadProgression()
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    fun loadProgression() {
        viewModelScope.launch {
            try {
                val overview = progressRepository.loadOverview()
                _progressionOverview.value = overview
            } catch (_: Exception) {
            }
        }
        loadExerciseLastReps()
    }

    fun loadExerciseLastReps() {
        viewModelScope.launch {
            try {
                val records = liftHistoryDao.getAll()
                if (records.isEmpty()) {
                    _exerciseLastReps.value = emptyMap()
                    return@launch
                }
                val grouped = records.groupBy { it.exerciseType }
                val resultMap = mutableMapOf<String, Int>()

                for ((exerciseType, list) in grouped) {
                    val latestRecord = list.maxByOrNull { it.timestampMillis } ?: continue
                    val latestWorkoutId = latestRecord.workoutId
                    val sessionSets = if (latestWorkoutId.isNotBlank()) {
                        list.filter { it.workoutId == latestWorkoutId }
                    } else {
                        val sessionWindow = 3 * 3600 * 1000L
                        list.filter { kotlin.math.abs(it.timestampMillis - latestRecord.timestampMillis) <= sessionWindow }
                    }
                    val maxRepsInSession = sessionSets.maxOfOrNull { it.reps } ?: 0
                    resultMap[exerciseType] = maxRepsInSession
                }
                _exerciseLastReps.value = resultMap
            } catch (_: Exception) {
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
