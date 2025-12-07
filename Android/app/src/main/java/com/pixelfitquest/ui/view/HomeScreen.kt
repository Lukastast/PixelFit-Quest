package com.pixelfitquest.ui.view

import android.util.Log
import androidx.activity.compose.LocalActivity
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.pixelfitquest.R
import com.pixelfitquest.model.workout.Workout
import com.pixelfitquest.viewmodel.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    onScreenReady: () -> Unit = {}
) {
    val activity = LocalActivity.current
    val context = LocalContext.current


    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        Log.d("HomeScreen", "Initializing HomeScreen")
        viewModel.initialize(restartApp, activity)
    }

    val userData by viewModel.userData.collectAsState()
    val workouts by viewModel.workouts.collectAsState()


    val prefs = remember { context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE) }
    var showTutorial by remember { mutableStateOf(false) }


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
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading...", color = Color.White, fontSize = 16.sp)
            }
        }
        return
    }

    val level = userData?.level ?: 0
    val coins = userData?.coins ?: 0
    val exp = userData?.exp ?: 0
    val streak = userData?.streak ?: 0
    val maxExp by viewModel.currentMaxExp.collectAsState()
    val todaySteps by viewModel.todaySteps.collectAsState()
    val stepGoal by viewModel.stepGoal.collectAsState()
    val rank by viewModel.rank.collectAsState()
    val totalUsers by viewModel.totalUsers.collectAsState()
    val dailyMissions by viewModel.dailyMissions.collectAsState()
    val completedMissions by viewModel.completedMissions.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showAchievements by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(30000L)
            viewModel.refreshSteps(activity)
        }
    }

    val displayLevel = if (level >= 30) "Max" else level.toString()
    val progressIndex = if (maxExp > 0) {
        ((exp.toFloat() / maxExp) * 5f).toInt().coerceIn(0, 5)
    } else {
        0
    }

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

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .height(60.dp)
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
                        contentDescription = "Coin icon",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = "Coins: $coins",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.streak),
                        contentDescription = "Streak icon",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                    Text(
                        text = "$streak",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
                Text(
                    text = "Level: $displayLevel",
                    fontSize = 14.sp,
                    color = Color.White
                )
                Box(
                    modifier = Modifier.size(width = 80.dp, height = 16.dp),
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
                        contentDescription = "XP bar",
                        modifier = Modifier.matchParentSize()
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 84.dp, start = 16.dp, end = 16.dp)
                .height(80.dp)
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
                    text = "Steps",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = MaterialTheme.typography.titleMedium.fontWeight
                )
                Text(
                    text = "$todaySteps / $stepGoal",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = MaterialTheme.typography.titleMedium.fontWeight
                )
                Text(
                    text = "+50 EXP, +10 Coins",
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 172.dp, start = 16.dp, end = 16.dp)
                .height(100.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(150.dp)
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
                        Text(
                            text = ordinal(rank),
                            fontSize = 16.sp,
                            color = Color.White,
                            fontWeight = MaterialTheme.typography.titleMedium.fontWeight
                        )
                        Text(
                            text = "/ $totalUsers users",
                            fontSize = 16.sp,
                            color = Color.White,
                            fontWeight = MaterialTheme.typography.titleMedium.fontWeight
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Image(
                    painter = painterResource(id = R.drawable.achievement_button),
                    contentDescription = "Achievements",
                    modifier = Modifier
                        .size(100.dp)
                        .clickable { showAchievements = true }
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 280.dp, start = 16.dp, end = 16.dp)
                .height(140.dp),
            contentAlignment = Alignment.Center
        ) {
            if (workouts.isEmpty()) {
                Text(
                    text = "No completed workouts yet",
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
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

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 428.dp, start = 16.dp, end = 16.dp)
                .height(250.dp)
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Daily Missions",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                dailyMissions.forEach { (mission, reward) ->
                    val isCompleted = completedMissions.contains(mission)
                    val effectiveCompleted = isCompleted || (when {
                        mission.startsWith("Walk") -> {
                            val target = mission.split(" ")[1].toLongOrNull() ?: 0
                            todaySteps >= target
                        }
                        mission.startsWith("Complete") -> {
                            val target = mission.split(" ")[1].toIntOrNull() ?: 0
                            todaysWorkouts >= target
                        }
                        else -> false
                    })
                    val progress = if (effectiveCompleted) "Completed" else {
                        if (mission.startsWith("Walk")) {
                            val target = mission.split(" ")[1].toLongOrNull() ?: 0
                            "$todaySteps / $target"
                        } else if (mission.startsWith("Complete")) {
                            val target = mission.split(" ")[1].toIntOrNull() ?: 0
                            "$todaysWorkouts / $target"
                        } else ""
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = mission,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = progress,
                            color = if (effectiveCompleted) Color.Green else Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "+$reward",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        if (showAchievements) {
            Dialog(onDismissRequest = { showAchievements = false }) {
                Box(
                    modifier = Modifier
                        .width(400.dp)
                        .height(200.dp)
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
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Achievements",
                            fontSize = 18.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            achievements.forEach { (ach, isUnlocked) ->
                                val icon = if (isUnlocked) ach.unlockedIcon else ach.lockedIcon
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Image(
                                        painter = painterResource(id = icon),
                                        contentDescription = ach.name,
                                        modifier = Modifier
                                            .size(80.dp)
                                            .padding(8.dp)
                                            .clickable {
                                                coroutineScope.launch {
                                                    snackbarHostState.currentSnackbarData?.dismiss()
                                                    snackbarHostState.showSnackbar(ach.description)
                                                }
                                            }
                                    )
                                    Text(
                                        text = ach.name,
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
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

@Composable
fun WorkoutCard(
    workout: Workout,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(id = R.drawable.fourth_and_more),
            contentDescription = null,
            modifier = Modifier.size(120.dp, 140.dp),
            contentScale = ContentScale.Fit
        )
        Column(
            modifier = Modifier
                .matchParentSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = workout.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = workout.date.formatDate(),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp
            )
        }
    }
}

private val dateFormatter = DateTimeFormatter.ofPattern("d MMM, yyyy")
    .withZone(ZoneId.systemDefault())

fun String.formatDate(): String {
    return try {
        val instant = Instant.parse(this)
        dateFormatter.format(instant)
    } catch (e: Exception) {
        "Unknown date"
    }
}