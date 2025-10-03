package com.pixelfitquest.viewmodel

import com.pixelfitquest.model.BaseWorkoutState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.ArrayDeque
import java.util.Deque
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.sqrt

@HiltViewModel
class WorkoutViewModel @Inject constructor() : PixelFitViewModel() {

    private val _workoutState = MutableStateFlow(BaseWorkoutState())
    val workoutState: StateFlow<BaseWorkoutState> = _workoutState.asStateFlow()

    private val repIntervalMs = 1000L  // 1s minimum time between reps
    private val lowPassAlpha = 0.8f    // Gravity smoothing

    private val dtHistory: Deque<Float> = ArrayDeque<Float>(3)// Window for averaging (increase size for stronger filtering)

    private val dtWindowSize = 3

    // For per-rep ROM
    private var currentDisplacement = 0f
    private var currentVelocity = 0f
    private var minPos = 0f
    private var maxPos = 0f
    private var lastVerticalAccel = 0f
    private var lastXAccel = 0f
    private var lastYAccel = 0f
    private var gravityVector = floatArrayOf(0f, 0f, 9.81f)
    private var lastTimestamp: Long = 0L

    // For rep phases: Track bottom for completion
    private var repStartPos = 0f
    private var bottomPos = 0f
    private var hasBottom = false

    // For averaging and failed reps
    private var totalRepTime: Long = 0L
    private var lastPeakTime = 0L  // Time of last top (peak)
    private var stabilizationTimeMs = 3000L
    private var failedReps = 0

    fun onSensorDataUpdated(accelerometer: FloatArray?, timestamp: Long) {
        val currentState = _workoutState.value
        if (!currentState.isTracking) return

        val currentTime = System.currentTimeMillis()
        if (currentTime < currentState.workoutStartTime + stabilizationTimeMs) return

        accelerometer?.let { values ->
            // Real delta time
            val rawDt = if (lastTimestamp > 0) {
                (timestamp - lastTimestamp) / 1_000_000_000f
            } else 0.02f
            dtHistory.addLast(rawDt)
            if (dtHistory.size > dtWindowSize) {
                dtHistory.removeFirst()
            }
            val dt = dtHistory.average().toFloat()

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

            // Upward unit = -gravityUnit (opposite to gravity for positive upward)
            val upwardUnit = floatArrayOf(-gravityUnit[0], -gravityUnit[1], -gravityUnit[2])

            // Net acceleration
            val netAccel = floatArrayOf(accel[0] - gravityVector[0], accel[1] - gravityVector[1], accel[2] - gravityVector[2])
            val netAccelX = netAccel[0]
            val netAccelY = netAccel[1]

            // Vertical acceleration: Project net accel onto upward direction (orientation-independent)
            val verticalAccel = netAccel[0] * upwardUnit[0] + netAccel[1] * upwardUnit[1] + netAccel[2] * upwardUnit[2]

            // Always integrate first
            lastVerticalAccel = verticalAccel  // Update early for consistency
            val prevVelocity = currentVelocity
            currentDisplacement += currentVelocity * dt + 0.5f * verticalAccel * dt * dt
            currentVelocity += verticalAccel * dt

            // Detect top (reaching peak: velocity crosses from positive to negative)
            val timeSincePeak = currentTime - lastPeakTime
            if (prevVelocity >= 0f && currentVelocity < 0f &&  // Velocity cross to negative (start down)
                timeSincePeak > repIntervalMs) {
                lastPeakTime = currentTime
                if (hasBottom) {
                    // Rep complete: Reached top after bottom
                    val repTime = currentTime - currentState.lastRepTime
                    totalRepTime += repTime
                    val newReps = currentState.reps + 1
                    val downDelta = bottomPos - repStartPos  // Negative for down
                    val upDelta = currentDisplacement - bottomPos  // Positive for up
                    val downROM = downDelta * 100f  // Negative cm
                    val upROM = upDelta * 100f  // Positive cm
                    val newAvg = if (newReps > 0) (totalRepTime / newReps).toFloat() else 0f
                    val newEstROM = abs(downROM) + upROM

                    // Reset for next rep
                    repStartPos = currentDisplacement
                    bottomPos = currentDisplacement
                    hasBottom = false
                    minPos = currentDisplacement
                    maxPos = currentDisplacement
                    currentVelocity = 0f

                    _workoutState.value = currentState.copy(
                        reps = newReps,
                        lastRepTime = currentTime,
                        avgRepTime = newAvg,
                        estimatedROM = newEstROM,
                        downROM = downROM,
                        upROM = upROM,
                        verticalAccel = verticalAccel,
                        xAccel = netAccelX,
                        yAccel = netAccelY,
                        failedReps = currentState.failedReps
                    )
                } else {
                    // Incomplete: Top reached without bottom (e.g., no full down phase)
                    failedReps++
                    // Reset positions
                    repStartPos = currentDisplacement
                    bottomPos = currentDisplacement
                    hasBottom = false
                    minPos = currentDisplacement
                    maxPos = currentDisplacement
                    currentVelocity = 0f

                    _workoutState.value = currentState.copy(
                        verticalAccel = verticalAccel,
                        xAccel = netAccelX,
                        yAccel = netAccelY,
                        failedReps = currentState.failedReps + 1
                    )
                }
            } else {
                // Detect bottom (velocity crosses from negative to positive)
                if (prevVelocity <= 0f && currentVelocity > 0f && !hasBottom) {
                    bottomPos = currentDisplacement
                    hasBottom = true
                }

                minPos = minOf(minPos, currentDisplacement)
                maxPos = maxOf(maxPos, currentDisplacement)

                // Bound drift
                if (abs(currentDisplacement) > 1.5f) {
                    currentDisplacement = 0f
                    currentVelocity *= 0.5f
                    minPos = 0f
                    maxPos = 0f
                }

                // Update state if changed
                val needsUpdate = abs(verticalAccel - currentState.verticalAccel) > 0.1f ||
                        abs(netAccelX - currentState.xAccel) > 0.1f ||
                        abs(netAccelY - currentState.yAccel) > 0.1f
                if (needsUpdate) {
                    _workoutState.value = currentState.copy(
                        verticalAccel = verticalAccel,
                        xAccel = netAccelX,
                        yAccel = netAccelY
                    )
                }
            }
        }
    }

