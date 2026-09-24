package com.pixelfitquest.feature.achievements

import androidx.lifecycle.viewModelScope
import com.pixelfitquest.feature.achievements.data.AchievementsRepository
import com.pixelfitquest.feature.achievements.model.AchievementCategory
import com.pixelfitquest.feature.achievements.model.AchievementStatusFilter
import com.pixelfitquest.feature.achievements.model.AchievementsUiState
import com.pixelfitquest.viewmodel.PixelFitViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    repository: AchievementsRepository,
) : PixelFitViewModel() {
    private val selectedCategory = MutableStateFlow<AchievementCategory?>(null)
    private val statusFilter = MutableStateFlow(AchievementStatusFilter.ALL)
    private val selectedId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AchievementsUiState> = combine(
        repository.observeItems(),
        selectedCategory,
        statusFilter,
        selectedId,
    ) { items, category, status, id ->
        AchievementsUiState(
            items = items,
            selectedCategory = category,
            statusFilter = status,
            selectedId = id,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AchievementsUiState(),
    )

    fun onCategorySelected(category: AchievementCategory?) {
        selectedCategory.value = category
    }

    fun onStatusFilterSelected(status: AchievementStatusFilter) {
        statusFilter.value = status
    }

    fun onNextCategory() {
        val allCategories = listOf(null) + AchievementCategory.entries
        val currentIndex = allCategories.indexOf(selectedCategory.value)
        val nextIndex = (currentIndex + 1) % allCategories.size
        selectedCategory.value = allCategories[nextIndex]
    }

    fun onPreviousCategory() {
        val allCategories = listOf(null) + AchievementCategory.entries
        val currentIndex = allCategories.indexOf(selectedCategory.value)
        val prevIndex = if (currentIndex <= 0) allCategories.size - 1 else currentIndex - 1
        selectedCategory.value = allCategories[prevIndex]
    }

    fun onAchievementTapped(id: String) {
        selectedId.value = id
    }
}
