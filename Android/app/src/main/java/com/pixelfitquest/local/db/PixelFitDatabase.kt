package com.pixelfitquest.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pixelfitquest.feature.achievements.data.AchievementDao
import com.pixelfitquest.feature.achievements.data.AchievementProgressEntity
import com.pixelfitquest.feature.levels.data.LevelStateEntity
import com.pixelfitquest.feature.levels.data.LevelsDao
import com.pixelfitquest.feature.levels.data.UnlockedCosmeticEntity
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
        LevelStateEntity::class,
        UnlockedCosmeticEntity::class,
    ],
    version = 8,
    exportSchema = false,
)
abstract class PixelFitDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun templateDao(): TemplateDao
    abstract fun liftHistoryDao(): LiftHistoryDao
    abstract fun weeklyStreakDao(): WeeklyStreakDao
    abstract fun achievementDao(): AchievementDao
    abstract fun levelsDao(): LevelsDao

    companion object {
        val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN lastSleepRewardDate TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN lastWeeklyHeartRewardWeek TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN equippedGym TEXT NOT NULL DEFAULT 'gym_standard'")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN unlockedGymsCsv TEXT NOT NULL DEFAULT 'gym_standard'")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN equippedAppBackground TEXT NOT NULL DEFAULT 'bg_classic'")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN unlockedAppBackgroundsCsv TEXT NOT NULL DEFAULT 'bg_classic,bg_ember'")
            }
        }
    }
}
