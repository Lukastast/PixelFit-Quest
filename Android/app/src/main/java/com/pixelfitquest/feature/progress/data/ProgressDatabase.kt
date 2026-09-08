package com.pixelfitquest.feature.progress.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [LiftHistoryEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class ProgressDatabase : RoomDatabase() {
    abstract fun liftHistoryDao(): LiftHistoryDao
}
