package com.pixelfitquest.feature.home.model

enum class DwellingTier(
    val id: String,
    val displayName: String,
    val minLevel: Int,
) {
    TARP("dwelling_tarp", "Worn Tarp", 1),
    TENT("dwelling_tent", "Campfire Tent", 5),
    SHACK("dwelling_shack", "Wooden Shack", 10),
    COTTAGE("dwelling_cottage", "Stone Cottage", 15),
    CASTLE("dwelling_castle", "Grand Keep", 25);

    companion object {
        fun forLevel(level: Int): DwellingTier =
            entries.lastOrNull { level >= it.minLevel } ?: TARP
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
