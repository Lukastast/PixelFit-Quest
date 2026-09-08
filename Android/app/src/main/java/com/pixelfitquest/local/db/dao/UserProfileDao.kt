package com.pixelfitquest.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pixelfitquest.local.db.entity.LOCAL_PROFILE_ID
import com.pixelfitquest.local.db.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    suspend fun get(id: String = LOCAL_PROFILE_ID): UserProfileEntity?

    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    fun observe(id: String = LOCAL_PROFILE_ID): Flow<UserProfileEntity?>
}
