package com.pixelfitquest.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelfitquest.R
import com.pixelfitquest.model.Exercise
import com.pixelfitquest.model.WorkoutSet
import com.pixelfitquest.viewmodel.WorkoutResumeViewModel

data class ExerciseWithSets(
    val exercise: Exercise,
    val sets: List<WorkoutSet>,
    val avgWorkoutScore: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutResumeScreen(
    openScreen: (String) -> Unit,  // Navigation callback
    viewModel: WorkoutResumeViewModel,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.summary.collectAsState()
    val exercisesWithSets by viewModel.exercisesWithSets.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resume") },
                navigationIcon = {
                    IconButton(onClick = { openScreen("home") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6C7A88)
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .paint(
                    painter = painterResource(id = R.drawable.logsigninbackground),
                    contentScale = ContentScale.Crop
                )
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Total XP and Coins Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF8A9AA8))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Workout Complete!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(start = 8.dp, end = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // XP Value (uncomment icon if available)
                            // Icon(painter = painterResource(id = R.drawable.ic_xp), contentDescription = "XP Earned", modifier = Modifier.size(24.dp))
                            Text(
                                text = "+${summary.totalXp} XP",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Green
                            )
                            // Coins Icon + Value
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.coin),
                                    contentDescription = "Coins Earned",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.Unspecified
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                Text(
                                    text = "+${summary.totalCoins} Coins",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)  // Gold
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.size(16.dp))
            }

            // Per-Exercise Cards
            items(exercisesWithSets) { exerciseWithSets ->
                val exercise = exerciseWithSets.exercise
                val sets = exerciseWithSets.sets

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF8A9AA8))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Exercise Header
                        Text(
                            text = exercise.type.name.replace("_", " "),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "${sets.size} sets completed",
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            sets.forEach { set ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF566474)
                                    )
                                ) {
                                    Column() {

                                        Row(
                                            modifier = Modifier.fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically

                                        ) {
                                            Text(
                                                text = "Set ${set.setNumber}",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium
                                            )

                                        }

                                        Spacer(modifier = Modifier.size(4.dp))

                                        Row(modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text("ROM Score", fontSize = 12.sp, color = Color.Gray)
                                                Text(
                                                    "${set.romScore.toInt()}/100",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = when {
                                                        set.romScore >= 90 -> Color.Green
                                                        set.romScore >= 70 -> Color.Yellow
                                                        else -> Color(0xFFFFA500)
                                                    }
                                                )
                                            }
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text("Set Score", fontSize = 12.sp, color = Color.Gray)
                                                Text(
                                                    text = "${set.workoutScore.toInt()}/100",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when {
                                                        set.workoutScore >= 90 -> Color.Green
                                                        set.workoutScore >= 70 -> Color.Yellow
                                                        set.workoutScore >= 50 -> Color(0xFFFFA500)  // Orange
                                                        else -> Color.Red
                                                    }
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.size(4.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {

                                            Column(
                                                modifier = Modifier.weight(1f),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text("Reps", fontSize = 12.sp, color = Color.Gray)
                                                Text("${set.reps}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                            }

                                            Column(
                                                modifier = Modifier.weight(1f),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text("KG", fontSize = 12.sp, color = Color.Gray)
                                                Text("${set.weight.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                            }
                                        }


                                        Row(
                                            modifier = Modifier.fillMaxWidth()
                                                    .padding(start = 16.dp, end = 16.dp),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {

                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                                                Text("X Tilt Score", fontSize = 12.sp, color = Color.Gray)

                                                Spacer(modifier = Modifier.height(6.dp))

                                                TiltScoreBar(set.xTiltScore)
                                                Text(
                                                    "${set.xTiltScore.toInt()}/100",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color.White
                                                )
                                                Text("Z Tilt Score", fontSize = 12.sp, color = Color.Gray)

                                                Spacer(modifier = Modifier.height(6.dp))

                                                TiltScoreBar(set.zTiltScore)
                                                Text(
                                                    "${set.zTiltScore.toInt()}/100",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))
            }

            // Optional: Empty State
            if (exercisesWithSets.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No exercises completed yet.",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

    }
}

@Composable
fun TiltScoreBar(
    tilt: Float,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(14.dp)
) {
    val clamped = tilt.coerceIn(-100f, 100f)
    val positionFraction = (clamped + 100f) / 200f

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(7.dp))
    ) {
        // Full gradient background bar
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Red,     // -100
                            Color.Green,   // 0
                            Color.Red      // +100
                        )
                    )
                )
        )

        // Calculate marker X position
        val markerX = (constraints.maxWidth * positionFraction).toInt()

        // Marker
        Box(
            modifier = Modifier
                .offset { IntOffset(markerX - 6, 0) } // center the dot
                .size(12.dp)
                .clip(CircleShape)
                .background(Color.White) // marker color (white stands out)
                .border(2.dp, Color.Black.copy(alpha = 0.6f), CircleShape)
        )
    }
}


