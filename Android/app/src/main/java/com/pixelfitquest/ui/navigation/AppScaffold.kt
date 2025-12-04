package com.pixelfitquest.ui.navigation

import android.media.MediaPlayer
import androidx.compose.foundation.background
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pixelfitquest.helpers.CUSTOMIZATION_SCREEN
import com.pixelfitquest.helpers.HOME_SCREEN
import com.pixelfitquest.helpers.INTRO_SCREEN
import com.pixelfitquest.helpers.LOGIN_SCREEN
import com.pixelfitquest.helpers.SETTINGS_SCREEN
import com.pixelfitquest.helpers.SIGNUP_SCREEN
import com.pixelfitquest.helpers.SPLASH_SCREEN
import com.pixelfitquest.helpers.WORKOUT_CUSTOMIZATION_SCREEN
import com.pixelfitquest.helpers.WORKOUT_SCREEN
import com.pixelfitquest.helpers.TypewriterText
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
import com.pixelfitquest.viewmodel.GlobalSettingsViewModel
import com.pixelfitquest.viewmodel.WorkoutResumeViewModel
import kotlinx.coroutines.delay

@Composable
fun AppScaffold() {
    val snackbarHostState = remember { SnackbarHostState() }
    val appState = rememberAppState(snackbarHostState)
    val navController = appState.navController
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val isUserLoggedIn = appState.currentUser != null

    val globalSettingsViewModel: GlobalSettingsViewModel = hiltViewModel()
    val userSettings by globalSettingsViewModel.userSettingsRepository.getUserSettings().collectAsState(initial = null)

    val hasBottomBar = isUserLoggedIn && currentRoute in listOf(
        HOME_SCREEN,
        WORKOUT_SCREEN,
        CUSTOMIZATION_SCREEN,
        SETTINGS_SCREEN,
        WORKOUT_CUSTOMIZATION_SCREEN
    )

    var settingsLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(userSettings) {
        if (userSettings != null && !settingsLoaded) {
            settingsLoaded = true
        }
    }

    // Tutorial overlay state - managed globally
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE) }

    var showTutorial by remember { mutableStateOf(false) }
    var tutorialText by remember { mutableStateOf("") }

    // Track when screen content is ready (passed from individual screens)
    var screenContentReady by remember { mutableStateOf(false) }

    // Reset when route changes
    LaunchedEffect(currentRoute) {
        screenContentReady = false
        showTutorial = false
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
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
                            .height(80.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        items.forEach { item ->
                            val interactionSource = remember { MutableInteractionSource() }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .height(80.dp)
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = null,
                                        role = Role.Tab
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
                            }
                        }
                    }
                }
            }
        }
    ) { innerPaddingModifier ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .paint(
                    painter = painterResource(id = R.drawable.logsigninbackground),
                    contentScale = ContentScale.Crop
                )
        ) {
            val musicVolume = remember { derivedStateOf { (userSettings?.musicVolume ?: 50) / 100f } }

            DisposableEffect(settingsLoaded) {
                if (settingsLoaded && appState.mediaPlayer == null) {
                    val mediaPlayerLocal = MediaPlayer.create(context, R.raw.cavern_quest)?.apply {
                        isLooping = true
                        setVolume(musicVolume.value, musicVolume.value)
                        start()
                    }
                    appState.mediaPlayer = mediaPlayerLocal
                }
                onDispose {
                    appState.mediaPlayer?.release()
                    appState.mediaPlayer = null
                }
            }

            LaunchedEffect(musicVolume.value) {
                if (settingsLoaded) {
                    appState.mediaPlayer?.let { player ->
                        player.setVolume(musicVolume.value, musicVolume.value)
                    }
                }
            }

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_PAUSE -> {
                            appState.mediaPlayer?.pause()
                        }
                        Lifecycle.Event.ON_RESUME -> {
                            if (settingsLoaded && appState.mediaPlayer != null) {
                                appState.mediaPlayer?.start()
                            }
                        }
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            NavHost(
                navController = appState.navController,
                startDestination = SPLASH_SCREEN,
                modifier = if (hasBottomBar) Modifier.padding(innerPaddingModifier) else Modifier
            ) {
                pixelFitGraph(
                    appState = appState,
                    onScreenReady = { screen ->
                        screenContentReady = true

                        // Check if we should show tutorial for this screen
                        when (screen) {
                            HOME_SCREEN -> {
                                if (prefs.getBoolean("first_time_home_screen", true)) {
                                    tutorialText = "Welcome to the Home Screen! Here you can view your level, coins, experience, and streak at the top. Track your daily steps for rewards. Check your rank on the leaderboard and check you unlocked achievements by pressing the achievements trophy. See your completed workouts and resume them. And dont forget to complete daily missions for extra rewards."
                                    showTutorial = true
                                }
                            }
                            CUSTOMIZATION_SCREEN -> {
                                if (prefs.getBoolean("first_time_customization", true)) {
                                    tutorialText = "Welcome to Customization! Choose your character's gender and variant. Basic is free, fitness gives bonuses for coins, premium coming soon. Select and buy to customize."
                                    showTutorial = true
                                }
                            }
                            SETTINGS_SCREEN -> {
                                if (prefs.getBoolean("first_time_settings", true)) {
                                    tutorialText = "Welcome to Settings! Here you can adjust music volume. Sign out or delete your account if needed."
                                    showTutorial = true
                                }
                            }
                            WORKOUT_CUSTOMIZATION_SCREEN -> {
                                if (prefs.getBoolean("first_time_workout_customization", true)) {
                                    tutorialText = "Welcome to Workout Customization! Select exercises by checking them, adjust sets and weights. Enter a name to save as template. Use 'Start Workout' to begin your adventure of acquiring coins and exp."
                                    showTutorial = true
                                }
                            }
                        }
                    }
                )
            }

            // Centralized tutorial overlay
            if (showTutorial && screenContentReady) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable(enabled = false) { },
                    contentAlignment = Alignment.Center
                ) {
                    TypewriterText(
                        text = tutorialText,
                        onComplete = {
                            when (currentRoute) {
                                HOME_SCREEN -> prefs.edit().putBoolean("first_time_home_screen", false).apply()
                                CUSTOMIZATION_SCREEN -> prefs.edit().putBoolean("first_time_customization", false).apply()
                                SETTINGS_SCREEN -> prefs.edit().putBoolean("first_time_settings", false).apply()
                                WORKOUT_CUSTOMIZATION_SCREEN -> prefs.edit().putBoolean("first_time_workout_customization", false).apply()
                            }
                            showTutorial = false
                        },
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

fun NavGraphBuilder.pixelFitGraph(
    appState: AppState,
    onScreenReady: (String) -> Unit
) {
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
            navController = appState.navController,
            onScreenReady = { onScreenReady(HOME_SCREEN) }
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
            openScreen = { route -> appState.navigate(route) },
        )
    }
    composable(WORKOUT_CUSTOMIZATION_SCREEN) {
        WorkoutCustomizationScreen(
            onStartWorkout = { plan, templateName ->
                val gson = Gson()
                val planJson = gson.toJson(plan)
                appState.navigate("$WORKOUT_SCREEN/$planJson/$templateName")
            },
        )
    }
    composable(SETTINGS_SCREEN) {
        SettingsScreen(
            restartApp = { route -> appState.clearAndNavigate(route) },
        )
    }
}