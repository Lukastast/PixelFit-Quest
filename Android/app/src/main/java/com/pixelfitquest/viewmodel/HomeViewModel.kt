package com.pixelfitquest.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.pixelfitquest.Helpers.SPLASH_SCREEN
import com.pixelfitquest.model.UserGameData
import com.pixelfitquest.model.service.AccountService
import com.pixelfitquest.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accountService: AccountService,
    private val userRepository: UserRepository
) : PixelFitViewModel() {

    private val _userGameData = MutableStateFlow<UserGameData?>(null)
    val userGameData: StateFlow<UserGameData?> = _userGameData.asStateFlow()

    private val _currentMaxExp = MutableStateFlow(100)  // Default for level 1
    val currentMaxExp: StateFlow<Int> = _currentMaxExp.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Removed MAX_LEVEL companion object; use userRepository.MAX_LEVEL instead if needed

    fun initialize(restartApp: (String) -> Unit) {
        // Auth subscription: Restart on logout
        launchCatching {
            accountService.currentUser.collect { user ->
                if (user == null) {
                    restartApp(SPLASH_SCREEN)
                }
            }
        }

        // Load progression config first (for XP calculations), then load user data
        viewModelScope.launch {
            try {
                userRepository.loadProgressionConfig()
                // Only load user data after config is loaded
                loadUserData()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load progression config"
            }
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                userRepository.getUserGameData().collect { data ->
                    _userGameData.value = data
                    // Update maxExp reactively for current level
                    if (data != null) {
                        val max = userRepository.getExpRequiredForLevel(data.level)
                        _currentMaxExp.value = max
                    } else {
                        _currentMaxExp.value = 100  // Fallback
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load user data"
            }
        }
    }

    fun addCoins(amount: Int) {
        if (amount <= 0) return
        viewModelScope.launch {
            try {
                val current = _userGameData.value ?: return@launch
                userRepository.updateUserGameData(mapOf("coins" to current.coins + amount))
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update coins"
            }
        }
    }

    fun addExp(amount: Int) {
        if (amount <= 0) return
        viewModelScope.launch {
            try {
                userRepository.updateExp(amount)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update exp"
            }
        }
    }

    fun incrementStreak() {
        viewModelScope.launch {
            try {
                userRepository.updateStreak(increment = true)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update streak"
            }
        }
    }

    fun resetStreak() {
        viewModelScope.launch {
            try {
                userRepository.updateStreak(reset = true)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to reset streak"
            }
        }
    }

    fun resetUserData() {
        viewModelScope.launch {
            try {
                userRepository.updateUserGameData(mapOf(
                    "level" to 1,
                    "exp" to 0,
                    "coins" to 0,  // Optional: Reset coins too
                    "streak" to 0  // Optional: Reset streak
                ))
                Log.d("HomeVM", "User data reset to level 1")
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to reset user data"
            }
        }
    }
}