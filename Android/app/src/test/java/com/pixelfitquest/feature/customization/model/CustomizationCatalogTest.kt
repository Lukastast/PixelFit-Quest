package com.pixelfitquest.feature.customization.model

import com.pixelfitquest.R
import com.pixelfitquest.feature.home.model.DwellingTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomizationCatalogTest {

    @Test
    fun charactersCatalog_containsAllExpectedCharacters() {
        val chars = CustomizationCatalog.characters
        assertEquals(4, chars.size)
        val basic = chars.find { it.id == "basic" }
        assertNotNull(basic)
        assertEquals(UnlockType.DEFAULT, basic?.unlockType)

        val fitness = chars.find { it.id == "fitness" }
        assertNotNull(fitness)
        assertEquals(UnlockType.COINS, fitness?.unlockType)
        assertEquals(100, fitness?.coinPrice)
        assertEquals(5, fitness?.minLevel)

        val shadow = chars.find { it.id == "shadow" }
        assertNotNull(shadow)
        assertEquals(UnlockType.LEVEL, shadow?.unlockType)
        assertEquals(12, shadow?.minLevel)

        val premium = chars.find { it.id == "premium" }
        assertNotNull(premium)
        assertEquals(UnlockType.COMING_SOON, premium?.unlockType)
        assertTrue(premium?.isPremium == true)
    }

    @Test
    fun homeDwellingsCatalog_hasAutoAndAllTiers() {
        val dwellings = CustomizationCatalog.homeDwellings
        val auto = dwellings.find { it.id == null }
        assertNotNull(auto)
        assertEquals(UnlockType.DEFAULT, auto?.unlockType)

        DwellingTier.entries.forEach { tier ->
            val found = dwellings.find { it.id == tier.id }
            assertNotNull("Expected dwelling tier ${tier.id} in catalog", found)
        }

        val gymDwelling = dwellings.find { it.id == DwellingTier.GYM.id }
        assertEquals(UnlockType.COINS, gymDwelling?.unlockType)
        assertEquals(150, gymDwelling?.coinPrice)
    }

    @Test
    fun gymsCatalog_hasMixOfBuyOnlyAndLevelUpOnly() {
        val gyms = CustomizationCatalog.gyms
        assertTrue(gyms.size >= 6)

        val defaultGym = gyms.find { it.id == "gym_standard" }
        assertNotNull(defaultGym)
        assertEquals(UnlockType.DEFAULT, defaultGym?.unlockType)

        // Level-up only gyms
        val duskGym = gyms.find { it.id == "gym_dusk" }
        assertNotNull(duskGym)
        assertEquals(UnlockType.LEVEL, duskGym?.unlockType)
        assertEquals(10, duskGym?.minLevel)

        val cyberGym = gyms.find { it.id == "gym_cyber" }
        assertNotNull(cyberGym)
        assertEquals(UnlockType.LEVEL, cyberGym?.unlockType)
        assertEquals(20, cyberGym?.minLevel)

        val championGym = gyms.find { it.id == "gym_champion" }
        assertNotNull(championGym)
        assertEquals(UnlockType.LEVEL, championGym?.unlockType)
        assertEquals(25, championGym?.minLevel)

        // Buy-only gyms
        val dungeonGym = gyms.find { it.id == "gym_dungeon" }
        assertNotNull(dungeonGym)
        assertEquals(UnlockType.COINS, dungeonGym?.unlockType)
        assertEquals(150, dungeonGym?.coinPrice)

        val rooftopGym = gyms.find { it.id == "gym_rooftop" }
        assertNotNull(rooftopGym)
        assertEquals(UnlockType.COINS, rooftopGym?.unlockType)
        assertEquals(200, rooftopGym?.coinPrice)
    }

    @Test
    fun appBackgroundsCatalog_containsValidOptions() {
        val bgs = CustomizationCatalog.appBackgrounds
        assertTrue(bgs.isNotEmpty())

        val classic = bgs.find { it.id == "bg_classic" }
        assertNotNull(classic)
        assertEquals(R.drawable.logsigninbackground, classic?.drawableRes)

        val ember = bgs.find { it.id == "bg_ember" }
        assertNotNull(ember)
        assertEquals(R.drawable.home_theme_ember, ember?.drawableRes)
    }

    @Test
    fun helperMethods_resolveDrawables() {
        assertEquals(R.drawable.gym_background, CustomizationCatalog.gymDrawable("gym_standard"))
        assertEquals(R.drawable.home_theme_dusk_gym, CustomizationCatalog.gymDrawable("gym_dusk"))
        assertEquals(R.drawable.gym_background, CustomizationCatalog.gymDrawable("unknown"))

        assertEquals(R.drawable.logsigninbackground, CustomizationCatalog.appBackgroundDrawable("bg_classic"))
        assertEquals(R.drawable.home_theme_night, CustomizationCatalog.appBackgroundDrawable("bg_night"))
        assertEquals(R.drawable.logsigninbackground, CustomizationCatalog.appBackgroundDrawable("unknown"))
    }

    @Test
    fun resolveCharacterIdFromVariant_mapsCorrectly() {
        assertEquals("basic", CustomizationCatalog.resolveCharacterIdFromVariant("basic"))
        assertEquals("fitness", CustomizationCatalog.resolveCharacterIdFromVariant("male_fitness"))
        assertEquals("fitness", CustomizationCatalog.resolveCharacterIdFromVariant("female_fitness"))
        assertEquals("shadow", CustomizationCatalog.resolveCharacterIdFromVariant("shadow"))
        assertEquals("premium", CustomizationCatalog.resolveCharacterIdFromVariant("male_premium"))
        assertEquals("premium", CustomizationCatalog.resolveCharacterIdFromVariant("female_premium"))
        assertEquals("basic", CustomizationCatalog.resolveCharacterIdFromVariant("unknown"))
    }
}
