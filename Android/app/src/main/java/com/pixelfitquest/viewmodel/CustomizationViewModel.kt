package com.pixelfitquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelfitquest.model.CharacterData
import com.pixelfitquest.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomizationViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _characterData = MutableStateFlow(CharacterData())
    val characterData: StateFlow<CharacterData> = _characterData.asStateFlow()

    init {
        loadCharacterData()
    }

    private fun loadCharacterData() {
        viewModelScope.launch {
            userRepository.getCharacterData().collect { data ->
                _characterData.value = data ?: CharacterData()
            }
        }
    }

    fun updateGender(gender: String) {
        _characterData.value = _characterData.value.copy(gender = gender)
        saveData()
    }

    private fun saveData() {
        viewModelScope.launch {
            userRepository.saveCharacterData(_characterData.value)
        }
    }
}