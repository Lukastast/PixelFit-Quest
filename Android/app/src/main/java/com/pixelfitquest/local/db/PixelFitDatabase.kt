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
    version = 12,
    exportSchema = true,
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

        val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN lastVitalityBonusDate TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE user_profile ADD COLUMN dwellingLegacyMigrated INTEGER NOT NULL DEFAULT 0",
                )
            }
        }

        val MIGRATION_10_11 = object : androidx.room.migration.Migration(10, 11) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN skillForm INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN skillIron INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN skillVitality INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN skillRespecDate TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN rewardedSetsDate TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN rewardedSetsCount INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_11_12 = object : androidx.room.migration.Migration(11, 12) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS user_profile_new")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS user_profile_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        musicVolume INTEGER NOT NULL,
                        level INTEGER NOT NULL,
                        coins INTEGER NOT NULL,
                        exp INTEGER NOT NULL,
                        streak INTEGER NOT NULL,
                        lastActivityDate TEXT NOT NULL,
                        lastStepsRewardDate TEXT NOT NULL,
                        lastSleepRewardDate TEXT NOT NULL,
                        lastWeeklyHeartRewardWeek TEXT NOT NULL,
                        lastVitalityBonusDate TEXT NOT NULL,
                        lastStreakUpdateDate TEXT NOT NULL,
                        characterGender TEXT NOT NULL,
                        characterVariant TEXT NOT NULL,
                        unlockedVariantsCsv TEXT NOT NULL,
                        equippedHomeUpgrade TEXT NOT NULL,
                        unlockedHomeUpgradesCsv TEXT NOT NULL,
                        equippedGym TEXT NOT NULL,
                        unlockedGymsCsv TEXT NOT NULL,
                        equippedAppBackground TEXT NOT NULL,
                        unlockedAppBackgroundsCsv TEXT NOT NULL,
                        dwellingLegacyMigrated INTEGER NOT NULL,
                        skillForm INTEGER NOT NULL,
                        skillIron INTEGER NOT NULL,
                        skillVitality INTEGER NOT NULL,
                        skillRespecDate TEXT NOT NULL,
                        rewardedSetsDate TEXT NOT NULL,
                        rewardedSetsCount INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO user_profile_new (
                        id, musicVolume, level, coins, exp, streak,
                        lastActivityDate, lastStepsRewardDate, lastSleepRewardDate,
                        lastWeeklyHeartRewardWeek, lastVitalityBonusDate, lastStreakUpdateDate,
                        characterGender, characterVariant, unlockedVariantsCsv,
                        equippedHomeUpgrade, unlockedHomeUpgradesCsv,
                        equippedGym, unlockedGymsCsv,
                        equippedAppBackground, unlockedAppBackgroundsCsv,
                        dwellingLegacyMigrated,
                        skillForm, skillIron, skillVitality, skillRespecDate,
                        rewardedSetsDate, rewardedSetsCount
                    )
                    SELECT
                        id, musicVolume, level, coins, exp, streak,
                        lastActivityDate, lastStepsRewardDate, lastSleepRewardDate,
                        lastWeeklyHeartRewardWeek, lastVitalityBonusDate, lastStreakUpdateDate,
                        characterGender, characterVariant, unlockedVariantsCsv,
                        equippedHomeUpgrade, unlockedHomeUpgradesCsv,
                        equippedGym, unlockedGymsCsv,
                        equippedAppBackground, unlockedAppBackgroundsCsv,
                        dwellingLegacyMigrated,
                        skillForm, skillIron, skillVitality, skillRespecDate,
                        rewardedSetsDate, rewardedSetsCount
                    FROM user_profile
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE user_profile")
                db.execSQL("ALTER TABLE user_profile_new RENAME TO user_profile")
            }
        }
    }
}
