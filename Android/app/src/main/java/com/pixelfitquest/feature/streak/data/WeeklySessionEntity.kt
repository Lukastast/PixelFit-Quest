package com.pixelfitquest.feature.streak.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weekly_sessions",
    indices = [Index(value = ["weekStartIso"])],
)
data class WeeklySessionEntity(
    @PrimaryKey val workoutId: String,
    val completedAtEpochMillis: Long,
    val weekStartIso: String,
)
