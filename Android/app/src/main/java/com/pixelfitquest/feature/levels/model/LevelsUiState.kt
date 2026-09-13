package com.pixelfitquest.feature.levels.model

data class LevelsUiState(
    val progress: LevelProgress = LevelProgress(),
    val items: List<CosmeticItem> = emptyList(),
    val equipped: EquippedCosmetics = EquippedCosmetics(),
    val selectedKind: CosmeticKind? = null,
    val selectedId: String? = null,
    val pendingLevelUp: LevelUpResult? = null,
) {
    val visibleItems: List<CosmeticItem>
        get() = if (selectedKind == null) {
            items
        } else {
            items.filter { it.definition.kind == selectedKind }
        }

    val selectedItem: CosmeticItem?
        get() = selectedId?.let { id -> items.find { it.definition.id == id } }

    val equippedTitleName: String
        get() = items.find { it.definition.id == equipped.titleId }?.definition?.name.orEmpty()

    val unlockedCount: Int get() = items.count { it.unlocked }

    val totalCount: Int get() = items.size
}
