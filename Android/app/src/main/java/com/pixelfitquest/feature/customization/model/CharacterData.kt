package com.pixelfitquest.feature.customization.model

data class CharacterData(
    val gender: String = "male",
    val variant: String = "basic",
    val unlockedVariants: List<String> = listOf("basic"),
    val equippedHomeUpgrade: String? = null,
    val unlockedHomeUpgrades: List<String> = emptyList(),
) {
    constructor() : this(
        gender = "male",
        variant = "basic",
        unlockedVariants = listOf("basic"),
        equippedHomeUpgrade = null,
        unlockedHomeUpgrades = emptyList(),
    )
}