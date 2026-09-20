package com.pixelfitquest.feature.customization.model

data class CharacterData(
    val gender: String = "male",
    val variant: String = "basic",
    val unlockedVariants: List<String> = listOf("basic"),
    val equippedHomeUpgrade: String? = null,
    val unlockedHomeUpgrades: List<String> = emptyList(),
    val equippedGym: String = "gym_standard",
    val unlockedGyms: List<String> = listOf("gym_standard"),
    val equippedAppBackground: String = "bg_classic",
    val unlockedAppBackgrounds: List<String> = listOf("bg_classic", "bg_ember"),
) {
    constructor() : this(
        gender = "male",
        variant = "basic",
        unlockedVariants = listOf("basic"),
        equippedHomeUpgrade = null,
        unlockedHomeUpgrades = emptyList(),
        equippedGym = "gym_standard",
        unlockedGyms = listOf("gym_standard"),
        equippedAppBackground = "bg_classic",
        unlockedAppBackgrounds = listOf("bg_classic", "bg_ember"),
    )
}