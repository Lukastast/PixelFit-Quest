package com.pixelfitquest.feature.levels.data

import com.pixelfitquest.feature.levels.cosmetics.CharacterSkinPort
import com.pixelfitquest.feature.levels.cosmetics.CloudProgressMirror
import com.pixelfitquest.feature.levels.cosmetics.HomeThemePort
import com.pixelfitquest.feature.levels.cosmetics.LocalXpPort
import com.pixelfitquest.feature.levels.model.CosmeticCatalog
import com.pixelfitquest.feature.levels.model.CosmeticItem
import com.pixelfitquest.feature.levels.model.CosmeticKind
import com.pixelfitquest.feature.levels.model.EquippedCosmetics
import com.pixelfitquest.feature.levels.model.LevelProgress
import com.pixelfitquest.feature.levels.model.LevelUpResult
import com.pixelfitquest.feature.levels.model.LevelsPersistedState
import com.pixelfitquest.feature.levels.model.LevelsSnapshot
import com.pixelfitquest.feature.levels.progression.CosmeticUnlocker
import com.pixelfitquest.feature.levels.progression.LevelCurve
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cosmetics + XP. XP/level SoT is user_profile via [ProfileXpSource].
 * Cosmetics rows live on pixelfit.db (level_state / unlocked_cosmetic).
 */
interface LevelsRepository : LocalXpPort, HomeThemePort, CharacterSkinPort {
    fun observeSnapshot(): Flow<LevelsSnapshot>

    fun observePendingLevelUp(): Flow<LevelUpResult?>

    suspend fun equipCosmetic(id: String): Boolean

    suspend fun importRemoteIfEmpty(remoteLevel: Int, remoteXpIntoLevel: Int)

    suspend fun dismissLevelUp()
}

