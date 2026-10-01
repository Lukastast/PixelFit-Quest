package com.pixelfitquest.feature.levels.cosmetics

import com.pixelfitquest.feature.levels.model.CosmeticCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AvatarSkinBridgeTest {

    @Test
    fun mapsVariantsOntoCatalogIds() {
        assertEquals(CosmeticCatalog.SKIN_BASIC, AvatarSkinBridge.cosmeticIdForVariant("basic"))
        assertEquals(CosmeticCatalog.SKIN_FITNESS, AvatarSkinBridge.cosmeticIdForVariant("male_fitness"))
        assertEquals(CosmeticCatalog.SKIN_FITNESS, AvatarSkinBridge.cosmeticIdForVariant("female_fitness"))
        assertEquals(CosmeticCatalog.SKIN_SHADOW, AvatarSkinBridge.cosmeticIdForVariant("shadow"))
        assertEquals(CosmeticCatalog.SKIN_CAPE_HERO, AvatarSkinBridge.cosmeticIdForVariant("cape_hero"))
        assertEquals(CosmeticCatalog.SKIN_IRON_OAK, AvatarSkinBridge.cosmeticIdForVariant("iron_oak"))
        assertEquals(CosmeticCatalog.SKIN_EMBER_SPARROW, AvatarSkinBridge.cosmeticIdForVariant("ember_sparrow"))
        assertEquals(CosmeticCatalog.SKIN_COIL_SHADE, AvatarSkinBridge.cosmeticIdForVariant("coil_shade"))
    }

    @Test
    fun premiumIsNotLevelUnlocked() {
        assertFalse(
            AvatarSkinBridge.isUnlockedByLevel(
                "male_premium",
                setOf(CosmeticCatalog.SKIN_BASIC, CosmeticCatalog.SKIN_FITNESS),
            ),
        )
        assertEquals(null, AvatarSkinBridge.unlockLevel("female_premium"))
    }

    @Test
    fun shadowSpriteUsesLockedSheet() {
        assertEquals(
            "locked_male",
            AvatarSkinBridge.spriteKey("shadow", gender = "male", unlocked = true),
        )
        assertEquals(
            "locked_woman",
            AvatarSkinBridge.spriteKey("shadow", gender = "female", unlocked = true),
        )
    }

    @Test
    fun fitnessUsesUnlockedSheetOnlyWhenOwned() {
        assertEquals(
            "fitness_character_male_idle",
            AvatarSkinBridge.spriteKey("male_fitness", gender = "male", unlocked = true),
        )
        assertEquals(
            "locked_male",
            AvatarSkinBridge.spriteKey("male_fitness", gender = "male", unlocked = false),
        )
    }
}

    @Test
    fun originalArchetypeSprites() {
        assertEquals(
            "cape_hero_idle",
            AvatarSkinBridge.spriteKey("cape_hero", gender = "male", unlocked = true),
        )
        assertEquals(
            "iron_oak_idle",
            AvatarSkinBridge.spriteKey("iron_oak", gender = "female", unlocked = true),
        )
        assertEquals(
            "locked_male",
            AvatarSkinBridge.spriteKey("ember_sparrow", gender = "male", unlocked = false),
        )
    }

