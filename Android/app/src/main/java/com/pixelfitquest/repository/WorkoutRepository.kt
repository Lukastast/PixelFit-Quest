package com.pixelfitquest.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pixelfitquest.model.Exercise
import com.pixelfitquest.model.Workout
import com.pixelfitquest.model.WorkoutSet
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@ViewModelScoped
class WorkoutRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val workoutsSubcollection get() = usersCollection.document(currentUserId()).collection("workouts")

    private val usersCollection = firestore.collection("users")

    private fun currentUserId(): String = auth.currentUser?.uid ?: throw IllegalStateException("No user logged in")

    suspend fun saveWorkout(workout: Workout) {
        try {
            workoutsSubcollection.document(workout.id).set(workout.toMap()).await()
        } catch (e: Exception) {
            throw e  // Let caller handle (e.g., ViewModel)
        }
    }

    suspend fun saveExercise(exercise: Exercise) {
        usersCollection.document(currentUserId())
            .collection("workouts").document(exercise.workoutId)
            .collection("exercises").document(exercise.id)
            .set(exercise.toMap()).await()
    }

    suspend fun saveSet(set: WorkoutSet) {
        usersCollection.document(currentUserId())
            .collection("workouts").document(set.workoutId)
            .collection("exercises").document(set.exerciseId)
            .collection("sets").document(set.id).set(set.toMap()).await()
        Log.d("WorkoutRepo", "Saved set ${set.id} under workout ${set.workoutId}/exercise ${set.exerciseId}")
    }
    suspend fun saveFullWorkout(workout: Workout, exercises: List<Exercise>, sets: Map<String, List<WorkoutSet>>) {
        saveWorkout(workout)
        exercises.forEach { saveExercise(it) }
        sets.forEach { (exerciseId, setList) ->
            setList.forEach { saveSet(it) }
        }
    }
    // Real-time Flow for user's workouts (all types, sorted by date desc)
    fun getWorkouts(): Flow<List<Workout>> = callbackFlow {
        val listener = workoutsSubcollection
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    trySend(emptyList())
                } else {
                    val workouts = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            Workout.fromMap(doc.data ?: emptyMap())
                        } catch (ex: Exception) {
                            null
                        }
                    } ?: emptyList()
                    trySend(workouts)
                }
            }
        awaitClose { listener.remove() }
    }

    // One-time fetch (e.g., for offline or specific queries)
    suspend fun fetchWorkoutsOnce(limit: Int = 50): List<Workout> {
        return try {
            val snapshot = workoutsSubcollection
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                try {
                    Workout.fromMap(doc.data ?: emptyMap())
                } catch (ex: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Delete a workout (no auto-rollback; handle aggregates manually if needed)
    suspend fun deleteWorkout(workoutId: String) {
        try {
            workoutsSubcollection.document(workoutId).delete().await()
        } catch (e: Exception) {
            throw e
        }
    }

    // Optional: Filtered fetch (e.g., by type)
    suspend fun fetchWorkoutsByType(type: String, limit: Int = 20): List<Workout> {
        return try {
            val snapshot = workoutsSubcollection
                .whereEqualTo("type", type)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                try {
                    Workout.fromMap(doc.data ?: emptyMap())
                } catch (ex: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}