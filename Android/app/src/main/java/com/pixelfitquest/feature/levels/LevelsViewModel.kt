package com.pixelfitquest.feature.levels

import androidx.lifecycle.viewModelScope
import com.pixelfitquest.feature.levels.cosmetics.AvatarSkinBridge
import com.pixelfitquest.feature.levels.data.LevelsRepository
import com.pixelfitquest.feature.levels.model.CosmeticKind
import com.pixelfitquest.feature.levels.model.LevelsUiState
import com.pixelfitquest.viewmodel.PixelFitViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LevelsViewModel @Inject constructor(
    private val repository: LevelsRepository,
) : PixelFitViewModel() {
    private val selectedKind = MutableStateFlow<CosmeticKind?>(null)
    private val selectedId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<LevelsUiState> = combine(
        repository.observeSnapshot(),
        repository.observePendingLevelUp(),
        selectedKind,
        selectedId,
    ) { snapshot, pending, kind, id ->
        LevelsUiState(
            progress = snapshot.progress,
            items = snapshot.items,
            equipped = snapshot.equipped,
            selectedKind = kind,
            selectedId = id,
            pendingLevelUp = pending,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LevelsUiState(),
    )

    fun onFilterSelected(kind: CosmeticKind?) {
        selectedKind.value = if (kind != null && selectedKind.value == kind) null else kind
    }

    fun onCosmeticTapped(id: String) {
        selectedId.value = if (selectedId.value == id) null else id
    }

    fun equipSelected() {
        val id = selectedId.value ?: return
        launchCatching { repository.equipCosmetic(id) }
    }

    fun equipCosmetic(id: String) {
        launchCatching { repository.equipCosmetic(id) }
    }

    fun equipByAvatarVariant(variant: String) {
        launchCatching {
            repository.equipCosmetic(AvatarSkinBridge.cosmeticIdForVariant(variant))
        }
    }

    fun dismissLevelUp() {
        launchCatching { repository.dismissLevelUp() }
    }

    fun importRemoteIfEmpty(level: Int, exp: Int) {
        launchCatching { repository.importRemoteIfEmpty(level, exp) }
    }
}
