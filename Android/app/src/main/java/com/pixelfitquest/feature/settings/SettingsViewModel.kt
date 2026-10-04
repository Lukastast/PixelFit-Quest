package com.pixelfitquest.feature.settings

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.pixelfitquest.R
import com.pixelfitquest.helpers.ERROR_TAG
import com.pixelfitquest.helpers.SnackbarManager
import com.pixelfitquest.helpers.UNEXPECTED_CREDENTIAL
import com.pixelfitquest.debug.GodModePrefs
import com.pixelfitquest.firebase.model.User
import com.pixelfitquest.firebase.model.UserData
import com.pixelfitquest.firebase.service.AccountService
import com.pixelfitquest.firebase.repository.UserRepository
import com.pixelfitquest.feature.workout.RestTimerPrefs
import com.pixelfitquest.feature.workout.WorkoutWeightPrefs
import com.pixelfitquest.feature.workout.analysis.FullRomStore
import com.pixelfitquest.feature.workout.orientation.WorkoutOrientationPrefs
import com.pixelfitquest.feature.workout.sensor.MountSide
import com.pixelfitquest.feature.workout.sensor.MountSidePrefs
import com.pixelfitquest.feature.workout.sensor.TraceExportPrefs
import com.pixelfitquest.local.CloudBackup
import com.pixelfitquest.viewmodel.PixelFitViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val accountService: AccountService,
    private val userRepository: UserRepository,
    private val cloudBackup: CloudBackup,
    private val fullRomStore: FullRomStore,
    @ApplicationContext context: Context,
) : PixelFitViewModel() {

    private val appContext = context

    private val prefs: SharedPreferences =
        context.getSharedPreferences(WorkoutOrientationPrefs.PREFS_NAME, Context.MODE_PRIVATE)

    private val _user = MutableStateFlow(User())
    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData.asStateFlow()

    private val _workoutLandscapeEnabled = MutableStateFlow(WorkoutOrientationPrefs.isEnabled(prefs))
    val workoutLandscapeEnabled: StateFlow<Boolean> = _workoutLandscapeEnabled.asStateFlow()

    private val _preWorkoutWeightCheckEnabled =
        MutableStateFlow(WorkoutWeightPrefs.isPreWorkoutCheckEnabled(prefs))
    val preWorkoutWeightCheckEnabled: StateFlow<Boolean> = _preWorkoutWeightCheckEnabled.asStateFlow()

    private val _weightSuggestionEnabled =
        MutableStateFlow(WorkoutWeightPrefs.isWeightSuggestionEnabled(prefs))
    val weightSuggestionEnabled: StateFlow<Boolean> = _weightSuggestionEnabled.asStateFlow()

    private val _weightSuggestionRepThreshold =
        MutableStateFlow(WorkoutWeightPrefs.getRepThreshold(prefs))
    val weightSuggestionRepThreshold: StateFlow<Int> = _weightSuggestionRepThreshold.asStateFlow()

    private val _godModeEnabled = MutableStateFlow(GodModePrefs.isEnabled(prefs))
    val godModeEnabled: StateFlow<Boolean> = _godModeEnabled.asStateFlow()

    private val _mountSide = MutableStateFlow(MountSidePrefs.get(prefs))
    val mountSide: StateFlow<MountSide> = _mountSide.asStateFlow()

    private val _traceExportEnabled = MutableStateFlow(TraceExportPrefs.isEnabled(prefs))
    val traceExportEnabled: StateFlow<Boolean> = _traceExportEnabled.asStateFlow()

    private val _restTimerEnabled = MutableStateFlow(RestTimerPrefs.isEnabled(prefs))
    val restTimerEnabled: StateFlow<Boolean> = _restTimerEnabled.asStateFlow()

    private val _restTimerSeconds = MutableStateFlow(RestTimerPrefs.getSeconds(prefs))
    val restTimerSeconds: StateFlow<Int> = _restTimerSeconds.asStateFlow()

    private val _restAutostart = MutableStateFlow(RestTimerPrefs.isAutostartEnabled(prefs))
    val restAutostart: StateFlow<Boolean> = _restAutostart.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    val user: StateFlow<User> = _user.asStateFlow()

    init {
        launchCatching {
            accountService.currentUser.collect { signedIn ->
                _user.value = signedIn ?: User()
            }
        }
        loadUserData()
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

    fun setWorkoutLandscapeEnabled(enabled: Boolean) {
        WorkoutOrientationPrefs.setEnabled(prefs, enabled)
        _workoutLandscapeEnabled.value = enabled
    }

    fun setPreWorkoutWeightCheckEnabled(enabled: Boolean) {
        WorkoutWeightPrefs.setPreWorkoutCheckEnabled(prefs, enabled)
        _preWorkoutWeightCheckEnabled.value = enabled
    }

    fun setWeightSuggestionEnabled(enabled: Boolean) {
        WorkoutWeightPrefs.setWeightSuggestionEnabled(prefs, enabled)
        _weightSuggestionEnabled.value = enabled
    }

    fun setWeightSuggestionRepThreshold(threshold: Int) {
        val clamped = threshold.coerceIn(5, 30)
        WorkoutWeightPrefs.setRepThreshold(prefs, clamped)
        _weightSuggestionRepThreshold.value = clamped
    }

    fun setGodModeEnabled(enabled: Boolean) {
        GodModePrefs.setEnabled(prefs, enabled)
        _godModeEnabled.value = enabled
    }

    fun setMountSide(side: MountSide) {
        MountSidePrefs.set(prefs, side)
        _mountSide.value = side
    }

    fun setTraceExportEnabled(enabled: Boolean) {
        TraceExportPrefs.setEnabled(prefs, enabled)
        _traceExportEnabled.value = enabled
    }

    fun setRestTimerEnabled(enabled: Boolean) {
        RestTimerPrefs.setEnabled(prefs, enabled)
        _restTimerEnabled.value = enabled
    }

    fun setRestTimerSeconds(seconds: Int) {
        val clamped = RestTimerPrefs.coerceSeconds(seconds)
        RestTimerPrefs.setSeconds(prefs, clamped)
        _restTimerSeconds.value = clamped
    }

    fun setRestAutostart(enabled: Boolean) {
        RestTimerPrefs.setAutostartEnabled(prefs, enabled)
        _restAutostart.value = enabled
    }

    fun resetLearnedRange() {
        fullRomStore.clearAll()
        SnackbarManager.showMessage(appContext.getString(R.string.settings_rom_reset_done))
    }

}