    fun startWorkout() {
        val startTime = System.currentTimeMillis()
        totalRepTime = 0L
        lastPeakTime = startTime
        lastVerticalAccel = 0f
        lastXAccel = 0f
        lastYAccel = 0f
        currentDisplacement = 0f
        currentVelocity = 0f
        minPos = 0f
        maxPos = 0f
        repStartPos = 0f
        bottomPos = 0f
        hasBottom = false
        failedReps = 0
        lastTimestamp = 0L
        gravityVector = floatArrayOf(0f, 0f, 9.81f)  // Will adapt quickly
        _workoutState.value = _workoutState.value.copy(
            isTracking = true,
            workoutStartTime = startTime,
            lastRepTime = startTime,
            estimatedROM = 0f,
            downROM = 0f,
            upROM = 0f,
            verticalAccel = 0f,
            xAccel = 0f,
            yAccel = 0f,
            failedReps = 0
        )
    }

    fun stopWorkout() {
        _workoutState.value = _workoutState.value.copy(isTracking = false)
    }

    fun resetWorkout() {
        totalRepTime = 0L
        lastPeakTime = 0L
        lastVerticalAccel = 0f
        lastXAccel = 0f
        lastYAccel = 0f
        currentDisplacement = 0f
        currentVelocity = 0f
        minPos = 0f
        maxPos = 0f
        repStartPos = 0f
        bottomPos = 0f
        hasBottom = false
        failedReps = 0
        lastTimestamp = 0L
        gravityVector = floatArrayOf(0f, 0f, 9.81f)
        _workoutState.value = BaseWorkoutState()
    }
}