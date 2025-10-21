package com.pixelfitquest.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pixelfitquest.model.Workout
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

    // Save a completed workout to subcollection
    suspend fun saveWorkout(workout: Workout) {
        try {
            workoutsSubcollection.document(workout.id).set(workout.toMap()).await()
        } catch (e: Exception) {
            throw e  // Let caller handle (e.g., ViewModel)
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
                            null  // Skip corrupted docs
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