package com.pixelfitquest.ui.view

import com.pixelfitquest.ui.components.AutoSizeText
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixelfitquest.ui.theme.typography
import com.pixelfitquest.viewmodel.WorkoutViewModel

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
        AutoSizeText(
            text = "Workout Tracker",
            style = typography.titleLarge.copy(color = MaterialTheme.colorScheme.primary),
            modifier = Modifier.padding(bottom = 8.dp),
            maxFontSize = 50.sp,
            minFontSize = 30.sp
        )

        // Controls
        Button(
            onClick = { viewModel.startWorkout() },
            enabled = !workoutState.isTracking
        ) {
            Text("Start Tracking")
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

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
                // Use the actual gravity magnitude from the state, not verticalAccel + 9.81f
                //if (abs(workoutState.gravityMag - 9.81f) > 2f) {
                //                    Text(
                //                        "Tilt detected: Hold steadier for accuracy",
                //                    )
                //                }
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