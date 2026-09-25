package com.pixelfitquest.debug

import android.content.SharedPreferences
import com.pixelfitquest.feature.customization.model.CharacterItem
import com.pixelfitquest.feature.customization.model.UnlockType
import com.pixelfitquest.feature.customization.model.isCharacterUnlocked
import com.pixelfitquest.feature.customization.model.isShopItemUnlocked
import com.pixelfitquest.feature.customization.model.meetsLevelGate
import com.pixelfitquest.feature.customization.model.purchaseBlockedByLevel
import com.pixelfitquest.feature.levels.cosmetics.AvatarSkinBridge
import com.pixelfitquest.feature.levels.model.CosmeticCatalog
import com.pixelfitquest.feature.levels.progression.CosmeticUnlocker
import com.pixelfitquest.feature.streak.model.WeeklyStreakRewards
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GodModeTest {

    private lateinit var fakePrefs: FakeSharedPreferences

    @Before
    fun setUp() {
        fakePrefs = FakeSharedPreferences()
    }

    @After
    fun tearDown() {
        GodModePrefs.setEnabled(fakePrefs, false)
    }

    @Test
    fun togglingGodModeUpdatesActiveState() {
        GodModePrefs.setEnabled(fakePrefs, true)
        assertTrue(GodModePrefs.isGodModeActive)
        assertTrue(fakePrefs.getBoolean(GodModePrefs.KEY_GOD_MODE, false))

        GodModePrefs.setEnabled(fakePrefs, false)
        assertFalse(GodModePrefs.isGodModeActive)
        assertFalse(fakePrefs.getBoolean(GodModePrefs.KEY_GOD_MODE, false))
    }

    @Test
    fun godModeUnlocksShopAccessAndBypassesLevelGates() {
        GodModePrefs.setEnabled(fakePrefs, true)

        assertTrue(meetsLevelGate(minLevel = 99, userLevel = 1))
        assertFalse(purchaseBlockedByLevel(unlocked = false, minLevel = 99, userLevel = 1))

        // Level-locked item
        assertTrue(
            isShopItemUnlocked(
                unlockType = UnlockType.LEVEL,
                minLevel = 50,
                userLevel = 1,
                owned = false,
            )
        )

        // Coin-locked item
        assertTrue(
            isShopItemUnlocked(
                unlockType = UnlockType.COINS,
                minLevel = 10,
                userLevel = 1,
                owned = false,
            )
        )

        // Coming soon item
        assertTrue(
            isShopItemUnlocked(
                unlockType = UnlockType.COMING_SOON,
                minLevel = null,
                userLevel = 1,
                owned = false,
            )
        )

        // Character item
        val shadowItem = CharacterItem(
            id = "shadow",
            name = "Shadow",
            description = "Test",
            unlockType = UnlockType.LEVEL,
            minLevel = 12,
        )
        assertTrue(
            isCharacterUnlocked(
                item = shadowItem,
                variant = "shadow",
                userLevel = 1,
                unlockedVariants = emptySet(),
                unlockedLevelSkinIds = emptySet(),
            )
        )
    }

    @Test
    fun godModeUnlocksAllCosmeticsInProgression() {
        GodModePrefs.setEnabled(fakePrefs, true)

        val unlocked = CosmeticUnlocker.unlockedIds(level = 1)
        val allIds = CosmeticCatalog.all.map { it.id }.toSet()
        assertEquals(allIds, unlocked)
        assertTrue(unlocked.contains(CosmeticCatalog.HOME_LEGEND))
        assertTrue(unlocked.contains(CosmeticCatalog.TITLE_ASCENDANT))
    }

    @Test
    fun godModeUnlocksAllWeeklyStreakOutfits() {
        GodModePrefs.setEnabled(fakePrefs, true)

        assertEquals(WeeklyStreakRewards.skins, WeeklyStreakRewards.unlocked(streakWeeks = 0))
        assertTrue(WeeklyStreakRewards.isSkinUnlocked(WeeklyStreakRewards.SKIN_LEGEND, streakWeeks = 0))
        assertTrue(WeeklyStreakRewards.isSkinUnlocked(WeeklyStreakRewards.SKIN_PHOENIX, streakWeeks = 0))
        assertTrue(WeeklyStreakRewards.isSkinUnlocked(WeeklyStreakRewards.SKIN_EMBER, streakWeeks = 0))
    }

    @Test
    fun godModeUnlocksAvatarSkinBridge() {
        GodModePrefs.setEnabled(fakePrefs, true)

        assertTrue(AvatarSkinBridge.isUnlockedByLevel("male_fitness", emptySet()))
        assertTrue(AvatarSkinBridge.isUnlockedByLevel("shadow", emptySet()))
        assertTrue(AvatarSkinBridge.isUnlockedByLevel("male_premium", emptySet()))

        assertEquals(
            "fitness_character_male_idle",
            AvatarSkinBridge.spriteKey("male_fitness", gender = "male", unlocked = false),
        )
    }

    private class FakeSharedPreferences : SharedPreferences {
        private val data = mutableMapOf<String, Any>()

        override fun getAll(): MutableMap<String, *> = data
        override fun getString(key: String?, defValue: String?): String? = data[key] as? String ?: defValue
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
            @Suppress("UNCHECKED_CAST") (data[key] as? MutableSet<String> ?: defValues)
        override fun getInt(key: String?, defValue: Int): Int = data[key] as? Int ?: defValue
        override fun getLong(key: String?, defValue: Long): Long = data[key] as? Long ?: defValue
        override fun getFloat(key: String?, defValue: Float): Float = data[key] as? Float ?: defValue
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = data[key] as? Boolean ?: defValue
        override fun contains(key: String?): Boolean = data.containsKey(key)
        override fun edit(): SharedPreferences.Editor = FakeEditor(data)
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

        private class FakeEditor(private val backingData: MutableMap<String, Any>) : SharedPreferences.Editor {
            private val pending = mutableMapOf<String, Any?>()
            private var clearPending = false

            override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                if (key != null) pending[key] = value
                return this
            }
            override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor {
                if (key != null) pending[key] = values
                return this
            }
            override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
                if (key != null) pending[key] = value
                return this
            }
            override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
                if (key != null) pending[key] = value
                return this
            }
            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
                if (key != null) pending[key] = value
                return this
            }
            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
                if (key != null) pending[key] = value
                return this
            }
            override fun remove(key: String?): SharedPreferences.Editor {
                if (key != null) pending[key] = null
                return this
            }
            override fun clear(): SharedPreferences.Editor {
                clearPending = true
                return this
            }
            override fun commit(): Boolean {
                apply()
                return true
            }
            override fun apply() {
                if (clearPending) backingData.clear()
                pending.forEach { (k, v) ->
                    if (v == null) backingData.remove(k) else backingData[k] = v
                }
                pending.clear()
            }
        }
    }
}
