package com.pixelfitquest.feature.customization.model

import com.pixelfitquest.R
import com.pixelfitquest.feature.home.model.DwellingTier
import com.pixelfitquest.feature.home.model.DwellingVisuals

enum class UnlockType {
    DEFAULT,
    LEVEL,
    COINS,
    COMING_SOON
}

data class CharacterItem(
    val id: String,
    val name: String,
    val description: String,
    val bonusDescription: String? = null,
    val unlockType: UnlockType,
    val minLevel: Int? = null,
    val coinPrice: Int? = null,
    val isPremium: Boolean = false,
)

data class HomeDwellingItem(
    val id: String?, // null = Auto (by level)
    val name: String,
    val description: String,
    val landscapeRes: Int,
    val unlockType: UnlockType,
    val minLevel: Int? = null,
    val coinPrice: Int? = null,
)

data class GymItem(
    val id: String,
    val name: String,
    val description: String,
    val drawableRes: Int,
    val unlockType: UnlockType,
    val minLevel: Int? = null,
    val coinPrice: Int? = null,
)

data class AppBackgroundItem(
    val id: String,
    val name: String,
    val description: String,
    val drawableRes: Int,
    val unlockType: UnlockType,
    val minLevel: Int? = null,
    val coinPrice: Int? = null,
)

object CustomizationCatalog {
    val characters = listOf(
        CharacterItem(
            id = "basic",
            name = "Wanderer",
            description = "The classic adventurer look.",
            bonusDescription = null,
            unlockType = UnlockType.DEFAULT,
        ),
        CharacterItem(
            id = "fitness",
            name = "Gym Fit",
            description = "High-performance training gear.",
            bonusDescription = "+2 XP and +2 coins on workout, mission, and health rewards",
            unlockType = UnlockType.COINS,
            minLevel = 8,
            coinPrice = 180,
        ),
        CharacterItem(
            id = "shadow",
            name = "Shadow",
            description = "Silhouette fighter formed from dark resolve.",
            bonusDescription = null,
            unlockType = UnlockType.LEVEL,
            minLevel = 12,
        ),
        CharacterItem(
            id = "premium",
            name = "Hero",
            description = "Legendary cape warrior armor.",
            bonusDescription = null,
            unlockType = UnlockType.COMING_SOON,
            isPremium = true,
        ),
    )

    val homeDwellings = listOf(
        HomeDwellingItem(
            id = null,
            name = "Best Owned",
            description = "Shows the finest home you already own.",
            landscapeRes = R.drawable.dwelling_tarp_landscape,
            unlockType = UnlockType.DEFAULT,
        ),
    ) + DwellingTier.entries.map { tier ->
        HomeDwellingItem(
            id = tier.id,
            name = tier.displayName,
            description = tier.shopDescription(),
            landscapeRes = DwellingVisuals.landscapeBgRes(tier),
            unlockType = if (tier.coinPrice > 0) UnlockType.COINS else UnlockType.DEFAULT,
            minLevel = tier.minLevel,
            coinPrice = tier.coinPrice.takeIf { it > 0 },
        )
    }

    val gyms = listOf(
        GymItem(
            id = "gym_standard",
            name = "Iron Gym",
            description = "Classic brick training floor with weight stations.",
            drawableRes = R.drawable.gym_background,
            unlockType = UnlockType.DEFAULT,
        ),
        GymItem(
            id = "gym_dusk",
            name = "Dusk Gym",
            description = "Warm sunset light streaming through the gym arches.",
            drawableRes = R.drawable.home_theme_dusk_gym,
            unlockType = UnlockType.LEVEL,
            minLevel = 10,
        ),
        GymItem(
            id = "gym_dungeon",
            name = "Dungeon Vault",
            description = "Torchlit ancient stone vault with heavy iron equipment.",
            drawableRes = R.drawable.gym_dungeon,
            unlockType = UnlockType.COINS,
            minLevel = 18,
            coinPrice = 320,
        ),
        GymItem(
            id = "gym_cyber",
            name = "Cyber Synth",
            description = "Neon cyan and purple synthwave training studio.",
            drawableRes = R.drawable.gym_cyber,
            unlockType = UnlockType.LEVEL,
            minLevel = 20,
        ),
        GymItem(
            id = "gym_rooftop",
            name = "Rooftop Summit",
            description = "Open-air rooftop gym overlooking the city skyline.",
            drawableRes = R.drawable.gym_rooftop,
            unlockType = UnlockType.COINS,
            minLevel = 40,
            coinPrice = 700,
        ),
        GymItem(
            id = "gym_champion",
            name = "Champion Hall",
            description = "Gold-trimmed marble arena for elite lifting legends.",
            drawableRes = R.drawable.gym_champion,
            unlockType = UnlockType.LEVEL,
            minLevel = 25,
        ),
    )

    val appBackgrounds = listOf(
        AppBackgroundItem(
            id = "bg_classic",
            name = "Classic Quest",
            description = "The classic PixelFit Quest stone and forest background.",
            drawableRes = R.drawable.logsigninbackground,
            unlockType = UnlockType.DEFAULT,
        ),
        AppBackgroundItem(
            id = "bg_ember",
            name = "Ember Hall",
            description = "Warm torchlit stone brick atmosphere.",
            drawableRes = R.drawable.home_theme_ember,
            unlockType = UnlockType.DEFAULT,
        ),
        AppBackgroundItem(
            id = "bg_night",
            name = "Night Watch",
            description = "Deep midnight blue stone brick atmosphere.",
            drawableRes = R.drawable.home_theme_night,
            unlockType = UnlockType.LEVEL,
            minLevel = 15,
        ),
        AppBackgroundItem(
            id = "bg_legend",
            name = "Legend Keep",
            description = "Gleaming golden halls of triumph and glory.",
            drawableRes = R.drawable.home_theme_legend,
            unlockType = UnlockType.LEVEL,
            minLevel = 25,
        ),
    )

    fun gymDrawable(id: String?): Int {
        return gyms.find { it.id == id }?.drawableRes ?: R.drawable.gym_background
    }

    fun appBackgroundDrawable(id: String?): Int {
        return appBackgrounds.find { it.id == id }?.drawableRes ?: R.drawable.logsigninbackground
    }

    fun resolveCharacterIdFromVariant(variant: String): String {
        return when {
            variant.contains("fitness") -> "fitness"
            variant.contains("premium") -> "premium"
            variant == "shadow" -> "shadow"
            else -> "basic"
        }
    }
}
