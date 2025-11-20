package com.pixelfitquest.ui.view

import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.pixelfitquest.Helpers.HOME_SCREEN
import com.pixelfitquest.R
import com.pixelfitquest.model.WorkoutPlan
import com.pixelfitquest.ui.components.CharacterIdleAnimation
import com.pixelfitquest.ui.components.PixelArtButton
import com.pixelfitquest.ui.theme.determination
import com.pixelfitquest.viewmodel.WorkoutViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    plan: WorkoutPlan,  // From nav arg
    templateName: String = "workout",
    openScreen: (String) -> Unit,  // Navigation callback
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val viewModel: WorkoutViewModel = hiltViewModel()
    val state by viewModel.workoutState.collectAsState()
    val context = LocalContext.current
    val activity = LocalActivity.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val characterData by viewModel.characterData.collectAsState()

    val currentExercise = plan.items.getOrNull(state.currentExerciseIndex)?.exercise?.name ?: "Unknown"
    val currentSets = plan.items.getOrNull(state.currentExerciseIndex)?.sets ?: 0
    val currentWeight = plan.items.getOrNull(state.currentExerciseIndex)?.weight ?: 0.0

    LaunchedEffect(state.showFeedback) {
        if (state.showFeedback) {
            delay(1500L)  // Show for 1.5s
            viewModel.hideFeedback()  // Add method to set showFeedback = false
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { workoutId ->
            if (workoutId != null) {
                navController.navigate("workout_resume/$workoutId") {  // FIXED: Direct navController with options
                    popUpTo(HOME_SCREEN) {  // Clear stack up to Home
                        inclusive = false  // Keep Home
                    }
                    launchSingleTop = true  // Avoid duplicates
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (plan.items.isEmpty()) {
            viewModel.setError("No workout plan provided—cannot start tracking")
            openScreen("workout_customization")  // Redirect back
            return@LaunchedEffect
        }
        viewModel.startWorkoutFromPlan(plan, templateName)
    }

    if (plan.items.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: Invalid workout plan. Returning to customization...")
        }
        return
    }

    DisposableEffect(Unit) {
        // Set to landscape
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        onDispose {
            // Revert to portrait (or sensor default) on exit
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    // FIXED: Sensor listener setup
    DisposableEffect(context, lifecycleOwner) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        if (accelerometer == null ) {
            // Handle no sensor (e.g., show error)
            viewModel.setError("No acclerometer sensor found")

            return@DisposableEffect onDispose {}
        }
        else if (gyroscope == null) {
            viewModel.setError("No gyroscope sensor found")

            return@DisposableEffect onDispose {}
        }


        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        val accelData = floatArrayOf(event.values[0], event.values[1], event.values[2])
                        viewModel.onSensorDataUpdated(accelData, event.timestamp)  // Pass gyro as null
                    }
                    Sensor.TYPE_GYROSCOPE -> {
                        val gyroData = floatArrayOf(event.values[0], event.values[1], event.values[2])
                        //viewModel.onGyroDataUpdated(gyroData, event.timestamp)  // FIXED: Separate for gyro
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        coroutineScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
                sensorManager.registerListener(listener, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Background image (fills entire screen)
        Image(
            painter = painterResource(id = R.drawable.gym_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Row( modifier = Modifier.align(Alignment.TopCenter)) {
            // Buttons
            if (state.isSetActive) {
                PixelArtButton(
                    onClick = { viewModel.finishSet() },
                    imageRes = R.drawable.pause_button_unclicked,
                    pressedRes = R.drawable.pause_button_clicked,
                    modifier = Modifier.size(80.dp, 80.dp)
                )
            } else {
                PixelArtButton(
                    onClick = { viewModel.startSet()  },
                    imageRes = R.drawable.play_button_unclicked,
                    pressedRes = R.drawable.play_button_clicked,
                    modifier = Modifier.size(80.dp, 80.dp)
                )
            }

            Box(
                modifier = Modifier
                    .padding(top = 32.dp, start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = currentExercise.replace("_", " "),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,  // Or your theme color for visibility
                    modifier = Modifier
                        .background(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
            PixelArtButton(
                onClick = { openScreen("workout_customization") },
                imageRes = R.drawable.stop_button_unclicked,
                pressedRes = R.drawable.stop_button_clicked,
                modifier = Modifier
                    .size(80.dp, 80.dp)

            )
        }
        Row( modifier = Modifier
            .padding(top = 70.dp, start = 16.dp, end = 16.dp)
            .align(Alignment.TopCenter)
        ) {
            Text(text ="Set: ${state.currentSetNumber} / $currentSets, Reps: ${state.reps}, Weight: $currentWeight Kg",
                modifier.background(
                    color = Color.Black.copy(alpha = 0.7f),
            shape = RoundedCornerShape(8.dp)
            ))
        }

        Box(
            modifier = Modifier.fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {

            CharacterIdleAnimation(
                modifier = Modifier
                    .size(120.dp),
                gender = characterData.gender,
                variant = characterData.variant,
                isAnimating = true
            )
        }

        Box(modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center ) {

            AnimatedVisibility(
                visible = state.showFeedback,
                enter = scaleIn(initialScale = 0f) + fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = scaleOut(targetScale = 0f) + fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            ) {
                if (state.feedback != null) {
                    val feedback = state.feedback!!
                    Log.d("Feedback", "Showing feedback: ${feedback.text}")
                    Text(
                        text = feedback.text,
                        fontSize = 48.sp,  // Large pop-up text
                        fontWeight = FontWeight.Bold,
                        color = feedback.color,
                        fontFamily = determination,
                        modifier = Modifier
                            .scale(feedback.scale)  // Score-based scale (e.g., Perfect bigger)
                            .background(
                                color = feedback.color.copy(alpha = 0.2f),  // Light glow background
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp)
                    )
                }
            }
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterEnd  // Right middle alignment
        ) {
            Card(
                modifier = Modifier
                    .padding(end = 8.dp)  // Right padding to avoid edge
                    .widthIn(max = 150.dp),
                        colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.6f)  // 60% opacity black (adjust 0.0f-1.0f)
                        ),
                shape = RoundedCornerShape(8.dp)  // Rounded like other cards
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),  // Inner padding
                    verticalArrangement = Arrangement.Center
                ) {
                    // FIXED: Access accel/ROM in UI
                    Text("ROM Score: \n${state.romScore.toInt()} / 100 \navg = ${state.avgRomScore.toInt()}")
                    Text("X Tilt Score: \n-100 / ${state.tiltXScore.toInt()} / 100 \navg = ${state.avgTiltXScore.toInt()}")
                    Text("Z Tilt Score: \n-100 / ${state.tiltZScore.toInt()} / 100 \navg = ${state.avgTiltZScore.toInt()}")
                }
            }
        }
    }
    }

