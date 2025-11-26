package com.pixelfitquest.ui.view

import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.pixelfitquest.helpers.HOME_SCREEN
import com.pixelfitquest.R
import com.pixelfitquest.model.WorkoutFeedback
import com.pixelfitquest.model.WorkoutPlan
import com.pixelfitquest.ui.components.CharacterIdleAnimation
import com.pixelfitquest.ui.components.PixelArtButton
import com.pixelfitquest.ui.theme.determination
import com.pixelfitquest.viewmodel.WorkoutViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
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
    var currentFeedback by remember { mutableStateOf<WorkoutFeedback?>(null) }
    val animState = remember { Animatable(0f) }
    var countdownNumber by remember { mutableStateOf<Int?>(null) }

    val currentExercise = plan.items.getOrNull(state.currentExerciseIndex)?.exercise?.name ?: "Unknown"
    val currentSets = plan.items.getOrNull(state.currentExerciseIndex)?.sets ?: 0
    val currentWeight = plan.items.getOrNull(state.currentExerciseIndex)?.weight ?: 0.0

    LaunchedEffect(Unit) {
        viewModel.countdownEvent.collectLatest {
            countdownNumber = 3
            repeat(3) { i ->
                delay(1000L)
                countdownNumber = 3 - i - 1
            }
            countdownNumber = null
            delay(800L)
            countdownNumber = -1
        }
    }

    LaunchedEffect(Unit) {
        viewModel.feedbackEvent.collect { feedback ->
            // If an animation is already running → instantly finish the old one
            if (animState.isRunning) {
                animState.snapTo(1f)      // Force complete instantly
                animState.animateTo(0f)   // Quick fade out
            }

            currentFeedback = feedback
            animState.snapTo(0f)
            animState.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = 500f)
            )
            delay(1200L)
            animState.animateTo(0f)
            currentFeedback = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { workoutId ->
                navController.navigate("workout_resume/$workoutId") {  // FIXED: Direct navController with options
                    popUpTo(HOME_SCREEN) {  // Clear stack up to Home
                        inclusive = false  // Keep Home
                    }
                    launchSingleTop = true  // Avoid duplicates
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

        Row( modifier = Modifier
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            .align(Alignment.TopCenter)
        ) {
            Text(text ="Set: ${state.currentSetNumber} / $currentSets, Reps: ${state.reps}, Weight: $currentWeight Kg",
                modifier.background(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                ))
        }

        Row( modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 32.dp)) {
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
                    .padding(top = 24.dp, start = 16.dp, end = 16.dp)
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

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            countdownNumber?.let { number ->
                val text = if (number >= 0) "${number + 1}" else "GO!"
                val color = if (number >= 0) Color.Yellow else Color.Green

                Text(
                    text = text,
                    fontSize = 120.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontFamily = determination,
                    modifier = Modifier
                        .scale(animState.value * 1.5f)
                        .alpha(animState.value)
                )
            }

            currentFeedback?.let { feedback ->
                Text(
                    text = feedback.text,
                    fontFamily = determination,
                    fontSize = 48.sp,
                    color = feedback.color,
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = animState.value * feedback.scale
                            scaleY = animState.value * feedback.scale
                            alpha = animState.value
                        }
                        .background(feedback.color.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(8.dp)
                )
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

