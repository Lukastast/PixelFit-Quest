package com.pixelfitquest.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.pixelfitquest.model.CharacterData
import com.pixelfitquest.model.Exercise
import com.pixelfitquest.model.ExerciseType
import com.pixelfitquest.model.UserSettings
import com.pixelfitquest.model.Workout
import com.pixelfitquest.model.WorkoutFeedback
import com.pixelfitquest.model.WorkoutPlan
import com.pixelfitquest.model.WorkoutSet
import com.pixelfitquest.repository.UserRepository
import com.pixelfitquest.repository.UserSettingsRepository
import com.pixelfitquest.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.util.ArrayDeque
import java.util.Deque
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.sqrt

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    private val workoutRepository: WorkoutRepository,
    private val userRepository: UserRepository
) : PixelFitViewModel() {

    private val _workoutState = MutableStateFlow(WorkoutState())
    val workoutState: StateFlow<WorkoutState> = _workoutState.asStateFlow()

    private val _userSettings = MutableStateFlow<UserSettings?>(null)
    val userSettings: StateFlow<UserSettings?> = _userSettings.asStateFlow()

    private val _feedbackEvent = Channel<WorkoutFeedback>(Channel.BUFFERED)
    val feedbackEvent = _feedbackEvent.receiveAsFlow()
    private val _countdownEvent = Channel<Unit>(Channel.BUFFERED)
    val countdownEvent = _countdownEvent.receiveAsFlow()


    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private lateinit var workoutName: String

    private val repIntervalMs = 1000L  // For 2-3s rep cycles
    private val lowPassAlpha = 0.7f
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

    // Plan-based tracking
    private var currentPlan: WorkoutPlan? = null
    private var currentExerciseIndex = 0
    private var currentSetNumber = 1
    private var isSetActive = false
    private var currentExerciseType: ExerciseType? = null
    private var workoutId: String = ""

    // Tilt
    private var tiltXSum = 0f
    private var tiltYSum = 0f
    private var tiltZSum = 0f
    private var tiltSampleCount = 0
    private var baselineTiltX = 0f
    private var baselineTiltY = 0f
    private var baselineTiltZ = 0f

    private val maxTiltAccel = 4.0f  // FIXED: Threshold for max tilt (tune via testing)

    //animation
    private val _characterData = MutableStateFlow(CharacterData())
    val characterData: StateFlow<CharacterData> = _characterData.asStateFlow()


    //navigation
    private val _navigationEvent = MutableSharedFlow<String>(replay = 1)
    val navigationEvent: SharedFlow<String> = _navigationEvent.asSharedFlow()


    init {
        launchCatching {
            loadUserData()
            loadCharacterData()
        }
    }

    fun onSensorDataUpdated(accelerometer: FloatArray?, timestamp: Long) {
        val currentState = _workoutState.value
        if (!currentState.isTracking || !isSetActive) return

        val currentTime = System.currentTimeMillis()
        if (currentTime < currentState.workoutStartTime + stabilizationTimeMs) return

        accelerometer?.let { values ->
            val dt = calculateDt(timestamp)

            val accel = floatArrayOf(values[0], values[1], values[2])
            updateGravityVector(accel)

            val upwardUnit = normalizeGravity()
            val (netAccelX, netAccelY, netAccelZ ) = calculateNetAccel(accel)
            val netAccel = floatArrayOf(netAccelX, netAccelY, netAccelZ)
            val verticalAccel = calculateVerticalAccel(netAccel, upwardUnit)

            val smoothedAccel = smoothAccel(verticalAccel)
            accumulateTilt(netAccelX, netAccelZ, netAccelY)
            // Always update accel state (for UI, even if not integrating)
            updateAccelStateIfChanged(verticalAccel, netAccelX, netAccelY, currentState)

            // Guard integration/detection
            if (!isSetActive) return@let

            // Integrate
            lastVerticalAccel = verticalAccel
            val prevVelocity = currentVelocity
            currentDisplacement += currentVelocity * dt + 0.5f * smoothedAccel * dt * dt
            currentVelocity += smoothedAccel * dt

            velHistory.addLast(currentVelocity)
            if (velHistory.size > hysteresisWindow) {
                velHistory.removeFirst()
            }

            // Detect phases
            detectTop(prevVelocity, currentVelocity, currentTime)  // OPTIMIZED: Extracted
            detectBottom(prevVelocity, currentVelocity, currentDisplacement, repStartPos)  // OPTIMIZED: Extracted
        }
    }

    private fun calculateDt(timestamp: Long): Float {
        val rawDt = if (lastTimestamp > 0) {
            (timestamp - lastTimestamp) / 1_000_000_000f
        } else 0.02f
        dtHistory.addLast(rawDt)
        if (dtHistory.size > dtWindowSize) {
            dtHistory.removeFirst()
        }
        lastTimestamp = timestamp
        return if (dtHistory.size < 2) rawDt else dtHistory.average().toFloat()
    }

    private fun updateGravityVector(accel: FloatArray) {
        gravityVector[0] = lowPassAlpha * gravityVector[0] + (1 - lowPassAlpha) * accel[0]
        gravityVector[1] = lowPassAlpha * gravityVector[1] + (1 - lowPassAlpha) * accel[1]
        gravityVector[2] = lowPassAlpha * gravityVector[2] + (1 - lowPassAlpha) * accel[2]
    }

    private fun normalizeGravity(): FloatArray {
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
        return floatArrayOf(-gravityUnit[0], -gravityUnit[1], -gravityUnit[2])
    }

    private fun calculateNetAccel(accel: FloatArray): Triple<Float, Float, Float> {
        val netAccel = floatArrayOf(
            accel[0] - gravityVector[0],
            accel[1] - gravityVector[1],
            accel[2] - gravityVector[2]
        )
        val netAccelX = netAccel[0]
        val netAccelY = netAccel[1]
        val netAccelZ = netAccel[2]
        return Triple(netAccelX, netAccelY, netAccelZ)
    }

    private fun calculateVerticalAccel(netAccel: FloatArray, upwardUnit: FloatArray): Float {
        return netAccel[0] * upwardUnit[0] + netAccel[1] * upwardUnit[1] + netAccel[2] * upwardUnit[2]
    }
    private fun smoothAccel(verticalAccel: Float): Float {
        accelHistory.addLast(verticalAccel)
        if (accelHistory.size > 2) {
            accelHistory.removeFirst()
        }
        return if (accelHistory.size < 2) verticalAccel else accelHistory.average().toFloat()
    }

    private fun updateAccelStateIfChanged(verticalAccel: Float, netAccelX: Float, netAccelY: Float, currentState: WorkoutState) {
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

    private fun detectTop(prevVelocity: Float, currentVelocity: Float, currentTime: Long) {
        val timeSincePeak = currentTime - lastPeakTime
        val lastVels = velHistory.toList().takeLast(hysteresisWindow.coerceAtMost(velHistory.size))  // OPTIMIZED: Compute once
        val positiveCount = lastVels.count { it >= 0f }
        val positiveHysteresis = velHistory.size >= hysteresisWindow / 2 && positiveCount >= (lastVels.size * 0.6).toInt()
        if (prevVelocity >= 0f && currentVelocity < 0f &&
            positiveHysteresis && timeSincePeak > repIntervalMs
        ) {
            lastPeakTime = currentTime
            if (hasBottom) {
                onRepCompleted()
            } else {
                // Failed rep logic (your existing)
                repStartPos = currentDisplacement
                bottomPos = currentDisplacement
                hasBottom = false
                minPos = currentDisplacement
                maxPos = currentDisplacement
                this.currentVelocity = 0f
                velHistory.clear()
                accelHistory.clear()

                _workoutState.value = _workoutState.value.copy(
                    verticalAccel = lastVerticalAccel,
                    xAccel = _workoutState.value.xAccel,  // Preserve from state
                    yAccel = _workoutState.value.yAccel,
                )
            }
            accelHistory.clear()
        }
    }

    private fun detectBottom(prevVelocity: Float, currentVelocity: Float, currentDisplacement: Float, repStartPos: Float) {
        val lastVels = velHistory.toList().takeLast(hysteresisWindow.coerceAtMost(velHistory.size))  // OPTIMIZED: Reuse if possible
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
            this.currentDisplacement = 0f
            this.currentVelocity *= 0.5f
            minPos = 0f
            maxPos = 0f
            velHistory.clear()
            accelHistory.clear()
        }
    }

    private fun accumulateTilt(netAccelX: Float, netAccelZ: Float, netAccelY: Float) {
        val relativeX = netAccelX - baselineTiltX
        val relativeY = netAccelY - baselineTiltY
        val relativeZ = netAccelZ - baselineTiltZ

        tiltXSum += relativeX
        tiltYSum += relativeY
        tiltZSum += relativeZ
        tiltSampleCount++
        //maybe add a sample count that keeps increasing tiltsum until corrected? and then have the treshholds
        //if (tiltSampleCount % 5 == 0)
        //Log.d("TiltAccumDebug", "New sample: X=$tiltXSum, Y=$tiltYSum; Z=$tiltZSum total samples: $tiltSampleCount")
    }
    fun startWorkoutFromPlan(plan: WorkoutPlan, templateName: String? = null) {
        workoutName = templateName?.takeIf { it.isNotBlank() }
            ?.lowercase()
            ?.replace(" ", "_")
            ?: "workout"
        workoutId = "${workoutName}_${LocalDate.now()}_${System.currentTimeMillis() % 1_000_000}"
        currentPlan = plan
        currentExerciseIndex = 0
        currentSetNumber = 1
        isSetActive = false
        currentExerciseType = plan.items[0].exercise
        val startTime = System.currentTimeMillis()
        totalRepTime = 0L
        lastPeakTime = startTime
        lastVerticalAccel = 0f
        currentDisplacement = 0f
        currentVelocity = 0f
        minPos = 0f
        maxPos = 0f
        repStartPos = 0f
        bottomPos = 0f
        hasBottom = false
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
        baselineTiltX = 0f
        baselineTiltY = 0f
        baselineTiltZ = 0f
        viewModelScope.launch {
            _countdownEvent.send(Unit)
        }

        Log.d("WorkoutVM", "Started set $currentSetNumber")
    }

    fun finishSet() {
        if (!isSetActive) return
        isSetActive = false
        _workoutState.value = _workoutState.value.copy(isSetActive = false)

        saveCurrentSet()  // Assumes this adds to completedSets and saves

        val currentPlan = currentPlan ?: return
        val currentItem = currentPlan.items.getOrNull(currentExerciseIndex) ?: return

        // NEW: Check if this was the last set *before* increment
        val wasLastSet = currentSetNumber == currentItem.sets
        currentSetNumber++

        if (currentSetNumber > currentItem.sets) {  // Now advances after last set
            // NEW: Call finishExercise() here if it was the last set (saves aggregate before advancing)
            if (wasLastSet) {
                finishExercise()  // Save-only now—no double increment
            }

            currentExerciseIndex++
            currentSetNumber = 1
            if (currentExerciseIndex >= currentPlan.items.size) {
                Log.d("WorkoutVM", "Workout complete!")
                finishWorkout()
                return
            }
            currentExerciseType = currentPlan.items[currentExerciseIndex].exercise
        }

        _workoutState.value = _workoutState.value.copy(
            currentSetNumber = currentSetNumber,
            currentExerciseIndex = currentExerciseIndex
        )
        Log.d("WorkoutVM", "Finished set $ (currentSetNumber - 1), advanced to set $currentSetNumber of exercise $currentExerciseIndex")
    }
    fun finishExercise() {

        val exerciseId = currentExerciseType!!.name.lowercase().replace("_", "-")
        val exercise = Exercise(
            id = exerciseId,
            workoutId = workoutId,
            type = currentExerciseType!!,
            totalSets = currentPlan!!.items[currentExerciseIndex].sets,
            weight = _workoutState.value.weight,
            notes = _workoutState.value.notes
        )
        viewModelScope.launch {
            workoutRepository.saveExercise(exercise)
            Log.d("WorkoutVM", "finish exercise() Saved exercise $exercise.id under workout $workoutId")
        }
    }

    fun finishWorkout() {  // Call when all exercises done
        val workout = Workout(
            id = workoutId,
            date = Instant.now().toString(),
            name = workoutName,
            totalExercises = currentPlan!!.items.size,
            totalSets = currentPlan!!.items.sumOf { it.sets },
            overallScore = _workoutState.value.avgRomScore,
            notes = null
        )
        launchCatching {
            workoutRepository.saveWorkout(workout)

            // Increment streak only once per day
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            dateFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val today = dateFormat.format(java.util.Date())
            val lastStreakUpdateDate = userRepository.getUserField("last_streak_update_date") as? String ?: ""

            if (lastStreakUpdateDate != today) {
                userRepository.updateStreak(increment = true)
                userRepository.updateUserGameData(mapOf("last_streak_update_date" to today))
                Log.d("WorkoutVM", "Streak incremented for $today")
            } else {
                Log.d("WorkoutVM", "Streak already incremented today")
            }

            stopWorkout()
            Log.d("WorkoutVM", "Saved workout, should navigate to resume $workoutId")
            _navigationEvent.emit(workoutId)
        }
    }
    private fun saveCurrentSet() {
        val currentState = _workoutState.value
        if (currentPlan == null || currentExerciseType == null) return
        val exerciseId = currentExerciseType!!.name.lowercase().replace("_", "-")
        val setId = "set_${currentSetNumber}"

        val set = WorkoutSet(
            id = setId,
            exerciseId = exerciseId,
            workoutId = workoutId,
            setNumber = currentSetNumber,
            reps = currentState.reps,
            romScore = currentState.romScore,
            workoutScore = (currentState.romScore + (100-abs(currentState.avgTiltXScore)) + (100-abs(currentState.avgTiltZScore))) / 3f,
            xTiltScore = currentState.avgTiltXScore,
            zTiltScore = currentState.avgTiltZScore,
            avgRepTime = currentState.avgRepTime,
            verticalAccel = currentState.verticalAccel,
            weight = currentState.weight,

            notes = currentState.notes
        )
        viewModelScope.launch {
            workoutRepository.saveSet(set)
            Log.d("WorkoutVM", "Saved set $setId for exercise $exerciseId")
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

    private fun loadCharacterData() {
        viewModelScope.launch {
            userRepository.getCharacterData().collect { data ->
                _characterData.value = data ?: CharacterData()
            }
        }
    }

    private fun updateFeedback(score: Float) {
        val feedback = when {
            score >= 90 -> WorkoutFeedback.PERFECT
            score >= 80 -> WorkoutFeedback.EXCELLENT
            score >= 70 -> WorkoutFeedback.GREAT
            score >= 50 -> WorkoutFeedback.GOOD
            else -> WorkoutFeedback.MISS
        }
       triggerFeedback(feedback)
    }

    fun triggerFeedback(feedback: WorkoutFeedback) {
        viewModelScope.launch {
            _feedbackEvent.send(feedback)  // One-time emission
        }
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

        // ROM calculation
        val downDelta = bottomPos - repStartPos
        val upDelta = currentDisplacement - bottomPos
        val downROM = abs(downDelta) * 100f
        val upROM = abs(upDelta) * 100f
        val newEstROM = downROM + upROM

        val newAvgRepTime = if (newReps > 0) (totalRepTime / newReps).toFloat() else 0f

        //  Score calculation (use currentWorkoutType)
        val lastRepScore = calculateRomScore(newEstROM)
        val newTotalRom = currentState.totalRom + lastRepScore
        val avgRomScore = if (newReps > 0) (newTotalRom / newReps.toFloat()).coerceIn(0f, 100f) else 0f

        // Tilt score
        val avgTiltX = if (tiltSampleCount > 0) tiltXSum / tiltSampleCount else 0f
        val avgTiltZ = if (tiltSampleCount > 0) tiltZSum / tiltSampleCount else 0f
        val tiltXScore = ((avgTiltX / maxTiltAccel) * 120f).coerceIn(-100f, 100f)
        val tiltZScore = ((avgTiltZ / maxTiltAccel) * 120f).coerceIn(-100f, 100f) // FIXED: X tilt (-100 left lowest, +100 right lowest)

        val newTotalTiltX = currentState.totalTiltX + tiltXScore
        val newTotalTiltZ = currentState.totalTiltZ + tiltZScore
        val avgTiltXScore = if (newReps > 0) (newTotalTiltX / newReps.toFloat()).coerceIn(-100f, 100f) else 0f
        val avgTiltZScore = if (newReps > 0) (newTotalTiltZ / newReps.toFloat()).coerceIn(-100f, 100f) else 0f

        val workoutScore = (avgRomScore + (100-abs(avgTiltXScore)) + (100-abs(100-avgTiltZScore))) / 3f
        val feedbackScore = (lastRepScore * 2 + (100-abs(tiltXScore)) + (100-abs(tiltZScore))) / 4f

        Log.d("TiltDebug", "Rep avg X: $avgTiltX, Y: $avgTiltZ | Score X: $tiltXScore, Z: $tiltZScore")

        tiltXSum = 0f
        tiltZSum = 0f
        tiltSampleCount = 0
        // Reset for next rep
        repStartPos = currentDisplacement
        bottomPos = currentDisplacement
        hasBottom = false
        minPos = currentDisplacement
        maxPos = currentDisplacement
        currentVelocity = 0f
        velHistory.clear()
        accelHistory.clear()

        // Update state
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
            totalTiltX = newTotalTiltX,
            totalTiltZ = newTotalTiltZ,
            avgRomScore = avgRomScore,
            avgTiltXScore = avgTiltXScore,
            avgTiltZScore = avgTiltZScore,
            tiltXScore = tiltXScore,
            tiltZScore = tiltZScore,
            workoutScore = workoutScore
        )
        updateFeedback(feedbackScore)
        Log.d("WorkoutVM", "Rep completed: $newReps reps, ROM: $newEstROM cm")
    }

    data class WorkoutState( 
        val isTracking: Boolean = false,
        val userHeightCm: Float? = null,
        val theoreticalMaxRomCm: Float? = null,
        val romScore: Float = 0f,
        val avgRomScore: Float = 0f,
        val avgTiltXScore: Float = 0f,
        val avgTiltZScore: Float = 0f,
        val avgRepTime: Float = 0f,
        val reps: Int = 0,
        val lastRepTime: Long = 0L,
        val estimatedROM: Float = 0f,
        val totalRom: Float = 0f,
        val totalTiltX: Float = 0f,
        val totalTiltZ: Float = 0f,
        val downROM: Float = 0f,
        val upROM: Float = 0f,
        val workoutStartTime: Long = 0L,
        val verticalAccel: Float = 0f,
        val xAccel: Float = 0f,
        val yAccel: Float = 0f,
        val isSetActive: Boolean = false,
        val currentSetNumber: Int = 1,
        val totalSets: Int = 0,
        val currentExerciseIndex: Int = 0,  
        val workoutScore: Float = 0f,
        val timingScore: Float = 0f,
        val tiltXScore: Float = 0f,  // tilt (-100 left, +100 right, 0 centered)
        val tiltZScore: Float = 0f,
        val weight: Float = 0f,
        val notes: String? = null,
        val feedback: WorkoutFeedback? = null,
        val showFeedback: Boolean = false
    )
}