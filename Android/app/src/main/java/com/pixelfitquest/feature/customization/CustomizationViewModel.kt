package com.pixelfitquest.feature.customization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelfitquest.feature.customization.model.CustomizationCatalog
import com.pixelfitquest.feature.customization.model.CustomizationScreenUiState
import com.pixelfitquest.feature.customization.model.CustomizationTab
import com.pixelfitquest.feature.customization.model.CharacterData
import com.pixelfitquest.firebase.model.UserData
import com.pixelfitquest.firebase.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class CustomizationViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _characterData = MutableStateFlow(CharacterData())
    val characterData: StateFlow<CharacterData> = _characterData.asStateFlow()

    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData.asStateFlow()

    private val _uiState = MutableStateFlow(CustomizationScreenUiState())
    val uiState: StateFlow<CustomizationScreenUiState> = _uiState.asStateFlow()

    private var hasInitializedSelections = false

    init {
        loadCharacterData()
        loadUserData()
    }

    private fun loadCharacterData() {
        viewModelScope.launch {
            userRepository.getCharacterData().collectLatest { data ->
                val char = data ?: CharacterData()
                _characterData.value = char
                val charId = CustomizationCatalog.resolveCharacterIdFromVariant(char.variant)
                _uiState.update { current ->
                    val currentEquippedCharId = CustomizationCatalog.resolveCharacterIdFromVariant(current.equippedVariant)
                    val newSelectedCharId = if (!hasInitializedSelections || current.selectedCharacterId == currentEquippedCharId) {
                        charId
                    } else {
                        current.selectedCharacterId
                    }
                    val newSelectedHomeId = if (!hasInitializedSelections || current.selectedHomeId == current.equippedHomeId) {
                        char.equippedHomeUpgrade
                    } else {
                        current.selectedHomeId
                    }
                    val newSelectedGymId = if (!hasInitializedSelections || current.selectedGymId == current.equippedGymId) {
                        char.equippedGym
                    } else {
                        current.selectedGymId
                    }
                    val newSelectedBgId = if (!hasInitializedSelections || current.selectedBackgroundId == current.equippedBackgroundId) {
                        char.equippedAppBackground
                    } else {
                        current.selectedBackgroundId
                    }
                    hasInitializedSelections = true
                    current.copy(
                        gender = char.gender,
                        selectedCharacterId = newSelectedCharId,
                        equippedVariant = char.variant,
                        unlockedVariants = char.unlockedVariants.toSet(),
                        selectedHomeId = newSelectedHomeId,
                        equippedHomeId = char.equippedHomeUpgrade,
                        unlockedHomeUpgrades = char.unlockedHomeUpgrades.toSet(),
                        selectedGymId = newSelectedGymId,
                        equippedGymId = char.equippedGym,
                        unlockedGyms = char.unlockedGyms.toSet(),
                        selectedBackgroundId = newSelectedBgId,
                        equippedBackgroundId = char.equippedAppBackground,
                        unlockedBackgrounds = char.unlockedAppBackgrounds.toSet(),
                    )
                }
            }
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            userRepository.getUserData().collectLatest { data ->
                _userData.value = data
                if (data != null) {
                    _uiState.update { current ->
                        current.copy(
                            userCoins = data.coins,
                            userLevel = data.level,
                            heightCm = data.height,
                            armLengthCm = data.armLength?.roundToInt() ?: ARM_CM_DEFAULT,
                        )
                    }
                }
            }
        }
    }

    fun selectTab(tab: CustomizationTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    // --- Character Actions ---

    fun selectGender(gender: String) {
        val currentVariant = _characterData.value.variant
        val newVariant = when {
            currentVariant.contains("fitness") -> if (gender == "female") "female_fitness" else "male_fitness"
            currentVariant.contains("premium") -> if (gender == "female") "female_premium" else "male_premium"
            else -> currentVariant
        }
        _characterData.value = _characterData.value.copy(gender = gender, variant = newVariant)
        _uiState.update { it.copy(gender = gender, equippedVariant = newVariant) }
        saveData()
    }

    fun updateGender(gender: String) = selectGender(gender)

    fun selectCharacter(characterId: String) {
        _uiState.update { it.copy(selectedCharacterId = characterId) }
    }

    fun equipCharacter(characterId: String) {
        val variant = resolveVariantForGender(characterId, _characterData.value.gender)
        _characterData.value = _characterData.value.copy(variant = variant)
        _uiState.update { it.copy(equippedVariant = variant, selectedCharacterId = characterId) }
        saveData()
    }

    fun updateVariant(variant: String) {
        val charId = CustomizationCatalog.resolveCharacterIdFromVariant(variant)
        _characterData.value = _characterData.value.copy(variant = variant)
        _uiState.update { it.copy(equippedVariant = variant, selectedCharacterId = charId) }
        saveData()
    }

    fun buyCharacter(characterId: String, price: Int) {
        viewModelScope.launch {
            val user = userRepository.fetchUserDataOnce()
            val current = _characterData.value
            val variant = resolveVariantForGender(characterId, current.gender)
            if (user != null && user.coins >= price && !current.unlockedVariants.contains(variant)) {
                userRepository.updateUserData(mapOf("coins" to (user.coins - price)))
                val newUnlocked = current.unlockedVariants + variant
                _characterData.value = current.copy(
                    variant = variant,
                    unlockedVariants = newUnlocked,
                )
                _uiState.update {
                    it.copy(
                        userCoins = user.coins - price,
                        equippedVariant = variant,
                        selectedCharacterId = characterId,
                        unlockedVariants = newUnlocked.toSet(),
                    )
                }
                saveData()
            }
        }
    }

    fun buyVariant(variant: String, price: Int) {
        viewModelScope.launch {
            val user = userRepository.fetchUserDataOnce()
            val current = _characterData.value
            if (user != null && user.coins >= price && !current.unlockedVariants.contains(variant)) {
                userRepository.updateUserData(mapOf("coins" to (user.coins - price)))
                val newUnlocked = current.unlockedVariants + variant
                val charId = CustomizationCatalog.resolveCharacterIdFromVariant(variant)
                _characterData.value = current.copy(
                    variant = variant,
                    unlockedVariants = newUnlocked,
                )
                _uiState.update {
                    it.copy(
                        userCoins = user.coins - price,
                        equippedVariant = variant,
                        selectedCharacterId = charId,
                        unlockedVariants = newUnlocked.toSet(),
                    )
                }
                saveData()
            }
        }
    }

    private fun resolveVariantForGender(characterId: String, gender: String): String {
        return when (characterId) {
            "basic" -> "basic"
            "fitness" -> if (gender == "female") "female_fitness" else "male_fitness"
            "shadow" -> "shadow"
            "premium" -> if (gender == "female") "female_premium" else "male_premium"
            else -> characterId
        }
    }

    // --- Home Actions ---

    fun selectHome(homeId: String?) {
        _uiState.update { it.copy(selectedHomeId = homeId) }
    }

    fun equipHome(homeId: String?) {
        _characterData.value = _characterData.value.copy(equippedHomeUpgrade = homeId)
        _uiState.update { it.copy(equippedHomeId = homeId, selectedHomeId = homeId) }
        saveData()
    }

    fun equipHomeUpgrade(upgradeId: String?) = equipHome(upgradeId)

    fun buyHome(homeId: String, price: Int) {
        viewModelScope.launch {
            val user = userRepository.fetchUserDataOnce()
            val current = _characterData.value
            if (user != null && user.coins >= price && !current.unlockedHomeUpgrades.contains(homeId)) {
                userRepository.updateUserData(mapOf("coins" to (user.coins - price)))
                val newUnlocked = current.unlockedHomeUpgrades + homeId
                _characterData.value = current.copy(
                    equippedHomeUpgrade = homeId,
                    unlockedHomeUpgrades = newUnlocked,
                )
                _uiState.update {
                    it.copy(
                        userCoins = user.coins - price,
                        equippedHomeId = homeId,
                        selectedHomeId = homeId,
                        unlockedHomeUpgrades = newUnlocked.toSet(),
                    )
                }
                saveData()
            }
        }
    }

    fun buyHomeUpgrade(upgradeId: String = GYM_UPGRADE_ID, price: Int = GYM_UPGRADE_PRICE) =
        buyHome(upgradeId, price)

    // --- Gym Actions ---

    fun selectGym(gymId: String) {
        _uiState.update { it.copy(selectedGymId = gymId) }
    }

    fun equipGym(gymId: String) {
        _characterData.value = _characterData.value.copy(equippedGym = gymId)
        _uiState.update { it.copy(equippedGymId = gymId, selectedGymId = gymId) }
        saveData()
    }

    fun buyGym(gymId: String, price: Int) {
        viewModelScope.launch {
            val user = userRepository.fetchUserDataOnce()
            val current = _characterData.value
            if (user != null && user.coins >= price && !current.unlockedGyms.contains(gymId)) {
                userRepository.updateUserData(mapOf("coins" to (user.coins - price)))
                val newUnlocked = current.unlockedGyms + gymId
                _characterData.value = current.copy(
                    equippedGym = gymId,
                    unlockedGyms = newUnlocked,
                )
                _uiState.update {
                    it.copy(
                        userCoins = user.coins - price,
                        equippedGymId = gymId,
                        selectedGymId = gymId,
                        unlockedGyms = newUnlocked.toSet(),
                    )
                }
                saveData()
            }
        }
    }

    // --- App Background Actions ---

    fun selectBackground(backgroundId: String) {
        _uiState.update { it.copy(selectedBackgroundId = backgroundId) }
    }

    fun equipBackground(backgroundId: String) {
        _characterData.value = _characterData.value.copy(equippedAppBackground = backgroundId)
        _uiState.update { it.copy(equippedBackgroundId = backgroundId, selectedBackgroundId = backgroundId) }
        saveData()
    }

    fun buyBackground(backgroundId: String, price: Int) {
        viewModelScope.launch {
            val user = userRepository.fetchUserDataOnce()
            val current = _characterData.value
            if (user != null && user.coins >= price && !current.unlockedAppBackgrounds.contains(backgroundId)) {
                userRepository.updateUserData(mapOf("coins" to (user.coins - price)))
                val newUnlocked = current.unlockedAppBackgrounds + backgroundId
                _characterData.value = current.copy(
                    equippedAppBackground = backgroundId,
                    unlockedAppBackgrounds = newUnlocked,
                )
                _uiState.update {
                    it.copy(
                        userCoins = user.coins - price,
                        equippedBackgroundId = backgroundId,
                        selectedBackgroundId = backgroundId,
                        unlockedBackgrounds = newUnlocked.toSet(),
                    )
                }
                saveData()
            }
        }
    }

    // --- Body Stats Actions ---

    fun setHeight(heightCm: Int) {
        if (heightCm !in HEIGHT_CM_MIN..HEIGHT_CM_MAX) return
        _uiState.update { it.copy(heightCm = heightCm) }
        viewModelScope.launch {
            userRepository.updateUserData(mapOf("height" to heightCm))
        }
    }

    fun setArmLength(armLengthCm: Int) {
        if (armLengthCm !in ARM_CM_MIN..ARM_CM_MAX) return
        _uiState.update { it.copy(armLengthCm = armLengthCm) }
        viewModelScope.launch {
            userRepository.updateUserData(mapOf("armLength" to armLengthCm.toFloat()))
        }
    }

    fun openStatsDialog() {
        _uiState.update { it.copy(isStatsDialogOpen = true) }
    }

    fun closeStatsDialog() {
        _uiState.update { it.copy(isStatsDialogOpen = false) }
    }

    private fun saveData() {
        viewModelScope.launch {
            userRepository.saveCharacterData(_characterData.value)
        }
    }

    companion object {
        const val HEIGHT_CM_MIN = 120
        const val HEIGHT_CM_MAX = 220
        const val ARM_CM_MIN = 20
        const val ARM_CM_MAX = 120
        const val ARM_CM_DEFAULT = 70
        const val GYM_UPGRADE_ID = "dwelling_gym"
        const val GYM_UPGRADE_PRICE = 150
    }
}