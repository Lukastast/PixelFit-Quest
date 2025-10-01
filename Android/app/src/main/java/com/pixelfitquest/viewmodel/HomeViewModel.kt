package com.pixelfitquest.viewmodel

import androidx.lifecycle.viewModelScope
import com.pixelfitquest.model.UserGameData
import com.pixelfitquest.model.service.AccountService
import com.pixelfitquest.repository.UserRepository  // Add this import
import com.pixelfitquest.Helpers.SIGNUP_SCREEN
import com.pixelfitquest.Helpers.SPLASH_SCREEN
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accountService: AccountService,
    private val userRepository: UserRepository  // Inject the repository
) : PixelFitViewModel() {

    private val _userGameData = MutableStateFlow<UserGameData?>(null)
    val userGameData: StateFlow<UserGameData?> = _userGameData.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        initialize { screen -> /* Your existing restart logic, e.g., navigate */ }
        loadUserData()  // Add data loading
    }

    fun initialize(restartApp: (String) -> Unit) {
        launchCatching {
            accountService.currentUser.collect { user ->
                if (user == null) restartApp(SPLASH_SCREEN)
            }
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                userRepository.getUserGameData().collect { data ->
                    _userGameData.value = data
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load user data"
            }
        }
    }

    fun addCoins(amount: Int) {
        viewModelScope.launch {
            try {
                val current = _userGameData.value ?: return@launch
                userRepository.updateUserGameData(mapOf("coins" to current.coins + amount))
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update coins"
            }
        }
    }

    // Add similar functions, e.g., addExp(amount: Int) { ... }
}