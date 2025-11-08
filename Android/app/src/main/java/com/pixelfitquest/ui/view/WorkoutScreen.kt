package com.pixelfitquest.ui.view

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.pixelfitquest.model.WorkoutPlan
import com.pixelfitquest.viewmodel.WorkoutViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    plan: WorkoutPlan,  // From nav arg
    templateName: String = "workout",
    openScreen: (String) -> Unit,  // Navigation callback
    modifier: Modifier = Modifier
) {
    val viewModel: WorkoutViewModel = hiltViewModel()
    val state by viewModel.workoutState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

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

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val currentExercise = plan.items.getOrNull(state.currentExerciseIndex)?.exercise?.name ?: "Unknown"
        val currentSets = plan.items.getOrNull(state.currentExerciseIndex)?.sets ?: 0
        val currentWeight = plan.items.getOrNull(state.currentExerciseIndex)?.weight ?: 0.0

        Text("Current Exercise: $currentExercise")
        Text("Set: ${state.currentSetNumber} / $currentSets")
        Text("Reps: ${state.reps}")
        Text("Weight: $currentWeight Kg")

        // FIXED: Access accel/ROM in UI
        Text("Vertical Accel: %.2f".format(state.verticalAccel))
        Text("ROM Score: ${state.romScore.toInt()} / 100")
        Text("Avg ROM Score: ${state.avgRomScore.toInt()} / 100")
        Text("X Tilt Score: -100 / ${state.tiltXScore.toInt()} / 100")
        Text("Z Tilt Score: -100 / ${state.tiltZScore.toInt()} / 100")
        Text("avg X Tilt Score: -100 / ${state.avgTiltXScore.toInt()} / 100")
        Text(" avg Z Tilt Score: -100 / ${state.avgTiltZScore.toInt()} / 100")

        // Buttons
        if (state.isSetActive) {
            Button(onClick = { viewModel.finishSet() }) {
                Text("Finish Set")
            }
        } else {
            Button(onClick = { viewModel.startSet() }) {
                Text("Start Set")
            }
        }
        if (currentSets == state.currentSetNumber - 1) {
            Button(onClick = { viewModel.finishExercise() }) { Text("Finish Exercise") }
        }

        Button(onClick = { openScreen("workout_customization") }) {
            Text("Back")
        }
    }
}