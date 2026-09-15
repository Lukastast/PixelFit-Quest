package com.pixelfitquest.feature.streak.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyStreakDao {
    @Query("SELECT * FROM weekly_streak_state WHERE id = 1")
    fun observeState(): Flow<WeeklyStreakStateEntity?>

    @Query("SELECT * FROM weekly_streak_state WHERE id = 1")
    suspend fun getState(): WeeklyStreakStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertState(state: WeeklyStreakStateEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSession(session: WeeklySessionEntity): Long

    @Query("DELETE FROM weekly_sessions WHERE workoutId = :workoutId")
    suspend fun deleteSession(workoutId: String)

    @Query("SELECT COUNT(*) FROM weekly_sessions WHERE weekStartIso = :weekStartIso")
    suspend fun countSessionsInWeek(weekStartIso: String): Int

    @Query("SELECT COUNT(*) FROM weekly_sessions WHERE weekStartIso = :weekStartIso")
    fun observeSessionCountInWeek(weekStartIso: String): Flow<Int>
}
