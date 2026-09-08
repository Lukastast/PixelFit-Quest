package com.pixelfitquest.feature.progress.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LiftHistoryDao {
    @Query("SELECT * FROM lift_history ORDER BY timestampMillis ASC")
    suspend fun getAll(): List<LiftHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LiftHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<LiftHistoryEntity>)
}
