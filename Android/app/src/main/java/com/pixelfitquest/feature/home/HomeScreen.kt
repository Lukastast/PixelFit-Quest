package com.pixelfitquest.feature.home

import android.content.res.Configuration
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.pixelfitquest.R
import com.pixelfitquest.feature.home.model.DwellingTier
import com.pixelfitquest.feature.levels.LevelUpDialog
import com.pixelfitquest.feature.levels.LevelsViewModel
import com.pixelfitquest.feature.streak.WeeklyStreakDialog
import com.pixelfitquest.feature.streak.WeeklyStreakViewModel
import com.pixelfitquest.health.HealthConnectStatus
import com.pixelfitquest.ui.navigation.LEVELS_SCREEN
import com.pixelfitquest.ui.theme.spacing
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    restartApp: (String) -> Unit,
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    weeklyStreakViewModel: WeeklyStreakViewModel = hiltViewModel(),
    levelsViewModel: LevelsViewModel = hiltViewModel(),
    onScreenReady: () -> Unit = {}
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val isLoading by viewModel.isLoading.collectAsState()
    val healthStatus by viewModel.healthStatus.collectAsState()
    val healthPermissionsGranted by viewModel.healthPermissionsGranted.collectAsState()
    val healthReady by viewModel.healthReady.collectAsState()
    var askedHealthPermissions by remember { mutableStateOf(false) }

    val healthPermissionContract = remember {
        PermissionController.createRequestPermissionResultContract()
    }
    val healthPermissionLauncher = rememberLauncherForActivityResult(
        contract = healthPermissionContract
    ) { granted ->
        viewModel.onHealthPermissionsResult(granted)
    }

    LaunchedEffect(Unit) {
        Log.d("HomeScreen", "Initializing HomeScreen")
        viewModel.initialize()
    }

    LaunchedEffect(healthReady, healthStatus, healthPermissionsGranted) {
        if (
            healthReady &&
            !askedHealthPermissions &&
            healthStatus == HealthConnectStatus.AVAILABLE &&
            !healthPermissionsGranted
        ) {
            askedHealthPermissions = true
            runCatching { healthPermissionLauncher.launch(viewModel.healthPermissions) }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshHealthMetrics()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val userData by viewModel.userData.collectAsState()
    val characterData by viewModel.characterData.collectAsState()
    val characterPose by viewModel.characterPose.collectAsState()
    val weeklyBoard by viewModel.weeklyBoard.collectAsState()
    val levelsState by levelsViewModel.uiState.collectAsState()

    LaunchedEffect(userData?.level, userData?.exp) {
        val data = userData ?: return@LaunchedEffect
        levelsViewModel.importRemoteIfEmpty(data.level, data.exp)
    }

    val weeklyStreak by weeklyStreakViewModel.snapshot.collectAsState()
    var showStreakDialog by remember { mutableStateOf(false) }
    var showMissionsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isLoading) {
        if (!isLoading) {
            onScreenReady()
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = Color.White)
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))
                Text(stringResource(R.string.loading), color = Color.White, fontSize = 16.sp)
            }
        }
        return
    }

    val level = levelsState.progress.level
    val dwellingTier = when (characterData.equippedHomeUpgrade) {
        "dwelling_gym" -> DwellingTier.GYM
        "dwelling_castle" -> DwellingTier.CASTLE
        "dwelling_cottage" -> DwellingTier.COTTAGE
        "dwelling_shack" -> DwellingTier.SHACK
        "dwelling_tent" -> DwellingTier.TENT
        "dwelling_tarp" -> DwellingTier.TARP
        else -> DwellingTier.forLevel(level)
    }
    val coins = userData?.coins ?: 0
    val streak = weeklyStreak.currentStreakWeeks
    LaunchedEffect(Unit) {
        while (true) {
            delay(30000L)
            viewModel.refreshHealthMetrics()
        }
    }

    val displayLevel = if (level >= 30) stringResource(R.string.max_level) else level.toString()
    val progressIndex = levelsState.progress.xpBarIndex

    val spacing = MaterialTheme.spacing

    Box(modifier = Modifier.fillMaxSize()) {
        DwellingScene(
            tier = dwellingTier,
            pose = characterPose,
            gender = characterData.gender,
            variant = characterData.variant,
            isLandscape = isLandscape,
            onPoseCycle = { viewModel.cyclePose() },
            modifier = Modifier.fillMaxSize()
        )

        StatsHudBar(
            coins = coins,
            streak = streak,
            displayLevel = displayLevel,
            progressIndex = progressIndex,
            onStreakClick = { showStreakDialog = true },
            onLevelClick = { navController.navigate(LEVELS_SCREEN) },
            isLandscape = isLandscape,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .displayCutoutPadding()
                .padding(
                    horizontal = if (isLandscape) spacing.scale(32) else spacing.screen,
                    vertical = if (isLandscape) spacing.xs else spacing.sm
                )
        )

        // Quick Missions Access Button
        val missionsButtonModifier = if (isLandscape) {
            Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(start = spacing.md, bottom = spacing.md)
                .size(spacing.scale(48))
        } else {
            Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(
                    end = spacing.md,
                    bottom = spacing.navBar + spacing.sm
                )
                .size(spacing.quickAccessIcon)
        }

        Box(
            modifier = missionsButtonModifier
                .clickable { showMissionsDialog = true }
        ) {
            Image(
                painter = painterResource(id = R.drawable.achievement_button),
                contentDescription = stringResource(R.string.daily_missions_title),
                modifier = Modifier.fillMaxSize()
            )
        }

        if (showStreakDialog) {
            WeeklyStreakDialog(
                snapshot = weeklyStreak,
                onDismiss = { showStreakDialog = false },
                onTargetChange = { weeklyStreakViewModel.setTargetSessionsPerWeek(it) }
            )
        }

        if (showMissionsDialog) {
            MissionsDialog(
                missions = weeklyBoard.missions,
                onDismiss = { showMissionsDialog = false },
            )
        }

        levelsState.pendingLevelUp?.let { pending ->
            LevelUpDialog(
                result = pending,
                onDismiss = { levelsViewModel.dismissLevelUp() },
                onOpenRewards = {
                    levelsViewModel.dismissLevelUp()
                    navController.navigate(LEVELS_SCREEN)
                },
            )
        }
    }
}
