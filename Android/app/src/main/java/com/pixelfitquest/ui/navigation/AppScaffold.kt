package com.pixelfitquest.ui.navigation

import android.content.res.Configuration
import android.media.MediaPlayer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
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
import com.pixelfitquest.R
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlan
import com.pixelfitquest.feature.customization.CustomizationScreen
import com.pixelfitquest.feature.achievements.AchievementsScreen
import com.pixelfitquest.feature.home.HomeScreen
import com.pixelfitquest.feature.levels.LevelsScreen
import com.pixelfitquest.feature.intro.IntroScreen
import com.pixelfitquest.feature.settings.SettingsScreen
import com.pixelfitquest.feature.splash.SplashScreen
import com.pixelfitquest.feature.workoutBuilder.WorkoutCustomizationScreen
import com.pixelfitquest.feature.workoutResume.WorkoutResumeScreen
import com.pixelfitquest.feature.progress.ProgressScreen
import com.pixelfitquest.feature.workouts.WorkoutsHistoryScreen
import com.pixelfitquest.feature.health.HealthCenterScreen
import com.pixelfitquest.feature.workout.WorkoutScreen
import com.pixelfitquest.viewmodel.GlobalSettingsViewModel
import com.pixelfitquest.feature.workoutResume.WorkoutResumeViewModel
import com.pixelfitquest.ui.theme.spacing

