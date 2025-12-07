package com.pixelfitquest.ui.view

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelfitquest.R
import com.pixelfitquest.model.enums.displayName
import com.pixelfitquest.model.workout.Exercise
import com.pixelfitquest.model.workout.WorkoutSet
import com.pixelfitquest.viewmodel.WorkoutResumeViewModel



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutResumeScreen(
    openScreen: (String) -> Unit,
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
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Session Complete!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "+${summary.totalXp} XP",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Green
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(painterResource(R.drawable.coin), null, Modifier.size(28.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "+${summary.totalCoins} Coins",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Text(
                            text = "Exercise Feedback",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        Spacer(Modifier.height(8.dp))

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(exercisesWithSets) { item ->
                                val exercise = item.exercise
                                val avgRom = item.avgWorkoutScore
                                val avgZTilt = item.sets.map { it.zTiltScore }.average().toInt()
                                val avgXTilt = item.sets.map { it.xTiltScore }.average().toInt()

                                ExerciseMiniFeedback(
                                    exerciseName = exercise.type.displayName(),
                                    avgRomScore = avgRom,
                                    avgZTiltScore = avgZTilt,
                                    avgXTiltScore = avgXTilt
                                )
                            }
                        }
                    }
                }
            }

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
                                    Column {

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
                                                        set.workoutScore >= 50 -> Color(0xFFFFA500)
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

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Red,
                            Color.Green,
                            Color.Red
                        )
                    )
                )
        )

        val markerX = (constraints.maxWidth * positionFraction).toInt()

        Box(
            modifier = Modifier
                .offset { IntOffset(markerX - 6, 0) }
                .size(12.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, Color.Black.copy(alpha = 0.6f), CircleShape)
        )
    }
}

    @Composable
    fun ExerciseMiniFeedback(
        exerciseName: String,
        avgRomScore: Int,
        avgZTiltScore: Int,
        avgXTiltScore: Int
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text(
                text = exerciseName,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                fontSize = 15.sp
            )

            Spacer(Modifier.height(6.dp))

            if (avgRomScore < 90) {
                FeedbackLine(
                    icon = painterResource(R.drawable.ic_pushup_person),
                    text = "Go deeper — full range = max gains!",
                    color = Color(0xFFFF8800)
                )
            } else {
                FeedbackLine(
                    icon = painterResource(R.drawable.ic_check_circle),
                    text = "Perfect range! 🎯",
                    color = Color.Green
                )
            }

            when {
                avgZTiltScore > 10 -> FeedbackLine(
                    icon = painterResource(R.drawable.ic_balance_scale),
                    text = "Pushing more to the right",
                    color = Color(0xFFFF8800)
                )
                avgZTiltScore < -10 -> FeedbackLine(
                    icon = painterResource(R.drawable.ic_balance_scale),
                    text = "Pushing more to the left",
                    color = Color(0xFFFF8800)
                )
                else -> FeedbackLine(
                    icon = painterResource(R.drawable.ic_check_circle),
                    text = "Perfect balance!",
                    color = Color.Green
                )
            }

            if (avgXTiltScore > 15) {
                FeedbackLine(
                    text = "Right side came closer — keep both arms straight up",
                    color = Color(0xFFFF8800)
                )
            } else if (avgXTiltScore < -15) {
                FeedbackLine(
                    text = "Left side came closer — push symmetrically",
                    color = Color(0xFFFF8800)
                )
            }
        }
    }

    @Composable
    fun FeedbackLine(
        icon: Painter? = null,
        text: String,
        color: Color
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon?.let {
                Image(painter = it, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(text, color = color, fontSize = 13.sp)
        }
    }