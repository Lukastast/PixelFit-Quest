package com.pixelfitquest.local.db

import android.app.Application
import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Smoke test that the checked-in v11 schema can be opened by [MigrationTestHelper].
 * Robolectric 4.17 can target SDK 36, but that android-all jar needs JDK 21.
 * Unit tests run on the JDK 17 toolchain, so this pins SDK 35.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class PixelFitDatabaseSchemaTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        PixelFitDatabase::class.java,
    )

    @Test
    fun createDatabase_atVersion11_hasUserProfile() {
        val db = helper.createDatabase(TEST_DB, 11)
        try {
            val columns = mutableSetOf<String>()
            db.query("PRAGMA table_info(user_profile)").use { cursor ->
                val nameIndex = cursor.getColumnIndexOrThrow("name")
                while (cursor.moveToNext()) {
                    columns += cursor.getString(nameIndex)
                }
            }
            assertEquals(EXPECTED_USER_PROFILE_COLUMNS, columns)

            db.query("PRAGMA user_version").use { cursor ->
                assertTrue("user_version row missing", cursor.moveToFirst())
                assertEquals(11, cursor.getInt(0))
            }
        } finally {
            db.close()
        }
    }

    private companion object {
        const val TEST_DB = "pixelfit-schema-test"

        val EXPECTED_USER_PROFILE_COLUMNS = setOf(
            "id",
            "height",
            "armLength",
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
