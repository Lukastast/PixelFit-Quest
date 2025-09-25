package com.PixelFitQuest.ui.view

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.PixelFitQuest.viewmodel.WorkoutViewModel
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    openScreen: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: WorkoutViewModel = hiltViewModel()
    val workoutState by viewModel.workoutState.collectAsState()

    // Check accelerometer availability only
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    val accelerometerData = remember { mutableStateOf(FloatArray(3)) }

    // Sensor listener (accelerometer only)
    val sensorListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                        accelerometerData.value = it.values.clone()
                        viewModel.onSensorDataUpdated(it.values.clone(), it.timestamp)  // Fixed: Pass timestamp
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Handle if needed
            }
        }
    }

    // Register/unregister accelerometer only based on tracking state
    LaunchedEffect(workoutState.isTracking) {
        if (workoutState.isTracking) {
            viewModel.startWorkout()
            sensorManager.registerListener(
                sensorListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME // ~20ms for real-time
            )
        } else {
            sensorManager.unregisterListener(sensorListener)
            viewModel.stopWorkout()
        }
    }

    // Dispose on exit
    DisposableEffect(Unit) {
        onDispose {
            sensorManager.unregisterListener(sensorListener)
            viewModel.resetWorkout()
        }
    }

    // UI
    if (accelerometer == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Accelerometer not available on this device.",
                color = MaterialTheme.colorScheme.error
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Workout Tracker (Orientation-Robust)",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.startWorkout() },
                enabled = !workoutState.isTracking
            ) {
                Text("Start Tracking")
            }
            Button(
                onClick = { viewModel.stopWorkout() },
                enabled = workoutState.isTracking
            ) {
                Text("Stop")
            }
            Button(
                onClick = { viewModel.resetWorkout() }
            ) {
                Text("Reset")
            }
        }

        // Metrics Display
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Reps: ${workoutState.reps}", style = MaterialTheme.typography.headlineSmall)
                Text("Avg Rep Time: ${String.format("%.1f", workoutState.avgRepTime / 1000f)}s")
                Text("Estimated ROM: ${String.format("%.1f", workoutState.estimatedROM)} cm")
                // Vertical accel (downward negative; orientation-independent)
                Text(
                    text = "Vertical Accel: ${String.format("%.2f", workoutState.verticalAccel)} m/s²",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (workoutState.verticalAccel < -1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                // Optional tilt warning (based on gravity mag deviation)
                val gravityMag = sqrt((workoutState.verticalAccel + 9.81f).pow(2).toDouble()).toFloat()  // Approx
                if (abs(gravityMag - 9.81f) > 2f) {
                    Text(
                        "Tilt detected: Hold steadier for accuracy",
                    )
                }
                if (workoutState.isTracking) {
                    Text("Tracking Active", color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Navigation
        Button(onClick = { openScreen("home") }) {
            Text("Back to Home")
        }
    }
}