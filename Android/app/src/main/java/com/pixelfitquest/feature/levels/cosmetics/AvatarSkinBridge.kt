package com.pixelfitquest.feature.levels.cosmetics

import com.pixelfitquest.debug.GodModePrefs
import com.pixelfitquest.feature.levels.model.CosmeticCatalog

/**
 * Maps customization carousel variants onto local cosmetic ids.
 * Customization keeps coin-buy; this only answers "unlocked by level?".
 */
object AvatarSkinBridge {
    const val VARIANT_BASIC = "basic"
    const val VARIANT_SHADOW = "shadow"
    const val VARIANT_IRON_OAK = "iron_oak"
    const val VARIANT_CAPE_HERO = "cape_hero"
    const val VARIANT_EMBER_SPARROW = "ember_sparrow"
    const val VARIANT_COIL_SHADE = "coil_shade"

    fun cosmeticIdForVariant(variant: String): String = when {
        variant == VARIANT_BASIC -> CosmeticCatalog.SKIN_BASIC
        variant == VARIANT_SHADOW -> CosmeticCatalog.SKIN_SHADOW
        variant == VARIANT_IRON_OAK -> CosmeticCatalog.SKIN_IRON_OAK
        variant == VARIANT_CAPE_HERO -> CosmeticCatalog.SKIN_CAPE_HERO
        variant == VARIANT_EMBER_SPARROW -> CosmeticCatalog.SKIN_EMBER_SPARROW
        variant == VARIANT_COIL_SHADE -> CosmeticCatalog.SKIN_COIL_SHADE
        variant.contains("fitness") -> CosmeticCatalog.SKIN_FITNESS
        else -> CosmeticCatalog.SKIN_BASIC
    }

    fun unlockLevel(variant: String): Int? {
        if (variant.contains("premium")) return null
        return CosmeticCatalog.byId(cosmeticIdForVariant(variant))?.unlockLevel
    }

    fun isUnlockedByLevel(variant: String, unlockedCosmeticIds: Set<String>): Boolean {
        if (GodModePrefs.isGodModeActive) return true
        if (variant.contains("premium")) return false
        return cosmeticIdForVariant(variant) in unlockedCosmeticIds
    }

    fun spriteKey(variant: String, gender: String, unlocked: Boolean): String {
        val female = gender == "female"
        val effectiveUnlocked = unlocked || GodModePrefs.isGodModeActive
        return when {
            variant == VARIANT_BASIC -> gender
            variant == VARIANT_SHADOW -> if (female) "locked_woman" else "locked_male"
            variant == VARIANT_IRON_OAK && effectiveUnlocked -> "iron_oak_idle"
            variant == VARIANT_CAPE_HERO && effectiveUnlocked -> "cape_hero_idle"
            variant == VARIANT_EMBER_SPARROW && effectiveUnlocked -> "ember_sparrow_idle"
            variant == VARIANT_COIL_SHADE && effectiveUnlocked -> "coil_shade_idle"
            variant.contains("fitness") && effectiveUnlocked -> {
                if (female) "fitness_character_woman_idle" else "fitness_character_male_idle"
            }
            !effectiveUnlocked -> {
                if (female) "locked_woman" else "locked_male"
            }
            else -> gender
        }
    }
}
