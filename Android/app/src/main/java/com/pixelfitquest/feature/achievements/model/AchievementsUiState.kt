package com.pixelfitquest.feature.achievements.model

data class AchievementsUiState(
    val items: List<AchievementItem> = emptyList(),
    val selectedCategory: AchievementCategory? = null,
    val selectedId: String? = null,
) {
    val visibleItems: List<AchievementItem>
        get() = if (selectedCategory == null) {
            items
        } else {
            items.filter { it.definition.category == selectedCategory }
        }

    val selectedItem: AchievementItem?
        get() = selectedId?.let { id -> items.find { it.definition.id == id } }

    val unlockedCount: Int get() = items.count { it.isUnlocked }

    val totalCount: Int get() = items.size
}
