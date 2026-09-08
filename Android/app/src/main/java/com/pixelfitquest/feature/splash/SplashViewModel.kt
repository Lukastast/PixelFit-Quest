package com.pixelfitquest.feature.splash

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelfitquest.ui.navigation.SessionRouter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val prefs: SharedPreferences,
) : ViewModel() {
    fun resolveStart(onResult: suspend (String) -> Unit) {
        viewModelScope.launch {
            val hasSeenIntro = prefs.getBoolean(SessionRouter.INTRO_SEEN_KEY, false)
            onResult(SessionRouter.startDestination(hasSeenIntro))
        }
    }
}