package com.pixelfitquest.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pixelfitquest.Helpers.SPLASH_SCREEN
import com.pixelfitquest.R
import com.pixelfitquest.viewmodel.SplashViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    var visible by remember { mutableStateOf(true) }

    AnimatedVisibility(
        visible = visible,
        exit = fadeOut(animationSpec = tween(durationMillis = 500))  // 0.5-second fade out
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_image),
            contentDescription = "Splash Screen",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
    }

    // Logic: Check auth, delay, fade out, then navigate
    LaunchedEffect(Unit) {
        viewModel.checkAuthState { isAuthenticated ->
            delay(1000)  // 1-second delay
            visible = false  // Trigger fade out
            delay(500)  // Wait for animation to finish
            if (isAuthenticated) {
                navController.navigate("home") {
                    popUpTo(SPLASH_SCREEN) { inclusive = true }
                }
            } else {
                navController.navigate("login") {
                    popUpTo(SPLASH_SCREEN) { inclusive = true }
                }
            }
        }
    }
}