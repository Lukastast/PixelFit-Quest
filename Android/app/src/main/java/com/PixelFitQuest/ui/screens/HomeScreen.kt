package com.PixelFitQuest.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "PixelFit Quest",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "PixelFit Quest is a fitness game that lets you \"level up\" in real life by tracking workouts, reps, and technique using smartphone sensors or smartwatches. Earn coins and XP to unlock retro 8/16-bit avatar outfits and cosmetics. With plans for daily challenges, clan competitions, and global leaderboards, it blends fitness, fun, and social interaction like no other app.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Justify,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(onClick = { /* TODO: Navigate to login or main game screen */ }) {
            Text("Get Started")
        }
    }
}