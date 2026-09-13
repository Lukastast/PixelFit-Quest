package com.pixelfitquest.feature.bodyMetrics

import androidx.lifecycle.viewModelScope
import com.pixelfitquest.firebase.model.UserData
import com.pixelfitquest.firebase.repository.UserRepository
import com.pixelfitquest.helpers.SnackbarManager
import com.pixelfitquest.viewmodel.PixelFitViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BodyMetricsViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : PixelFitViewModel() {

    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                userRepository.getUserData().collect { data ->
                    _userData.value = data
                }
            } catch (e: Exception) {
                SnackbarManager.showMessage(e.message ?: "Failed to load body metrics")
            }
        }
    }

    fun saveHeight(heightCm: Int) {
        if (heightCm !in 1..272) {
            SnackbarManager.showMessage("Height must be between 1 and 272 cm")
            return
        }
        viewModelScope.launch {
            try {
                userRepository.updateUserData(mapOf("height" to heightCm))
                SnackbarManager.showMessage("Height saved")
            } catch (e: Exception) {
                SnackbarManager.showMessage(e.message ?: "Failed to save height")
            }
        }
    }

    fun saveArmLength(armLengthCm: Float) {
        if (armLengthCm !in 20f..120f) {
            SnackbarManager.showMessage("Arm length must be between 20 and 120 cm")
            return
        }
        viewModelScope.launch {
            try {
                userRepository.updateUserData(mapOf("armLength" to armLengthCm))
                SnackbarManager.showMessage("Arm length saved")
            } catch (e: Exception) {
                SnackbarManager.showMessage(e.message ?: "Failed to save arm length")
            }
        }
    }

    fun saveBoth(heightCm: Int?, armLengthCm: Float?) {
        val updates = mutableMapOf<String, Any>()
        if (heightCm != null) {
            if (heightCm !in 1..272) {
                SnackbarManager.showMessage("Height must be between 1 and 272 cm")
                return
            }
            updates["height"] = heightCm
        }
        if (armLengthCm != null) {
            if (armLengthCm !in 20f..120f) {
                SnackbarManager.showMessage("Arm length must be between 20 and 120 cm")
                return
            }
            updates["armLength"] = armLengthCm
        }
        if (updates.isEmpty()) {
            SnackbarManager.showMessage("Enter height and/or arm length")
            return
        }
        viewModelScope.launch {
            try {
                userRepository.updateUserData(updates)
                SnackbarManager.showMessage("Body measurements saved")
            } catch (e: Exception) {
                SnackbarManager.showMessage(e.message ?: "Failed to save body measurements")
            }
        }
    }
}
