package com.pixelfitquest.ui.view

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.pixelfitquest.R
import com.pixelfitquest.ui.theme.PixelFitQuestTheme
import com.pixelfitquest.viewmodel.HomeViewModel
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    restartApp: (String) -> Unit,
    openScreen: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // UPDATED: Use LocalActivity.current (fixes cast comment; safe and recommended)
    val activity = LocalActivity.current

    LaunchedEffect(Unit) {
        viewModel.initialize(restartApp, activity)  // UPDATED: Pass Activity
    }

    val userGameData by viewModel.userGameData.collectAsState()
    val error by viewModel.error.collectAsState()

    val level = userGameData?.level ?: 0
    val coins = userGameData?.coins ?: 0
    val exp = userGameData?.exp ?: 0
    val streak = userGameData?.streak ?: 0
    val maxExp by viewModel.currentMaxExp.collectAsState()

    // NEW: Collect steps states
    val todaySteps by viewModel.todaySteps.collectAsState()
    val stepGoal by viewModel.stepGoal.collectAsState()

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
            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👟",
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = "${todaySteps} / $stepGoal",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = MaterialTheme.typography.titleMedium.fontWeight
                )
            }
        }

        // Main centered content (buttons only)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 180.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),  // 84 + 80 + 16 = 180.dp for space to buttons
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Temporary test buttons for level up, reset, streak (remove after testing)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = { viewModel.addExp(150) }) {  // Adjust amount to test level up (e.g., enough for 1-2 levels)
                    Text("Test Level Up (+150 XP)")
                }
                Button(onClick = { viewModel.addCoins(100) }) {
                    Text("Add 100 Coins")
                }
                Button(onClick = { viewModel.resetUserData() }) {
                    Text("Reset to Level 1")
                }
                Button(onClick = { viewModel.resetUnlockedVariants() }) {
                    Text("Reset Unlocked Variants")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.incrementStreak() }) {
                        Text("Streak ++")
                    }
                    Button(onClick = { viewModel.resetStreak() }) {
                        Text("Reset Streak")
                    }
                }

                // UPDATED: Refresh button now optional (auto-updates every 30s); keep for manual refresh
                Button(onClick = { viewModel.refreshSteps(activity) }) {  // UPDATED: Pass Activity
                    Text("Refresh Steps Now")
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

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PixelFitQuestTheme {
        HomeScreen(
            restartApp = {},
            openScreen = {}
        )
    }
}