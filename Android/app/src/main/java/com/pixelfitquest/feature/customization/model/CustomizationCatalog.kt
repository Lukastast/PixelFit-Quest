package com.pixelfitquest.feature.customization.model

import com.pixelfitquest.R
import com.pixelfitquest.feature.home.model.DwellingTier

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
            bonusDescription = "+2 coins & +2 exp per reward",
            unlockType = UnlockType.COINS,
            minLevel = 5,
            coinPrice = 100,
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
            name = "Auto (By Level)",
            description = "Dwelling evolves automatically as your hero levels up.",
            landscapeRes = R.drawable.dwelling_tarp_landscape,
            unlockType = UnlockType.DEFAULT,
        ),
        HomeDwellingItem(
            id = DwellingTier.TARP.id,
            name = "Worn Tarp",
            description = "A humble tarp shelter for the start of your journey.",
            landscapeRes = R.drawable.dwelling_tarp_landscape,
            unlockType = UnlockType.LEVEL,
            minLevel = 1,
        ),
        HomeDwellingItem(
            id = DwellingTier.TENT.id,
            name = "Campfire Tent",
            description = "A cozy campfire tent under the night sky.",
            landscapeRes = R.drawable.dwelling_tent_landscape,
            unlockType = UnlockType.LEVEL,
            minLevel = 5,
        ),
        HomeDwellingItem(
            id = DwellingTier.SHACK.id,
            name = "Wooden Shack",
            description = "A sturdy rustic cabin built from solid timber.",
            landscapeRes = R.drawable.dwelling_shack_landscape,
            unlockType = UnlockType.LEVEL,
            minLevel = 10,
        ),
        HomeDwellingItem(
            id = DwellingTier.COTTAGE.id,
            name = "Stone Cottage",
            description = "A warm stone cottage with a roaring fireplace.",
            landscapeRes = R.drawable.dwelling_cottage_landscape,
            unlockType = UnlockType.LEVEL,
            minLevel = 15,
        ),
        HomeDwellingItem(
            id = DwellingTier.CASTLE.id,
            name = "Grand Keep",
            description = "A majestic stone castle fit for a quest champion.",
            landscapeRes = R.drawable.dwelling_castle_landscape,
            unlockType = UnlockType.LEVEL,
            minLevel = 25,
        ),
        HomeDwellingItem(
            id = DwellingTier.GYM.id,
            name = "Iron Gym Dwelling",
            description = "An iron gym with personal workout gear and bed.",
            landscapeRes = R.drawable.dwelling_gym_landscape,
            unlockType = UnlockType.COINS,
            coinPrice = 150,
        ),
    )

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
            coinPrice = 150,
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
            coinPrice = 200,
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
