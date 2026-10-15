package com.pixelfitquest.feature.home

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.progressSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.pixelfitquest.R
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.components.molecules.RetroPanel
import com.pixelfitquest.components.molecules.RetroPill
import com.pixelfitquest.components.molecules.RetroStatBox
import com.pixelfitquest.feature.customization.model.CharacterData
import com.pixelfitquest.feature.home.model.CharacterPose
import com.pixelfitquest.feature.home.model.DwellingTier
import com.pixelfitquest.feature.home.model.HomeDisplayState
import com.pixelfitquest.feature.levels.LevelUpDialog
import com.pixelfitquest.feature.levels.LevelsViewModel
import com.pixelfitquest.feature.levels.XpStatusDialog
import com.pixelfitquest.feature.levels.progression.LevelCurve
import com.pixelfitquest.feature.streak.WeeklyStreakDialog
import com.pixelfitquest.feature.streak.WeeklyStreakViewModel
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlan
import com.pixelfitquest.ui.navigation.WORKOUTS_HISTORY_SCREEN
import com.pixelfitquest.ui.navigation.workoutRoute
import com.pixelfitquest.ui.theme.CrystalCyan
import com.pixelfitquest.ui.theme.DarkStone
import com.pixelfitquest.ui.theme.PlatinumWhite
import com.pixelfitquest.ui.theme.SilverSlate
import com.pixelfitquest.ui.theme.SilverSteel
import com.pixelfitquest.ui.theme.SlateBorderSubtle
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.SlateGroove
import com.pixelfitquest.ui.theme.determination
import com.pixelfitquest.ui.theme.spacing
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    restartApp: (String) -> Unit,
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    weeklyStreakViewModel: WeeklyStreakViewModel = hiltViewModel(),
    levelsViewModel: LevelsViewModel = hiltViewModel(),
    onStartWorkout: (WorkoutPlan, String) -> Unit = { plan, name ->
        navController.navigate(workoutRoute(plan, name))
    },
    onChooseAnotherTemplate: () -> Unit = {
        navController.navigate("$WORKOUTS_HISTORY_SCREEN?tab=templates")
    },
    onScreenReady: () -> Unit = {},
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        Log.d("HomeScreen", "Initializing HomeScreen")
        viewModel.initialize()
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
    val displayState by viewModel.homeDisplayState.collectAsState()
    val levelsState by levelsViewModel.uiState.collectAsState()

    LaunchedEffect(userData?.level, userData?.exp) {
        val data = userData ?: return@LaunchedEffect
        levelsViewModel.importRemoteIfEmpty(data.level, data.exp)
    }

    val weeklyStreak by weeklyStreakViewModel.snapshot.collectAsState()
    var showStreakDialog by remember { mutableStateOf(false) }
    var showXpStatusDialog by remember { mutableStateOf(false) }
    var showMissionsDialog by remember { mutableStateOf(false) }
    val weeklyBoard by viewModel.weeklyBoard.collectAsState()

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
    val streak = weeklyStreak.currentStreakWeeks
    val displayLevel = if (level >= LevelCurve.MAX_LEVEL) {
        stringResource(R.string.max_level)
    } else {
        level.toString()
    }
    val xpProgressFraction = (levelsState.progress.xpBarIndex.toFloat() / 5f).coerceIn(0f, 1f)

    LaunchedEffect(Unit) {
        while (true) {
            delay(30000L)
            viewModel.refreshHealthMetrics()
        }
    }

    HomeScreenContent(
        displayState = displayState,
        streak = streak,
        displayLevel = displayLevel,
        xpProgressFraction = xpProgressFraction,
        characterData = characterData,
        characterPose = characterPose,
        isLandscape = isLandscape,
        onPoseCycle = { viewModel.cyclePose() },
        onStreakClick = { showStreakDialog = true },
        onLevelClick = { showXpStatusDialog = true },
        onMissionsClick = { showMissionsDialog = true },
        onStartWorkout = onStartWorkout,
        onChooseAnotherTemplate = onChooseAnotherTemplate,
    )

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
            rerollAvailable = weeklyBoard.rerollAvailable,
            onReroll = viewModel::rerollMission,
            onDismiss = { showMissionsDialog = false },
        )
    }

    if (showXpStatusDialog) {
        XpStatusDialog(
            progress = levelsState.progress,
            skills = levelsState.skills,
            coins = userData?.coins ?: 0,
            streakSnapshot = weeklyStreak,
            onSpendSkill = levelsViewModel::spendSkill,
            onRespecSkills = levelsViewModel::respecSkills,
            respecDaysRemaining = levelsState.respecDaysRemaining,
            onStreakClick = {
                showXpStatusDialog = false
                showStreakDialog = true
            },
            onDismiss = { showXpStatusDialog = false },
        )
    }

    levelsState.pendingLevelUp?.let { pending ->
        LevelUpDialog(
            result = pending,
            skills = levelsState.skills,
            onSpendSkill = levelsViewModel::spendSkill,
            onDismiss = { levelsViewModel.dismissLevelUp() },
        )
    }
}

