package com.pixelfitquest.feature.levels.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.pixelfitquest.feature.levels.model.CosmeticCatalog
import com.pixelfitquest.feature.levels.model.LevelsPersistedState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Equipped cosmetics only. XP/level live on [com.pixelfitquest.local.db.entity.UserProfileEntity].
 */
@Entity(tableName = "level_state")
data class LevelStateEntity(
    @PrimaryKey val id: Int = 1,
    val equippedHomeThemeId: String = CosmeticCatalog.DEFAULT_HOME_ID,
    val equippedCharacterSkinId: String = CosmeticCatalog.DEFAULT_SKIN_ID,
    val equippedTitleId: String = CosmeticCatalog.DEFAULT_TITLE_ID,
)

@Entity(tableName = "unlocked_cosmetic")
data class UnlockedCosmeticEntity(
    @PrimaryKey val cosmeticId: String,
    val unlockedAtEpochMs: Long,
)

@Dao
interface LevelsDao {
    @Query("SELECT * FROM level_state WHERE id = 1")
    fun observeState(): Flow<LevelStateEntity?>

    @Query("SELECT * FROM level_state WHERE id = 1")
    suspend fun getState(): LevelStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertState(entity: LevelStateEntity)

    @Query("SELECT * FROM unlocked_cosmetic")
    fun observeUnlocks(): Flow<List<UnlockedCosmeticEntity>>

    @Query("SELECT * FROM unlocked_cosmetic")
    suspend fun getUnlocks(): List<UnlockedCosmeticEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUnlocks(items: List<UnlockedCosmeticEntity>)
}

/**
 * Cosmetics equip + unlock rows. [totalXp] on [LevelsPersistedState] is not persisted here —
 * repository fills it from user_profile in memory via LevelCurve.
 */
interface LevelsStore {
    fun observe(): Flow<LevelsPersistedState>
    suspend fun load(): LevelsPersistedState
    suspend fun save(state: LevelsPersistedState)
}

@Singleton
class RoomLevelsStore @Inject constructor(
    private val dao: LevelsDao,
) : LevelsStore {
    override fun observe(): Flow<LevelsPersistedState> = combine(
        dao.observeState(),
        dao.observeUnlocks(),
    ) { state, unlocks -> merge(state, unlocks) }

    override suspend fun load(): LevelsPersistedState =
        merge(dao.getState(), dao.getUnlocks())

    override suspend fun save(state: LevelsPersistedState) {
        dao.upsertState(
            LevelStateEntity(
                id = 1,
                equippedHomeThemeId = state.equippedHomeThemeId,
                equippedCharacterSkinId = state.equippedCharacterSkinId,
                equippedTitleId = state.equippedTitleId,
            ),
        )
        dao.upsertUnlocks(
            state.unlockedIds.map { id ->
                UnlockedCosmeticEntity(
                    cosmeticId = id,
                    unlockedAtEpochMs = state.unlockedAtEpochMs[id] ?: 0L,
                )
            },
        )
    }
}

class InMemoryLevelsStore : LevelsStore {
    private val state = MutableStateFlow(LevelsPersistedState())

    override fun observe(): Flow<LevelsPersistedState> = state.asStateFlow()

    override suspend fun load(): LevelsPersistedState = state.value

    override suspend fun save(next: LevelsPersistedState) {
        state.value = next
    }
}

private fun merge(
    state: LevelStateEntity?,
    unlocks: List<UnlockedCosmeticEntity>,
): LevelsPersistedState {
    val entity = state ?: LevelStateEntity()
    return LevelsPersistedState(
        totalXp = 0,
        equippedHomeThemeId = entity.equippedHomeThemeId,
        equippedCharacterSkinId = entity.equippedCharacterSkinId,
        equippedTitleId = entity.equippedTitleId,
        unlockedIds = unlocks.map { it.cosmeticId }.toSet(),
        unlockedAtEpochMs = unlocks.associate { it.cosmeticId to it.unlockedAtEpochMs },
    )
}
