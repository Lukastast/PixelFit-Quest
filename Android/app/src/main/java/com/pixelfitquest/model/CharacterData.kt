package com.pixelfitquest.model

data class CharacterData(
    val gender: String = "male",
    val variant: String = "basic",
    val unlockedVariants: List<String> = listOf("basic")
) {
    constructor() : this(gender = "male", variant = "basic", unlockedVariants = listOf("basic"))
}