package com.pixelfitquest.viewmodel  // Changed from com.PixelFitQuest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class IntroViewModel @Inject constructor() : ViewModel() {
    fun onIntroComplete(navController: NavController) {
        val route = "home"  // Replace with auth logic if needed
        navController.navigate(route) {
            popUpTo("intro") { inclusive = true }
        }
    }
}