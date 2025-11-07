package com.pixelfitquest.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.pixelfitquest.R
import com.pixelfitquest.ui.theme.PixelFitQuestTheme
import com.pixelfitquest.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    restartApp: (String) -> Unit,
    openScreen: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { viewModel.initialize(restartApp) }

    val userGameData by viewModel.userGameData.collectAsState()
    val error by viewModel.error.collectAsState()

    val level = userGameData?.level ?: 0
    val coins = userGameData?.coins ?: 0
    val exp = userGameData?.exp ?: 0
    val streak = userGameData?.streak ?: 0
    val maxExp by viewModel.currentMaxExp.collectAsState()

    val displayLevel = if (level >= 30) "Max" else level.toString()

    // Progress index for bar images (0-5, 20% steps)
    val progressIndex = if (maxExp > 0) {
        ((exp.toFloat() / maxExp) * 5f).toInt().coerceIn(0, 5)
    } else {
        0
    }

    // Exact % for overlay label (aligns bar visually with true progress)
    val exactProgressPercent = if (maxExp > 0) {
        ((exp.toFloat() / maxExp) * 100f).toInt()
    } else {
        0
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Top stats row
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
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
                    color = MaterialTheme.colorScheme.primary
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
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Level
            Text(
                text = "Level: $displayLevel",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )

            // XP bar with % text overlaid
            Box(
                modifier = Modifier.size(width = 80.dp, height = 16.dp),
                contentAlignment = Alignment.Center  // Centers text over image
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
                    modifier = Modifier.matchParentSize()  // Fills the Box
                )
            }
        }

        // Main centered content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PixelFit \n\nQuest",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "PixelFit Quest is a fitness game that lets you \"level up\" in real life by tracking workouts, reps, and technique using smartphone sensors or smartwatches. Earn coins and XP to unlock retro 8/16-bit avatar outfits and cosmetics. With plans for daily challenges, clan competitions, and global leaderboards, it blends fitness, fun, and social interaction like no other app.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Justify,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            if (error != null) {
                Text(
                    text = "Error: $error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
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