package com.PixelFitQuest.viewmodel


import com.PixelFitQuest.model.service.AccountService
import com.PixelFitQuest.Helpers.SIGNUP_SCREEN
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accountService: AccountService,
) : PixelFitViewModel() {

    fun initialize(restartApp: (String) -> Unit) {
        launchCatching {
            accountService.currentUser.collect { user ->
                if (user == null) restartApp(SIGNUP_SCREEN)
            }
        }
    }
}