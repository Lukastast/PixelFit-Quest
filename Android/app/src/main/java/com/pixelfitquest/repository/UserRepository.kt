package com.pixelfitquest.repository

import android.util.Log

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.pixelfitquest.model.CharacterData
import com.pixelfitquest.model.UserData
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@ViewModelScoped
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
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
    
    fun getUserData(): Flow<UserData?> = callbackFlow {
        val user = auth.currentUser ?: run {
            close()
            return@callbackFlow
        }
        val listener = usersCollection.document(user.uid).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e(TAG, "Error loading user data", e)
                trySend(null)
            } else {
                val data = snapshot?.toObject<UserData>()
                trySend(data ?: UserData())
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun fetchUserDataOnce(): UserData? {
        val user = auth.currentUser ?: return null
        return try {
            val doc = usersCollection.document(user.uid).get().await()
            doc.toObject<UserData>() ?: UserData()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch user data", e)
            null
        }
    }

    suspend fun initUserData() {
        val user = auth.currentUser ?: throw Exception("No user logged in")
        try {
            usersCollection.document(user.uid).set(UserData()).await()
            Log.d(TAG, "Initialized user data")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init user data", e)
            throw e
        }
    }

    suspend fun updateUserData(updates: Map<String, Any>) {
        val user = auth.currentUser ?: throw Exception("No user logged in")
        val docRef = usersCollection.document(user.uid)
        try {
            val doc = docRef.get().await()
            if (doc.exists()) {
                docRef.update(updates).await()
            } else {
                // Create with defaults if not exists
                val defaultData = UserData().toMutableMap()
                defaultData.putAll(updates)
                docRef.set(defaultData).await()
            }
            Log.d(TAG, "Updated user data: $updates")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update user data", e)
            throw e
        }
    }

    suspend fun getUserField(field: String): Any? {
        val user = auth.currentUser ?: return null
        return try {
            val doc = usersCollection.document(user.uid).get().await()
            doc.get(field)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get field $field", e)
            null
        }
    }

    suspend fun loadProgressionConfig() {
        try {
            val configDoc = firestore.collection("configs").document("game_progression").get().await()
            val progressionMap = configDoc.get("levels") as? Map<String, Long> ?: emptyMap()
            cachedProgression.clear()  // Safe concurrent clear
            progressionMap.forEach { (levelStr, expReq) ->
                val level = levelStr.toIntOrNull()
                if (level != null && level <= MAX_LEVEL) {
                    cachedProgression[level] = expReq.toInt()  // Concurrent put
                }
            }
            if (cachedProgression.isEmpty()) {
                initializeDefaultProgression()
            }
            Log.d(TAG, "Loaded progression config")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load config, using defaults", e)
            // Populate fallback as above if not already done
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

        val user = auth.currentUser ?: throw Exception("No user logged in")
        val docRef = usersCollection.document(user.uid)
        try {
            val snapshot = docRef.get().await()
            val currentData = snapshot.toObject<UserData>() ?: UserData()
            var currentLevel = currentData.level.coerceAtMost(MAX_LEVEL)  // Cap on load
            var currentExp = currentData.exp

            var newExp = currentExp + amount
            while (true) {
                if (currentLevel >= MAX_LEVEL) {

                    val maxLevelExp = getExpRequiredForLevel(MAX_LEVEL)
                    newExp = newExp.coerceAtMost(maxLevelExp)
                    break
                }
                val expRequiredForNext = getExpRequiredForLevel(currentLevel)
                if (newExp >= expRequiredForNext) {
                    newExp -= expRequiredForNext
                    currentLevel++
                } else {
                    break
                }
            }
            currentLevel = currentLevel.coerceAtMost(MAX_LEVEL)

            val updates = mapOf(
                "level" to currentLevel,
                "exp" to newExp
            )
            docRef.update(updates).await()
            Log.d(TAG, "Updated exp: +$amount, new level: $currentLevel (capped at $MAX_LEVEL), new exp: $newExp")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update exp", e)
            throw e
        }
    }

    suspend fun updateStreak(increment: Boolean = true, reset: Boolean = false) {
        val user = auth.currentUser ?: throw Exception("No user logged in")
        val docRef = usersCollection.document(user.uid)
        try {
            val snapshot = docRef.get().await()

            if (!snapshot.exists()) {
                docRef.set(UserData()).await()
                Log.d(TAG, "Created new user document for streak update")
            }

            val currentData = snapshot.toObject<UserData>() ?: UserData()
            val currentStreak = currentData.streak
            val lastActivityDate = snapshot.getString("last_activity_date") ?: ""

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val today = dateFormat.format(Date())
            val yesterday = dateFormat.format(Date(System.currentTimeMillis() - MILLIS_PER_DAY))

            var newStreak = currentStreak
            var newLastActivityDate = today

            if (reset) {
                newStreak = 0
            } else if (increment) {
                if (lastActivityDate.isEmpty()) {
                    newStreak = 1
                } else if (lastActivityDate == today) {
                } else if (lastActivityDate == yesterday) {
                    newStreak++
                } else {
                    newStreak = 1
                }
                newLastActivityDate = today
            }

            val updates = mapOf(
                "streak" to newStreak,
                "last_activity_date" to newLastActivityDate
            )
            docRef.update(updates).await()
            Log.d(TAG, "Updated streak to $newStreak for UTC date $today (last: $lastActivityDate)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update streak", e)
            throw e
        }
    }

    suspend fun saveCharacterData(data: CharacterData) {
        val user = auth.currentUser ?: throw Exception("No user logged in")
        try {
            usersCollection.document(user.uid).update(
                "character", mapOf(
                    "gender" to data.gender,
                    "variant" to data.variant,
                    "unlockedVariants" to data.unlockedVariants
                )
            ).await()
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
                val gender = charData?.get("gender") as? String ?: "male"
                val variant = charData?.get("variant") as? String ?: "basic"
                val unlockedVariants = charData?.get("unlockedVariants") as? List<String> ?: listOf("basic")
                trySend(CharacterData(gender, variant, unlockedVariants))
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
                    variant = map["variant"] as? String ?: "basic",
                    unlockedVariants = map["unlockedVariants"] as? List<String> ?: listOf("basic")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch character data", e)
            null
        }
    }

    suspend fun resetUnlockedVariants() {
        val user = auth.currentUser ?: return
        try {
            usersCollection.document(user.uid).update(
                "character.unlockedVariants", listOf("basic")
            ).await()
            Log.d(TAG, "Reset unlocked variants to basic")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reset unlocked variants", e)
        }
    }

    private fun UserData.toMutableMap(): MutableMap<String, Any> = mutableMapOf(
        "level" to level,
        "coins" to coins,
        "exp" to exp,
        "streak" to streak,
        "height" to height
    )
    
    suspend fun getLeaderboard(): List<Pair<String, UserData>> {
        return try {
            val snapshot = usersCollection.get().await()
            snapshot.documents.mapNotNull { doc ->
                val data = doc.toObject<UserData>() ?: return@mapNotNull null
                Pair(doc.id, data)
            }.sortedWith(compareByDescending<Pair<String, UserData>> { it.second.level }.thenByDescending { it.second.exp })
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch leaderboard", e)
            emptyList()
        }
    }
}