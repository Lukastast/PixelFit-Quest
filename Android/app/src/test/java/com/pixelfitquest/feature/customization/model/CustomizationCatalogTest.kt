package com.pixelfitquest.feature.customization.model

import com.pixelfitquest.R
import com.pixelfitquest.feature.home.model.DwellingTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        assertEquals(180, fitness?.coinPrice)
        assertEquals(8, fitness?.minLevel)

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
    fun fitnessGear_requiresCoinsEvenAfterItsLevelGate() {
        val fitness = CustomizationCatalog.characters.first { it.id == "fitness" }
        assertFalse(
            isCharacterUnlocked(
                item = fitness,
                variant = "male_fitness",
                userLevel = 8,
                unlockedVariants = emptySet(),
                unlockedLevelSkinIds = emptySet(),
            ),
        )
        assertTrue(purchaseBlockedByLevel(unlocked = false, minLevel = 8, userLevel = 7))
        assertFalse(purchaseBlockedByLevel(unlocked = false, minLevel = 8, userLevel = 8))
        assertTrue(
            isCharacterUnlocked(
                item = fitness,
                variant = "male_fitness",
                userLevel = 3,
                unlockedVariants = setOf("male_fitness"),
                unlockedLevelSkinIds = emptySet(),
            ),
        )
    }

    @Test
    fun grandfatherFitness_onlyForHeroesAlreadyPastTheOldGate() {
        val granted = grandfatherFitnessVariants(
            level = 8,
            ownedVariants = listOf("basic"),
            alreadyMigrated = false,
        )
        assertTrue(granted.contains("male_fitness"))
        assertTrue(granted.contains("female_fitness"))
        val newbie = grandfatherFitnessVariants(
            level = 1,
            ownedVariants = listOf("basic"),
            alreadyMigrated = false,
        )
        assertFalse(newbie.contains("male_fitness"))
        val later = grandfatherFitnessVariants(
            level = 20,
            ownedVariants = listOf("basic"),
            alreadyMigrated = true,
        )
        assertFalse(later.contains("male_fitness"))
    }

    @Test
    fun homeDwellingsCatalog_hasAutoAndAllTiers() {
        val dwellings = CustomizationCatalog.homeDwellings
        val auto = dwellings.find { it.id == null }
        assertNotNull(auto)
        assertEquals(UnlockType.DEFAULT, auto?.unlockType)
        assertEquals("Best Owned", auto?.name)

        DwellingTier.entries.forEach { tier ->
            val found = dwellings.find { it.id == tier.id }
            assertNotNull("Expected dwelling tier ${tier.id} in catalog", found)
        }

        val gymDwelling = dwellings.find { it.id == DwellingTier.GYM.id }
        assertEquals(UnlockType.COINS, gymDwelling?.unlockType)
        assertEquals(DwellingTier.GYM.coinPrice, gymDwelling?.coinPrice)
        assertEquals(DwellingTier.GYM.minLevel, gymDwelling?.minLevel)
        val tarp = dwellings.find { it.id == DwellingTier.TARP.id }
        assertEquals(UnlockType.DEFAULT, tarp?.unlockType)
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
        assertEquals(18, dungeonGym?.minLevel)
        assertEquals(320, dungeonGym?.coinPrice)

        val rooftopGym = gyms.find { it.id == "gym_rooftop" }
        assertNotNull(rooftopGym)
        assertEquals(UnlockType.COINS, rooftopGym?.unlockType)
        assertEquals(40, rooftopGym?.minLevel)
        assertEquals(700, rooftopGym?.coinPrice)
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
