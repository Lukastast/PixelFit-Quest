package com.PixelFitQuest.viewmodel


import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.sqrt

@HiltViewModel
class WorkoutViewModel @Inject constructor() : PixelFitViewModel() {

    private val _workoutState = MutableStateFlow(WorkoutState())
    val workoutState: StateFlow<WorkoutState> = _workoutState.asStateFlow()

    private val downThreshold = -1.5f  // Negative for downward motion
    private val repIntervalMs = 1500L  // For 2-3s rep cycles
    private val lowPassAlpha = 0.8f    // Gravity smoothing

    // For per-rep ROM
    private var currentDisplacement = 0f
    private var currentVelocity = 0f
    private var minPos = 0f
    private var maxPos = 0f
    private var lastVerticalAccel = 0f
    private var gravityVector = floatArrayOf(0f, 0f, 9.81f)
    private var lastTimestamp: Long = 0L

    // For averaging
    private var totalRepTime: Long = 0L
    private var lastPeakTime = 0L
    private var stabilizationTimeMs = 3000L

    fun onSensorDataUpdated(accelerometer: FloatArray?, timestamp: Long) {
        val currentState = _workoutState.value
        if (!currentState.isTracking) return

        val currentTime = System.currentTimeMillis()
        if (currentTime < currentState.workoutStartTime + stabilizationTimeMs) return

        accelerometer?.let { values ->
            // Real delta time
            val dt = if (lastTimestamp > 0) {
                (timestamp - lastTimestamp) / 1_000_000_000f
            } else 0.02f
            lastTimestamp = timestamp

            val accel = floatArrayOf(values[0], values[1], values[2])

            // Update gravity vector
            gravityVector[0] = lowPassAlpha * gravityVector[0] + (1 - lowPassAlpha) * accel[0]
            gravityVector[1] = lowPassAlpha * gravityVector[1] + (1 - lowPassAlpha) * accel[1]
            gravityVector[2] = lowPassAlpha * gravityVector[2] + (1 - lowPassAlpha) * accel[2]

            // Normalize to unit vector (downward gravity direction)
            val gravityMag = sqrt((gravityVector[0]*gravityVector[0] + gravityVector[1]*gravityVector[1] + gravityVector[2]*gravityVector[2]).toDouble()).toFloat()
            val gravityUnit = if (gravityMag > 0) {
                floatArrayOf(gravityVector[0] / gravityMag, gravityVector[1] / gravityMag, gravityVector[2] / gravityMag)
            } else {
                floatArrayOf(0f, 0f, 1f)
            }

            // FIXED: Upward unit = -gravityUnit (opposite to gravity for positive upward)
            val upwardUnit = floatArrayOf(-gravityUnit[0], -gravityUnit[1], -gravityUnit[2])

            // Vertical acceleration: Project net accel onto upward direction
            val netAccel = floatArrayOf(accel[0] - gravityVector[0], accel[1] - gravityVector[1], accel[2] - gravityVector[2])
            val verticalAccel = netAccel[0] * upwardUnit[0] + netAccel[1] * upwardUnit[1] + netAccel[2] * upwardUnit[2]

            // Rep detection: Downward start
            val timeSincePeak = currentTime - lastPeakTime
            if (verticalAccel < downThreshold &&
                lastVerticalAccel >= 0f &&
                timeSincePeak > repIntervalMs) {
                val repTime = currentTime - currentState.lastRepTime
                val newReps = currentState.reps + 1
                lastPeakTime = currentTime
                totalRepTime += repTime

                val repROM = (maxPos - minPos) * 100f  // To cm
                minPos = currentDisplacement
                maxPos = currentDisplacement
                currentVelocity = 0f  // Reset to curb drift

                _workoutState.value = currentState.copy(
                    reps = newReps,
                    lastRepTime = currentTime,
                    avgRepTime = if (newReps > 0) (totalRepTime / newReps).toFloat() else 0f,
                    estimatedROM = repROM,
                    verticalAccel = verticalAccel
                )
            } else {
                lastVerticalAccel = verticalAccel

                // Improved integration
                currentVelocity += verticalAccel * dt
                currentDisplacement += currentVelocity * dt + 0.5f * verticalAccel * dt * dt

                minPos = minOf(minPos, currentDisplacement)
                maxPos = maxOf(maxPos, currentDisplacement)

                // Bound drift
                if (abs(currentDisplacement) > 1.5f) {
                    currentDisplacement = 0f
                    currentVelocity *= 0.5f
                }

                if (abs(verticalAccel - currentState.verticalAccel) > 0.1f) {
                    _workoutState.value = currentState.copy(verticalAccel = verticalAccel)
                }
            }
        }
    }

    fun startWorkout() {
        val startTime = System.currentTimeMillis()
        totalRepTime = 0L
        lastPeakTime = startTime
        lastVerticalAccel = 0f
        currentDisplacement = 0f
        currentVelocity = 0f
        minPos = 0f
        maxPos = 0f
        lastTimestamp = 0L
        gravityVector = floatArrayOf(0f, 0f, 9.81f)  // Will adapt quickly
        _workoutState.value = _workoutState.value.copy(
            isTracking = true,
            workoutStartTime = startTime,
            lastRepTime = startTime,
            estimatedROM = 0f,
            verticalAccel = 0f
        )
    }

    fun stopWorkout() {
        _workoutState.value = _workoutState.value.copy(isTracking = false)
    }

    fun resetWorkout() {
        totalRepTime = 0L
        lastPeakTime = 0L
        lastVerticalAccel = 0f
        currentDisplacement = 0f
        currentVelocity = 0f
        minPos = 0f
        maxPos = 0f
        lastTimestamp = 0L
        gravityVector = floatArrayOf(0f, 0f, 9.81f)
        _workoutState.value = WorkoutState()
    }
}

data class WorkoutState(
    val isTracking: Boolean = false,
    val reps: Int = 0,
    val lastRepTime: Long = 0L,
    val avgRepTime: Float = 0f,
    val estimatedROM: Float = 0f,
    val workoutStartTime: Long = 0L,
    val verticalAccel: Float = 0f
)