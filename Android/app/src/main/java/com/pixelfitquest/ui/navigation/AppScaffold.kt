package com.pixelfitquest.ui.navigation

import android.content.res.Configuration
import android.media.MediaPlayer
import android.net.Uri
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
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
import com.pixelfitquest.feature.workouts.WorkoutsTab
import com.pixelfitquest.feature.health.HealthCenterScreen
import com.pixelfitquest.feature.workout.WorkoutScreen
import com.pixelfitquest.viewmodel.GlobalSettingsViewModel
import com.pixelfitquest.feature.workoutResume.WorkoutResumeViewModel
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.SlateGroove
import com.pixelfitquest.ui.theme.determination
import com.pixelfitquest.ui.theme.spacing

internal fun shouldShowBottomBar(route: String?): Boolean = route != null && (
    route == HOME_SCREEN || route.startsWith(WORKOUTS_HISTORY_SCREEN) ||
    route == HEALTH_CENTER_SCREEN || route == CUSTOMIZATION_SCREEN ||
    route == SETTINGS_SCREEN || route == PROGRESS_SCREEN ||
    route.startsWith(WORKOUT_CUSTOMIZATION_SCREEN)
)

internal fun selectedBottomNavItem(currentRoute: String?, tabArg: String? = null): BottomNavItem? = when {
    currentRoute == null -> null
    currentRoute == HOME_SCREEN -> BottomNavItem.Home
    currentRoute.startsWith(WORKOUTS_HISTORY_SCREEN) -> {
        val tab = tabArg ?: currentRoute.substringAfter("tab=", "").substringBefore("&").takeIf { it.isNotEmpty() && it != "{tab}" }
        if (tab.equals("history", ignoreCase = true)) BottomNavItem.History else BottomNavItem.Templates
    }
    currentRoute.startsWith(WORKOUT_CUSTOMIZATION_SCREEN) -> BottomNavItem.Templates
    currentRoute == PROGRESS_SCREEN -> BottomNavItem.History
    currentRoute == HEALTH_CENTER_SCREEN || currentRoute == SETTINGS_SCREEN || currentRoute == CUSTOMIZATION_SCREEN -> BottomNavItem.Health
    else -> null
}

internal const val NAV_TAB_KEY = "tab"

private fun NavBackStackEntry.isWorkoutsEntry() = destination.route?.startsWith(WORKOUTS_HISTORY_SCREEN) == true

/** Current workouts tab: an in-screen switch (saved state) wins over the route argument. */
internal fun NavBackStackEntry.workoutsTab(): String? =
    (if (isWorkoutsEntry()) savedStateHandle.get<String>(NAV_TAB_KEY) else null) ?: arguments?.getString(NAV_TAB_KEY)

internal fun NavBackStackEntry.setWorkoutsTab(tab: String) {
    savedStateHandle[NAV_TAB_KEY] = tab
}

/** Observable [workoutsTab] so the bar/rail highlight follows in-screen tab switches without re-navigating. */
@Composable
internal fun NavBackStackEntry?.observedTabArg(): String? {
    val entry = this ?: return null
    if (!entry.isWorkoutsEntry()) return entry.arguments?.getString(NAV_TAB_KEY)
    val flow = remember(entry) { entry.savedStateHandle.getStateFlow<String?>(NAV_TAB_KEY, null) }
    return flow.collectAsState().value ?: entry.arguments?.getString(NAV_TAB_KEY)
}

internal fun NavHostController.navigateToBottomNav(route: String) {
    val currentRoute = currentBackStackEntry?.destination?.route
    val currentTabArg = currentBackStackEntry?.workoutsTab()
    val currentItem = selectedBottomNavItem(currentRoute, currentTabArg)
    val targetItem = selectedBottomNavItem(route)
    val atRoot = currentRoute == HOME_SCREEN || currentRoute == HEALTH_CENTER_SCREEN ||
        currentRoute?.startsWith(WORKOUTS_HISTORY_SCREEN) == true
    if (currentItem != null && currentItem == targetItem) {
        if (atRoot) return
        val popped = when (targetItem) {
            BottomNavItem.Health -> popBackStack(HEALTH_CENTER_SCREEN, inclusive = false)
            BottomNavItem.Home -> popBackStack(HOME_SCREEN, inclusive = false)
            BottomNavItem.Templates, BottomNavItem.History ->
                popBackStack("$WORKOUTS_HISTORY_SCREEN?tab={tab}", inclusive = false) ||
                    popBackStack(WORKOUTS_HISTORY_SCREEN, inclusive = false)
        }
        if (popped && selectedBottomNavItem(currentBackStackEntry?.destination?.route, currentBackStackEntry?.workoutsTab()) == targetItem) {
            return
        }
    }
    val fromWorkouts = currentRoute?.startsWith(WORKOUTS_HISTORY_SCREEN) == true
    val toWorkouts = route.startsWith(WORKOUTS_HISTORY_SCREEN)
    navigate(route) {
        popUpTo(HOME_SCREEN) { saveState = atRoot && !fromWorkouts && !toWorkouts }
        launchSingleTop = true
        restoreState = atRoot && !toWorkouts
    }
}

