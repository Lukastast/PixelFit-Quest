package com.pixelfitquest.ui.navigation


import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pixelfitquest.Helpers.CUSTOMIZATION_SCREEN
import com.pixelfitquest.Helpers.HOME_SCREEN
import com.pixelfitquest.Helpers.INTRO_SCREEN
import com.pixelfitquest.Helpers.LOGIN_SCREEN
import com.pixelfitquest.Helpers.SETTINGS_SCREEN
import com.pixelfitquest.Helpers.SIGNUP_SCREEN
import com.pixelfitquest.Helpers.SPLASH_SCREEN
import com.pixelfitquest.Helpers.WORKOUT_CUSTOMIZATION_SCREEN
import com.pixelfitquest.Helpers.WORKOUT_SCREEN
import com.pixelfitquest.R
import com.pixelfitquest.model.WorkoutPlan
import com.pixelfitquest.ui.screens.LoginScreen
import com.pixelfitquest.ui.view.CustomizationScreen
import com.pixelfitquest.ui.view.HomeScreen
import com.pixelfitquest.ui.view.IntroScreen
import com.pixelfitquest.ui.view.SettingsScreen
import com.pixelfitquest.ui.view.SignupScreen
import com.pixelfitquest.ui.view.SplashScreen
import com.pixelfitquest.ui.view.WorkoutCustomizationScreen
import com.pixelfitquest.ui.view.WorkoutResumeScreen
import com.pixelfitquest.ui.view.WorkoutScreen
import com.pixelfitquest.viewmodel.WorkoutResumeViewModel

@Composable
fun AppScaffold() {
    val snackbarHostState = remember { SnackbarHostState() }
    val appState = rememberAppState(snackbarHostState)
    val navController = appState.navController
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val isUserLoggedIn = appState.currentUser != null

    val hasBottomBar = isUserLoggedIn && currentRoute in listOf(
        HOME_SCREEN,
        WORKOUT_SCREEN,
        CUSTOMIZATION_SCREEN,
        SETTINGS_SCREEN,
        WORKOUT_CUSTOMIZATION_SCREEN
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,  // Back to default for clean slate
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (hasBottomBar) {
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
                        BottomNavItem.WorkoutCustomization
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),  // Matches default NavigationBar height; adjust if needed
                        horizontalArrangement = Arrangement.SpaceEvenly  // Evenly spaces items; tweak for your design
                    ) {
                        items.forEach { item ->
                            val interactionSource = remember { MutableInteractionSource() }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .height(80.dp)  // Ensures full tap area
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = null,  // Disables ripple/shadow effect
                                        role = Role.Tab  // Provides semantics for accessibility
                                    ) {
                                        appState.navigate(item.route)
                                    }
                            ) {
                                Icon(
                                    painter = painterResource(
                                        id = if (currentRoute == item.route) item.selectedIcon else item.unSelectedIcon
                                    ),
                                    contentDescription = item.label,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(72.dp),
                                )
                                // Uncomment and add label if needed: Text(item.label, style = ... )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPaddingModifier ->
        // Apply the background image to the entire content area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .paint(
                    painter = painterResource(id = R.drawable.logsigninbackground),
                    contentScale = ContentScale.Crop
                )
        ) {
            // Simple NavHost—no extra wrappers or backgrounds
            NavHost(
                navController = appState.navController,
                startDestination = SPLASH_SCREEN,
                modifier = if (hasBottomBar) Modifier.padding(innerPaddingModifier) else Modifier
            ) {
                pixelFitGraph(appState)
            }
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
            openScreen = { route -> appState.navigate(route) },
            navController = appState.navController
        )
    }

    composable(
        route = "workout_resume/{workoutId}",
        arguments = listOf(navArgument("workoutId") { type = NavType.StringType })
    ) { backStackEntry ->

        val workoutId = backStackEntry.arguments?.getString("workoutId") ?: ""

        val viewModel: WorkoutResumeViewModel =
            hiltViewModel(viewModelStoreOwner = backStackEntry)

        WorkoutResumeScreen(
            openScreen = { route -> appState.navigate(route) },
            viewModel = viewModel,
        )
    }

    composable(
        route = "$WORKOUT_SCREEN/{planJson}/{templateName}",
        arguments = listOf(
            navArgument("planJson") { type = NavType.StringType; nullable = false; defaultValue = "" },
            navArgument("templateName") { type = NavType.StringType; nullable = true; defaultValue = "workout" }
        )
    ) { backStackEntry ->
        val planJson = backStackEntry.arguments?.getString("planJson") ?: ""
        val templateName = backStackEntry.arguments?.getString("templateName") ?: ""
        val gson = Gson()
        val plan = if (planJson.isNotBlank()) {
            val type = object : TypeToken<WorkoutPlan>() {}.type
            gson.fromJson(planJson, type) ?: WorkoutPlan(emptyList())
        } else {
            WorkoutPlan(emptyList())
        }
        WorkoutScreen(
            plan = plan,
            templateName = templateName,
            openScreen = { route -> appState.navigate(route) },
            navController = appState.navController
        )
    }

    composable(CUSTOMIZATION_SCREEN) {
        CustomizationScreen(
            openScreen = { route -> appState.navigate(route) }
        )
    }
    composable(WORKOUT_CUSTOMIZATION_SCREEN) {
        WorkoutCustomizationScreen(
            onStartWorkout = { plan, templateName ->
                val gson = Gson()
                val planJson = gson.toJson(plan)
                appState.navigate("$WORKOUT_SCREEN/$planJson/$templateName")
            }
        )
    }
    composable(SETTINGS_SCREEN) {
        SettingsScreen(
            restartApp = { route -> appState.clearAndNavigate(route) }
        )
    }
}