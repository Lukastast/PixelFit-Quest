package com.pixelfitquest.feature.levels.progression

import com.pixelfitquest.feature.levels.model.CosmeticCatalog
import com.pixelfitquest.feature.levels.model.CosmeticDefinition

object CosmeticUnlocker {
    fun unlockedIds(
        level: Int,
        catalog: List<CosmeticDefinition> = CosmeticCatalog.all,
    ): Set<String> = catalog
        .filter { it.unlockLevel <= level }
        .map { it.id }
        .toSet()

    fun newlyUnlocked(
        previousLevel: Int,
        newLevel: Int,
        catalog: List<CosmeticDefinition> = CosmeticCatalog.all,
    ): List<CosmeticDefinition> {
        if (newLevel <= previousLevel) return emptyList()
        return catalog.filter { it.unlockLevel > previousLevel && it.unlockLevel <= newLevel }
    }
}
