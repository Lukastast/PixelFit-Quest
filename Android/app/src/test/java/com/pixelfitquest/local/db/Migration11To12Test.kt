package com.pixelfitquest.local.db

import android.app.Application
import android.database.Cursor
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.platform.app.InstrumentationRegistry
import com.pixelfitquest.local.db.entity.UserProfileEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * v11 stored height and arm length on user_profile. v12 rebuilds the table without them.
 * Robolectric 4.17 can target SDK 36, but that android-all jar needs JDK 21.
 * Unit tests run on the JDK 17 toolchain, so this pins SDK 35.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class Migration11To12Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        PixelFitDatabase::class.java,
    )

    @Test
    fun migrate11To12_keepsProfileAndDropsBodyColumns() {
        val created = helper.createDatabase(DB_KEEPS, 11)
        try {
            insertSeed(created)
        } finally {
            created.close()
        }

        val migrated = helper.runMigrationsAndValidate(
            DB_KEEPS,
            12,
            true,
            PixelFitDatabase.MIGRATION_11_12,
        )
        try {
            assertUserProfileColumns(migrated)
            assertEquals(1, countRows(migrated))
            assertSeededValues(migrated)
        } finally {
            migrated.close()
        }
    }

    @Test
    fun migrate11To12_emptyProfileTable() {
        helper.createDatabase(DB_EMPTY, 11).close()

        val migrated = helper.runMigrationsAndValidate(
            DB_EMPTY,
            12,
            true,
            PixelFitDatabase.MIGRATION_11_12,
        )
        try {
            assertUserProfileColumns(migrated)
            assertEquals(0, countRows(migrated))
        } finally {
            migrated.close()
        }
    }

    @Test
    fun migrate11To12_leftoverTempTable() {
        val created = helper.createDatabase(DB_LEFTOVER, 11)
        try {
            created.execSQL("CREATE TABLE user_profile_new (id TEXT NOT NULL PRIMARY KEY)")
            created.execSQL("INSERT INTO user_profile_new (id) VALUES ('junk-leftover')")
            insertSeed(created)
        } finally {
            created.close()
        }

        val migrated = helper.runMigrationsAndValidate(
            DB_LEFTOVER,
            12,
            true,
            PixelFitDatabase.MIGRATION_11_12,
        )
        try {
            assertUserProfileColumns(migrated)
            assertEquals(1, countRows(migrated))
            assertSeededValues(migrated)
            migrated.query(
                "SELECT COUNT(*) FROM user_profile WHERE id = 'junk-leftover'",
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(0, cursor.getInt(0))
            }
        } finally {
            migrated.close()
        }
    }

    @Test
    fun openAtV12_withAllMigrations() = runBlocking {
        val created = helper.createDatabase(DB_REOPEN, 11)
        try {
            insertSeed(created)
        } finally {
            created.close()
        }
        helper.runMigrationsAndValidate(
            DB_REOPEN,
            12,
            true,
            PixelFitDatabase.MIGRATION_11_12,
        ).close()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.databaseBuilder(context, PixelFitDatabase::class.java, DB_REOPEN)
            .addMigrations(
                PixelFitDatabase.MIGRATION_6_7,
                PixelFitDatabase.MIGRATION_7_8,
                PixelFitDatabase.MIGRATION_8_9,
                PixelFitDatabase.MIGRATION_9_10,
                PixelFitDatabase.MIGRATION_10_11,
                PixelFitDatabase.MIGRATION_11_12,
            )
            .allowMainThreadQueries()
            .build()
        try {
            assertEquals(seedEntity(), db.userProfileDao().get(Seed.ID))
        } finally {
            db.close()
        }
    }

    @Test
    fun freshInstall_opensV12AndRoundTripsProfile() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(DB_FRESH)
        val db = Room.databaseBuilder(context, PixelFitDatabase::class.java, DB_FRESH)
            .addMigrations(
                PixelFitDatabase.MIGRATION_6_7,
                PixelFitDatabase.MIGRATION_7_8,
                PixelFitDatabase.MIGRATION_8_9,
                PixelFitDatabase.MIGRATION_9_10,
                PixelFitDatabase.MIGRATION_10_11,
                PixelFitDatabase.MIGRATION_11_12,
            )
            .allowMainThreadQueries()
            .build()
        try {
            db.query("PRAGMA user_version", null).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(12, cursor.getInt(0))
            }
            val entity = seedEntity()
            db.userProfileDao().upsert(entity)
            assertEquals(entity, db.userProfileDao().get(entity.id))
        } finally {
            db.close()
        }
    }

    private fun insertSeed(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            INSERT INTO user_profile (
                id, height, armLength, musicVolume, level, coins, exp, streak,
                lastActivityDate, lastStepsRewardDate, lastSleepRewardDate,
                lastWeeklyHeartRewardWeek, lastVitalityBonusDate, lastStreakUpdateDate,
                characterGender, characterVariant, unlockedVariantsCsv,
                equippedHomeUpgrade, unlockedHomeUpgradesCsv,
                equippedGym, unlockedGymsCsv,
                equippedAppBackground, unlockedAppBackgroundsCsv,
                dwellingLegacyMigrated,
                skillForm, skillIron, skillVitality, skillRespecDate,
                rewardedSetsDate, rewardedSetsCount
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any>(
                Seed.ID,
                Seed.HEIGHT,
                Seed.ARM_LENGTH,
                Seed.MUSIC_VOLUME,
                Seed.LEVEL,
                Seed.COINS,
                Seed.EXP,
                Seed.STREAK,
                Seed.LAST_ACTIVITY_DATE,
                Seed.LAST_STEPS_REWARD_DATE,
                Seed.LAST_SLEEP_REWARD_DATE,
                Seed.LAST_WEEKLY_HEART_REWARD_WEEK,
                Seed.LAST_VITALITY_BONUS_DATE,
                Seed.LAST_STREAK_UPDATE_DATE,
                Seed.CHARACTER_GENDER,
                Seed.CHARACTER_VARIANT,
                Seed.UNLOCKED_VARIANTS_CSV,
                Seed.EQUIPPED_HOME_UPGRADE,
                Seed.UNLOCKED_HOME_UPGRADES_CSV,
                Seed.EQUIPPED_GYM,
                Seed.UNLOCKED_GYMS_CSV,
                Seed.EQUIPPED_APP_BACKGROUND,
                Seed.UNLOCKED_APP_BACKGROUNDS_CSV,
                Seed.DWELLING_LEGACY_MIGRATED,
                Seed.SKILL_FORM,
                Seed.SKILL_IRON,
                Seed.SKILL_VITALITY,
                Seed.SKILL_RESPEC_DATE,
                Seed.REWARDED_SETS_DATE,
                Seed.REWARDED_SETS_COUNT,
            ),
        )
    }

    private fun assertUserProfileColumns(db: SupportSQLiteDatabase) {
        val columns = mutableListOf<String>()
        db.query("PRAGMA table_info(user_profile)").use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            while (cursor.moveToNext()) {
                columns += cursor.getString(nameIndex)
            }
        }
        assertEquals(EXPECTED_COLUMNS, columns)
        assertFalse(columns.contains("height"))
        assertFalse(columns.contains("armLength"))
    }

    private fun countRows(db: SupportSQLiteDatabase): Int {
        db.query("SELECT COUNT(*) FROM user_profile").use { cursor ->
            assertTrue(cursor.moveToFirst())
            return cursor.getInt(0)
        }
    }

    private fun assertSeededValues(db: SupportSQLiteDatabase) {
        db.query("SELECT * FROM user_profile").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(Seed.ID, cursor.string("id"))
            assertEquals(Seed.MUSIC_VOLUME, cursor.int("musicVolume"))
            assertEquals(Seed.LEVEL, cursor.int("level"))
            assertEquals(Seed.COINS, cursor.int("coins"))
            assertEquals(Seed.EXP, cursor.int("exp"))
            assertEquals(Seed.STREAK, cursor.int("streak"))
            assertEquals(Seed.LAST_ACTIVITY_DATE, cursor.string("lastActivityDate"))
            assertEquals(Seed.LAST_STEPS_REWARD_DATE, cursor.string("lastStepsRewardDate"))
            assertEquals(Seed.LAST_SLEEP_REWARD_DATE, cursor.string("lastSleepRewardDate"))
            assertEquals(Seed.LAST_WEEKLY_HEART_REWARD_WEEK, cursor.string("lastWeeklyHeartRewardWeek"))
            assertEquals(Seed.LAST_VITALITY_BONUS_DATE, cursor.string("lastVitalityBonusDate"))
            assertEquals(Seed.LAST_STREAK_UPDATE_DATE, cursor.string("lastStreakUpdateDate"))
            assertEquals(Seed.CHARACTER_GENDER, cursor.string("characterGender"))
            assertEquals(Seed.CHARACTER_VARIANT, cursor.string("characterVariant"))
            assertEquals(Seed.UNLOCKED_VARIANTS_CSV, cursor.string("unlockedVariantsCsv"))
            assertEquals(Seed.EQUIPPED_HOME_UPGRADE, cursor.string("equippedHomeUpgrade"))
            assertEquals(Seed.UNLOCKED_HOME_UPGRADES_CSV, cursor.string("unlockedHomeUpgradesCsv"))
            assertEquals(Seed.EQUIPPED_GYM, cursor.string("equippedGym"))
            assertEquals(Seed.UNLOCKED_GYMS_CSV, cursor.string("unlockedGymsCsv"))
            assertEquals(Seed.EQUIPPED_APP_BACKGROUND, cursor.string("equippedAppBackground"))
            assertEquals(Seed.UNLOCKED_APP_BACKGROUNDS_CSV, cursor.string("unlockedAppBackgroundsCsv"))
            assertEquals(Seed.DWELLING_LEGACY_MIGRATED, cursor.int("dwellingLegacyMigrated"))
            assertEquals(Seed.SKILL_FORM, cursor.int("skillForm"))
            assertEquals(Seed.SKILL_IRON, cursor.int("skillIron"))
            assertEquals(Seed.SKILL_VITALITY, cursor.int("skillVitality"))
            assertEquals(Seed.SKILL_RESPEC_DATE, cursor.string("skillRespecDate"))
            assertEquals(Seed.REWARDED_SETS_DATE, cursor.string("rewardedSetsDate"))
            assertEquals(Seed.REWARDED_SETS_COUNT, cursor.int("rewardedSetsCount"))
            assertFalse(cursor.moveToNext())
        }
    }

    private fun seedEntity(): UserProfileEntity = UserProfileEntity(
        id = Seed.ID,
        musicVolume = Seed.MUSIC_VOLUME,
        level = Seed.LEVEL,
        coins = Seed.COINS,
        exp = Seed.EXP,
        streak = Seed.STREAK,
        lastActivityDate = Seed.LAST_ACTIVITY_DATE,
        lastStepsRewardDate = Seed.LAST_STEPS_REWARD_DATE,
        lastSleepRewardDate = Seed.LAST_SLEEP_REWARD_DATE,
        lastWeeklyHeartRewardWeek = Seed.LAST_WEEKLY_HEART_REWARD_WEEK,
        lastVitalityBonusDate = Seed.LAST_VITALITY_BONUS_DATE,
        lastStreakUpdateDate = Seed.LAST_STREAK_UPDATE_DATE,
        characterGender = Seed.CHARACTER_GENDER,
        characterVariant = Seed.CHARACTER_VARIANT,
        unlockedVariantsCsv = Seed.UNLOCKED_VARIANTS_CSV,
        equippedHomeUpgrade = Seed.EQUIPPED_HOME_UPGRADE,
        unlockedHomeUpgradesCsv = Seed.UNLOCKED_HOME_UPGRADES_CSV,
        equippedGym = Seed.EQUIPPED_GYM,
        unlockedGymsCsv = Seed.UNLOCKED_GYMS_CSV,
        equippedAppBackground = Seed.EQUIPPED_APP_BACKGROUND,
        unlockedAppBackgroundsCsv = Seed.UNLOCKED_APP_BACKGROUNDS_CSV,
        dwellingLegacyMigrated = Seed.DWELLING_LEGACY_MIGRATED,
        skillForm = Seed.SKILL_FORM,
        skillIron = Seed.SKILL_IRON,
        skillVitality = Seed.SKILL_VITALITY,
        skillRespecDate = Seed.SKILL_RESPEC_DATE,
        rewardedSetsDate = Seed.REWARDED_SETS_DATE,
        rewardedSetsCount = Seed.REWARDED_SETS_COUNT,
    )

    private fun Cursor.string(column: String): String = getString(getColumnIndexOrThrow(column))

    private fun Cursor.int(column: String): Int = getInt(getColumnIndexOrThrow(column))

    private object Seed {
        const val ID = "hero-1"
        const val HEIGHT = 185
        const val ARM_LENGTH = 71.5
        const val MUSIC_VOLUME = 73
        const val LEVEL = 17
        const val COINS = 420
        const val EXP = 1337
        const val STREAK = 9
        const val LAST_ACTIVITY_DATE = "2026-10-01"
        const val LAST_STEPS_REWARD_DATE = "2026-10-02"
        const val LAST_SLEEP_REWARD_DATE = "2026-10-03"
        const val LAST_WEEKLY_HEART_REWARD_WEEK = "2026-W40"
        const val LAST_VITALITY_BONUS_DATE = "2026-10-04"
        const val LAST_STREAK_UPDATE_DATE = "2026-10-05"
        const val CHARACTER_GENDER = "female"
        const val CHARACTER_VARIANT = "iron_oak"
        const val UNLOCKED_VARIANTS_CSV = "basic,iron_oak"
        const val EQUIPPED_HOME_UPGRADE = "dwelling_gym"
        const val UNLOCKED_HOME_UPGRADES_CSV = "dwelling_tent,dwelling_gym"
        const val EQUIPPED_GYM = "gym_iron"
        const val UNLOCKED_GYMS_CSV = "gym_standard,gym_iron"
        const val EQUIPPED_APP_BACKGROUND = "bg_night"
        const val UNLOCKED_APP_BACKGROUNDS_CSV = "bg_classic,bg_ember,bg_night"
        const val DWELLING_LEGACY_MIGRATED = 1
        const val SKILL_FORM = 2
        const val SKILL_IRON = 3
        const val SKILL_VITALITY = 1
        const val SKILL_RESPEC_DATE = "2026-09-15"
        const val REWARDED_SETS_DATE = "2026-10-06"
        const val REWARDED_SETS_COUNT = 5
    }

    private companion object {
        const val DB_KEEPS = "pixelfit-m11-12-keeps"
        const val DB_EMPTY = "pixelfit-m11-12-empty"
        const val DB_LEFTOVER = "pixelfit-m11-12-leftover"
        const val DB_REOPEN = "pixelfit-m11-12-reopen"
        const val DB_FRESH = "pixelfit-m11-12-fresh"

        val EXPECTED_COLUMNS = listOf(
            "id",
            "musicVolume",
            "level",
            "coins",
            "exp",
            "streak",
            "lastActivityDate",
            "lastStepsRewardDate",
            "lastSleepRewardDate",
            "lastWeeklyHeartRewardWeek",
            "lastVitalityBonusDate",
            "lastStreakUpdateDate",
            "characterGender",
            "characterVariant",
            "unlockedVariantsCsv",
            "equippedHomeUpgrade",
            "unlockedHomeUpgradesCsv",
            "equippedGym",
            "unlockedGymsCsv",
            "equippedAppBackground",
            "unlockedAppBackgroundsCsv",
            "dwellingLegacyMigrated",
            "skillForm",
            "skillIron",
            "skillVitality",
            "skillRespecDate",
            "rewardedSetsDate",
            "rewardedSetsCount",
        )
    }
}
