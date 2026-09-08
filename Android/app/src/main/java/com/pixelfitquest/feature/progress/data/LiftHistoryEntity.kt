package com.pixelfitquest.feature.progress.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pixelfitquest.feature.progress.model.LiftSetRecord
import com.pixelfitquest.feature.workout.model.WorkoutSet
import com.pixelfitquest.feature.workout.model.enums.ExerciseType

@Entity(
    tableName = "lift_history",
    indices = [
        Index(value = ["exerciseType", "timestampMillis"]),
        Index(value = ["workoutId"]),
    ],
)
data class LiftHistoryEntity(
    @PrimaryKey val id: String,
    val workoutId: String,
    val exerciseType: String,
    val timestampMillis: Long,
    val weightKg: Float,
    val reps: Int,
    val romScore: Float,
    val stabilityScore: Float,
)

fun LiftHistoryEntity.toRecord(): LiftSetRecord = LiftSetRecord(
    id = id,
    workoutId = workoutId,
    exerciseType = exerciseType,
    timestampMillis = timestampMillis,
    weightKg = weightKg,
    reps = reps,
    romScore = romScore,
    stabilityScore = stabilityScore,
)

fun LiftSetRecord.toEntity(): LiftHistoryEntity = LiftHistoryEntity(
    id = id,
    workoutId = workoutId,
    exerciseType = exerciseType,
    timestampMillis = timestampMillis,
    weightKg = weightKg,
    reps = reps,
    romScore = romScore,
    stabilityScore = stabilityScore,
)

fun WorkoutSet.toLiftHistoryEntity(exerciseType: ExerciseType): LiftHistoryEntity = LiftHistoryEntity(
    id = id,
    workoutId = workoutId,
    exerciseType = exerciseType.type,
    timestampMillis = timestamp,
    weightKg = weight,
    reps = reps,
    romScore = romScore,
    stabilityScore = stabilityScore,
)
