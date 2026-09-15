package com.pixelfitquest.feature.customization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelfitquest.feature.customization.model.CharacterData
import com.pixelfitquest.firebase.model.UserData
import com.pixelfitquest.firebase.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomizationViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _characterData = MutableStateFlow(CharacterData())
    val characterData: StateFlow<CharacterData> = _characterData.asStateFlow()
    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData.asStateFlow()

    init {
        loadCharacterData()
        loadUserData()
    }

    private fun loadCharacterData() {
        viewModelScope.launch {
            userRepository.getCharacterData().collectLatest { data ->
                _characterData.value = data ?: CharacterData()
            }
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            userRepository.getUserData().collectLatest { data ->
                _userData.value = data
            }
        }
    }

    fun setHeight(heightCm: Int) {
        if (heightCm !in HEIGHT_CM_MIN..HEIGHT_CM_MAX) return
        viewModelScope.launch {
            userRepository.updateUserData(mapOf("height" to heightCm))
        }
    }

    fun setArmLength(armLengthCm: Int) {
        if (armLengthCm !in ARM_CM_MIN..ARM_CM_MAX) return
        viewModelScope.launch {
            userRepository.updateUserData(mapOf("armLength" to armLengthCm.toFloat()))
        }
    }

    companion object {
        const val HEIGHT_CM_MIN = 120
        const val HEIGHT_CM_MAX = 220
        const val ARM_CM_MIN = 20
        const val ARM_CM_MAX = 120
        const val ARM_CM_DEFAULT = 70
    }

    fun updateGender(gender: String) {
        _characterData.value = _characterData.value.copy(gender = gender)
        saveData()
    }

    fun updateVariant(variant: String) {
        _characterData.value = _characterData.value.copy(variant = variant)
        saveData()
    }

    private fun unlockVariant(variant: String) {
        val newUnlocked = _characterData.value.unlockedVariants + variant
        _characterData.value = _characterData.value.copy(unlockedVariants = newUnlocked)
        saveData()
    }

    fun buyVariant(variant: String, price: Int) {
        viewModelScope.launch {
            val gameData = userRepository.fetchUserDataOnce()
            if (gameData != null && gameData.coins >= price && !_characterData.value.unlockedVariants.contains(variant)) {
                userRepository.updateUserData(mapOf("coins" to (gameData.coins - price)))
                unlockVariant(variant)
                updateVariant(variant)
            }
        }
    }

    private fun saveData() {
        viewModelScope.launch {
            userRepository.saveCharacterData(_characterData.value)
        }
    }
}