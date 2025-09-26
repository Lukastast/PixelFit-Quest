package com.pixelfitquest.viewmodel

import com.pixelfitquest.model.User
import com.pixelfitquest.model.service.AccountService
import com.pixelfitquest.Helpers.LOGIN_SCREEN
import com.pixelfitquest.R
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
            restartApp(LOGIN_SCREEN)
            //restartApp(SPLASH_SCREEN)
        }
    }

    fun onDeleteAccountClick(restartApp: (String) -> Unit) {
        launchCatching {
            accountService.deleteAccount()
            //restartApp(SPLASH_SCREEN)
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