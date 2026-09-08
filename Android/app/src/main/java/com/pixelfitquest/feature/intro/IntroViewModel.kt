package com.pixelfitquest.feature.intro

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.pixelfitquest.ui.navigation.HOME_SCREEN
import com.pixelfitquest.ui.navigation.INTRO_SCREEN
import com.pixelfitquest.ui.navigation.SessionRouter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class IntroViewModel @Inject constructor(
    private val prefs: SharedPreferences,
) : ViewModel() {
    fun onIntroComplete(navController: NavController) {
        prefs.edit().putBoolean(SessionRouter.INTRO_SEEN_KEY, true).apply()
        navController.navigate(HOME_SCREEN) {
            popUpTo(INTRO_SCREEN) { inclusive = true }
        }
    }
}