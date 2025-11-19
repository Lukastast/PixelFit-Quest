package com.pixelfitquest.ui.view

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pixelfitquest.R
import com.pixelfitquest.model.Workout
import com.pixelfitquest.utils.NotificationHelper
import com.pixelfitquest.viewmodel.HomeViewModel
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun HomeScreen(
    restartApp: (String) -> Unit,
    openScreen: (String) -> Unit,  // Updated: Accept optional arg for workout ID
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // UPDATED: Use LocalActivity.current (fixes cast comment; safe and recommended)
    val activity = LocalActivity.current

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.initialize(restartApp, activity)  // UPDATED: Pass Activity
    }

    val userGameData by viewModel.userGameData.collectAsState()
    val error by viewModel.error.collectAsState()

    val workouts by viewModel.workouts.collectAsState()  // NEW: Collect workouts from ViewModel using getWorkouts()

    val level = userGameData?.level ?: 0
    val coins = userGameData?.coins ?: 0
    val exp = userGameData?.exp ?: 0
    val streak = userGameData?.streak ?: 0
    val maxExp by viewModel.currentMaxExp.collectAsState()

    // NEW: Collect steps states
    val todaySteps by viewModel.todaySteps.collectAsState()
    val stepGoal by viewModel.stepGoal.collectAsState()

    // NEW: Collect leaderboard states
    val rank by viewModel.rank.collectAsState()
    val totalUsers by viewModel.totalUsers.collectAsState()

    // NEW: Collect daily missions and completed
    val dailyMissions by viewModel.dailyMissions.collectAsState()
    val completedMissions by viewModel.completedMissions.collectAsState()

    // UPDATED: Removed invalid 'infinite = true' (fixes LaunchedEffect signature / delay errors)
    LaunchedEffect(Unit) {
        while (true) {
            delay(30000L)  // 30 seconds
            viewModel.refreshSteps(activity)
        }
    }

    val displayLevel = if (level >= 30) "Max" else level.toString()

    // Progress index for bar images (0-5, 20% steps)
    val progressIndex = if (maxExp > 0) {
        ((exp.toFloat() / maxExp) * 5f).toInt().coerceIn(0, 5)
    } else {
        0
    }

    // NEW: Calculate today's workouts count for mission progress
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    val today = dateFormat.format(Date())
    val todaysWorkouts = workouts.count { it.date == today }

    Box(modifier = Modifier.fillMaxSize()) {
        // Top stats row with background image
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .height(60.dp)  // Fixed height to fit the background image and content
        ) {
            // Background image behind the stats
            Image(
                painter = painterResource(id = R.drawable.info_background),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Stats row on top of the image
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Coins with icon
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

                // Streak with icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.streak),
                        contentDescription = "Streak icon",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 2.dp))  // Reduced space
                    Text(
                        text = "$streak",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                // Level
                Text(
                    text = "Level: $displayLevel",
                    fontSize = 14.sp,
                    color = Color.White
                )

                // XP bar
                Box(
                    modifier = Modifier.size(width = 80.dp, height = 16.dp),
                    contentAlignment = Alignment.Center  // Centers the entire content (image + text)
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
                        modifier = Modifier.matchParentSize()  // Fills the Box completely
                    )
                }
            }
        }

        // Steps display positioned right under the stats box with small space
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 84.dp, start = 16.dp, end = 16.dp)  // 16 + 60 + 8 = 84.dp for small 8.dp space
                .height(80.dp)
        ) {
            // Background image for step counter
            Image(
                painter = painterResource(id = R.drawable.info_background_higher),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Steps text on top of the image (centered)
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

        // NEW: Leaderboard display positioned under the steps box
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 172.dp, start = 16.dp, end = 16.dp)  // 84 + 80 + 8 = 172.dp for small 8.dp space
                .height(100.dp)
        ) {
            // Background image based on rank (assume you have these drawables: first_place, second_place, etc.)
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

            // Placement text on top (centered)
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

        LazyRow(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(top = 280.dp, start = 16.dp, end = 16.dp),  // Moved below leaderboard (172 + 100 + 8 = 280.dp)
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically

        ) {
            if (workouts.isEmpty()) {
                item {
                    Text(
                        text = "No completed workouts yet",
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(16.dp)

                    )
                }
            } else {
                items(workouts) { workout ->
                    WorkoutCard(
                        workout = workout,
                        onClick = {
                            // FIXED: Navigate to resume screen with workout ID
                            navController.navigate("workout_resume/${workout.id}")
                        }
                    )
                }
            }
        }

        // NEW: Daily missions box
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 400.dp, start = 16.dp, end = 16.dp)  // Adjust position below workouts (280 + 100 + 20 = 400.dp; adjust based on LazyRow height)
                .height(250.dp)  // Increased height for bigger image
        ) {
            // Background image
            Image(
                painter = painterResource(id = R.drawable.questloginboard_wider),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            // Missions content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Daily Missions",
                    fontSize = 18.sp,  // Smaller font size
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                dailyMissions.forEach { (mission, reward) ->
                    val isCompleted = completedMissions.contains(mission)
                    val progress = if (isCompleted) "Completed" else {
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
                            fontSize = 14.sp  // Smaller font size
                        )
                        Text(
                            text = progress,
                            color = if (isCompleted) Color.Green else Color.White,
                            fontSize = 14.sp  // Smaller font size
                        )
                        Text(
                            text = "+$reward",
                            color = Color.White,
                            fontSize = 14.sp  // Smaller font size
                        )
                    }
                }
            }
        }

        // Main centered content (buttons only)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 660.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),  // Adjust top padding to below missions (400 + 250 + 10 = 660.dp)
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Temporary test buttons for level up, reset, streak (remove after testing)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    Button(onClick = { viewModel.addExp(150) }) {
                        Text("Test Level Up (+150 XP)")
                    }
                }
                item {
                    Button(onClick = { viewModel.addCoins(100) }) {
                        Text("Add 100 Coins")
                    }
                }
                item {
                    Button(onClick = { viewModel.resetUserData() }) {
                        Text("Reset to Level 1")
                    }
                }
                item {
                    Button(onClick = { viewModel.resetUnlockedVariants() }) {
                        Text("Reset Unlocked Variants")
                    }
                }
                item {
                    Button(onClick = { viewModel.incrementStreak() }) {
                        Text("Streak ++")
                    }
                }
                item {
                    Button(onClick = { viewModel.resetStreak() }) {
                        Text("Reset Streak")
                    }
                }
                item {
                    Button(onClick = { viewModel.refreshSteps(activity) }) {  // UPDATED: Pass Activity
                        Text("Refresh Steps Now")
                    }
                }
                // NEW: Test buttons for notifications
                item {
                    Button(onClick = { NotificationHelper.showStepGoalCompletedNotification(context) }) {
                        Text("Test Step Goal Completed")
                    }
                }
                item {
                    Button(onClick = { NotificationHelper.showStepGoalReminderNotification(context) }) {
                        Text("Test Step Goal Reminder")
                    }
                }
                item {
                    Button(onClick = { NotificationHelper.showWorkoutCompletedNotification(context) }) {
                        Text("Test Workout Completed")
                    }
                }
                item {
                    Button(onClick = { NotificationHelper.showWorkoutReminderNotification(context) }) {
                        Text("Test Workout Reminder")
                    }
                }
            }

            if (error != null) {
                Text(
                    text = "Error: $error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

// NEW: Helper function for ordinal suffixes (1st, 2nd, etc.)
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
    Box(  // Use Box instead of Card so we can remove rounded corners completely
        modifier = Modifier
            .width(140.dp)
            .height(180.dp)
            .clickable(onClick = onClick)
    ) {
        // Full background image (gray stone texture)
        Image(
            painter = painterResource(id = R.drawable.info_background_even_even_higher),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark semi-transparent overlay for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Initial letter circle
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF6200EE).copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = workout.name.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Workout name
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

            // Date
            Text(
                text = workout.date.formatDate(),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
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

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    //PixelFitQuestTheme {
    //   HomeScreen(
    //        restartApp = {},
    //       openScreen = {},
    //  }
}