@Composable
fun AppScaffold() {
    val snackbarHostState = remember { SnackbarHostState() }
    val appState = rememberAppState(snackbarHostState)
    val navController = appState.navController
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentTabArg = backStackEntry.observedTabArg()
    val globalSettingsViewModel: GlobalSettingsViewModel = hiltViewModel()
    val userSettings by globalSettingsViewModel.userRepository.getUserData().collectAsState(initial = null)
    val characterData by globalSettingsViewModel.userRepository.getCharacterData().collectAsState(initial = null)

    val hasBottomBar = shouldShowBottomBar(currentRoute)

    var settingsLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(userSettings) {
        if (userSettings != null && !settingsLoaded) {
            settingsLoaded = true
        }
    }

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (hasBottomBar && !isLandscape) {
                    PixelFitNavBar(currentRoute, { navController.navigateToBottomNav(it.route) }, selectedTabArg = currentTabArg)
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
                    PixelFitNavRail(currentRoute, { navController.navigateToBottomNav(it.route) }, selectedTabArg = currentTabArg)
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

@Composable
internal fun PixelFitNavBar(
    selectedRoute: String?, onItemClick: (BottomNavItem) -> Unit, modifier: Modifier = Modifier,
    selectedTabArg: String? = null, items: List<BottomNavItem> = BottomNavItem.items,
) {
    val spacing = MaterialTheme.spacing
    val navBarHeight = spacing.navBar
    val navBarInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
    val totalNavBarHeight = navBarHeight + navBarInsets.asPaddingValues().calculateBottomPadding()
    val selectedItem = selectedBottomNavItem(selectedRoute, selectedTabArg)
    Box(
        modifier = modifier.offset(y = (-1).dp).height(totalNavBarHeight + 1.dp).navBarPortraitBackground(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(navBarHeight).windowInsetsPadding(navBarInsets.only(WindowInsetsSides.Horizontal)),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                NavTabItem(
                    item = item, isSelected = selectedItem == item,
                    iconSize = spacing.scale(52), fontSize = 11.sp, onClick = { onItemClick(item) },
                    modifier = Modifier.weight(1f).height(navBarHeight).widthIn(min = 48.dp).heightIn(min = 48.dp),
                )
            }
        }
    }
}

@Composable
internal fun PixelFitNavRail(
    selectedRoute: String?, onItemClick: (BottomNavItem) -> Unit, modifier: Modifier = Modifier,
    selectedTabArg: String? = null, items: List<BottomNavItem> = BottomNavItem.items,
) {
    val spacing = MaterialTheme.spacing
    val sideBarWidth = spacing.sideBarWidth
    val sideBarInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.End + WindowInsetsSides.Vertical)
    val endInset = sideBarInsets.asPaddingValues().calculateEndPadding(LocalLayoutDirection.current)
    val selectedItem = selectedBottomNavItem(selectedRoute, selectedTabArg)
    Box(
        modifier = modifier.offset(x = (-1).dp).width(sideBarWidth + endInset + 1.dp).fillMaxHeight().navBarVerticalBackground(),
        contentAlignment = Alignment.CenterStart,
    ) {
        BoxWithConstraints(
            modifier = Modifier.width(sideBarWidth).fillMaxHeight().windowInsetsPadding(sideBarInsets.only(WindowInsetsSides.Vertical)),
            contentAlignment = Alignment.Center,
        ) {
            val itemCount = items.size.coerceAtLeast(1)
            val spacingDp = if (maxHeight >= 360.dp) spacing.scale(4) else spacing.scale(2)
            val itemSlotHeight = ((maxHeight - spacingDp * (itemCount - 1)) / itemCount).coerceAtMost(spacing.scale(76)).coerceAtLeast(48.dp)
            val dynamicIconSize = (itemSlotHeight - 18.dp).coerceIn(38.dp, 50.dp)
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(spacingDp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items.forEach { item ->
                    NavTabItem(
                        item = item, isSelected = selectedItem == item,
                        iconSize = dynamicIconSize, fontSize = 9.sp, onClick = { onItemClick(item) },
                        modifier = Modifier.width(sideBarWidth.coerceAtLeast(48.dp)).height(itemSlotHeight),
                    )
                }
            }
        }
    }
}

