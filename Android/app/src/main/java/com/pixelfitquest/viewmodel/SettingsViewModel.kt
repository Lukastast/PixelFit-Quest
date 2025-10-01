package com.pixelfitquest.viewmodel

import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.pixelfitquest.Helpers.ERROR_TAG
import com.pixelfitquest.Helpers.HOME_SCREEN
import com.pixelfitquest.Helpers.LOGIN_SCREEN
import com.pixelfitquest.model.User
import com.pixelfitquest.model.service.AccountService
import com.pixelfitquest.Helpers.SPLASH_SCREEN
import com.pixelfitquest.Helpers.UNEXPECTED_CREDENTIAL
import com.pixelfitquest.R
import com.pixelfitquest.model.service.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val accountService: AccountService) : PixelFitViewModel() {

    private val _user = MutableStateFlow(User())
    val user: StateFlow<User> = _user.asStateFlow()

    init {
        launchCatching {
            _user.value = accountService.getUserProfile()
        }
    }

    fun onUpdateDisplayNameClick(newDisplayName: String) {
        launchCatching {
            accountService.updateDisplayName(newDisplayName)
            _user.value = accountService.getUserProfile()
        }
    }
    fun onSignOutClick(restartApp: (String) -> Unit) {
        launchCatching {
            accountService.signOut()
            restartApp(SPLASH_SCREEN)
        }
    }

    fun onDeleteAccountClick(restartApp: (String) -> Unit) {
        launchCatching {
            accountService.deleteAccount()
            restartApp(SPLASH_SCREEN)
        }
    }

    fun linkAccountWithGoogle(credential: Credential) {
            launchCatching(
                onError = { e ->
                    // Handle error, e.g., update a UI state for error message
                    Log.e(ERROR_TAG, "Google linking failed", e)
                }
            ) {
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    accountService.linkAccountWithGoogle(googleIdTokenCredential.idToken)
                    _user.value = accountService.getUserProfile() // Refresh user profile to reflect the link
                    // Optionally, show success toast or update UI state
                } else {
                    Log.e(ERROR_TAG, UNEXPECTED_CREDENTIAL)
                    // Handle unexpected credential type
                }
            }
    }

    fun getProfilePictureModel(): Any? {
        val url = user.value.profilePictureUrl
        return if (url.isNullOrBlank()) {
            R.mipmap.app_icon_round  // Int: Local resource ID
        } else {
            url  // String: Remote URL
        }
    }
}