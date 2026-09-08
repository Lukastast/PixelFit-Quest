package com.pixelfitquest.feature.levels.model

enum class CosmeticKind {
    HOME_THEME,
    CHARACTER_SKIN,
    TITLE,
}

data class CosmeticDefinition(
    val id: String,
    val kind: CosmeticKind,
    val unlockLevel: Int,
    val name: String,
    val description: String,
)

data class CosmeticItem(
    val definition: CosmeticDefinition,
    val unlocked: Boolean,
    val equipped: Boolean,
)

data class EquippedCosmetics(
    val homeThemeId: String = CosmeticCatalog.DEFAULT_HOME_ID,
    val characterSkinId: String = CosmeticCatalog.DEFAULT_SKIN_ID,
    val titleId: String = CosmeticCatalog.DEFAULT_TITLE_ID,
)

data class LevelProgress(
    val totalXp: Int = 0,
    val level: Int = 1,
    val xpIntoLevel: Int = 0,
    val xpToNext: Int = 100,
) {
    val isMaxLevel: Boolean get() = level >= 30

    val xpFraction: Float
        get() = if (xpToNext <= 0) {
            1f
        } else {
            (xpIntoLevel.toFloat() / xpToNext.toFloat()).coerceIn(0f, 1f)
        }

    /** Matches the six-frame home XP bar (0 / 20 / 40 / 60 / 80 / 100). */
    val xpBarIndex: Int get() = (xpFraction * 5f).toInt().coerceIn(0, 5)
}

data class LevelUpResult(
    val previous: LevelProgress,
    val current: LevelProgress,
    val newlyUnlocked: List<CosmeticDefinition>,
) {
    val leveledUp: Boolean get() = current.level > previous.level
    val levelsGained: Int get() = (current.level - previous.level).coerceAtLeast(0)
}

data class LevelsSnapshot(
    val progress: LevelProgress = LevelProgress(),
    val equipped: EquippedCosmetics = EquippedCosmetics(),
    val items: List<CosmeticItem> = emptyList(),
)

data class LevelsPersistedState(
    val totalXp: Int = 0,
    val equippedHomeThemeId: String = CosmeticCatalog.DEFAULT_HOME_ID,
    val equippedCharacterSkinId: String = CosmeticCatalog.DEFAULT_SKIN_ID,
    val equippedTitleId: String = CosmeticCatalog.DEFAULT_TITLE_ID,
    val unlockedIds: Set<String> = emptySet(),
    val unlockedAtEpochMs: Map<String, Long> = emptyMap(),
)
