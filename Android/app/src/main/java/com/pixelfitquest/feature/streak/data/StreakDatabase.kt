package com.pixelfitquest.feature.streak.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        WeeklySessionEntity::class,
        WeeklyStreakStateEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class StreakDatabase : RoomDatabase() {
    abstract fun weeklyStreakDao(): WeeklyStreakDao
}
