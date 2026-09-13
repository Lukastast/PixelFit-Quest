package com.pixelfitquest.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pixelfitquest.feature.achievements.data.AchievementDao
import com.pixelfitquest.feature.achievements.data.AchievementProgressEntity
import com.pixelfitquest.feature.progress.data.LiftHistoryDao
import com.pixelfitquest.feature.progress.data.LiftHistoryEntity
import com.pixelfitquest.feature.streak.data.WeeklySessionEntity
import com.pixelfitquest.feature.streak.data.WeeklyStreakDao
import com.pixelfitquest.feature.streak.data.WeeklyStreakStateEntity
import com.pixelfitquest.local.db.dao.TemplateDao
import com.pixelfitquest.local.db.dao.UserProfileDao
import com.pixelfitquest.local.db.dao.WorkoutDao
import com.pixelfitquest.local.db.entity.LocalExerciseEntity
import com.pixelfitquest.local.db.entity.LocalSetEntity
import com.pixelfitquest.local.db.entity.LocalTemplateEntity
import com.pixelfitquest.local.db.entity.LocalWorkoutEntity
import com.pixelfitquest.local.db.entity.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        LocalWorkoutEntity::class,
        LocalExerciseEntity::class,
        LocalSetEntity::class,
        LocalTemplateEntity::class,
        LiftHistoryEntity::class,
        WeeklySessionEntity::class,
        WeeklyStreakStateEntity::class,
        AchievementProgressEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class PixelFitDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun templateDao(): TemplateDao
    abstract fun liftHistoryDao(): LiftHistoryDao
    abstract fun weeklyStreakDao(): WeeklyStreakDao
    abstract fun achievementDao(): AchievementDao
}
