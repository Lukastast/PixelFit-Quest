package com.pixelfitquest.feature.home

import android.content.Context
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.pixelfitquest.R
import com.pixelfitquest.components.molecules.WorkoutCard
import com.pixelfitquest.feature.levels.HomeThemeBackdrop
import com.pixelfitquest.feature.levels.LevelUpDialog
import com.pixelfitquest.feature.levels.LevelsViewModel
import com.pixelfitquest.feature.streak.WeeklyStreakDialog
import com.pixelfitquest.feature.streak.WeeklyStreakViewModel
import com.pixelfitquest.health.HealthConnectIntents
import com.pixelfitquest.health.HealthConnectStatus
import com.pixelfitquest.ui.navigation.ACHIEVEMENTS_SCREEN
import com.pixelfitquest.ui.navigation.LEVELS_SCREEN
import com.pixelfitquest.ui.navigation.PROGRESS_SCREEN
import com.pixelfitquest.ui.theme.spacing
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
    val workouts by viewModel.workouts.collectAsState()

    val levelsState by levelsViewModel.uiState.collectAsState()

    LaunchedEffect(userData?.level, userData?.exp) {
        val data = userData ?: return@LaunchedEffect
        levelsViewModel.importRemoteIfEmpty(data.level, data.exp)
    }



    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    var showTutorial by remember { mutableStateOf(false) }
    val weeklyStreak by weeklyStreakViewModel.snapshot.collectAsState()
    var showStreakDialog by remember { mutableStateOf(false) }


    LaunchedEffect(isLoading) {
        Log.d("HomeScreen", "Loading state changed: isLoading=$isLoading")
        if (!isLoading) {
            Log.d("HomeScreen", "Screen finished loading, calling onScreenReady")
            onScreenReady()


            val isFirstTime = prefs.getBoolean("first_time_home_screen", true)
            Log.d("HomeScreen", "First time check: $isFirstTime")
            if (isFirstTime) {
                delay(300)
                Log.d("HomeScreen", "Showing tutorial")
                showTutorial = true
            }
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
    val coins = userData?.coins ?: 0
    val streak = weeklyStreak.currentStreakWeeks
    val healthMetrics by viewModel.healthMetrics.collectAsState()
    val todaySteps = healthMetrics.steps
    val stepGoal = healthMetrics.stepGoal
    val heartRateBpm = healthMetrics.heartRateBpm
    val rank by viewModel.rank.collectAsState()
    val totalUsers by viewModel.totalUsers.collectAsState()
    val leaderboardLocked by viewModel.leaderboardLocked.collectAsState()
    val dailyMissions by viewModel.dailyMissions.collectAsState()
    val completedMissions by viewModel.completedMissions.collectAsState()
    val progressEntryLabel = stringResource(R.string.progress_home_entry)

    LaunchedEffect(Unit) {
        while (true) {
            delay(30000L)
            viewModel.refreshHealthMetrics()
        }
    }

    val displayLevel = if (level >= 30) stringResource(R.string.max_level) else level.toString()
    val progressIndex = levelsState.progress.xpBarIndex

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    val today = dateFormat.format(Date())
    val todaysWorkouts = workouts.count { workout ->
        try {
            val instant = Instant.parse(workout.date)
            val workoutDate = instant.atZone(ZoneId.of("UTC")).toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            workoutDate == today
        } catch (e: Exception) {
            false
        }
    }

    val spacing = MaterialTheme.spacing

    Box(modifier = Modifier.fillMaxSize()) {
        HomeThemeBackdrop(themeId = levelsState.equipped.homeThemeId)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screen, vertical = spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing.barSm)
        ) {
            Image(
                painter = painterResource(id = R.drawable.info_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.coin),
                        contentDescription = stringResource(R.string.coin_icon_desc),
                        modifier = Modifier.size(spacing.scale(20))
                    )
                    Spacer(modifier = Modifier.padding(horizontal = spacing.xxs))
                    Text(
                        text = stringResource(R.string.coins_count, coins),
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showStreakDialog = true }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.streak),
                        contentDescription = stringResource(
                            R.string.weekly_streak_hud_desc,
                            streak
                        ),
                        modifier = Modifier.size(spacing.scale(20))
                    )
                    Spacer(modifier = Modifier.padding(horizontal = spacing.xxxs))
                    Text(
                        text = "$streak",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
                Text(
                    text = stringResource(R.string.level_display, displayLevel),
                    fontSize = 14.sp,
                    color = Color.White,
                    modifier = Modifier.clickable {
                        navController.navigate(LEVELS_SCREEN)
                    }
                )
                Box(
                    modifier = Modifier
                        .size(width = spacing.scale(80), height = spacing.md)
                        .clickable { navController.navigate(LEVELS_SCREEN) },
                    contentAlignment = Alignment.Center
                ) {
                    val xpPainter = when (progressIndex) {
                        0 -> painterResource(id = R.drawable.xp_0_percent)
                        1 -> painterResource(id = R.drawable.xp_20_percent)
                        2 -> painterResource(id = R.drawable.xp_40_percent)
                        3 -> painterResource(id = R.drawable.xp_60_percent)
                        4 -> painterResource(id = R.drawable.xp_80_percent)
                        5 -> painterResource(id = R.drawable.xp_100_percent)
                        else -> painterResource(id = R.drawable.xp_0_percent)
                    }
                    Image(
                        painter = xpPainter,
                        contentDescription = stringResource(R.string.xp_bar_desc),
                        modifier = Modifier.matchParentSize()
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing.barMd)
                .clickable {
                    when (healthStatus) {
                        HealthConnectStatus.AVAILABLE -> {
                            if (healthPermissionsGranted) {
                                viewModel.refreshHealthMetrics()
                            } else {
                                runCatching {
                                    healthPermissionLauncher.launch(viewModel.healthPermissions)
                                }
                            }
                        }
                        HealthConnectStatus.UPDATE_REQUIRED -> {
                            HealthConnectIntents.openPlayStore(context)
                        }
                        HealthConnectStatus.UNAVAILABLE -> Unit
                    }
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.info_background_higher),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.steps_label),
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = MaterialTheme.typography.titleMedium.fontWeight
                )
                Text(
                    text = stringResource(R.string.steps_progress, todaySteps, stepGoal),
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = MaterialTheme.typography.titleMedium.fontWeight
                )
                Text(
                    text = stepsSubtitle(healthStatus, healthPermissionsGranted, heartRateBpm),
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing.barLg)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(spacing.scale(150))
                        .fillMaxHeight()
                ) {
                    val rankBackground = when (rank) {
                        1 -> R.drawable.first_place
                        2 -> R.drawable.second_place
                        3 -> R.drawable.third_place
                        else -> R.drawable.fourth_and_more
                    }
                    Image(
                        painter = painterResource(id = rankBackground),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (leaderboardLocked) {
                            Text(
                                text = stringResource(R.string.leaderboard_pro_badge),
                                fontSize = 16.sp,
                                color = Color.White,
                                fontWeight = MaterialTheme.typography.titleMedium.fontWeight
                            )
                            Text(
                                text = stringResource(R.string.leaderboard_pro_hint),
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = MaterialTheme.typography.titleMedium.fontWeight
                            )
                        } else {
                            Text(
                                text = ordinal(rank),
                                fontSize = 16.sp,
                                color = Color.White,
                                fontWeight = MaterialTheme.typography.titleMedium.fontWeight
                            )
                            Text(
                                text = stringResource(R.string.total_users_count, totalUsers),
                                fontSize = 16.sp,
                                color = Color.White,
                                fontWeight = MaterialTheme.typography.titleMedium.fontWeight
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(spacing.md))
                Image(
                    painter = painterResource(id = R.drawable.achievement_button),
                    contentDescription = stringResource(R.string.achievements_button_desc),
                    modifier = Modifier
                        .size(spacing.barLg)
                        .clickable { navController.navigate(ACHIEVEMENTS_SCREEN) }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing.workoutRow)
                .padding(horizontal = spacing.md),
            contentAlignment = Alignment.Center
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(PROGRESS_SCREEN) }
                        .padding(bottom = spacing.xs)
                        .semantics { contentDescription = progressEntryLabel },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.progress_home_entry),
                        color = Color(0xFFFFD700),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = ">",
                        color = Color(0xFFFFD700),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (workouts.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_workouts_yet),
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        LazyRow(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(workouts) { workout ->
                                WorkoutCard(
                                    workout = workout,
                                    onClick = {
                                        navController.navigate("workout_resume/${workout.id}")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing.missions)
        ) {
            Image(
                painter = painterResource(id = R.drawable.questloginboard_wider),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.daily_missions_title),
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(spacing.xs))
                dailyMissions.forEach { (mission, reward) ->
                    val isCompleted = completedMissions.contains(mission)
                    val effectiveCompleted = isCompleted || when {
                        mission.startsWith("Walk") -> {
                            val target = mission.split(" ")[1].toLongOrNull() ?: 0
                            todaySteps >= target
                        }
                        mission.startsWith("Complete") -> {
                            val target = mission.split(" ")[1].toIntOrNull() ?: 0
                            todaysWorkouts >= target
                        }
                        else -> false
                    }

                    val progressText = if (effectiveCompleted) {
                        if (mission.startsWith("Walk")) {
                            val target = mission.split(" ")[1].toLongOrNull() ?: 0
                            "$target / $target"
                        } else {
                            val target = mission.split(" ")[1].toIntOrNull() ?: 0
                            "$target / $target"
                        }
                    } else {
                        if (mission.startsWith("Walk")) {
                            val target = mission.split(" ")[1].toLongOrNull() ?: 0
                            "$todaySteps / $target"
                        } else {
                            val target = mission.split(" ")[1].toIntOrNull() ?: 0
                            "$todaysWorkouts / $target"
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = spacing.scale(6)),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = mission,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = progressText,
                            color = if (effectiveCompleted) Color(0xFF4CAF50) else Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (effectiveCompleted) FontWeight.Bold else FontWeight.Normal
                        )

                        val rewardParts = reward.split(":", limit = 2).map { it.trim() }
                        val rewardType = if (rewardParts.size == 2) rewardParts[0].lowercase() else ""
                        val rewardAmount = if (rewardParts.size == 2) rewardParts[1].toIntOrNull() ?: 0 else 0

                        val isCoins = rewardType == "coins"
                        val isExp = rewardType == "exp"

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "+$rewardAmount",
                                color = Color(0xFFFFD700),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.width(spacing.xs))

                            if (isCoins) {
                                Image(
                                    painter = painterResource(id = R.drawable.coin),
                                    contentDescription = stringResource(R.string.coins_reward_desc),
                                    modifier = Modifier.size(spacing.md)
                                )
                            } else if (isExp) {
                                Text(
                                    text = stringResource(R.string.exp_label),
                                    color = Color(0xFFFFD700),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        }

        if (showStreakDialog) {
            WeeklyStreakDialog(
                snapshot = weeklyStreak,
                onDismiss = { showStreakDialog = false },
                onTargetChange = { weeklyStreakViewModel.setTargetSessionsPerWeek(it) }
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

@Composable
private fun stepsSubtitle(
    healthStatus: HealthConnectStatus,
    permissionsGranted: Boolean,
    heartRateBpm: Long?,
): String {
    return when {
        healthStatus == HealthConnectStatus.UNAVAILABLE ->
            stringResource(R.string.health_connect_unavailable_hint)
        healthStatus == HealthConnectStatus.UPDATE_REQUIRED ->
            stringResource(R.string.health_connect_update_hint)
        !permissionsGranted ->
            stringResource(R.string.health_connect_grant_hint)
        heartRateBpm != null && heartRateBpm > 0L ->
            stringResource(R.string.steps_hr_reward_hint, heartRateBpm)
        else ->
            stringResource(R.string.steps_reward_hint)
    }
}

fun ordinal(i: Int): String {
    val suffix = when {
        i % 100 in 11..13 -> "th"
        i % 10 == 1 -> "st"
        i % 10 == 2 -> "nd"
        i % 10 == 3 -> "rd"
        else -> "th"
    }
    return "$i$suffix"
}
