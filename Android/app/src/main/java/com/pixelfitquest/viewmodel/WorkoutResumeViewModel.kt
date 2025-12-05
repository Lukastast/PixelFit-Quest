package com.pixelfitquest.viewmodel

import android.util.Log
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelfitquest.model.UserGameData
import com.pixelfitquest.model.Workout
import com.pixelfitquest.repository.UserRepository
import com.pixelfitquest.repository.WorkoutRepository
import com.pixelfitquest.ui.view.ExerciseWithSets
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutResumeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userGameData = MutableStateFlow<UserGameData?>(null)
    val userGameData: StateFlow<UserGameData?> = _userGameData.asStateFlow()
    private val workoutId: String = savedStateHandle.get<String>("workoutId") ?: ""

    private val _summary = MutableStateFlow(WorkoutSummary(0, 0, 0f))
    val summary: StateFlow<WorkoutSummary> = _summary.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private val _exercisesWithSets = MutableStateFlow<List<ExerciseWithSets>>(emptyList())
    val exercisesWithSets: StateFlow<List<ExerciseWithSets>> = _exercisesWithSets.asStateFlow()

    init {
        if (workoutId.isNotBlank()) {
            loadWorkoutData()
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                userRepository.getUserGameData().collect { data ->
                    _userGameData.value = data

                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load user data"
            }
        }
    }
    private fun loadWorkoutData() {
        viewModelScope.launch {
            val workout = workoutRepository.getWorkout(workoutId) ?: return@launch
            loadExercisesAndSets(workout)
        }
    }

    private fun loadExercisesAndSets(workout: Workout) {
        viewModelScope.launch {
            try {
                // 1. Load all exercises for this workout
                val exercises = workoutRepository.getExercisesByWorkoutId(workoutId)

                // 2. Load ALL sets for this workout (no exercise filter yet)
                val allSets = workoutRepository.getSetsByWorkoutId(workoutId)

                // 3. Group sets by exerciseId
                val groupedSets = allSets.groupBy { it.exerciseId }

                // 4. Combine exercise + its sets + calculate average score
                val exercisesWithSetsList = exercises
                    .mapNotNull { exercise ->
                        val exerciseSets = groupedSets[exercise.id] ?: emptyList()
                        if (exerciseSets.isNotEmpty()) {
                            val avgScore = exerciseSets
                                .map { it.workoutScore }
                                .average()
                                .toInt()
                                .coerceIn(0, 100)

                            ExerciseWithSets(
                                exercise = exercise,
                                sets = exerciseSets,
                                avgWorkoutScore = avgScore
                            )
                        } else null
                    }


                _exercisesWithSets.value = exercisesWithSetsList

                _summary.value = calculateSummary(exercisesWithSetsList)

                if (!workout.rewardsAwarded) {
                    awardRewards(_summary.value)
                    workoutRepository.updateWorkout(workoutId, mapOf("rewardsAwarded" to true))
                }

            } catch (e: Exception) {
                Log.e("WorkoutResumeVM", "Failed to load exercises/sets", e)
                _exercisesWithSets.value = emptyList()
            }
        }
    }

    private fun calculateSummary(exercisesWithSets: List<ExerciseWithSets>): WorkoutSummary {
        val allSets = exercisesWithSets.flatMap { it.sets }

        var totalXp = 0
        var totalReps = 0

        allSets.forEach { set ->
            val reps = set.reps.coerceAtLeast(0)
            val score = set.workoutScore.coerceIn(0f, 100f)

            totalReps += reps

            val multiplier = when {
                score >= 90 -> 2.0
                score >= 80 -> 1.5
                else -> 1.0
            }

            totalXp += (reps * multiplier).toInt()
        }

        val totalCoins = totalReps / 5
        val avgScore = allSets.map { it.workoutScore }.average().toFloat()

        return WorkoutSummary(
            totalXp = totalXp,
            totalCoins = totalCoins,
            avgScore = avgScore
        )
    }

    private fun awardRewards(summary: WorkoutSummary) {
        addXp(summary.totalXp)
        addCoins(summary.totalCoins)
    }

    private fun addXp(amount: Int) {
        if (amount <= 0) return
        viewModelScope.launch {
            try {
                userRepository.updateExp(amount)
                Log.d("ResumeVM", "Added $amount XP")
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update XP"
            }
        }
    }

    // NEW: Add Coins (inspired by HomeViewModel.addCoins)
    private fun addCoins(amount: Int) {
        if (amount <= 0) return
        viewModelScope.launch {
            try {
                val current = _userGameData.value ?: return@launch
                userRepository.updateUserGameData(mapOf("coins" to current.coins + amount))
                Log.d("ResumeVM", "Added $amount Coins")
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update coins"
            }
        }
    }


    data class WorkoutSummary(
        val totalXp: Int,
        val totalCoins: Int,
        val avgScore: Float = 0f  // NEW: For UI display
    )
}
