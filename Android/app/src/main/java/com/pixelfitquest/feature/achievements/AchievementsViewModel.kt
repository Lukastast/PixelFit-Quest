package com.pixelfitquest.feature.achievements

import androidx.lifecycle.viewModelScope
import com.pixelfitquest.feature.achievements.data.AchievementsRepository
import com.pixelfitquest.feature.achievements.model.AchievementCategory
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
    private val selectedId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AchievementsUiState> = combine(
        repository.observeItems(),
        selectedCategory,
        selectedId,
    ) { items, category, id ->
        AchievementsUiState(
            items = items,
            selectedCategory = category,
            selectedId = id,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AchievementsUiState(),
    )

    fun onCategorySelected(category: AchievementCategory?) {
        selectedCategory.value = if (category != null && selectedCategory.value == category) {
            null
        } else {
            category
        }
    }

    fun onAchievementTapped(id: String) {
        selectedId.value = if (selectedId.value == id) null else id
    }
}
