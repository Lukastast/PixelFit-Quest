package com.pixelfitquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelfitquest.model.service.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val accountService: AccountService
) : ViewModel() {
    fun checkAuthState(onResult: suspend (Boolean) -> Unit) {
        viewModelScope.launch {
            val isAuthenticated = accountService.hasUser()
            onResult(isAuthenticated)
        }
    }
}