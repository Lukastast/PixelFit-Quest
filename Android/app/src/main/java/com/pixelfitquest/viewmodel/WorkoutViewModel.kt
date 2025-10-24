package com.pixelfitquest.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.pixelfitquest.model.Exercise
import com.pixelfitquest.model.UserSettings
import com.pixelfitquest.model.Workout
import com.pixelfitquest.model.WorkoutPlan
import com.pixelfitquest.model.WorkoutSet
import com.pixelfitquest.model.ExerciseType
import com.pixelfitquest.repository.UserSettingsRepository
import com.pixelfitquest.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.ArrayDeque
import java.util.Deque
import java.util.UUID
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.sqrt

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    private val workoutRepository: WorkoutRepository
) : PixelFitViewModel() {  // Assume PixelFitViewModel provides launchCatching if needed

    private val _workoutState = MutableStateFlow(WorkoutState())
    val workoutState: StateFlow<WorkoutState> = _workoutState.asStateFlow()

    private val _userSettings = MutableStateFlow<UserSettings?>(null)
    val userSettings: StateFlow<UserSettings?> = _userSettings.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val repIntervalMs = 1000L  // For 2-3s rep cycles
    private val lowPassAlpha = 0.9f
    private val minPhaseDisp = 0.05f
    private val hysteresisWindow = 3
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
    private val accelHistory: Deque<Float> = ArrayDeque<Float>(2)

    // For rep phases
    private var repStartPos = 0f
    private var bottomPos = 0f
    private var hasBottom = false

    // For averaging and failed reps
    private var totalRepTime: Long = 0L
    private var lastPeakTime = 0L
    private var stabilizationTimeMs = 3000L
    private var failedReps = 0

    // Plan-based tracking
    private var currentPlan: WorkoutPlan? = null
    private var currentExerciseIndex = 0
    private var currentSetNumber = 1
    private var isSetActive = false
    private var currentExerciseType: ExerciseType? = null
    private var workoutId: String = ""

    init {
        launchCatching {
            loadUserData()
        }
    }

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
            val gravityMag = sqrt((gravityVector[0] * gravityVector[0] + gravityVector[1] * gravityVector[1] + gravityVector[2] * gravityVector[2]).toDouble()).toFloat()
            val gravityUnit = if (gravityMag > 0) {
                floatArrayOf(
                    gravityVector[0] / gravityMag,
                    gravityVector[1] / gravityMag,
                    gravityVector[2] / gravityMag
                )
            } else {
                floatArrayOf(0f, 0f, 1f)
            }

            val upwardUnit = floatArrayOf(-gravityUnit[0], -gravityUnit[1], -gravityUnit[2])

            val netAccel = floatArrayOf(
                accel[0] - gravityVector[0],
                accel[1] - gravityVector[1],
                accel[2] - gravityVector[2]
            )
            val netAccelX = netAccel[0]
            val netAccelY = netAccel[1]

            val verticalAccel = netAccel[0] * upwardUnit[0] + netAccel[1] * upwardUnit[1] + netAccel[2] * upwardUnit[2]

            // Smooth accel
            accelHistory.addLast(verticalAccel)
            if (accelHistory.size > 2) {
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
                positiveHysteresis && timeSincePeak > repIntervalMs
            ) {
                lastPeakTime = currentTime
                if (hasBottom) {
                    onRepCompleted()
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
                accelHistory.clear()
            } else {
                // Detect bottom
                val lastVels = velHistory.toList().takeLast(hysteresisWindow.coerceAtMost(velHistory.size))
                val negativeCount = lastVels.count { it <= 0f }
                val negativeHysteresis = velHistory.size >= hysteresisWindow / 2 && negativeCount >= (lastVels.size * 0.6).toInt()
                if (prevVelocity <= 0f && currentVelocity > 0f &&
                    negativeHysteresis && !hasBottom && abs(currentDisplacement - repStartPos) > minPhaseDisp
                ) {
                    bottomPos = currentDisplacement
                    hasBottom = true
                    accelHistory.clear()
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

    fun startWorkoutFromPlan(plan: WorkoutPlan) {
        currentPlan = plan
        currentExerciseIndex = 0
        currentSetNumber = 1
        isSetActive = false
        currentExerciseType = plan.items[0].exercise
        val startTime = System.currentTimeMillis()
        workoutId = "workout_${System.currentTimeMillis()}_${UUID.randomUUID()}"
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
            failedReps = 0,
            isSetActive = false,
            currentSetNumber = 1,
            totalSets = plan.items.sumOf { it.sets },
            currentExerciseIndex = 0
        )
    }

    fun startSet() {
        if (isSetActive) return
        isSetActive = true
        _workoutState.value = _workoutState.value.copy(isSetActive = true, reps = 0)
        Log.d("WorkoutVM", "Started set $currentSetNumber")
    }

    fun finishSet() {
        if (!isSetActive) return
        isSetActive = false
        _workoutState.value = _workoutState.value.copy(isSetActive = false)

        saveCurrentSetAsWorkout()

        val currentPlan = currentPlan ?: return
        val currentItem = currentPlan.items.getOrNull(currentExerciseIndex) ?: return
        currentSetNumber++
        if (currentSetNumber > currentItem.sets) {
            currentExerciseIndex++
            currentSetNumber = 1
            if (currentExerciseIndex >= currentPlan.items.size) {
                Log.d("WorkoutVM", "Workout complete!")
                stopWorkout()
                return
            }
            currentExerciseType = currentPlan.items[currentExerciseIndex].exercise
        }
        _workoutState.value = _workoutState.value.copy(
            currentSetNumber = currentSetNumber,
            currentExerciseIndex = currentExerciseIndex  // FIXED: Sync
        )
        Log.d("WorkoutVM", "Finished set, advanced to $currentSetNumber")
    }
    fun finishExercise() {  // Call when all sets for exercise done
        // Aggregate exercise score from sets (query if needed, or track in VM)
        val exerciseScore = _workoutState.value.romScore  // Or compute
        val exercise = Exercise(
            id = "exercise_${currentExerciseIndex}_${System.currentTimeMillis()}",
            workoutId = workoutId,
            type = currentExerciseType!!,
            totalSets = currentPlan!!.items[currentExerciseIndex].sets,
            exerciseScore = exerciseScore,
            weight = _workoutState.value.weight,
            notes = _workoutState.value.notes
        )
        viewModelScope.launch {
            workoutRepository.saveExercise(exercise)
        }
        // Advance to next exercise
        currentExerciseIndex++
        currentSetNumber = 1
        if (currentExerciseIndex >= currentPlan!!.items.size) {
            finishWorkout()
        } else {
            currentExerciseType = currentPlan!!.items[currentExerciseIndex].exercise
            _workoutState.value = _workoutState.value.copy(currentExerciseIndex = currentExerciseIndex)
        }
    }

    fun finishWorkout() {  // Call when all exercises done
        val workout = Workout(
            id = workoutId,
            date = Instant.now().toString(),
            name = "Workout Session",  // From UI or plan
            totalExercises = currentPlan!!.items.size,
            totalSets = currentPlan!!.items.sumOf { it.sets },
            overallScore = _workoutState.value.avgRomScore,
            notes = null
        )
        viewModelScope.launch {
            workoutRepository.saveWorkout(workout)
            stopWorkout()
        }
    }
    private fun saveCurrentSetAsWorkout() {
        val currentState = _workoutState.value
        val currentPlan = currentPlan ?: return
        val currentType = currentExerciseType ?: return
        val exerciseId = "exercise_${currentExerciseIndex}_${System.currentTimeMillis()}"  // Or generate properly
        val setId = "set_${currentSetNumber}_${System.currentTimeMillis()}"

        val set = WorkoutSet(
            id = setId,
            exerciseId = exerciseId,
            setNumber = currentSetNumber,
            reps = currentState.reps,
            romScore = currentState.romScore,
            avgRepTime = currentState.avgRepTime,
            verticalAccel = currentState.verticalAccel,  // Or avg over set
            weight = currentState.weight,  // From UI
            notes = currentState.notes
        )
        viewModelScope.launch {
            workoutRepository.saveSet(set)
        }
    }



    fun stopWorkout() {
        isSetActive = false
        _workoutState.value = _workoutState.value.copy(isTracking = false, isSetActive = false)
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                userSettingsRepository.getUserSettings().collect { data ->
                    _userSettings.value = data
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load user data"
            }
        }
    }

    fun calculateRomScore(estimatedRomCm: Float): Float {
        val currentType = currentExerciseType ?: ExerciseType.BENCH_PRESS
        val heightCm = _userSettings.value?.height ?: return 0f
        val romFactor = currentType.romFactor
        val theoreticalMaxRom = heightCm * romFactor
        val score = (estimatedRomCm / theoreticalMaxRom * 100f).coerceIn(0f, 100f)
        return score
    }

    fun setError(message: String?) {
        _error.value = message
    }
    private fun onRepCompleted() {
        val currentState = _workoutState.value
        val currentTime = System.currentTimeMillis()
        val repTime = currentTime - currentState.lastRepTime
        totalRepTime += repTime
        val newReps = currentState.reps + 1

        // FIXED: ROM calculation (from your sensor logic)
        val downDelta = bottomPos - repStartPos
        val upDelta = currentDisplacement - bottomPos
        val downROM = downDelta * 100f
        val upROM = upDelta * 100f
        val newEstROM = abs(downROM) + upROM

        val newAvgRepTime = if (newReps > 0) (totalRepTime / newReps).toFloat() else 0f

        // FIXED: Score calculation (use currentWorkoutType)
        val lastRepScore = calculateRomScore(newEstROM)
        val newTotalRom = currentState.totalRom + lastRepScore
        val avgRomScore = if (newReps > 0) (newTotalRom / newReps.toFloat()) else 0f

        // FIXED: Reset for next rep
        repStartPos = currentDisplacement
        bottomPos = currentDisplacement
        hasBottom = false
        minPos = currentDisplacement
        maxPos = currentDisplacement
        currentVelocity = 0f
        velHistory.clear()
        accelHistory.clear()

        // FIXED: Update state
        _workoutState.value = currentState.copy(
            reps = newReps,
            lastRepTime = currentTime,
            avgRepTime = newAvgRepTime,
            estimatedROM = newEstROM,
            downROM = downROM,
            upROM = upROM,
            verticalAccel = lastVerticalAccel,  // From sensor
            xAccel = currentState.xAccel,  // Preserve
            yAccel = currentState.yAccel,  // Preserve
            romScore = lastRepScore,
            totalRom = newTotalRom,
            avgRomScore = avgRomScore,
            failedReps = currentState.failedReps  // Or increment if failed
        )

        Log.d("WorkoutVM", "Rep completed: $newReps reps, ROM: $newEstROM cm")


        val currentPlan = currentPlan ?: return
        val currentItem = currentPlan.items.getOrNull(currentExerciseIndex) ?: return
        if (newReps >= 10) {  // Or use a per-exercise rep goal
            finishSet()
        }
    }

    data class WorkoutState(
        val isTracking: Boolean = false,
        val userHeightCm: Float? = null,
        val theoreticalMaxRomCm: Float? = null,
        val romScore: Float = 0f,
        val avgRomScore: Float = 0f,
        val reps: Int = 0,
        val lastRepTime: Long = 0L,
        val avgRepTime: Float = 0f,
        val estimatedROM: Float = 0f,
        val totalRom: Float = 0f,
        val downROM: Float = 0f,
        val upROM: Float = 0f,
        val workoutStartTime: Long = 0L,
        val verticalAccel: Float = 0f,
        val xAccel: Float = 0f,
        val yAccel: Float = 0f,
        val failedReps: Int = 0,
        val perRepRomScores: List<Float> = emptyList(),
        val isSetActive: Boolean = false,
        val currentSetNumber: Int = 1,
        val totalSets: Int = 0,
        val currentExerciseIndex: Int = 0,  // FIXED: Add to state
        val workoutScore: Float = 0f,
        val timingScore: Float = 0f,
        val tiltScore: Float = 0f,
        val weight: Double = 0.0,
        val notes: String? = null
    )
}