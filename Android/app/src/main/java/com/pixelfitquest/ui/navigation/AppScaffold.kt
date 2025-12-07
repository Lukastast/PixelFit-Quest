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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.pixelfitquest.model.workout.WorkoutPlan
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

@Composable
fun AppScaffold() {
    val snackbarHostState = remember { SnackbarHostState() }
    val appState = rememberAppState(snackbarHostState)
    val navController = appState.navController
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val isUserLoggedIn = appState.currentUser != null

    val globalSettingsViewModel: GlobalSettingsViewModel = hiltViewModel()
    val userSettings by globalSettingsViewModel.userRepository.getUserData().collectAsState(initial = null)

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

    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE) }

    var showTutorial by remember { mutableStateOf(false) }
    var tutorialText by remember { mutableStateOf("") }
    var screenContentReady by remember { mutableStateOf(false) }
    var tutorialComplete by remember { mutableStateOf(false) }
    var skipTypewriter by remember { mutableStateOf(false) }

    LaunchedEffect(currentRoute) {
        screenContentReady = false
        showTutorial = false
        tutorialComplete = false
        skipTypewriter = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                        appState.mediaPlayer?.setVolume(musicVolume.value, musicVolume.value)
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

                            if (screen == HOME_SCREEN) {
                                if (prefs.getBoolean("first_time_home_screen", true)) {
                                    tutorialText = """
                                    Welcome to PixelFit Quest!

                                    HOME SCREEN  
                                    View your level, coins, exp and streak at the top. Track daily steps for rewards, check your rank and achievements with the trophy icon, resume past workouts, and complete daily missions for extra coins/exp.
                                    
                                    CUSTOMIZATION  
                                    Choose your character's gender and cloths that can be purchased with coins to equip and set your height for accurate tracking.
                                    
                                    WORKOUT CUSTOMIZATION  
                                    Check exercises to select them, adjust sets and weights, give a name to save as template, then tap "Start Workout" to earn coins and exp.
                                    
                                    SETTINGS  
                                    Adjust music volume, sign out or delete your account.  """.trimIndent()
                                    showTutorial = true
                                }
                            }
                        }
                    )
                }
            }
        }

        if (showTutorial && screenContentReady) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(horizontal = 32.dp)
                        .padding(top = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    TypewriterText(
                        text = tutorialText,
                        onComplete = {
                            tutorialComplete = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        skipToEnd = skipTypewriter
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 32.dp, end = 32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .clickable {
                            if (tutorialComplete) {
                                prefs
                                    .edit()
                                    .putBoolean("first_time_home_screen", false)
                                    .apply()
                                showTutorial = false
                                tutorialComplete = false
                                skipTypewriter = false
                            } else {
                                skipTypewriter = true
                            }
                        }
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = if (tutorialComplete) "START" else "SKIP",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black,
                        fontSize = 18.sp,
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
