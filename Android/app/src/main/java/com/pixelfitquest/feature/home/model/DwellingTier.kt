package com.pixelfitquest.feature.home.model

enum class DwellingTier(
    val id: String,
    val displayName: String,
    val minLevel: Int,
    val coinPrice: Int,
    val xpBonusPercent: Int,
    val coinBonusPercent: Int,
    /**
     * Level that used to grant this home for free. Null when it was never free.
     * Read only by the one-time migration, not by later level-ups.
     */
    val legacyFreeLevel: Int?,
) {
    TARP(
        id = "dwelling_tarp",
        displayName = "Worn Tarp",
        minLevel = 1,
        coinPrice = 0,
        xpBonusPercent = 0,
        coinBonusPercent = 0,
        legacyFreeLevel = 1,
    ),
    TENT(
        id = "dwelling_tent",
        displayName = "Campfire Tent",
        minLevel = 5,
        coinPrice = 100,
        xpBonusPercent = 0,
        coinBonusPercent = 8,
        legacyFreeLevel = 5,
    ),
    SHACK(
        id = "dwelling_shack",
        displayName = "Wooden Shack",
        minLevel = 12,
        coinPrice = 260,
        xpBonusPercent = 8,
        coinBonusPercent = 0,
        legacyFreeLevel = 10,
    ),
    COTTAGE(
        id = "dwelling_cottage",
        displayName = "Stone Cottage",
        minLevel = 25,
        coinPrice = 520,
        xpBonusPercent = 8,
        coinBonusPercent = 8,
        legacyFreeLevel = 15,
    ),
    CASTLE(
        id = "dwelling_castle",
        displayName = "Grand Keep",
        minLevel = 45,
        coinPrice = 1100,
        xpBonusPercent = 12,
        coinBonusPercent = 8,
        legacyFreeLevel = 25,
    ),
    GYM(
        id = "dwelling_gym",
        displayName = "Iron Gym",
        minLevel = 70,
        coinPrice = 2000,
        xpBonusPercent = 15,
        coinBonusPercent = 10,
        legacyFreeLevel = null,
    );

    fun bonusSentence(): String = when {
        xpBonusPercent > 0 && coinBonusPercent > 0 ->
            "+$xpBonusPercent% workout XP and +$coinBonusPercent% coins while equipped."
        xpBonusPercent > 0 -> "+$xpBonusPercent% workout XP while equipped."
        coinBonusPercent > 0 -> "+$coinBonusPercent% workout coins while equipped."
        else -> "No training bonus."
    }

    fun shopDescription(): String = when (this) {
        TARP -> "Starting camp. ${bonusSentence()}"
        else -> "Level $minLevel. ${bonusSentence()}"
    }

    companion object {
        fun fromId(id: String?): DwellingTier? = entries.find { it.id == id }

        fun legacyGrantedIds(level: Int): Set<String> = entries
            .filter { tier ->
                val gate = tier.legacyFreeLevel
                gate != null && level >= gate
            }
            .map { it.id }
            .toSet()

        fun bestOwned(ownedIds: Collection<String>): DwellingTier {
            val owned = ownedIds.toSet()
            return entries.lastOrNull { it.id in owned } ?: TARP
        }

        /** Equipped home, or the best one already owned. Never invents a home from level alone. */
        fun resolve(equippedId: String?, ownedIds: Collection<String>): DwellingTier {
            return fromId(equippedId) ?: bestOwned(ownedIds)
        }

        fun migrateOwnership(
            level: Int,
            ownedIds: Collection<String>,
            alreadyMigrated: Boolean,
        ): List<String> {
            val kept = ownedIds.filter { fromId(it) != null }.distinct()
            if (alreadyMigrated) return kept
            return (kept + legacyGrantedIds(level)).distinct()
        }
    }
}

enum class CharacterPose {
    STANDING,
    SITTING,
    LYING;

    fun next(): CharacterPose = when (this) {
        STANDING -> SITTING
        SITTING -> LYING
        LYING -> STANDING
    }
}
