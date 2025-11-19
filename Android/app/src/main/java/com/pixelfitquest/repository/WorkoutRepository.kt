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

    // Real-time Flow for user's workouts (all types, sorted by date desc)
    fun getWorkouts(): Flow<List<Workout>> = callbackFlow {
        val listener = workoutsSubcollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    trySend(emptyList())
                } else {
                    val workouts = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            Workout.fromMap(doc.data ?: emptyMap()) // Now resolves
                        } catch (ex: Exception) {
                            null
                        }
                    } ?: emptyList()
                    trySend(workouts)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun getAllCompletedWorkouts(): List<Workout> {
        return try {
            val snapshot = usersCollection
                .document(currentUserId())
                .collection("workouts")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    Workout.fromMap(doc.data ?: emptyMap())
                } catch (e: Exception) {
                    Log.w("WorkoutRepo", "Failed to parse workout ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("WorkoutRepo", "Failed to fetch completed workouts", e)
            emptyList()
        }
    }
    suspend fun getSetsByWorkoutId(workoutId: String): List<WorkoutSet> {
        return try {
            val exercises = getExercisesByWorkoutId(workoutId)
            Log.d("WorkoutRepo", "Found ${exercises.size} exercises for sets load on $workoutId")

            val allSets = mutableListOf<WorkoutSet>()
            exercises.forEach { exercise ->
                try {
                    val setsSnapshot = workoutsSubcollection
                        .document(workoutId)
                        .collection("exercises")
                        .document(exercise.id)
                        .collection("sets")
                        .get()
                        .await()

                    Log.d("WorkoutRepo", "TEST: Exercise.id used to load sets: '${exercise.id}'")

                    Log.d("WorkoutRepo", "Snapshot for sets under exercise ${exercise.id} has ${setsSnapshot.documents.size} docs")

                    val exerciseSets = setsSnapshot.documents.mapNotNull { doc ->
                        try {
                            val data = doc.data ?: emptyMap()
                            val enrichedData = data.toMutableMap().apply {
                                this["workoutId"] = workoutId
                                this["exerciseId"] = exercise.id
                            }
                            WorkoutSet.fromMap(enrichedData)
                        } catch (ex: Exception) {
                            Log.w("WorkoutRepo", "Failed to parse set ${doc.id}: ${ex.message}")
                            null
                        }
                    }
                    allSets.addAll(exerciseSets)
                } catch (ex: Exception) {
                    Log.e("WorkoutRepo", "Failed to load sets for exercise ${exercise.id}: ${ex.message}")
                    // Continue to next exercise—don't fail all
                }
            }

            Log.d("WorkoutRepo", "Total sets loaded: ${allSets.size}")
            allSets
        } catch (e: Exception) {
            Log.e("WorkoutRepo", "Failed to load sets for $workoutId: ${e.message}")
            emptyList()
        }
    }
    suspend fun getExercisesByWorkoutId(workoutId: String): List<Exercise> {
        return try {
            val userId = currentUserId()  // Log this
            Log.d("WorkoutRepo", "Loading exercises for userId: '$userId', workoutId: '$workoutId'")
            if (userId.isBlank()) {
                Log.e("WorkoutRepo", "currentUserId() is blank—cannot query user-specific path")
                return emptyList()
            }

            val snapshot = workoutsSubcollection  // This calls get(), so fresh userId
                .document(workoutId)
                .collection("exercises")
                .get()
                .await()

            Log.d("WorkoutRepo", "Snapshot for exercises under user $userId / workout $workoutId has ${snapshot.documents.size} docs")

            // Log first doc ID if any (for verification)
            snapshot.documents.firstOrNull()?.let { doc ->
                Log.d("WorkoutRepo", "Sample exercise doc ID: '${doc.id}', raw data keys: ${doc.data?.keys}")
            }

            val parsedExercises = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: emptyMap()
                    Log.d("WorkoutRepo", "Parsing exercise '${doc.id}' with data: $data")  // Temp: Log full map for first few; remove in prod
                    data.let { Exercise.fromMap(it) }  // No need for workoutId inject if already in map
                } catch (ex: Exception) {
                    Log.w("WorkoutRepo", "Failed to parse exercise ${doc.id}: ${ex.message}; data: ${doc.data?.keys}")
                    null
                }
            }
            Log.d("WorkoutRepo", "Parsed ${parsedExercises.size} exercises from ${snapshot.documents.size} docs")
            parsedExercises
        } catch (e: Exception) {
            Log.e("WorkoutRepo", "Failed to load exercises for $workoutId: ${e.message}")
            emptyList()
        }
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

    suspend fun getWorkout(workoutId: String): Workout? {
        return try {
            val snapshot = workoutsSubcollection  // FIXED: Use your user-specific subcollection getter
                .document(workoutId)
                .get()
                .await()  // Suspend until complete

            snapshot.data?.let { data ->
                try {
                    Workout.fromMap(data)  // Your parsing logic
                } catch (ex: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
           null
        }
    }

    suspend fun updateWorkout(workoutId: String, updates: Map<String, Any>) {
        usersCollection.document(currentUserId())
            .collection("workouts")
            .document(workoutId)
            .update(updates)
            .await()
    }

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