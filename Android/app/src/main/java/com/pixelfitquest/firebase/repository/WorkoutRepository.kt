package com.pixelfitquest.firebase.repository

import android.util.Log
import com.pixelfitquest.feature.workout.model.Exercise
import com.pixelfitquest.feature.workout.model.Workout
import com.pixelfitquest.feature.workout.model.WorkoutSet
import com.pixelfitquest.local.LocalPixelFitStore
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ViewModelScoped
class WorkoutRepository @Inject constructor(
    private val localStore: LocalPixelFitStore,
) {
    suspend fun saveWorkout(workout: Workout) {
        localStore.saveWorkout(workout)
    }

    suspend fun saveExercise(exercise: Exercise) {
        localStore.saveExercise(exercise)
    }

    suspend fun saveSet(set: WorkoutSet) {
        localStore.saveSet(set)
        Log.d("WorkoutRepo", "Saved set ${set.id} under workout ${set.workoutId}/exercise ${set.exerciseId}")
    }

    fun getWorkouts(): Flow<List<Workout>> = localStore.observeWorkouts()

    suspend fun getAllCompletedWorkouts(): List<Workout> {
        return try {
            localStore.getAllWorkouts()
        } catch (e: Exception) {
            Log.e("WorkoutRepo", "Failed to fetch completed workouts", e)
            emptyList()
        }
    }

    suspend fun getSetsByWorkoutId(workoutId: String): List<WorkoutSet> {
        return try {
            localStore.getSets(workoutId)
        } catch (e: Exception) {
            Log.e("WorkoutRepo", "Failed to load sets for $workoutId", e)
            emptyList()
        }
    }

    suspend fun getExercisesByWorkoutId(workoutId: String): List<Exercise> {
        return try {
            localStore.getExercises(workoutId)
        } catch (e: Exception) {
            Log.e("WorkoutRepo", "Failed to load exercises for $workoutId", e)
            emptyList()
        }
    }

    suspend fun getAllExercises(): List<Exercise> {
        return try {
            localStore.getAllExercises()
        } catch (e: Exception) {
            Log.e("WorkoutRepo", "Failed to load all exercises", e)
            emptyList()
        }
    }

    suspend fun fetchWorkoutsOnce(limit: Int = 50): List<Workout> {
        return try {
            localStore.getWorkouts(limit)
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun deleteWorkout(workoutId: String) {
        localStore.deleteWorkout(workoutId)
    }

    suspend fun getWorkout(workoutId: String): Workout? {
        return try {
            localStore.getWorkout(workoutId)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun updateWorkout(workoutId: String, updates: Map<String, Any>) {
        localStore.updateWorkout(workoutId, updates)
    }

    suspend fun fetchWorkoutsByType(type: String, limit: Int = 20): List<Workout> {
        return fetchWorkoutsOnce(limit).filter { it.name.equals(type, ignoreCase = true) }
    }
}