@Composable
fun AppScaffold() {
    val snackbarHostState = remember { SnackbarHostState() }
    val appState = rememberAppState(snackbarHostState)
    val navController = appState.navController
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val globalSettingsViewModel: GlobalSettingsViewModel = hiltViewModel()
    val userSettings by globalSettingsViewModel.userRepository.getUserData().collectAsState(initial = null)

    val hasBottomBar = currentRoute?.let { route ->
        route == HOME_SCREEN ||
        route == WORKOUTS_HISTORY_SCREEN ||
        route == HEALTH_CENTER_SCREEN ||
        route == CUSTOMIZATION_SCREEN ||
        route == SETTINGS_SCREEN ||
        route == PROGRESS_SCREEN ||
        route.startsWith(WORKOUT_CUSTOMIZATION_SCREEN)
    } ?: false

    var settingsLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(userSettings) {
        if (userSettings != null && !settingsLoaded) {
            settingsLoaded = true
        }
    }

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val spacing = MaterialTheme.spacing
    val navBarHeight = spacing.navBarHeight(isLandscape)
    val navIconSize = spacing.navIconSize(isLandscape)

    val navBarInsets = WindowInsets.safeDrawing.only(
        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
    )
    val bottomInset = navBarInsets.asPaddingValues().calculateBottomPadding()
    val totalNavBarHeight = navBarHeight + bottomInset

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

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (hasBottomBar) {
                    val navBarModifier = if (isLandscape) {
                        Modifier
                            .height(totalNavBarHeight)
                            .navBarLandscapeBackground()
                    } else {
                        Modifier
                            .height(totalNavBarHeight)
                            .paint(
                                painter = painterResource(id = R.drawable.navbar),
                                contentScale = ContentScale.Crop
                            )
                    }

                    NavigationBar(
                        modifier = navBarModifier,
                        containerColor = Color.Transparent,
                        windowInsets = navBarInsets
                    ) {
                        val items = listOf(
                            BottomNavItem.Home,
                            BottomNavItem.Customization,
                            BottomNavItem.Workouts,
                            BottomNavItem.HealthCenter,
                            BottomNavItem.Settings,
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(navBarHeight),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items.forEach { item ->
                                val interactionSource = remember { MutableInteractionSource() }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .height(navBarHeight)
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
                                            id = if (currentRoute == item.route || (item == BottomNavItem.Workouts && currentRoute.startsWith(WORKOUT_CUSTOMIZATION_SCREEN))) {
                                                item.selectedIcon
                                            } else {
                                                item.unSelectedIcon
                                            }
                                        ),
                                        contentDescription = item.label,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(navIconSize),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPaddingModifier ->
            NavHost(
                navController = appState.navController,
                startDestination = SPLASH_SCREEN,
                modifier = if (hasBottomBar && currentRoute != HOME_SCREEN) {
                    Modifier
                        .statusBarsPadding()
                        .padding(bottom = innerPaddingModifier.calculateBottomPadding())
                } else {
                    Modifier
                }
            ) {
                pixelFitGraph(appState = appState)
            }
        }
    }
}

fun NavGraphBuilder.pixelFitGraph(
    appState: AppState,
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

    composable(HOME_SCREEN) {
        HomeScreen(
            restartApp = { route -> appState.clearAndNavigate(route) },
            navController = appState.navController,
        )
    }

    composable(WORKOUTS_HISTORY_SCREEN) {
        WorkoutsHistoryScreen(
            onWorkoutClick = { workoutId ->
                appState.navigate("workout_resume/$workoutId")
            },
            onStartNewWorkout = {
                appState.navigate(WORKOUT_CUSTOMIZATION_SCREEN)
            },
            onCreateTemplate = {
                appState.navigate("$WORKOUT_CUSTOMIZATION_SCREEN?isTemplate=true")
            },
            onEditTemplate = { templateId ->
                appState.navigate("$WORKOUT_CUSTOMIZATION_SCREEN?templateId=$templateId&isTemplate=true")
            },
            onStartWorkout = { plan, templateName ->
                val gson = Gson()
                val planJson = gson.toJson(plan)
                appState.navigate("$WORKOUT_SCREEN/$planJson/$templateName")
            }
        )
    }

    composable(HEALTH_CENTER_SCREEN) {
        HealthCenterScreen()
    }

    composable(ACHIEVEMENTS_SCREEN) {
        AchievementsScreen(
            onBack = { appState.popUp() }
        )
    }

    composable(LEVELS_SCREEN) {
        LevelsScreen(
            onBack = { appState.popUp() }
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
            onWorkoutDeleted = {
                if (!appState.navController.popBackStack(HOME_SCREEN, inclusive = false)) {
                    appState.clearAndNavigate(HOME_SCREEN)
                }
            },
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
        CustomizationScreen()
    }

    composable(
        route = "$WORKOUT_CUSTOMIZATION_SCREEN?templateId={templateId}&isTemplate={isTemplate}",
        arguments = listOf(
            navArgument("templateId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument("isTemplate") {
                type = NavType.BoolType
                defaultValue = false
            }
        )
    ) { backStackEntry ->
        val isTemplate = backStackEntry.arguments?.getBoolean("isTemplate") ?: false
        WorkoutCustomizationScreen(
            isTemplateMode = isTemplate,
            onStartWorkout = { plan, templateName ->
                val gson = Gson()
                val planJson = gson.toJson(plan)
                appState.navigate("$WORKOUT_SCREEN/$planJson/$templateName")
            },
            onBack = {
                appState.popUp()
            },
            onTemplateSaved = {
                appState.popUp()
            }
        )
    }

    composable(SETTINGS_SCREEN) {
        SettingsScreen(
            restartApp = { route -> appState.clearAndNavigate(route) },
        )
    }

    composable(PROGRESS_SCREEN) {
        ProgressScreen(
            onBack = { appState.popUp() },
        )
    }
}

/**
 * Renders the pixel-art stone navbar background (R.drawable.navbar) without distortion
 * across wide landscape screens using a 3-patch technique:
 * - Preserves the left and right decorative rounded caps with moss/vines without stretching
 * - Seamlessly stretches the uniform stone body across the middle width
 * - Keeps the top highlight border and bottom shadow border at their full vertical height
 * - Eliminates transparent canvas margins from the source image
 */
@Composable
private fun Modifier.navBarLandscapeBackground(): Modifier {
    val navBarBitmap = ImageBitmap.imageResource(id = R.drawable.navbar)
    return this.drawBehind {
        val dstWidth = size.width.toInt()
        val dstHeight = size.height.toInt()
        if (dstWidth <= 0 || dstHeight <= 0) return@drawBehind

        // Active content bounds within navbar.png (1200x168):
        // Bounding box of the stone bar: x in [195..990] (width 795), y in [4..166] (height 162)
        val srcLeft = 195
        val srcRight = 990
        val srcTop = 4
        val srcBottom = 166
        val srcHeight = srcBottom - srcTop // 162
        val capWidth = 45 // Width of rounded corner caps with moss/vines (195..240 and 945..990)

        val scaleY = dstHeight.toFloat() / srcHeight.toFloat()
        val scaledCapWidth = (capWidth * scaleY).toInt().coerceAtMost(dstWidth / 2)
        val centerDstWidth = (dstWidth - 2 * scaledCapWidth).coerceAtLeast(0)

        // 1. Left decorative cap
        drawImage(
            image = navBarBitmap,
            srcOffset = IntOffset(srcLeft, srcTop),
            srcSize = IntSize(capWidth, srcHeight),
            dstOffset = IntOffset(0, 0),
            dstSize = IntSize(scaledCapWidth, dstHeight),
            filterQuality = FilterQuality.None
        )

        // 2. Center stone bar (stretches seamlessly across the width)
        if (centerDstWidth > 0) {
            val centerSrcWidth = (srcRight - capWidth) - (srcLeft + capWidth)
            drawImage(
                image = navBarBitmap,
                srcOffset = IntOffset(srcLeft + capWidth, srcTop),
                srcSize = IntSize(centerSrcWidth, srcHeight),
                dstOffset = IntOffset(scaledCapWidth, 0),
                dstSize = IntSize(centerDstWidth, dstHeight),
                filterQuality = FilterQuality.None
            )
        }

        // 3. Right decorative cap
        drawImage(
            image = navBarBitmap,
            srcOffset = IntOffset(srcRight - capWidth, srcTop),
            srcSize = IntSize(capWidth, srcHeight),
            dstOffset = IntOffset(dstWidth - scaledCapWidth, 0),
            dstSize = IntSize(scaledCapWidth, dstHeight),
            filterQuality = FilterQuality.None
        )
    }
}
