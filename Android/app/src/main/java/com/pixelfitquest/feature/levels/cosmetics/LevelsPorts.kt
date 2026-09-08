package com.pixelfitquest.feature.levels.cosmetics

import com.pixelfitquest.feature.levels.model.LevelProgress
import com.pixelfitquest.feature.levels.model.LevelUpResult
import kotlinx.coroutines.flow.Flow

/**
 * Grant XP on the phone. Workout / home call this.
 * Must not require login and must not call Firebase.
 */
interface LocalXpPort {
    suspend fun awardXp(amount: Int, reason: String = ""): LevelUpResult

    fun observeProgress(): Flow<LevelProgress>
}

/**
 * Home reads the equipped background id. Lives in the levels package so
 * home/achievements agents do not own the catalog.
 */
fun interface HomeThemePort {
    fun observeEquippedThemeId(): Flow<String>
}

/**
 * Customization overlay: which skins the local level ladder has unlocked.
 * Coin purchases stay in CustomizationViewModel.
 */
interface CharacterSkinPort {
    fun observeUnlockedSkinIds(): Flow<Set<String>>

    fun observeEquippedSkinId(): Flow<String>

    suspend fun equipSkin(id: String): Boolean
}

/**
 * Pro cloud leaderboard / multi-device XP mirror.
 * Default is a no-op — do not wire Firestore here.
 */
fun interface CloudProgressMirror {
    suspend fun mirror(progress: LevelProgress)
}

class NoOpCloudProgressMirror : CloudProgressMirror {
    override suspend fun mirror(progress: LevelProgress) = Unit
}
