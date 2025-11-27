package com.pixelfitquest.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.pixelfitquest.model.UserSettings
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

    @ViewModelScoped
    class UserSettingsRepository @Inject constructor(
        private val firestore: FirebaseFirestore,
        private val auth: FirebaseAuth
    ) {
        private val usersCollection = firestore.collection("users")

        fun getUserSettings(): Flow<UserSettings?> = callbackFlow {
            val user = auth.currentUser ?: run {
                close()  // Close channel if no user
                return@callbackFlow
            }
            val listener = usersCollection.document(user.uid).addSnapshotListener { snapshot, e ->
                if (e != null) {
                    trySend(null)  // Non-suspending send
                } else {
                    val data = snapshot?.toObject<UserSettings>()
                    trySend(data ?: UserSettings())
                }
            }
            awaitClose { listener.remove() }  // Cleanup listener on close
        }

        // One-time fetch (unchanged)
        suspend fun fetchUserSettingsOnce(): UserSettings? {
            val user = auth.currentUser ?: return null
            return try {
                val doc = usersCollection.document(user.uid).get().await()
                doc.toObject<UserSettings>() ?: UserSettings()
            } catch (e: Exception) {
                null
            }
        }

        // Initialize data for new users (unchanged)
        suspend fun initUserSettings() {
            val user = auth.currentUser ?: throw Exception("No user logged in")
            usersCollection.document(user.uid).set(UserSettings()).await()
        }

        // Update fields (updated to create if not exists)
    suspend fun updateUserSettings(updates: Map<String, Any>) {
        val user = auth.currentUser ?: throw IllegalStateException("No user logged in")
        val docRef = usersCollection.document(user.uid)
        try {
            val doc = docRef.get().await()
            if (doc.exists()) {
                docRef.update(updates).await()
            } else {
                // Create with defaults and apply updates
                val newData = UserSettings().toMutableMap()  // Convert to map for merging
                newData.putAll(updates)
                docRef.set(newData).await()
            }
        } catch (e: Exception) {
            throw e  // Let ViewModel handle
        }
    }
}
// Helper extension to convert data class to map (add if needed)
private fun UserSettings.toMutableMap(): MutableMap<String, Any> = mutableMapOf(
    "height" to height
)