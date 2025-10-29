package com.pixelfitquest.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.pixelfitquest.model.UserGameData
import com.pixelfitquest.model.CharacterData
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
    private val TAG = "UserRepository"

    // Game Data (level, coins, exp)

    fun getUserGameData(): Flow<UserGameData?> = callbackFlow {
        val user = auth.currentUser ?: run {
            close()
            return@callbackFlow
        }
        val listener = usersCollection.document(user.uid).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e(TAG, "Error loading game data", e)
                trySend(null)
            } else {
                val data = snapshot?.toObject<UserGameData>()
                trySend(data ?: UserGameData())
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun fetchUserGameDataOnce(): UserGameData? {
        val user = auth.currentUser ?: return null
        return try {
            val doc = usersCollection.document(user.uid).get().await()
            doc.toObject<UserGameData>() ?: UserGameData()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch game data", e)
            null
        }
    }

    suspend fun initUserGameData() {
        val user = auth.currentUser ?: throw Exception("No user logged in")
        try {
            usersCollection.document(user.uid).set(UserGameData()).await()
            Log.d(TAG, "Initialized game data")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init game data", e)
            throw e
        }
    }

    suspend fun updateUserGameData(updates: Map<String, Any>) {
        val user = auth.currentUser ?: throw Exception("No user logged in")
        val docRef = usersCollection.document(user.uid)
        try {
            val doc = docRef.get().await()
            if (doc.exists()) {
                docRef.update(updates).await()
            } else {
                // Create with defaults if not exists
                val defaultData = UserGameData().toMutableMap()
                defaultData.putAll(updates)
                docRef.set(defaultData).await()
            }
            Log.d(TAG, "Updated game data: $updates")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update game data", e)
            throw e
        }
    }

    // Character Data (gender, variants)

    // In UserRepository class (replace character methods)
    suspend fun saveCharacterData(data: CharacterData) {
        val user = auth.currentUser ?: throw Exception("No user logged in")
        try {
            usersCollection.document(user.uid).update("character", data).await()
        } catch (e: Exception) {
            throw e
        }
    }

    fun getCharacterData(): Flow<CharacterData?> = callbackFlow {
        val user = auth.currentUser ?: run {
            close()
            return@callbackFlow
        }
        val listener = usersCollection.document(user.uid).addSnapshotListener { snapshot, e ->
            if (e != null) {
                trySend(null)
            } else {
                val charData = snapshot?.get("character") as? Map<String, Any?>
                val gender = charData?.get("gender") as? String ?: "female"
                trySend(CharacterData(gender = gender))
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun fetchCharacterDataOnce(): CharacterData? {
        val user = auth.currentUser ?: return null
        return try {
            val doc = usersCollection.document(user.uid).get().await()
            val charData = doc.get("character") as? Map<String, Any?>
            charData?.let { map ->
                CharacterData(
                    gender = map["gender"] as? String ?: "male",
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch character data", e)
            null
        }
    }

    private fun UserGameData.toMutableMap(): MutableMap<String, Any> = mutableMapOf(
        "level" to level,
        "coins" to coins,
        "exp" to exp
    )
}