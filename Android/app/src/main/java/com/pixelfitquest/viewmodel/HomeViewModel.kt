package com.pixelfitquest.viewmodel


import com.pixelfitquest.model.service.AccountService
import com.pixelfitquest.Helpers.SIGNUP_SCREEN
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