@Singleton
class DefaultLevelsRepository @Inject constructor(
    private val store: LevelsStore,
    private val xpSource: ProfileXpSource,
    private val cloudMirror: CloudProgressMirror,
) : LevelsRepository {
    private val mutex = Mutex()
    private val pendingLevelUp = MutableStateFlow<LevelUpResult?>(null)

    override fun observeSnapshot(): Flow<LevelsSnapshot> = flow {
        ensureBaseline()
        combine(
            xpSource.observeProgress(),
            store.observe(),
        ) { progress, cosmetics ->
            toSnapshot(progress, withBaseline(cosmetics, progress.level))
        }.collect { emit(it) }
    }

    override fun observeProgress(): Flow<LevelProgress> =
        observeSnapshot().map { it.progress }.distinctUntilChanged()

    override fun observeEquippedThemeId(): Flow<String> =
        observeSnapshot().map { it.equipped.homeThemeId }.distinctUntilChanged()

    override fun observeUnlockedSkinIds(): Flow<Set<String>> =
        observeSnapshot()
            .map { snapshot ->
                snapshot.items
                    .filter { it.definition.kind == CosmeticKind.CHARACTER_SKIN && it.unlocked }
                    .map { it.definition.id }
                    .toSet()
            }
            .distinctUntilChanged()

    override fun observeEquippedSkinId(): Flow<String> =
        observeSnapshot().map { it.equipped.characterSkinId }.distinctUntilChanged()

    override fun observePendingLevelUp(): Flow<LevelUpResult?> = pendingLevelUp.asStateFlow()

    override suspend fun awardXp(amount: Int, reason: String): LevelUpResult {
        return mutex.withLock {
            val (previous, current) = xpSource.award(amount)
            val newly = if (amount <= 0) {
                emptyList()
            } else {
                CosmeticUnlocker.newlyUnlocked(previous.level, current.level)
            }
            val loaded = withBaseline(store.load(), current.level)
            val stamp = System.currentTimeMillis()
            val unlockedAt = loaded.unlockedAtEpochMs.toMutableMap()
            newly.forEach { def ->
                if (def.id !in unlockedAt) unlockedAt[def.id] = stamp
            }
            val unlockedIds = loaded.unlockedIds +
                newly.map { it.id } +
                CosmeticUnlocker.unlockedIds(current.level)
            val saved = loaded.copy(
                unlockedIds = unlockedIds,
                unlockedAtEpochMs = unlockedAt,
            )
            store.save(saved)
            val result = LevelUpResult(
                previous = previous,
                current = current,
                newlyUnlocked = newly,
                coinsGranted = LevelCurve.coinsForLevels(previous.level, current.level),
            )
            if (result.leveledUp) {
                pendingLevelUp.value = result
            }
            runCatching { cloudMirror.mirror(current) }
            result
        }
    }

    override suspend fun equipCosmetic(id: String): Boolean = mutex.withLock {
        val progress = xpSource.loadProgress()
        val loaded = withBaseline(store.load(), progress.level)
        val definition = CosmeticCatalog.byId(id) ?: return@withLock false
        if (id !in loaded.unlockedIds) return@withLock false
        val next = when (definition.kind) {
            CosmeticKind.HOME_THEME -> loaded.copy(equippedHomeThemeId = id)
            CosmeticKind.CHARACTER_SKIN -> loaded.copy(equippedCharacterSkinId = id)
            CosmeticKind.TITLE -> loaded.copy(equippedTitleId = id)
        }
        store.save(next)
        true
    }

    override suspend fun equipSkin(id: String): Boolean = equipCosmetic(id)

    override suspend fun importRemoteIfEmpty(remoteLevel: Int, remoteXpIntoLevel: Int) {
        mutex.withLock {
            val seeded = xpSource.seedIfEmpty(remoteLevel, remoteXpIntoLevel)
            if (!seeded) return@withLock
            val progress = xpSource.loadProgress()
            store.save(withBaseline(store.load(), progress.level))
        }
    }

    override suspend fun dismissLevelUp() {
        pendingLevelUp.value = null
    }

    private suspend fun ensureBaseline() {
        mutex.withLock {
            val progress = xpSource.loadProgress()
            val loaded = store.load()
            val next = withBaseline(loaded, progress.level)
            if (next != loaded) {
                store.save(next)
            }
        }
    }

    private fun withBaseline(state: LevelsPersistedState, level: Int): LevelsPersistedState {
        val required = CosmeticUnlocker.unlockedIds(level)
        val stamp = System.currentTimeMillis()
        val unlockedAt = state.unlockedAtEpochMs.toMutableMap()
        required.forEach { id ->
            if (id !in unlockedAt) unlockedAt[id] = stamp
        }
        val home = equippedOrDefault(
            state.equippedHomeThemeId,
            required,
            CosmeticCatalog.DEFAULT_HOME_ID,
        )
        val skin = equippedOrDefault(
            state.equippedCharacterSkinId,
            required,
            CosmeticCatalog.DEFAULT_SKIN_ID,
        )
        val title = equippedOrDefault(
            state.equippedTitleId,
            required,
            CosmeticCatalog.DEFAULT_TITLE_ID,
        )
        return state.copy(
            unlockedIds = state.unlockedIds + required,
            unlockedAtEpochMs = unlockedAt,
            equippedHomeThemeId = home,
            equippedCharacterSkinId = skin,
            equippedTitleId = title,
        )
    }

    private fun equippedOrDefault(
        current: String,
        unlocked: Set<String>,
        defaultId: String,
    ): String = if (current in unlocked) current else defaultId

    private fun toSnapshot(progress: LevelProgress, state: LevelsPersistedState): LevelsSnapshot {
        val equipped = EquippedCosmetics(
            homeThemeId = state.equippedHomeThemeId,
            characterSkinId = state.equippedCharacterSkinId,
            titleId = state.equippedTitleId,
        )
        val items = CosmeticCatalog.all.map { definition ->
            CosmeticItem(
                definition = definition,
                unlocked = definition.id in state.unlockedIds ||
                    definition.unlockLevel <= progress.level,
                equipped = definition.id == when (definition.kind) {
                    CosmeticKind.HOME_THEME -> equipped.homeThemeId
                    CosmeticKind.CHARACTER_SKIN -> equipped.characterSkinId
                    CosmeticKind.TITLE -> equipped.titleId
                },
            )
        }
        return LevelsSnapshot(
            progress = progress,
            equipped = equipped,
            items = items,
        )
    }
}
