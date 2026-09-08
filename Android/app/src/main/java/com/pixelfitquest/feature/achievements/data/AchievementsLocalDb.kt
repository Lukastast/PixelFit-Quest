package com.pixelfitquest.feature.achievements.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import com.pixelfitquest.feature.achievements.model.AchievementProgress
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "achievement_progress")
data class AchievementProgressEntity(
    @PrimaryKey val achievementId: String,
    val currentValue: Long = 0L,
    val unlockedAtEpochMs: Long? = null,
    val rewardGranted: Boolean = false,
)

fun AchievementProgressEntity.toProgress(): AchievementProgress = AchievementProgress(
    achievementId = achievementId,
    currentValue = currentValue,
    unlockedAtEpochMs = unlockedAtEpochMs,
    rewardGranted = rewardGranted,
)

fun AchievementProgress.toEntity(): AchievementProgressEntity = AchievementProgressEntity(
    achievementId = achievementId,
    currentValue = currentValue,
    unlockedAtEpochMs = unlockedAtEpochMs,
    rewardGranted = rewardGranted,
)

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievement_progress")
    fun observeAll(): Flow<List<AchievementProgressEntity>>

    @Query("SELECT * FROM achievement_progress")
    suspend fun getAll(): List<AchievementProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<AchievementProgressEntity>)
}

@Database(
    entities = [AchievementProgressEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AchievementsDatabase : RoomDatabase() {
    abstract fun achievementDao(): AchievementDao
}
