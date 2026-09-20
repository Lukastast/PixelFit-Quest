package com.pixelfitquest.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pixelfitquest.local.db.entity.LocalExerciseEntity
import com.pixelfitquest.local.db.entity.LocalSetEntity
import com.pixelfitquest.local.db.entity.LocalWorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWorkout(entity: LocalWorkoutEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExercise(entity: LocalExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSet(entity: LocalSetEntity)

    @Query("SELECT * FROM workouts WHERE id IN (SELECT DISTINCT workoutId FROM exercises) ORDER BY date DESC")
    fun observeWorkouts(): Flow<List<LocalWorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE id IN (SELECT DISTINCT workoutId FROM exercises) ORDER BY date DESC")
    suspend fun getAllWorkouts(): List<LocalWorkoutEntity>

    @Query("SELECT * FROM workouts WHERE id IN (SELECT DISTINCT workoutId FROM exercises) ORDER BY date DESC LIMIT :limit")
    suspend fun getWorkouts(limit: Int): List<LocalWorkoutEntity>

    @Query("DELETE FROM workouts WHERE id NOT IN (SELECT DISTINCT workoutId FROM exercises)")
    suspend fun deleteWorkoutsWithoutExercises()

    @Query("SELECT * FROM workouts WHERE id = :id LIMIT 1")
    suspend fun getWorkout(id: String): LocalWorkoutEntity?

    @Query("SELECT * FROM exercises WHERE workoutId = :workoutId")
    suspend fun getExercises(workoutId: String): List<LocalExerciseEntity>

    @Query("SELECT * FROM workout_sets WHERE workoutId = :workoutId")
    suspend fun getSets(workoutId: String): List<LocalSetEntity>

    @Query("DELETE FROM workouts WHERE id = :id")
    suspend fun deleteWorkout(id: String)

    @Query("DELETE FROM exercises WHERE workoutId = :workoutId")
    suspend fun deleteExercises(workoutId: String)

    @Query("DELETE FROM workout_sets WHERE workoutId = :workoutId")
    suspend fun deleteSets(workoutId: String)
}
