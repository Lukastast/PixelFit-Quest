package com.pixelfitquest.viewmodel

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

    private val _workoutState = MutableStateFlow(WorkoutState())
    val workoutState: StateFlow<WorkoutState> = _workoutState.asStateFlow()
    private val repIntervalMs = 1000L  // For 2-3s rep cycles
    private val lowPassAlpha = 0.9f   // FIXED: Lower to 0.9 for faster adaptation, reduces over-accumulation
    private val minPhaseDisp = 0.05f   // Min |disp| for bottom
    private val hysteresisWindow = 3    // Vel samples
    private val dtHistory: Deque<Float> = ArrayDeque<Float>(3)
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

    // For hysteresis and smoothing
    private val velHistory: Deque<Float> = ArrayDeque<Float>(hysteresisWindow)
    private val accelHistory: Deque<Float> = ArrayDeque<Float>(2)  // FIXED: Smaller window (2) for less lag in phase transitions

    // For rep phases
    private var repStartPos = 0f
    private var bottomPos = 0f
    private var hasBottom = false

    // For averaging and failed reps
    private var totalRepTime: Long = 0L
    private var lastPeakTime = 0L
    private var stabilizationTimeMs = 3000L
    private var failedReps = 0

    fun onSensorDataUpdated(accelerometer: FloatArray?, timestamp: Long) {
        val currentState = _workoutState.value
        if (!currentState.isTracking) return

        val currentTime = System.currentTimeMillis()
        if (currentTime < currentState.workoutStartTime + stabilizationTimeMs) return

        accelerometer?.let { values ->
            // Dt filtering with fallback
            val rawDt = if (lastTimestamp > 0) {
                (timestamp - lastTimestamp) / 1_000_000_000f
            } else 0.02f
            dtHistory.addLast(rawDt)
            if (dtHistory.size > dtWindowSize) {
                dtHistory.removeFirst()
            }
            val dt = if (dtHistory.size < 2) rawDt else dtHistory.average().toFloat()

            lastTimestamp = timestamp
            val accel = floatArrayOf(values[0], values[1], values[2])

            // Update gravity vector
            gravityVector[0] = lowPassAlpha * gravityVector[0] + (1 - lowPassAlpha) * accel[0]
            gravityVector[1] = lowPassAlpha * gravityVector[1] + (1 - lowPassAlpha) * accel[1]
            gravityVector[2] = lowPassAlpha * gravityVector[2] + (1 - lowPassAlpha) * accel[2]

            // Normalize
            val gravityMag = sqrt((gravityVector[0]*gravityVector[0] + gravityVector[1]*gravityVector[1] + gravityVector[2]*gravityVector[2]).toDouble()).toFloat()
            val gravityUnit = if (gravityMag > 0) {
                floatArrayOf(gravityVector[0] / gravityMag, gravityVector[1] / gravityMag, gravityVector[2] / gravityMag)
            } else {
                floatArrayOf(0f, 0f, 1f)
            }

            val upwardUnit = floatArrayOf(-gravityUnit[0], -gravityUnit[1], -gravityUnit[2])

            val netAccel = floatArrayOf(accel[0] - gravityVector[0], accel[1] - gravityVector[1], accel[2] - gravityVector[2])
            val netAccelX = netAccel[0]
            val netAccelY = netAccel[1]
            val netAccelZ = netAccel[2]

            val verticalAccel = netAccel[0] * upwardUnit[0] + netAccel[1] * upwardUnit[1] + netAccel[2] * upwardUnit[2]

            // Smooth accel
            accelHistory.addLast(verticalAccel)
            if (accelHistory.size > 2) {  // FIXED: Size 2 for less lag
                accelHistory.removeFirst()
            }
            val smoothedAccel = if (accelHistory.size < 2) verticalAccel else accelHistory.average().toFloat()

            // Integrate
            lastVerticalAccel = verticalAccel
            val prevVelocity = currentVelocity
            currentDisplacement += currentVelocity * dt + 0.5f * smoothedAccel * dt * dt
            currentVelocity += smoothedAccel * dt

            velHistory.addLast(currentVelocity)
            if (velHistory.size > hysteresisWindow) {
                velHistory.removeFirst()
            }

            // Detect top
            val timeSincePeak = currentTime - lastPeakTime
            val lastVels = velHistory.toList().takeLast(hysteresisWindow.coerceAtMost(velHistory.size))
            val positiveCount = lastVels.count { it >= 0f }
            val positiveHysteresis = velHistory.size >= hysteresisWindow / 2 && positiveCount >= (lastVels.size * 0.6).toInt()
            if (prevVelocity >= 0f && currentVelocity < 0f &&
                positiveHysteresis && timeSincePeak > repIntervalMs) {
                lastPeakTime = currentTime
                if (hasBottom) {
                    val repTime = currentTime - currentState.lastRepTime
                    totalRepTime += repTime
                    val newReps = currentState.reps + 1
                    val downDelta = bottomPos - repStartPos
                    val upDelta = currentDisplacement - bottomPos
                    val downROM = downDelta * 100f
                    val upROM = upDelta * 100f  // FIXED: upDelta
                    val newEstROM = abs(downROM) + upROM  // No offset

                    val newAvg = if (newReps > 0) (totalRepTime / newReps).toFloat() else 0f

                    repStartPos = currentDisplacement
                    bottomPos = currentDisplacement
                    hasBottom = false
                    minPos = currentDisplacement
                    maxPos = currentDisplacement
                    currentVelocity = 0f
                    velHistory.clear()
                    accelHistory.clear()

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
                    failedReps++
                    repStartPos = currentDisplacement
                    bottomPos = currentDisplacement
                    hasBottom = false
                    minPos = currentDisplacement
                    maxPos = currentDisplacement
                    currentVelocity = 0f
                    velHistory.clear()
                    accelHistory.clear()

                    _workoutState.value = currentState.copy(
                        verticalAccel = verticalAccel,
                        xAccel = netAccelX,
                        yAccel = netAccelY,
                        failedReps = currentState.failedReps + 1
                    )
                }
                accelHistory.clear()  // FIXED: Clear at top to avoid carry-over lag
            } else {
                // Detect bottom
                val lastVels = velHistory.toList().takeLast(hysteresisWindow.coerceAtMost(velHistory.size))
                val negativeCount = lastVels.count { it <= 0f }
                val negativeHysteresis = velHistory.size >= hysteresisWindow / 2 && negativeCount >= (lastVels.size * 0.6).toInt()
                if (prevVelocity <= 0f && currentVelocity > 0f &&
                    negativeHysteresis && !hasBottom && abs(currentDisplacement - repStartPos) > minPhaseDisp) {
                    bottomPos = currentDisplacement
                    hasBottom = true
                    accelHistory.clear()  // FIXED: Clear at bottom to prevent negative lag into up
                }

                minPos = minOf(minPos, currentDisplacement)
                maxPos = maxOf(maxPos, currentDisplacement)

                if (abs(currentDisplacement) > 1.5f) {
                    currentDisplacement = 0f
                    currentVelocity *= 0.5f
                    minPos = 0f
                    maxPos = 0f
                    velHistory.clear()
                    accelHistory.clear()
                }

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
        velHistory.clear()
        accelHistory.clear()
        dtHistory.clear()
        lastTimestamp = 0L
        gravityVector = floatArrayOf(0f, 0f, 9.81f)
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
        velHistory.clear()
        accelHistory.clear()
        dtHistory.clear()
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
    val downROM: Float = 0f,
    val upROM: Float = 0f,
    val workoutStartTime: Long = 0L,
    val verticalAccel: Float = 0f,
    val xAccel: Float = 0f,
    val yAccel: Float = 0f,
    val failedReps: Int = 0
)