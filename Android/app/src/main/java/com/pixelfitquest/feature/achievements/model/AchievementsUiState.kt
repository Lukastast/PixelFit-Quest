package com.pixelfitquest.feature.achievements.model

import com.pixelfitquest.R

enum class AchievementStatusFilter(val labelRes: Int) {
    ALL(R.string.achievements_filter_all),
    IN_PROGRESS(R.string.achievements_filter_in_progress),
    UNLOCKED(R.string.achievements_filter_unlocked),
}

data class AchievementsUiState(
    val items: List<AchievementItem> = emptyList(),
    val selectedCategory: AchievementCategory? = null,
    val statusFilter: AchievementStatusFilter = AchievementStatusFilter.ALL,
    val selectedId: String? = null,
) {
    val visibleItems: List<AchievementItem>
        get() {
            var filtered = if (selectedCategory == null) {
                items
            } else {
                items.filter { it.definition.category == selectedCategory }
            }
            filtered = when (statusFilter) {
                AchievementStatusFilter.ALL -> filtered
                AchievementStatusFilter.IN_PROGRESS -> filtered.filter { !it.isUnlocked }
                AchievementStatusFilter.UNLOCKED -> filtered.filter { it.isUnlocked }
            }
            return filtered
        }

    val selectedItem: AchievementItem?
        get() {
            if (selectedId != null) {
                val fromVisible = visibleItems.find { it.definition.id == selectedId }
                return fromVisible
                    ?: visibleItems.firstOrNull()
                    ?: items.find { it.definition.id == selectedId }
                    ?: items.firstOrNull()
            }
            return visibleItems.firstOrNull { it.isUnlocked }
                ?: visibleItems.firstOrNull()
                ?: items.firstOrNull { it.isUnlocked }
                ?: items.firstOrNull()
        }

    val unlockedCount: Int get() = items.count { it.isUnlocked }

    val totalCount: Int get() = items.size
}
