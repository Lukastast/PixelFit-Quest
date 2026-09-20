package com.pixelfitquest.feature.customization.model

enum class CustomizationTab {
    Character,
    Home,
    Gym,
    Background,
}

data class CustomizationScreenUiState(
    val selectedTab: CustomizationTab = CustomizationTab.Character,
    val gender: String = "male",
    val selectedCharacterId: String = "basic",
    val equippedVariant: String = "basic",
    val unlockedVariants: Set<String> = setOf("basic"),
    val selectedHomeId: String? = null,
    val equippedHomeId: String? = null,
    val unlockedHomeUpgrades: Set<String> = emptySet(),
    val selectedGymId: String = "gym_standard",
    val equippedGymId: String = "gym_standard",
    val unlockedGyms: Set<String> = setOf("gym_standard"),
    val selectedBackgroundId: String = "bg_classic",
    val equippedBackgroundId: String = "bg_classic",
    val unlockedBackgrounds: Set<String> = setOf("bg_classic", "bg_ember"),
    val userCoins: Int = 0,
    val userLevel: Int = 1,
    val heightCm: Int = 178,
    val armLengthCm: Int = 70,
    val isStatsDialogOpen: Boolean = false,
)
