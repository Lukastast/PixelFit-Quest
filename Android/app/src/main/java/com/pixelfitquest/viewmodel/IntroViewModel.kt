package com.pixelfitquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class IntroViewModel @Inject constructor(
) : ViewModel() {
    fun onIntroComplete(navController: NavController) {
        navController.navigate("home") {
            popUpTo("intro") { inclusive = true }
        }
    }
}