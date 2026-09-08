package com.pixelfitquest.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pixelfitquest.local.db.entity.LocalTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LocalTemplateEntity)

    @Query("SELECT * FROM templates ORDER BY createdAt DESC")
    fun observe(): Flow<List<LocalTemplateEntity>>

    @Query("SELECT * FROM templates ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getAll(limit: Int = 50): List<LocalTemplateEntity>

    @Query("SELECT * FROM templates WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): LocalTemplateEntity?

    @Query("DELETE FROM templates WHERE id = :id")
    suspend fun delete(id: String)
}
