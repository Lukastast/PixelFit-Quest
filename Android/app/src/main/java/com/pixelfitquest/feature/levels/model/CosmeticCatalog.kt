package com.pixelfitquest.feature.levels.model

/**
 * Static local catalog. Level-ups unlock cosmetics only — no paywall, no cloud.
 */
object CosmeticCatalog {
    const val DEFAULT_HOME_ID = "home_stone"
    const val DEFAULT_SKIN_ID = "skin_basic"
    const val DEFAULT_TITLE_ID = "title_rookie"

    const val HOME_STONE = DEFAULT_HOME_ID
    const val HOME_GYM = "home_gym"
    const val HOME_EMBER = "home_ember"
    const val HOME_DUSK = "home_dusk"
    const val HOME_NIGHT = "home_night"
    const val HOME_LEGEND = "home_legend"

    const val SKIN_BASIC = DEFAULT_SKIN_ID
    const val SKIN_FITNESS = "skin_fitness"
    const val SKIN_SHADOW = "skin_shadow"

    const val TITLE_ROOKIE = DEFAULT_TITLE_ID
    const val TITLE_SCOUT = "title_scout"
    const val TITLE_ADVENTURER = "title_adventurer"
    const val TITLE_CHAMPION = "title_champion"
    const val TITLE_LEGEND = "title_legend"
    const val TITLE_VETERAN = "title_veteran"
    const val TITLE_WARDEN = "title_warden"
    const val TITLE_MYTHIC = "title_mythic"
    const val TITLE_ASCENDANT = "title_ascendant"

    val all: List<CosmeticDefinition> = listOf(
        cosmetic(
            id = HOME_STONE,
            kind = CosmeticKind.HOME_THEME,
            unlockLevel = 1,
            name = "Dungeon Stone",
            description = "The classic quest hall.",
        ),
        cosmetic(
            id = HOME_GYM,
            kind = CosmeticKind.HOME_THEME,
            unlockLevel = 3,
            name = "Iron Gym",
            description = "Orange brick training floor.",
        ),
        cosmetic(
            id = HOME_EMBER,
            kind = CosmeticKind.HOME_THEME,
            unlockLevel = 8,
            name = "Ember Hall",
            description = "Torchlit rust-stone walls.",
        ),
        cosmetic(
            id = HOME_DUSK,
            kind = CosmeticKind.HOME_THEME,
            unlockLevel = 15,
            name = "Dusk Gym",
            description = "Sunset through the gym window.",
        ),
        cosmetic(
            id = HOME_NIGHT,
            kind = CosmeticKind.HOME_THEME,
            unlockLevel = 20,
            name = "Night Watch",
            description = "Moonlit navy dungeon brick.",
        ),
        cosmetic(
            id = HOME_LEGEND,
            kind = CosmeticKind.HOME_THEME,
            unlockLevel = 25,
            name = "Legend Keep",
            description = "Gold-trimmed halls of glory.",
        ),
        cosmetic(
            id = SKIN_BASIC,
            kind = CosmeticKind.CHARACTER_SKIN,
            unlockLevel = 1,
            name = "Wanderer",
            description = "Starting outfit.",
        ),
        cosmetic(
            id = SKIN_SHADOW,
            kind = CosmeticKind.CHARACTER_SKIN,
            unlockLevel = 12,
            name = "Shadow",
            description = "Silhouette fighter look.",
        ),
        cosmetic(
            id = TITLE_ROOKIE,
            kind = CosmeticKind.TITLE,
            unlockLevel = 1,
            name = "Rookie",
            description = "Every hero starts here.",
        ),
        cosmetic(
            id = TITLE_SCOUT,
            kind = CosmeticKind.TITLE,
            unlockLevel = 5,
            name = "Scout",
            description = "The first miles are behind you.",
        ),
        cosmetic(
            id = TITLE_ADVENTURER,
            kind = CosmeticKind.TITLE,
            unlockLevel = 10,
            name = "Adventurer",
            description = "Double-digit dedication.",
        ),
        cosmetic(
            id = TITLE_CHAMPION,
            kind = CosmeticKind.TITLE,
            unlockLevel = 20,
            name = "Champion",
            description = "The gym knows your name.",
        ),
        cosmetic(
            id = TITLE_LEGEND,
            kind = CosmeticKind.TITLE,
            unlockLevel = 30,
            name = "Legend",
            description = "A name the gym remembers.",
        ),
        cosmetic(
            id = TITLE_VETERAN,
            kind = CosmeticKind.TITLE,
            unlockLevel = 40,
            name = "Veteran",
            description = "Forty levels of showing up.",
        ),
        cosmetic(
            id = TITLE_WARDEN,
            kind = CosmeticKind.TITLE,
            unlockLevel = 60,
            name = "Warden",
            description = "The hall keeps watch with you.",
        ),
        cosmetic(
            id = TITLE_MYTHIC,
            kind = CosmeticKind.TITLE,
            unlockLevel = 80,
            name = "Mythic",
            description = "Stories start to sound unlikely.",
        ),
        cosmetic(
            id = TITLE_ASCENDANT,
            kind = CosmeticKind.TITLE,
            unlockLevel = 100,
            name = "Ascendant",
            description = "Level 100. The road is still open.",
        ),
    )

    private val index: Map<String, CosmeticDefinition> = all.associateBy { it.id }

    fun byId(id: String): CosmeticDefinition? = index[id]

    fun defaultsForKind(kind: CosmeticKind): String = when (kind) {
        CosmeticKind.HOME_THEME -> DEFAULT_HOME_ID
        CosmeticKind.CHARACTER_SKIN -> DEFAULT_SKIN_ID
        CosmeticKind.TITLE -> DEFAULT_TITLE_ID
    }
}

private fun cosmetic(
    id: String,
    kind: CosmeticKind,
    unlockLevel: Int,
    name: String,
    description: String,
): CosmeticDefinition = CosmeticDefinition(
    id = id,
    kind = kind,
    unlockLevel = unlockLevel,
    name = name,
    description = description,
)
