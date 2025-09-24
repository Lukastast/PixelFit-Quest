package com.PixelFitQuest.viewmodel

import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import com.PixelFitQuest.Helpers.ERROR_TAG
import com.PixelFitQuest.Helpers.UNEXPECTED_CREDENTIAL
import com.PixelFitQuest.ext.isValidEmail
import com.PixelFitQuest.ext.isValidPassword
import com.PixelFitQuest.model.service.AccountService
import com.PixelFitQuest.Helpers.HOME_SCREEN
import com.PixelFitQuest.Helpers.SIGNUP_SCREEN
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuthException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val accountService: AccountService
) : PixelFitViewModel() {
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
        resetState() // Optional: Reset to Idle on input change
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
        resetState() // Optional: Reset to Idle on input change
    }

    fun updateConfirmPassword(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
        resetState() // Optional: Reset to Idle on input change
    }

    private fun resetState() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Idle
        }
    }

    fun onSignUpClick(openAndPopUp: (String, String) -> Unit) {
        if (_email.value.isBlank() || _password.value.isBlank() || _confirmPassword.value.isBlank()) {
            _authState.value = AuthState.Error("Please fill all fields")
            return
        }
        if (!_email.value.isValidEmail()) {
            _authState.value = AuthState.Error("Invalid email format")
            return
        }
        if (!_password.value.isValidPassword()) {
            _authState.value = AuthState.Error("Password must be at least 6 characters, with 1 digit, 1 lowercase, and 1 uppercase letter")
            return
        }
        if (_password.value != _confirmPassword.value) {
            _authState.value = AuthState.Error("Passwords do not match")
            return
        }

        _authState.value = AuthState.Loading

        launchCatching(
            onError = { e ->
                val errorMessage = when (e) {
                    is FirebaseAuthException -> {
                        Log.e(ERROR_TAG, "Firebase error code: ${e.errorCode}") // Log for debugging
                        when (e.errorCode) {
                            "ERROR_INVALID_EMAIL" -> "Invalid email format"
                            "ERROR_EMAIL_ALREADY_IN_USE" -> "Email already in use"
                            "ERROR_WEAK_PASSWORD" -> "Password is too weak"
                            "ERROR_INVALID_CREDENTIAL" -> "Invalid credentials"
                            else -> e.message ?: "Signup failed"
                        }
                    }
                    else -> e.message ?: "An error occurred"
                }
                _authState.value = AuthState.Error(errorMessage)
            }
        ) {
            Log.d(ERROR_TAG, "Creating account with email: ${_email.value}")
            Log.d(ERROR_TAG, "Creating account with password: ${_password.value}")

            accountService.createAccountWithEmail(_email.value, _password.value)
            _authState.value = AuthState.Success
            openAndPopUp(HOME_SCREEN, SIGNUP_SCREEN)
        }
    }

    fun onSignUpWithGoogle(credential: Credential, openAndPopUp: (String, String) -> Unit) {
        _authState.value = AuthState.Loading

        launchCatching(
            onError = { e ->
                _authState.value = AuthState.Error(e.message ?: "Google signup failed")
            }
        ) {
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                accountService.signInWithGoogle(googleIdTokenCredential.idToken)
                _authState.value = AuthState.Success
                openAndPopUp(HOME_SCREEN, SIGNUP_SCREEN)
            } else {
                Log.e(ERROR_TAG, UNEXPECTED_CREDENTIAL)
                _authState.value = AuthState.Error("Unexpected credential type")
            }
        }
    }
}