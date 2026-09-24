package com.pixelfitquest.feature.levels

import androidx.lifecycle.viewModelScope
import com.pixelfitquest.feature.levels.cosmetics.AvatarSkinBridge
import com.pixelfitquest.feature.levels.data.LevelsRepository
import com.pixelfitquest.feature.levels.model.CosmeticKind
import com.pixelfitquest.feature.levels.model.LevelsUiState
import com.pixelfitquest.feature.progression.RespecOutcome
import com.pixelfitquest.feature.progression.SkillBranch
import com.pixelfitquest.feature.progression.SkillTree
import com.pixelfitquest.firebase.repository.UserRepository
import com.pixelfitquest.helpers.SnackbarManager
import com.pixelfitquest.viewmodel.PixelFitViewModel
import java.time.LocalDate
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
    private val userRepository: UserRepository,
) : PixelFitViewModel() {
    private val selectedKind = MutableStateFlow<CosmeticKind?>(null)
    private val selectedId = MutableStateFlow<String?>(null)
    private val wallet = combine(
        userRepository.observeSkills(),
        userRepository.getUserData(),
    ) { skills, user ->
        skills to (user?.coins ?: 0)
    }

    val uiState: StateFlow<LevelsUiState> = combine(
        repository.observeSnapshot(),
        repository.observePendingLevelUp(),
        wallet,
        selectedKind,
        selectedId,
    ) { snapshot, pending, skillsAndCoins, kind, id ->
        val (skills, coins) = skillsAndCoins
        LevelsUiState(
            progress = snapshot.progress,
            items = snapshot.items,
            equipped = snapshot.equipped,
            selectedKind = kind,
            selectedId = id,
            pendingLevelUp = pending,
            skills = skills,
            coins = coins,
            respecDaysRemaining = SkillTree.daysUntilRespec(LocalDate.now(), skills.lastRespecDate),
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

    fun spendSkill(branch: SkillBranch) {
        launchCatching { userRepository.spendSkillPoint(branch) }
    }

    fun respecSkills() {
        launchCatching {
            when (userRepository.respecSkills()) {
                RespecOutcome.CANT_AFFORD -> SnackbarManager.showMessage("Need 150 coins to respec")
                RespecOutcome.COOLDOWN -> SnackbarManager.showMessage("Respec is available once a week")
                RespecOutcome.RESET, RespecOutcome.NOTHING_SPENT -> Unit
            }
        }
    }

    fun importRemoteIfEmpty(level: Int, exp: Int) {
        launchCatching { repository.importRemoteIfEmpty(level, exp) }
    }
}
