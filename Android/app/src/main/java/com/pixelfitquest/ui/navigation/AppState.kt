package com.pixelfitquest.ui.navigation

import android.media.MediaPlayer  // NEW: Import for MediaPlayer
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.pixelfitquest.helpers.SnackbarManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

@Stable
class AppState(
    private val snackbarHostState: SnackbarHostState,
    val navController: NavHostController,
    private val snackbarManager: SnackbarManager,
    private val firebaseAuth: FirebaseAuth,
    coroutineScope: CoroutineScope
) {
    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    // NEW: Shared MediaPlayer for background music (with volume control)
    var mediaPlayer: MediaPlayer? = null

    init {
        coroutineScope.launch {
            snackbarManager.snackbarMessages.filterNotNull().collect { message ->
                snackbarHostState.showSnackbar(message)
                snackbarManager.clearSnackbarState()
            }
        }
    }

    fun popUp() {
        navController.popBackStack()
    }

    fun navigate(route: String) {
        navController.navigate(route) { launchSingleTop = true }
    }

    fun navigate(route: String, builder: NavOptions.Builder.() -> Unit = {}) {
        val options = NavOptions.Builder().apply(builder).build()
        navController.navigate(route, options)
    }
    fun navigateAndPopUp(route: String, popUp: String) {
        navController.navigate(route) {
            launchSingleTop = true
            popUpTo(popUp) { inclusive = true }
        }
    }

    fun clearAndNavigate(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
            popUpTo(0) { inclusive = true }
        }
    }
}

@Composable
fun rememberAppState(
    snackbarHostState: SnackbarHostState,
    navController: NavHostController = rememberNavController(),
    snackbarManager: SnackbarManager = SnackbarManager,
    firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): AppState {
    return remember(snackbarHostState, navController, snackbarManager, firebaseAuth, coroutineScope) {
        AppState(snackbarHostState, navController, snackbarManager, firebaseAuth, coroutineScope)
    }
}