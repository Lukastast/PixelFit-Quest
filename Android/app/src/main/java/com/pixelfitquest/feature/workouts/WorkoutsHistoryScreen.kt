package com.pixelfitquest.feature.workouts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.pixelfitquest.R
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.components.molecules.formatDate
import com.pixelfitquest.feature.workout.model.Workout
import com.pixelfitquest.ui.LockToPortrait
import com.pixelfitquest.ui.theme.spacing

@Composable
fun WorkoutsHistoryScreen(
    onWorkoutClick: (String) -> Unit,
    onStartNewWorkout: () -> Unit,
    viewModel: WorkoutsHistoryViewModel = hiltViewModel(),
) {
    LockToPortrait()

    val workouts by viewModel.workouts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val spacing = MaterialTheme.spacing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.screen, vertical = spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Workout History",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD700),
            modifier = Modifier.padding(vertical = spacing.xs)
        )

        // Start Workout Button
        PixelArtButton(
            onClick = onStartNewWorkout,
            imageRes = R.drawable.button_unclicked,
            pressedRes = R.drawable.button_clicked,
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing.buttonHeight)
        ) {
            Text(
                text = "⚔ Start New Workout",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(spacing.md))

        if (isLoading) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        } else if (workouts.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No workouts yet!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(spacing.xs))
                    Text(
                        text = "Complete your first workout session to earn XP, coins, and level up your dwelling!",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = spacing.lg)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
                contentPadding = PaddingValues(bottom = spacing.lg)
            ) {
                items(workouts) { workout ->
                    WorkoutHistoryRow(
                        workout = workout,
                        onClick = { onWorkoutClick(workout.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutHistoryRow(
    workout: Workout,
    onClick: () -> Unit,
) {
    val spacing = MaterialTheme.spacing

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(spacing.scale(74))
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = R.drawable.info_background_higher),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = workout.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = workout.date.formatDate(),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
            Text(
                text = "View >",
                color = Color(0xFFFFD700),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
