package com.pixelfitquest.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pixelfitquest.Helpers.CUSTOMIZATION_SCREEN
import com.pixelfitquest.Helpers.HOME_SCREEN
import com.pixelfitquest.Helpers.INTRO_SCREEN
import com.pixelfitquest.Helpers.LOGIN_SCREEN
import com.pixelfitquest.Helpers.SETTINGS_SCREEN
import com.pixelfitquest.Helpers.SIGNUP_SCREEN
import com.pixelfitquest.Helpers.SPLASH_SCREEN
import com.pixelfitquest.Helpers.WORKOUT_SCREEN
import com.pixelfitquest.R
import com.pixelfitquest.ui.screens.LoginScreen
import com.pixelfitquest.ui.view.CustomizationScreen
import com.pixelfitquest.ui.view.HomeScreen
import com.pixelfitquest.ui.view.IntroScreen
import com.pixelfitquest.ui.view.SettingsScreen
import com.pixelfitquest.ui.view.SignupScreen
import com.pixelfitquest.ui.view.SplashScreen
import com.pixelfitquest.ui.view.WorkoutScreen

@Composable
fun AppScaffold() {
    val snackbarHostState = remember { SnackbarHostState() }
    val appState = rememberAppState(snackbarHostState)
    val navController = appState.navController
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val isUserLoggedIn = appState.currentUser != null

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            // Explicitly return nothing (empty composable) if on splash to avoid any layout space reservation
            if (currentRoute == SPLASH_SCREEN || currentRoute == INTRO_SCREEN) {
                // Empty - no bar
            } else if (isUserLoggedIn && currentRoute in listOf(
                    HOME_SCREEN,
                    WORKOUT_SCREEN,
                    CUSTOMIZATION_SCREEN,
                    SETTINGS_SCREEN
                )) {
                NavigationBar(
                    modifier = Modifier
                        .paint(
                            painter = painterResource(id = R.drawable.navbar),
                            contentScale = ContentScale.Crop
                        ),
                    containerColor = Color.Transparent
                ) {
                    val items = listOf(
                        BottomNavItem.Home,
                        BottomNavItem.Settings,
                        BottomNavItem.Customization,
                        BottomNavItem.Workout
                    )
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    painter = painterResource(
                                        id = if (currentRoute == item.route) item.selectedIcon else item.unSelectedIcon
                                    ),
                                    contentDescription = item.label,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(72.dp),
                                )
                            },
                            //label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent
                            ),
                            onClick = {
                                appState.navigate(item.route)
                            }
                        )
                    }
                }
            }
        }
    ) { innerPaddingModifier ->
        NavHost(
            navController = appState.navController,
            startDestination = SPLASH_SCREEN,
            modifier = Modifier.padding(innerPaddingModifier)
        ) {
            pixelFitGraph(appState)
        }
    }
}

fun NavGraphBuilder.pixelFitGraph(appState: AppState) {
    composable(INTRO_SCREEN) {
        IntroScreen(
            navController = appState.navController
        )
    }

    composable(SPLASH_SCREEN) {
        SplashScreen(
            navController = appState.navController
        )
    }

    composable(SIGNUP_SCREEN) {
        SignupScreen(
            openScreen = { route -> appState.navigate(route) },
            openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) }
        )
    }
    composable(LOGIN_SCREEN) {
        LoginScreen(
            openScreen = { route -> appState.navigate(route) },
            openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) }
        )
    }
    composable(HOME_SCREEN) {
        HomeScreen(
            restartApp = { route -> appState.clearAndNavigate(route) },
            openScreen = { route -> appState.navigate(route) }
        )
    }
    composable(WORKOUT_SCREEN) {
        WorkoutScreen(
            openScreen = { route -> appState.navigate(route) }
        )
    }
    composable(CUSTOMIZATION_SCREEN) {
        CustomizationScreen(
            openScreen = { route -> appState.navigate(route) }
        )
    }
    composable(SETTINGS_SCREEN) {
        SettingsScreen(
            restartApp = { route -> appState.clearAndNavigate(route) }
        )
    }
}