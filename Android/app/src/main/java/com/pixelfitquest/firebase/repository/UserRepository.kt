package com.pixelfitquest.firebase.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.pixelfitquest.feature.customization.model.CharacterData
import com.pixelfitquest.firebase.model.UserData
import com.pixelfitquest.local.CloudSyncPolicy
import com.pixelfitquest.local.LocalPixelFitStore
import com.pixelfitquest.local.UserProgression
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.ConcurrentHashMap

@ViewModelScoped
class UserRepository constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val localStore: LocalPixelFitStore,
    private val cloudSyncPolicy: CloudSyncPolicy,
) {
    private val usersCollection = firestore.collection("users")
    private val TAG = "UserRepository"

    companion object {
        private const val DEFAULT_BASE_EXP = 100
        private const val MAX_LEVEL = 30
        private const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L
        private val cachedProgression = ConcurrentHashMap<Int, Int>()

        @JvmStatic
        fun clearCacheForTesting() {
            cachedProgression.clear()
        }
    }

    fun getUserData(): Flow<UserData?> = localStore.observeUserData().map { it }

    suspend fun fetchUserDataOnce(): UserData? = localStore.getUserData()

    suspend fun initUserData() {
        localStore.ensureProfile()
    }

    suspend fun updateUserData(updates: Map<String, Any>) {
        localStore.updateUserData(updates)
    }

    suspend fun getUserField(field: String): Any? = localStore.getUserField(field)

    suspend fun loadProgressionConfig() {
        try {
            val configDoc = firestore.collection("configs").document("game_progression").get().await()
            val progressionMap = configDoc.get("levels") as? Map<String, Long> ?: emptyMap()
            cachedProgression.clear()
            progressionMap.forEach { (levelStr, expReq) ->
                val level = levelStr.toIntOrNull()
                if (level != null && level <= MAX_LEVEL) {
                    cachedProgression[level] = expReq.toInt()
                }
            }
            if (cachedProgression.isEmpty()) {
                initializeDefaultProgression()
            }
            Log.d(TAG, "Loaded progression config")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load config, using defaults", e)
            if (cachedProgression.isEmpty()) {
                initializeDefaultProgression()
            }
        }
    }

    private fun initializeDefaultProgression() {
        repeat(MAX_LEVEL) { level ->
            cachedProgression[level + 1] = DEFAULT_BASE_EXP * (level + 1)
        }
    }

    fun getMaxLevel(): Int = MAX_LEVEL

    fun getExpRequiredForLevel(level: Int): Int {
        return cachedProgression[level] ?: (DEFAULT_BASE_EXP * level)
    }

    suspend fun updateExp(amount: Int) {
        if (amount <= 0) return
        localStore.replaceProfile { current ->
            val result = UserProgression.applyExp(
                level = current.level,
                exp = current.exp,
                amount = amount,
                maxLevel = MAX_LEVEL,
                expRequiredForLevel = { getExpRequiredForLevel(it) },
            )
            current.copy(level = result.level, exp = result.exp)
        }
    }

    suspend fun updateStreak(increment: Boolean = true, reset: Boolean = false) {
        localStore.replaceProfile { current ->
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val today = dateFormat.format(Date())
            val yesterday = dateFormat.format(Date(System.currentTimeMillis() - MILLIS_PER_DAY))
            val result = UserProgression.applyStreak(
                currentStreak = current.streak,
                lastActivityDate = current.lastActivityDate,
                today = today,
                yesterday = yesterday,
                increment = increment,
                reset = reset,
            )
            current.copy(
                streak = result.streak,
                lastActivityDate = result.lastActivityDate,
            )
        }
    }

    suspend fun saveCharacterData(data: CharacterData) {
        localStore.saveCharacter(data)
    }

    fun getCharacterData(): Flow<CharacterData?> = localStore.observeCharacter().map { it }

    suspend fun fetchCharacterDataOnce(): CharacterData? = localStore.getCharacter()

    suspend fun resetUnlockedVariants() {
        localStore.resetUnlockedVariants()
    }

    suspend fun getLeaderboard(): List<Pair<String, UserData>> {
        if (!cloudSyncPolicy.isLeaderboardEnabled()) {
            return emptyList()
        }
        val user = auth.currentUser ?: return emptyList()
        return try {
            val snapshot = usersCollection.get().await()
            snapshot.documents.mapNotNull { doc ->
                val data = doc.toObject<UserData>() ?: return@mapNotNull null
                Pair(doc.id, data)
            }.sortedWith(
                compareByDescending<Pair<String, UserData>> { it.second.level }
                    .thenByDescending { it.second.exp }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch leaderboard", e)
            emptyList()
        }.also { _ ->
            Log.d(TAG, "Leaderboard fetch for uid=${user.uid}")
        }
    }
}
