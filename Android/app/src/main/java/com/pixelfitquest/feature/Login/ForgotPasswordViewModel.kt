package com.pixelfitquest.feature.Login

import com.pixelfitquest.feature.Login.ext.isValidEmail
import com.pixelfitquest.firebase.service.AccountService
import com.pixelfitquest.firebase.service.AuthState
import com.pixelfitquest.helpers.AuthErrorMapper
import com.pixelfitquest.helpers.ERROR_TAG
import com.pixelfitquest.viewmodel.PixelFitViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val accountService: AccountService
) : PixelFitViewModel() {
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
        resetState()
    }

    private fun resetState() {
        if (_authState.value is AuthState.Error || _authState.value is AuthState.Success) {
            _authState.value = AuthState.Idle
        }
    }

    fun onSendResetClick() {
        if (_authState.value is AuthState.Loading) return

        val email = _email.value.trim()
        if (email.isBlank()) {
            _authState.value = AuthState.Error("Please enter your email")
            return
        }
        if (!email.isValidEmail()) {
            _authState.value = AuthState.Error("Invalid email format")
            return
        }

        _authState.value = AuthState.Loading

        launchCatching(
            onError = { e ->
                val errorMessage = AuthErrorMapper.mapError(
                    e,
                    mappings = AuthErrorMapper.resetPasswordErrorMappings,
                    defaultMessage = "Could not send reset email",
                    errorTag = ERROR_TAG
                )
                _authState.value = AuthState.Error(errorMessage)
            }
        ) {
            accountService.sendPasswordResetEmail(email)
            _authState.value = AuthState.Success
        }
    }
}
