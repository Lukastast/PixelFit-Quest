package com.PixelFitQuest.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun WorkoutScreen(openScreen: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Workout Tracker",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Track your workouts, monitor repetitions, and improve your technique. Earn coins and XP to level up your avatar!",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Placeholder for workout stats
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text("Current Workout", style = MaterialTheme.typography.titleMedium)
                Text("Reps: 0", style = MaterialTheme.typography.bodyMedium)
                Text("Sets: 0", style = MaterialTheme.typography.bodyMedium)
                Text("Technique Score: N/A", style = MaterialTheme.typography.bodyMedium)
            }
        }

        // Workout controls
        Button(
            onClick = { /* TODO: Start workout tracking */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Start Workout")
        }

        Button(
            onClick = { /* TODO: View workout history */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("View History")
        }
    }
}
