package com.pixelfitquest.feature.settings

import android.content.Context
import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.pixelfitquest.R
import com.pixelfitquest.helpers.ERROR_TAG
import com.pixelfitquest.helpers.SnackbarManager
import com.pixelfitquest.helpers.UNEXPECTED_CREDENTIAL
import com.pixelfitquest.firebase.model.User
import com.pixelfitquest.firebase.model.UserData
import com.pixelfitquest.firebase.service.AccountService
import com.pixelfitquest.firebase.repository.UserRepository
import com.pixelfitquest.health.HealthConnectStatus
import com.pixelfitquest.health.HealthPermissions
import com.pixelfitquest.health.HealthRepository
import com.pixelfitquest.local.CloudBackup
import com.pixelfitquest.local.export.LocalExportService
import com.pixelfitquest.viewmodel.PixelFitViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val accountService: AccountService,
    private val userRepository: UserRepository,
    private val localExportService: LocalExportService,
    private val cloudBackup: CloudBackup,
    private val healthRepository: HealthRepository,
) : PixelFitViewModel() {

    private val _user = MutableStateFlow(User())
    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    val user: StateFlow<User> = _user.asStateFlow()

    private val _healthStatus = MutableStateFlow(HealthConnectStatus.UNAVAILABLE)
    val healthStatus: StateFlow<HealthConnectStatus> = _healthStatus.asStateFlow()

    private val _healthPermissionsGranted = MutableStateFlow(false)
    val healthPermissionsGranted: StateFlow<Boolean> = _healthPermissionsGranted.asStateFlow()

    val healthPermissions: Set<String> = HealthPermissions.required()

    init {
        launchCatching {
            accountService.currentUser.collect { signedIn ->
                _user.value = signedIn ?: User()
            }
        }
        loadUserData()
        refreshHealthStatus()
    }

    fun refreshHealthStatus() {
        viewModelScope.launch {
            _healthStatus.value = healthRepository.availability()
            val granted = healthRepository.grantedPermissions()
            _healthPermissionsGranted.value = HealthPermissions.hasStepsRead(granted)
        }
    }

    fun onHealthPermissionsResult(granted: Set<String>) {
        _healthPermissionsGranted.value = HealthPermissions.hasStepsRead(granted)
        refreshHealthStatus()
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
            _user.value = User()
            SnackbarManager.showMessage("Signed out. Workouts stay on this phone.")
        }
    }

    fun onDeleteAccountClick(restartApp: (String) -> Unit) {
        launchCatching {
            accountService.deleteAccount()
            _user.value = User()
            SnackbarManager.showMessage("Account removed. Local workouts were kept.")
        }
    }

    fun onGoogleSignIn(credential: Credential) {
        launchCatching(
            onError = { e ->
                Log.e(ERROR_TAG, "Google sign-in failed", e)
                SnackbarManager.showMessage(e.message ?: "Google sign-in failed")
            }
        ) {
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.Companion.createFrom(credential.data)
                if (accountService.hasUser()) {
                    accountService.linkAccountWithGoogle(googleIdTokenCredential.idToken)
                } else {
                    accountService.signInWithGoogle(googleIdTokenCredential.idToken)
                }
                _user.value = accountService.getUserProfile()
                SnackbarManager.showMessage("Signed in with Google")
            } else {
                Log.e(ERROR_TAG, UNEXPECTED_CREDENTIAL)
                SnackbarManager.showMessage("Unexpected credential type")
            }
        }
    }

    fun linkAccountWithGoogle(credential: Credential) {
        onGoogleSignIn(credential)
    }

    fun onBackupSyncClick() {
        launchCatching {
            val result = cloudBackup.syncNow()
            val message = result.exceptionOrNull()?.message
                ?: "Cloud backup requires PixelFit Pro"
            SnackbarManager.showMessage(message)
        }
    }

    fun exportJson(context: Context) {
        viewModelScope.launch {
            try {
                localExportService.shareJson(context)
            } catch (e: Exception) {
                Log.e(ERROR_TAG, "JSON export failed", e)
                SnackbarManager.showMessage(e.message ?: "JSON export failed")
            }
        }
    }

    fun exportCsv(context: Context) {
        viewModelScope.launch {
            try {
                localExportService.shareCsv(context)
            } catch (e: Exception) {
                Log.e(ERROR_TAG, "CSV export failed", e)
                SnackbarManager.showMessage(e.message ?: "CSV export failed")
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
    private fun loadUserData() {
        viewModelScope.launch {
            try {
                userRepository.getUserData().collect { data ->
                    _userData.value = data
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load user data"
            }
        }
    }
    fun setHeight(height: Int) {
        viewModelScope.launch {
            try {
                userRepository.updateUserData(
                    mapOf("height" to height)
                )
                loadUserData()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update height"
            }
        }
    }

    fun setMusicVolume(volume: Int) {
        viewModelScope.launch {
            try {
                userRepository.updateUserData(
                    mapOf("musicVolume" to volume)
                )
                loadUserData()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update music volume"
            }
        }
    }

}
