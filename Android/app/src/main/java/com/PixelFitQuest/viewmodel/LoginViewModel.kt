package com.PixelFitQuest.viewmodel

import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import com.PixelFitQuest.Helpers.ERROR_TAG
import com.PixelFitQuest.Helpers.UNEXPECTED_CREDENTIAL
import com.PixelFitQuest.ext.isValidEmail
import com.PixelFitQuest.model.service.AccountService
import com.PixelFitQuest.Helpers.HOME_SCREEN
import com.PixelFitQuest.Helpers.LOGIN_SCREEN
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuthException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val accountService: AccountService
) : PixelFitViewModel() {
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
        resetState()
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
        resetState()
    }

    private fun resetState() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Idle
        }
    }

    fun onLogInClick(openAndPopUp: (String, String) -> Unit) {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _authState.value = AuthState.Error("Please fill all fields")
            return
        }
        if (!_email.value.isValidEmail()) {
            _authState.value = AuthState.Error("Invalid email format")
            return
        }

        _authState.value = AuthState.Loading

        launchCatching(
            onError = { e ->
                val errorMessage = when (e) {
                    is FirebaseAuthException -> {
                        Log.e(ERROR_TAG, "Firebase error code: ${e.errorCode}")
                        when (e.errorCode) {
                            "ERROR_INVALID_EMAIL" -> "Invalid email"
                            "ERROR_WRONG_PASSWORD" -> "Incorrect password"
                            "ERROR_USER_NOT_FOUND" -> "No account found with this email"
                            "ERROR_USER_DISABLED" -> "Account disabled"
                            "ERROR_INVALID_CREDENTIAL" -> "Invalid email or password"
                            else -> e.message ?: "Login failed"
                        }
                    }
                    else -> e.message ?: "An error occurred"
                }
                _authState.value = AuthState.Error(errorMessage)
            }
        ) {
            accountService.signInWithEmail(_email.value, _password.value)
            _authState.value = AuthState.Success
            openAndPopUp(HOME_SCREEN, LOGIN_SCREEN)
        }
    }

    fun onLogInWithGoogle(credential: Credential, openAndPopUp: (String, String) -> Unit) {
        _authState.value = AuthState.Loading

        launchCatching(
            onError = { e ->
                _authState.value = AuthState.Error(e.message ?: "Google login failed")
            }
        ) {
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                accountService.signInWithGoogle(googleIdTokenCredential.idToken)
                _authState.value = AuthState.Success
                openAndPopUp(HOME_SCREEN, LOGIN_SCREEN)
            } else {
                Log.e(ERROR_TAG, UNEXPECTED_CREDENTIAL)
                _authState.value = AuthState.Error("Unexpected credential type")
            }
        }
    }
}