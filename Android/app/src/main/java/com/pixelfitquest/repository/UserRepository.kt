package com.pixelfitquest.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pixelfitquest.model.UserGameData
import com.google.firebase.firestore.toObject
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@ViewModelScoped
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val usersCollection = firestore.collection("users")

    // Real-time Flow for user data (unchanged)
    fun getUserGameData(): Flow<UserGameData?> = callbackFlow {
        val user = auth.currentUser ?: run {
            close()  // Close channel if no user
            return@callbackFlow
        }
        val listener = usersCollection.document(user.uid).addSnapshotListener { snapshot, e ->
            if (e != null) {
                trySend(null)  // Non-suspending send
            } else {
                val data = snapshot?.toObject<UserGameData>()
                trySend(data ?: UserGameData())
            }
        }
        awaitClose { listener.remove() }  // Cleanup listener on close
    }

    // One-time fetch (unchanged)
    suspend fun fetchUserGameDataOnce(): UserGameData? {
        val user = auth.currentUser ?: return null
        return try {
            val doc = usersCollection.document(user.uid).get().await()
            doc.toObject<UserGameData>() ?: UserGameData()
        } catch (e: Exception) {
            null
        }
    }

    // Initialize data for new users (unchanged)
    suspend fun initUserGameData() {
        val user = auth.currentUser ?: throw Exception("No user logged in")
        usersCollection.document(user.uid).set(UserGameData()).await()
    }

    // Update fields (updated to create if not exists)
    suspend fun updateUserGameData(updates: Map<String, Any>) {
        val user = auth.currentUser ?: throw Exception("No user logged in")
        val docRef = usersCollection.document(user.uid)
        try {
            val doc = docRef.get().await()
            if (doc.exists()) {
                docRef.update(updates).await()
            } else {
                // Create with defaults and apply updates
                val newData = UserGameData().toMutableMap()  // Convert to map for merging
                newData.putAll(updates)
                docRef.set(newData).await()
            }
        } catch (e: Exception) {
            throw e  // Let ViewModel handle
        }
    }
}

// Helper extension to convert data class to map (add if needed)
private fun UserGameData.toMutableMap(): MutableMap<String, Any> = mutableMapOf(
    "level" to level,
    "coins" to coins,
    "exp" to exp
)