@Composable
fun HomeScreenContent(
    displayState: HomeDisplayState,
    streak: Int,
    displayLevel: String,
    xpProgressFraction: Float,
    characterData: CharacterData,
    characterPose: CharacterPose,
    isLandscape: Boolean,
    onPoseCycle: () -> Unit,
    onStreakClick: () -> Unit,
    onLevelClick: () -> Unit,
    onStartWorkout: (WorkoutPlan, String) -> Unit,
    onChooseAnotherTemplate: () -> Unit,
    modifier: Modifier = Modifier,
    onMissionsClick: () -> Unit = {},
) {
    val spacing = MaterialTheme.spacing

    Box(modifier = modifier.fillMaxSize()) {
        // Layer 1: Dwelling Scene (Lifter relaxing in bedroom-gym)
        DwellingScene(
            tier = DwellingTier.GYM,
            pose = characterPose,
            gender = characterData.gender,
            variant = characterData.variant,
            isLandscape = isLandscape,
            onPoseCycle = onPoseCycle,
            modifier = Modifier.fillMaxSize()
        )

        if (isLandscape) {
            // Landscape layout:
            // Status pills and single RetroPanel stacked on the right
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .displayCutoutPadding()
                    .padding(end = spacing.scale(24), top = spacing.xs, bottom = spacing.xs),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(spacing.scale(6))
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WeeklyStreakPill(
                        streak = streak,
                        onClick = onStreakClick
                    )
                    LevelProgressPill(
                        displayLevel = displayLevel,
                        progressFraction = xpProgressFraction,
                        onClick = onLevelClick
                    )
                    MissionsPill(onClick = onMissionsClick)
                }

                HomeRetroPanel(
                    displayState = displayState,
                    onStartWorkout = {
                        onStartWorkout(
                            displayState.nextUpTemplate.plan,
                            displayState.nextUpTemplate.name
                        )
                    },
                    onChooseAnotherTemplate = onChooseAnotherTemplate,
                    modifier = Modifier.width(spacing.scale(350))
                )
            }
        } else {
            // Portrait layout:
            // Top: Status pills row
            // Bottom: Single RetroPanel
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(
                        start = spacing.screen,
                        end = spacing.screen,
                        top = spacing.sm,
                        bottom = spacing.navBar + spacing.sm
                    ),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WeeklyStreakPill(
                        streak = streak,
                        onClick = onStreakClick
                    )
                    LevelProgressPill(
                        displayLevel = displayLevel,
                        progressFraction = xpProgressFraction,
                        onClick = onLevelClick
                    )
                    MissionsPill(onClick = onMissionsClick)
                }

                HomeRetroPanel(
                    displayState = displayState,
                    onStartWorkout = {
                        onStartWorkout(
                            displayState.nextUpTemplate.plan,
                            displayState.nextUpTemplate.name
                        )
                    },
                    onChooseAnotherTemplate = onChooseAnotherTemplate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = spacing.scale(420))
                )
            }
        }
    }
}

@Composable
fun WeeklyStreakPill(
    streak: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RetroPill(
        onClick = onClick,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = R.drawable.streak),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.home_week_streak_pill, streak),
            fontFamily = determination,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = PlatinumWhite
        )
    }
}

@Composable
fun LevelProgressPill(
    displayLevel: String,
    progressFraction: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RetroPill(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.home_level_pill, displayLevel),
            fontFamily = determination,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = PlatinumWhite
        )
        Spacer(modifier = Modifier.width(8.dp))
        // Recessed XP progress bar
        val xpPercent = (progressFraction.coerceIn(0f, 1f) * 100).toInt()
        val xpDescription = stringResource(R.string.home_xp_progress_description, xpPercent)
        Box(
            modifier = Modifier
                .progressSemantics(progressFraction.coerceIn(0f, 1f))
                .semantics { contentDescription = xpDescription }
                .width(48.dp)
                .height(10.dp)
                .background(color = SlateGroove, shape = RoundedCornerShape(3.dp))
                .border(width = 1.dp, color = SlateBorderSubtle, shape = RoundedCornerShape(3.dp))
                .clip(RoundedCornerShape(3.dp))
        ) {
            val clampedFraction = progressFraction.coerceIn(0f, 1f)
            if (clampedFraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(clampedFraction)
                        .background(color = CrystalCyan, shape = RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

@Composable
fun MissionsPill(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RetroPill(
        onClick = onClick,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = R.drawable.achievement_button),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.home_missions_pill),
            fontFamily = determination,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = PlatinumWhite
        )
    }
}

@Composable
fun HomeRetroPanel(
    displayState: HomeDisplayState,
    onStartWorkout: () -> Unit,
    onChooseAnotherTemplate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RetroPanel(modifier = modifier) {
        // NEXT UP Header
        Text(
            text = stringResource(R.string.home_next_up_header),
            fontFamily = determination,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = CrystalCyan,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Template Name
        Text(
            text = displayState.nextUpTemplate.name.uppercase(),
            fontFamily = determination,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = PlatinumWhite,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Last done subtitle
        Text(
            text = lastDoneText(displayState.lastDone),
            fontSize = 12.sp,
            color = SilverSlate,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Large START Button. Image contentDescription is null so TalkBack reads the Text once.
        PixelArtButton(
            onClick = onStartWorkout,
            imageRes = R.drawable.button_unclicked,
            pressedRes = R.drawable.button_clicked,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = stringResource(R.string.home_start_button),
                fontFamily = determination,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkStone,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // "Choose another template" link
        Text(
            text = stringResource(R.string.home_choose_another_template),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = SilverSteel,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .minimumInteractiveComponentSize()
                .clickable(role = Role.Button, onClick = onChooseAnotherTemplate)
                .padding(vertical = 4.dp, horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SlateBorderSubtle)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // THIS WEEK Header
        Text(
            text = stringResource(R.string.home_this_week_header),
            fontFamily = determination,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = CrystalCyan,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Stats Row (Workouts & Sets)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RetroStatBox(
                value = displayState.weeklyStats.workoutsCount.toString(),
                label = stringResource(R.string.home_stat_workouts),
                modifier = Modifier.weight(1f)
            )
            RetroStatBox(
                value = displayState.weeklyStats.setsCount.toString(),
                label = stringResource(R.string.home_stat_sets),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
