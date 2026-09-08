package com.pixelfitquest.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class LocalWorkoutEntity(
    @PrimaryKey val id: String,
    val date: String,
    val payloadJson: String,
)

@Entity(
    tableName = "exercises",
    indices = [Index("workoutId")],
)
data class LocalExerciseEntity(
    @PrimaryKey val id: String,
    val workoutId: String,
    val payloadJson: String,
)

@Entity(
    tableName = "workout_sets",
    indices = [Index("workoutId"), Index("exerciseId")],
)
data class LocalSetEntity(
    @PrimaryKey val id: String,
    val workoutId: String,
    val exerciseId: String,
    val payloadJson: String,
)

@Entity(tableName = "templates")
data class LocalTemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: String,
    val payloadJson: String,
)
