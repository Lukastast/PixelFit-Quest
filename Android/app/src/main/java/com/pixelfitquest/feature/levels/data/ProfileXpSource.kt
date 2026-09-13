package com.pixelfitquest.feature.levels.data

import com.pixelfitquest.feature.levels.model.LevelProgress
import com.pixelfitquest.feature.levels.progression.LevelCurve
import com.pixelfitquest.local.LocalPixelFitStore
import com.pixelfitquest.local.UserProgression
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single XP/level wallet: [com.pixelfitquest.local.db.entity.UserProfileEntity].
 * LevelCurve totalXp is derived in memory only for cosmetic unlock math / HUD.
 */
interface ProfileXpSource {
    fun observeProgress(): Flow<LevelProgress>
    suspend fun loadProgress(): LevelProgress
    suspend fun award(amount: Int): Pair<LevelProgress, LevelProgress>
    suspend fun seedIfEmpty(remoteLevel: Int, remoteXpIntoLevel: Int): Boolean
}

@Singleton
class LocalProfileXpSource @Inject constructor(
    private val localStore: LocalPixelFitStore,
) : ProfileXpSource {
    override fun observeProgress(): Flow<LevelProgress> =
        localStore.observeUserData()
            .onStart { localStore.ensureProfile() }
            .map { data -> progressFromLevelExp(data.level, data.exp) }

    override suspend fun loadProgress(): LevelProgress {
        val profile = localStore.ensureProfile()
        return progressFromLevelExp(profile.level, profile.exp)
    }

    override suspend fun award(amount: Int): Pair<LevelProgress, LevelProgress> {
        val previous = loadProgress()
        if (amount <= 0) return previous to previous
        localStore.replaceProfile { current ->
            val result = UserProgression.applyExp(
                level = current.level,
                exp = current.exp,
                amount = amount,
                maxLevel = LevelCurve.MAX_LEVEL,
                expRequiredForLevel = { LevelCurve.xpToAdvance(it) },
            )
            current.copy(level = result.level, exp = result.exp)
        }
        val current = loadProgress()
        return previous to current
    }

    override suspend fun seedIfEmpty(remoteLevel: Int, remoteXpIntoLevel: Int): Boolean {
        val profile = localStore.ensureProfile()
        val empty = profile.level <= 1 && profile.exp <= 0
        if (!empty) return false
        val safeLevel = remoteLevel.coerceIn(1, LevelCurve.MAX_LEVEL)
        val into = remoteXpIntoLevel.coerceAtLeast(0)
            .coerceAtMost(LevelCurve.xpToAdvance(safeLevel))
        if (safeLevel <= 1 && into <= 0) return false
        localStore.replaceProfile { current ->
            current.copy(level = safeLevel, exp = into)
        }
        return true
    }
}

/** In-memory wallet for unit tests (no Room / profile DB). */
class InMemoryProfileXpSource(
    initialTotalXp: Int = 0,
) : ProfileXpSource {
    private val totalXp = MutableStateFlow(initialTotalXp.coerceIn(0, LevelCurve.maxTotalXp()))

    override fun observeProgress(): Flow<LevelProgress> =
        totalXp.asStateFlow().map { LevelCurve.progressFromTotalXp(it) }

    override suspend fun loadProgress(): LevelProgress =
        LevelCurve.progressFromTotalXp(totalXp.value)

    override suspend fun award(amount: Int): Pair<LevelProgress, LevelProgress> {
        val previous = loadProgress()
        if (amount <= 0) return previous to previous
        totalXp.value = (totalXp.value + amount).coerceAtMost(LevelCurve.maxTotalXp())
        return previous to loadProgress()
    }

    override suspend fun seedIfEmpty(remoteLevel: Int, remoteXpIntoLevel: Int): Boolean {
        if (totalXp.value > 0) return false
        totalXp.value = LevelCurve.totalXpFromRemote(remoteLevel, remoteXpIntoLevel)
        return true
    }
}

fun progressFromLevelExp(level: Int, exp: Int): LevelProgress {
    val total = LevelCurve.totalXpFromRemote(level, exp)
    return LevelCurve.progressFromTotalXp(total)
}