@Composable
private fun NavTabItem(
    item: BottomNavItem, isSelected: Boolean, iconSize: Dp, fontSize: TextUnit,
    onClick: () -> Unit, modifier: Modifier = Modifier,
) {
    val label = stringResource(item.labelRes)
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .selectable(selected = isSelected, interactionSource = interactionSource, indication = null, role = Role.Tab, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = label },
    ) {
        Icon(
            painter = painterResource(id = if (isSelected) item.selectedIcon else item.unSelectedIcon),
            contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(iconSize),
        )
        Text(
            text = label, fontFamily = determination, fontSize = fontSize, lineHeight = fontSize,
            fontWeight = FontWeight.Bold, color = if (isSelected) SlateGroove else SlateDeep,
            textDecoration = if (isSelected) TextDecoration.Underline else TextDecoration.None,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.offset(y = (-3).dp).clearAndSetSemantics {},
        )
    }
}

internal fun workoutRoute(plan: WorkoutPlan, templateName: String?): String {
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

    composable(
        route = "$WORKOUTS_HISTORY_SCREEN?tab={tab}",
        arguments = listOf(
            navArgument("tab") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val tabArg = backStackEntry.workoutsTab()
        val initialTab = if (tabArg.equals("history", ignoreCase = true)) {
            WorkoutsTab.HISTORY
        } else {
            WorkoutsTab.TEMPLATES
        }

        WorkoutsHistoryScreen(
            initialTab = initialTab,
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
            },
            onOpenProgression = {
                appState.navigate(PROGRESS_SCREEN)
            },
            onTabSelected = { tab ->
                backStackEntry.setWorkoutsTab(if (tab == WorkoutsTab.HISTORY) "history" else "templates")
            }
        )
    }

    composable(HEALTH_CENTER_SCREEN) {
        HealthCenterScreen(
            onOpenSettings = { appState.navigate(SETTINGS_SCREEN) },
            onOpenCustomization = { appState.navigate(CUSTOMIZATION_SCREEN) },
        )
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
            openScreen = { route ->
                if (route.startsWith(WORKOUTS_HISTORY_SCREEN)) {
                    appState.navController.navigate(route) {
                        popUpTo("workout_resume/{workoutId}") { inclusive = true }
                        launchSingleTop = true
                    }
                } else {
                    appState.navigate(route)
                }
            },
            onWorkoutDeleted = {
                if (!appState.navController.popBackStack(WORKOUTS_HISTORY_SCREEN, inclusive = false)) {
                    appState.navController.navigate("$WORKOUTS_HISTORY_SCREEN?tab=history") {
                        popUpTo("workout_resume/{workoutId}") { inclusive = true }
                        launchSingleTop = true
                    }
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

        // Active content bounds within navbar.png (1200x168 base dimensions):
        // Bounding box of the stone bar: x in [191..993] (width 802), y in [7..166] (height 159)
        val bmScaleX = navBarBitmap.width / 1200f
        val bmScaleY = navBarBitmap.height / 168f
        val srcLeft = (191 * bmScaleX).toInt()
        val srcRight = (993 * bmScaleX).toInt()
        val srcTop = (7 * bmScaleY).toInt()
        val srcBottom = (166 * bmScaleY).toInt()
        val srcHeight = (srcBottom - srcTop).coerceAtLeast(1)
        val capWidth = (45 * bmScaleX).toInt().coerceAtLeast(1)

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

        // Active content bounds within navbar_vertical.png (168x1200 base dimensions):
        // Bounding box of the stone bar: x in [7..161] (100% opaque stone), y in [195..990] (height 795)
        val bmScaleX = navBarBitmap.width / 168f
        val bmScaleY = navBarBitmap.height / 1200f
        val srcLeft = (7 * bmScaleX).toInt()
        val srcRight = (161 * bmScaleX).toInt()
        val srcWidth = (srcRight - srcLeft).coerceAtLeast(1)
        val srcTop = (195 * bmScaleY).toInt()
        val srcBottom = (990 * bmScaleY).toInt()
        val capHeight = (45 * bmScaleY).toInt().coerceAtLeast(1)

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
