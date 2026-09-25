package com.pixelfitquest.feature.customization.model

import com.pixelfitquest.debug.GodModePrefs
import com.pixelfitquest.feature.levels.cosmetics.AvatarSkinBridge

/**
 * Gym Fit used to unlock for free at level 5. Grant it once for heroes who
 * already reached that level. Later level-ups do not call this.
 */
fun grandfatherFitnessVariants(
    level: Int,
    ownedVariants: Collection<String>,
    alreadyMigrated: Boolean,
): List<String> {
    val kept = ownedVariants.filter { it.isNotBlank() }.distinct()
    if (alreadyMigrated || level < 5) return kept
    return (kept + listOf("male_fitness", "female_fitness")).distinct()
}

fun meetsLevelGate(minLevel: Int?, userLevel: Int): Boolean =
    GodModePrefs.isGodModeActive || minLevel == null || userLevel >= minLevel

/** True when the buy button must wait for a level, even if the item also has a price. */
fun purchaseBlockedByLevel(unlocked: Boolean, minLevel: Int?, userLevel: Int): Boolean =
    if (GodModePrefs.isGodModeActive) false else !unlocked && !meetsLevelGate(minLevel, userLevel)

fun isShopItemUnlocked(
    unlockType: UnlockType,
    minLevel: Int?,
    userLevel: Int,
    owned: Boolean,
): Boolean {
    if (GodModePrefs.isGodModeActive) return true
    return when (unlockType) {
        UnlockType.DEFAULT -> true
        UnlockType.LEVEL -> meetsLevelGate(minLevel, userLevel)
        UnlockType.COINS -> owned
        UnlockType.COMING_SOON -> false
    }
}

fun isCharacterUnlocked(
    item: CharacterItem,
    variant: String,
    userLevel: Int,
    unlockedVariants: Set<String>,
    unlockedLevelSkinIds: Set<String>,
): Boolean {
    if (GodModePrefs.isGodModeActive) return true
    if (item.unlockType == UnlockType.DEFAULT) return true
    if (item.unlockType == UnlockType.COMING_SOON) return false
    if (variant in unlockedVariants) return true
    val earnedOnTheLevelTrack = AvatarSkinBridge.isUnlockedByLevel(variant, unlockedLevelSkinIds)
    return when (item.unlockType) {
        UnlockType.LEVEL -> meetsLevelGate(item.minLevel, userLevel) || earnedOnTheLevelTrack
        // Coin gear stays bought, including copies the old level track already granted.
        UnlockType.COINS -> earnedOnTheLevelTrack
        UnlockType.DEFAULT, UnlockType.COMING_SOON -> false
    }
}
