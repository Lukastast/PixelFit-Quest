package com.pixelfitquest.feature.levels.cosmetics

import com.pixelfitquest.feature.levels.model.CosmeticCatalog

/**
 * Maps customization carousel variants onto local cosmetic ids.
 * Customization keeps coin-buy; this only answers "unlocked by level?".
 */
object AvatarSkinBridge {
    const val VARIANT_BASIC = "basic"
    const val VARIANT_SHADOW = "shadow"

    fun cosmeticIdForVariant(variant: String): String = when {
        variant == VARIANT_BASIC -> CosmeticCatalog.SKIN_BASIC
        variant == VARIANT_SHADOW -> CosmeticCatalog.SKIN_SHADOW
        variant.contains("fitness") -> CosmeticCatalog.SKIN_FITNESS
        else -> CosmeticCatalog.SKIN_BASIC
    }

    fun unlockLevel(variant: String): Int? {
        if (variant.contains("premium")) return null
        return CosmeticCatalog.byId(cosmeticIdForVariant(variant))?.unlockLevel
    }

    fun isUnlockedByLevel(variant: String, unlockedCosmeticIds: Set<String>): Boolean {
        if (variant.contains("premium")) return false
        return cosmeticIdForVariant(variant) in unlockedCosmeticIds
    }

    fun spriteKey(variant: String, gender: String, unlocked: Boolean): String {
        val female = gender == "female"
        return when {
            variant == VARIANT_BASIC -> gender
            variant == VARIANT_SHADOW -> if (female) "locked_woman" else "locked_male"
            variant.contains("fitness") && unlocked -> {
                if (female) "fitness_character_woman_idle" else "fitness_character_male_idle"
            }
            variant.contains("premium") || !unlocked -> {
                if (female) "locked_woman" else "locked_male"
            }
            else -> gender
        }
    }
}
