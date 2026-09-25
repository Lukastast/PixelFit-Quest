package com.pixelfitquest.ui.navigation

import android.content.res.Configuration
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalLayoutDirection
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
    val characterData by globalSettingsViewModel.userRepository.getCharacterData().collectAsState(initial = null)

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
    val navBarHeight = spacing.navBar
    val navIconSize = spacing.navIcon

    val navBarInsets = WindowInsets.safeDrawing.only(
        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
    )
    val bottomInset = navBarInsets.asPaddingValues().calculateBottomPadding()
    val totalNavBarHeight = navBarHeight + bottomInset

    val sideBarWidth = spacing.sideBarWidth
    val sideNavIconSize = spacing.sideNavIcon
    val sideBarInsets = WindowInsets.safeDrawing.only(
        WindowInsetsSides.End + WindowInsetsSides.Vertical
    )
    val endInset = sideBarInsets.asPaddingValues().calculateEndPadding(LocalLayoutDirection.current)
    val totalSideBarWidth = sideBarWidth + endInset

    val appBackgroundRes = remember(characterData?.equippedAppBackground) {
        com.pixelfitquest.feature.customization.model.CustomizationCatalog.appBackgroundDrawable(
            characterData?.equippedAppBackground
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(id = appBackgroundRes),
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

        val navItems = listOf(
            BottomNavItem.Home,
            BottomNavItem.Customization,
            BottomNavItem.Workouts,
            BottomNavItem.HealthCenter,
            BottomNavItem.Settings,
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (hasBottomBar && !isLandscape) {
                    Box(
                        modifier = Modifier
                            .offset(y = (-1).dp)
                            .height(totalNavBarHeight + 1.dp)
                            .navBarPortraitBackground(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(navBarHeight)
                                .windowInsetsPadding(
                                    navBarInsets.only(WindowInsetsSides.Horizontal)
                                ),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            navItems.forEach { item ->
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
            if (isLandscape && hasBottomBar) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        NavHost(
                            navController = appState.navController,
                            startDestination = SPLASH_SCREEN,
                            modifier = if (currentRoute != HOME_SCREEN) {
                                Modifier.statusBarsPadding()
                            } else {
                                Modifier
                            }
                        ) {
                            pixelFitGraph(appState = appState)
                        }
                    }

                    // Side Navigation Bar on the RIGHT
                    Box(
                        modifier = Modifier
                            .offset(x = (-1).dp)
                            .width(totalSideBarWidth + 1.dp)
                            .fillMaxHeight()
                            .navBarVerticalBackground(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BoxWithConstraints(
                            modifier = Modifier
                                .width(sideBarWidth)
                                .fillMaxHeight()
                                .windowInsetsPadding(sideBarInsets.only(WindowInsetsSides.Vertical)),
                            contentAlignment = Alignment.Center
                        ) {
                            val availableHeight = maxHeight
                            val spacingDp = if (availableHeight >= 360.dp) spacing.scale(6) else spacing.scale(3)
                            val totalSpacing = spacingDp * 4
                            val maxSlotHeight = (availableHeight - totalSpacing) / 5
                            // Dynamic icon size: expands up to 58dp if space allows, gracefully scales down on tight screens
                            val dynamicIconSize = (maxSlotHeight - 2.dp).coerceIn(46.dp, 58.dp)
                            val itemSlotHeight = maxSlotHeight.coerceAtMost(spacing.scale(64))

                            Column(
                                modifier = Modifier.fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy(spacingDp, Alignment.CenterVertically),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                navItems.forEach { item ->
                                    val interactionSource = remember { MutableInteractionSource() }
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .width(sideBarWidth)
                                            .height(itemSlotHeight)
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
                                            modifier = Modifier.size(dynamicIconSize),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
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
}

private fun workoutRoute(plan: WorkoutPlan, templateName: String?): String {
    val planJson = Uri.encode(Gson().toJson(plan))
    val name = Uri.encode(templateName?.takeIf { it.isNotBlank() } ?: "workout")
    return "$WORKOUT_SCREEN/$planJson/$name"
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
                appState.navigate(workoutRoute(plan, templateName))
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
                appState.popUp()
                appState.navigate(workoutRoute(plan, templateName))
            },
            onBack = {
                appState.popUp()
            },
            onTemplateSaved = {
                appState.popUp()
                appState.navigate(WORKOUTS_HISTORY_SCREEN)
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
 * Renders the horizontal pixel-art stone navbar background (R.drawable.navbar)
 * across portrait screens using a 3-patch technique:
 * - Preserves left and right decorative rounded caps with moss/vines without horizontal stretching
 * - Seamlessly stretches the uniform stone body across the middle width
 * - Eliminates transparent canvas margins (rows 0..3) so the stone border starts flush at y = 0
 */
@Composable
private fun Modifier.navBarPortraitBackground(): Modifier {
    val navBarBitmap = ImageBitmap.imageResource(id = R.drawable.navbar)
    return this.drawBehind {
        val dstWidth = size.width.toInt()
        val dstHeight = size.height.toInt()
        if (dstWidth <= 0 || dstHeight <= 0) return@drawBehind

        // Active content bounds within navbar.png (1200x168):
        // Bounding box of the stone bar: x in [191..993] (width 802), y in [7..166] (height 159)
        // Rows 0..3 are 100% transparent; row 4 is 96.5% transparent; rows 5..6 are translucent; row 7 is 100% opaque.
        val srcLeft = 191
        val srcRight = 993
        val srcWidth = srcRight - srcLeft // 802
        val srcTop = 7
        val srcBottom = 166
        val srcHeight = srcBottom - srcTop // 159
        val capWidth = 45 // Width of rounded corner caps with moss/vines

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

/**
 * Renders the vertical pixel-art stone navbar background (R.drawable.navbar_vertical)
 * across landscape screens using a 3-patch technique:
 * - Preserves top and bottom decorative rounded caps with moss/vines without stretching
 * - Seamlessly stretches the uniform stone body across the middle height
 * - Eliminates transparent canvas margins from the source image
 */
@Composable
private fun Modifier.navBarVerticalBackground(): Modifier {
    val navBarBitmap = ImageBitmap.imageResource(id = R.drawable.navbar_vertical)
    return this.drawBehind {
        val dstWidth = size.width.toInt()
        val dstHeight = size.height.toInt()
        if (dstWidth <= 0 || dstHeight <= 0) return@drawBehind

        // Active content bounds within navbar_vertical.png (168x1200):
        // Bounding box of the stone bar: x in [7..161] (100% opaque stone), y in [195..990] (height 795)
        val srcLeft = 7
        val srcRight = 161
        val srcWidth = srcRight - srcLeft // 154
        val srcTop = 195
        val srcBottom = 990
        val srcHeight = srcBottom - srcTop // 795
        val capHeight = 45 // Height of rounded corner caps with moss/vines (195..240 and 945..990)

        val scaleX = dstWidth.toFloat() / srcWidth.toFloat()
        val scaledCapHeight = (capHeight * scaleX).toInt().coerceAtMost(dstHeight / 2)
        val centerDstHeight = (dstHeight - 2 * scaledCapHeight).coerceAtLeast(0)

        // 1. Top decorative cap
        drawImage(
            image = navBarBitmap,
            srcOffset = IntOffset(srcLeft, srcTop),
            srcSize = IntSize(srcWidth, capHeight),
            dstOffset = IntOffset(0, 0),
            dstSize = IntSize(dstWidth, scaledCapHeight),
            filterQuality = FilterQuality.None
        )

        // 2. Center stone bar (stretches seamlessly vertically)
        if (centerDstHeight > 0) {
            val centerSrcHeight = (srcBottom - capHeight) - (srcTop + capHeight)
            drawImage(
                image = navBarBitmap,
                srcOffset = IntOffset(srcLeft, srcTop + capHeight),
                srcSize = IntSize(srcWidth, centerSrcHeight),
                dstOffset = IntOffset(0, scaledCapHeight),
                dstSize = IntSize(dstWidth, centerDstHeight),
                filterQuality = FilterQuality.None
            )
        }

        // 3. Bottom decorative cap
        drawImage(
            image = navBarBitmap,
            srcOffset = IntOffset(srcLeft, srcBottom - capHeight),
            srcSize = IntSize(srcWidth, capHeight),
            dstOffset = IntOffset(0, dstHeight - scaledCapHeight),
            dstSize = IntSize(dstWidth, scaledCapHeight),
            filterQuality = FilterQuality.None
        )
    }
}
