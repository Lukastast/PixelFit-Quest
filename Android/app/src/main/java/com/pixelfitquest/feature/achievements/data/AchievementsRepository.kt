package com.pixelfitquest.feature.achievements.data

import com.pixelfitquest.feature.achievements.model.AchievementCatalog
import com.pixelfitquest.feature.achievements.model.AchievementItem
import com.pixelfitquest.feature.achievements.model.AchievementMetric
import com.pixelfitquest.feature.achievements.model.AchievementProgress
import com.pixelfitquest.feature.achievements.model.LocalFitnessSnapshot
import com.pixelfitquest.feature.achievements.rewards.AchievementRewardSink
import com.pixelfitquest.feature.achievements.unlock.AchievementUnlocker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local achievement progress. Phone/Room is source of truth.
 * Do not add Firestore or Pro leaderboards here.
 */
interface AchievementsRepository {
    fun observeItems(): Flow<List<AchievementItem>>

    suspend fun applySnapshot(snapshot: LocalFitnessSnapshot): List<String>

    suspend fun incrementMetric(metric: AchievementMetric, delta: Long = 1L): List<String>

    suspend fun setMetric(metric: AchievementMetric, absoluteValue: Long): List<String>
}

interface AchievementProgressStore {
    fun observe(): Flow<List<AchievementProgress>>
    suspend fun load(): List<AchievementProgress>
    suspend fun saveAll(progress: List<AchievementProgress>)
}

@Singleton
class RoomAchievementProgressStore @Inject constructor(
    private val dao: AchievementDao,
) : AchievementProgressStore {
    override fun observe(): Flow<List<AchievementProgress>> =
        dao.observeAll().map { rows -> rows.map { it.toProgress() } }

    override suspend fun load(): List<AchievementProgress> =
        dao.getAll().map { it.toProgress() }

    override suspend fun saveAll(progress: List<AchievementProgress>) {
        dao.upsertAll(progress.map { it.toEntity() })
    }
}

class InMemoryAchievementProgressStore : AchievementProgressStore {
    private val state = MutableStateFlow<List<AchievementProgress>>(emptyList())

    override fun observe(): Flow<List<AchievementProgress>> = state.asStateFlow()

    override suspend fun load(): List<AchievementProgress> = state.value

    override suspend fun saveAll(progress: List<AchievementProgress>) {
        state.value = progress
    }
}

@Singleton
class DefaultAchievementsRepository @Inject constructor(
    private val store: AchievementProgressStore,
    private val rewardSink: AchievementRewardSink,
) : AchievementsRepository {

    override fun observeItems(): Flow<List<AchievementItem>> =
        store.observe().map { rows -> mergeWithCatalog(rows) }

    override suspend fun applySnapshot(snapshot: LocalFitnessSnapshot): List<String> {
        val existing = store.load().associateBy { it.achievementId }
        val evaluation = AchievementUnlocker.evaluate(
            existing = existing,
            snapshot = snapshot,
            nowEpochMs = System.currentTimeMillis(),
        )
        val withRewards = evaluation.updated.map { progress ->
            if (progress.achievementId !in evaluation.newlyUnlockedIds) {
                return@map progress
            }
            val definition = AchievementCatalog.byId(progress.achievementId) ?: return@map progress
            val granted = rewardSink.grant(definition.id, definition.reward)
            if (granted) progress.copy(rewardGranted = true) else progress
        }
        store.saveAll(withRewards)
        return evaluation.newlyUnlockedIds
    }

    override suspend fun incrementMetric(metric: AchievementMetric, delta: Long): List<String> {
        val current = store.load()
            .filter { row -> AchievementCatalog.byId(row.achievementId)?.metric == metric }
            .maxOfOrNull { it.currentValue } ?: 0L
        return applySnapshot(LocalFitnessSnapshot().copyFor(metric, current + delta))
    }

    override suspend fun setMetric(metric: AchievementMetric, absoluteValue: Long): List<String> {
        return applySnapshot(LocalFitnessSnapshot().copyFor(metric, absoluteValue))
    }

    private fun mergeWithCatalog(rows: List<AchievementProgress>): List<AchievementItem> {
        val byId = rows.associateBy { it.achievementId }
        return AchievementCatalog.all.map { definition ->
            AchievementItem(
                definition = definition,
                progress = byId[definition.id] ?: AchievementProgress(definition.id),
            )
        }
    }
}
