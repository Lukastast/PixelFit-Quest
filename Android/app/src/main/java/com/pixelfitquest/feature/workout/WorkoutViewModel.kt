package com.pixelfitquest.feature.workout

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.pixelfitquest.feature.customization.model.CharacterData
import com.pixelfitquest.feature.workout.analysis.AnalyzerUser
import com.pixelfitquest.feature.workout.analysis.DetectedRep
import com.pixelfitquest.feature.workout.analysis.ExerciseProfiles
import com.pixelfitquest.feature.workout.analysis.RomUnit
import com.pixelfitquest.feature.workout.analysis.SetAnalyzer
import com.pixelfitquest.feature.workout.model.Exercise
import com.pixelfitquest.feature.workout.model.RepRecord
import com.pixelfitquest.feature.workout.model.SetReviewState
import com.pixelfitquest.feature.workout.model.WORKOUT_SCHEMA_VERSION
import com.pixelfitquest.feature.workout.model.Workout
import com.pixelfitquest.feature.workout.model.WorkoutPhase
import com.pixelfitquest.feature.workout.model.WorkoutSet
import com.pixelfitquest.feature.workout.model.enums.ExerciseType
import com.pixelfitquest.feature.workout.model.enums.WorkoutFeedback
import com.pixelfitquest.feature.workout.sensor.ImuSample
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlan
import com.pixelfitquest.firebase.model.UserData
import com.pixelfitquest.firebase.repository.UserRepository
import com.pixelfitquest.firebase.repository.WorkoutRepository
import com.pixelfitquest.viewmodel.PixelFitViewModel
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
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val workoutRepository: WorkoutRepository,
    private val setAnalyzer: SetAnalyzer,
) : PixelFitViewModel() {

    private val _workoutState = MutableStateFlow(WorkoutState())
    val workoutState: StateFlow<WorkoutState> = _workoutState.asStateFlow()

    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData.asStateFlow()

    private val _feedbackEvent = Channel<WorkoutFeedback>(Channel.BUFFERED)
    val feedbackEvent = _feedbackEvent.receiveAsFlow()
    private val _countdownEvent = Channel<Unit>(Channel.BUFFERED)
    val countdownEvent = _countdownEvent.receiveAsFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _characterData = MutableStateFlow(CharacterData())
    val characterData: StateFlow<CharacterData> = _characterData.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String>(replay = 1)
    val navigationEvent: SharedFlow<String> = _navigationEvent.asSharedFlow()

    private var workoutName: String = "workout"
    private var workoutId: String = ""
    private var currentPlan: WorkoutPlan? = null
    private var currentExerciseIndex = 0
    private var currentSetNumber = 1
    private var currentExerciseType: ExerciseType? = null
    private val exerciseIds = mutableMapOf<Int, String>()
    private val samples = ArrayList<ImuSample>()
    private var savedExerciseForIndex = -1
    private var sessionFormSum = 0f
    private var sessionSetCount = 0
    private var sessionVolume = 0f
    private var sessionDurationMs = 0L
    private var exerciseFormSum = 0f
    private var exerciseSetCount = 0
    private var exerciseVolume = 0f

    init {
        launchCatching {
            loadUserData()
            loadCharacterData()
        }
    }

    fun startWorkoutFromPlan(plan: WorkoutPlan, templateName: String? = null) {
        workoutName = templateName?.takeIf { it.isNotBlank() }
            ?.lowercase()
            ?.replace(" ", "_")
            ?: "workout"
        workoutId = UUID.randomUUID().toString()
        currentPlan = plan
        currentExerciseIndex = 0
        currentSetNumber = 1
        currentExerciseType = plan.items.firstOrNull()?.exercise
        exerciseIds.clear()
        samples.clear()
        savedExerciseForIndex = -1
        sessionFormSum = 0f
        sessionSetCount = 0
        sessionVolume = 0f
        sessionDurationMs = 0L
        exerciseFormSum = 0f
        exerciseSetCount = 0
        exerciseVolume = 0f

        val initialWeight = plan.items.firstOrNull()?.weight ?: 0f
        _workoutState.value = WorkoutState(
            isTracking = true,
            phase = WorkoutPhase.Idle,
            currentSetNumber = 1,
            totalSets = plan.items.sumOf { it.sets },
            currentExerciseIndex = 0,
            weight = initialWeight,
        )

        launchCatching {
            workoutRepository.saveWorkout(
                Workout(
                    id = workoutId,
                    date = Instant.now().toString(),
                    name = workoutName,
                    schemaVersion = WORKOUT_SCHEMA_VERSION,
                    totalExercises = plan.items.size,
                    totalSets = plan.items.sumOf { it.sets },
                )
            )
        }
        ensureExerciseSaved()
    }

    fun startSet() {
        if (_workoutState.value.phase != WorkoutPhase.Idle) return
        ensureExerciseSaved()
        samples.clear()
        _workoutState.value = _workoutState.value.copy(
            phase = WorkoutPhase.Countdown,
            sampleCount = 0,
            recordingSeconds = 0f,
            review = null,
        )
        viewModelScope.launch { _countdownEvent.send(Unit) }
    }

    fun onCountdownFinished() {
        if (_workoutState.value.phase != WorkoutPhase.Countdown) return
        samples.clear()
        _workoutState.value = _workoutState.value.copy(
            phase = WorkoutPhase.Recording,
            sampleCount = 0,
            recordingSeconds = 0f,
        )
    }

    fun onRecordingTick(count: Int, firstNanos: Long, lastNanos: Long) {
        if (_workoutState.value.phase != WorkoutPhase.Recording) return
        if (count % 8 != 0) return
        val seconds = if (count < 2) 0f else (lastNanos - firstNanos) / 1_000_000_000f
        _workoutState.value = _workoutState.value.copy(
            sampleCount = count,
            recordingSeconds = seconds,
        )
    }

    fun finishSet(recorded: List<ImuSample>) {
        if (_workoutState.value.phase != WorkoutPhase.Recording) return
        samples.clear()
        samples.addAll(recorded)
        val type = currentExerciseType ?: return
        val profile = ExerciseProfiles.forType(type)
        val user = _userData.value
        val analysis = setAnalyzer.analyzeSet(
            samples = samples.toList(),
            profile = profile,
            user = AnalyzerUser(
                heightCm = user?.height ?: 178,
                armLengthCm = user?.armLength,
            ),
        )
        val review = SetReviewState(
            analysis = analysis,
            reps = analysis.reps,
            sampleCount = samples.size,
            setNumber = currentSetNumber,
            exerciseName = profile.displayName,
        )
        _workoutState.value = _workoutState.value.copy(
            phase = WorkoutPhase.Reviewing,
            sampleCount = samples.size,
            review = review,
        )
        Log.d("WorkoutVM", "Analyzed set $currentSetNumber: ${analysis.acceptedReps.size} accepted, ${analysis.candidateReps.size} candidates")
    }

    fun redoSet() {
        if (_workoutState.value.phase != WorkoutPhase.Reviewing) return
        samples.clear()
        _workoutState.value = _workoutState.value.copy(
            phase = WorkoutPhase.Idle,
            sampleCount = 0,
            recordingSeconds = 0f,
            review = null,
        )
    }

    fun acceptCandidate(repIndex: Int) = mutateReview { reps ->
        reps.map { if (it.index == repIndex) it.copy(accepted = true, tags = it.tags - "candidate") else it }
    }

    fun removeRep(repIndex: Int) = mutateReview { reps ->
        reps.filterNot { it.index == repIndex }.reindex()
    }

    fun mergeWithNext(repIndex: Int) = mutateReview { reps ->
        val i = reps.indexOfFirst { it.index == repIndex }
        if (i < 0 || i >= reps.lastIndex) return@mutateReview reps
        val a = reps[i]
        val b = reps[i + 1]
        val merged = a.copy(
            tEndNanos = b.tEndNanos,
            durationMs = a.durationMs + b.durationMs,
            romEstimate = maxOf(a.romEstimate, b.romEstimate),
            concentricMs = a.concentricMs + b.concentricMs,
            eccentricMs = a.eccentricMs + b.eccentricMs,
            romScore = maxOf(a.romScore, b.romScore),
            stabilityScore = averageOrNull(a.stabilityScore, b.stabilityScore),
            tempoScore = averageOrNull(a.tempoScore, b.tempoScore),
            formScore = (a.formScore + b.formScore) / 2f,
            tags = (a.tags + b.tags + "merged").distinct() - "candidate",
            accepted = true,
            confidence = maxOf(a.confidence, b.confidence),
        )
        (reps.take(i) + merged + reps.drop(i + 2)).reindex()
    }

    fun addRep() = mutateReview { reps ->
        val last = reps.lastOrNull()
        val stub = DetectedRep(
            index = reps.size,
            tStartNanos = last?.tEndNanos ?: 0L,
            tEndNanos = last?.tEndNanos ?: 0L,
            durationMs = 0L,
            romEstimate = 0f,
            romUnit = last?.romUnit ?: RomUnit.METERS,
            concentricMs = 0L,
            eccentricMs = 0L,
            pathDeviation = 0f,
            romScore = 100f,
            stabilityScore = null,
            tempoScore = null,
            formScore = 100f,
            tags = listOf("manual"),
            confidence = 1f,
            accepted = true,
        )
        (reps + stub).reindex()
    }

    fun adjustRom(repIndex: Int, delta: Int) = mutateReview { reps ->
        reps.map { rep ->
            if (rep.index != repIndex) rep
            else rep.withRomPercent(rep.romScore + delta)
        }
    }

    fun setRom(repIndex: Int, percent: Int) = mutateReview { reps ->
        reps.map { rep ->
            if (rep.index != repIndex) rep
            else rep.withRomPercent(percent.toFloat())
        }
    }

    fun confirmSet() {
        val review = _workoutState.value.review ?: return
        if (_workoutState.value.phase != WorkoutPhase.Reviewing) return

        val accepted = review.reps.filter { it.accepted }
        saveConfirmedSet(review, accepted)
        triggerSetFeedback(review.meanFormScore)

        val plan = currentPlan ?: return
        val currentItem = plan.items.getOrNull(currentExerciseIndex) ?: return
        val wasLastSet = currentSetNumber == currentItem.sets
        currentSetNumber++

        if (currentSetNumber > currentItem.sets) {
            if (wasLastSet) finishExercise()
            currentExerciseIndex++
            currentSetNumber = 1
            if (currentExerciseIndex >= plan.items.size) {
                finishWorkout()
                return
            }
            currentExerciseType = plan.items[currentExerciseIndex].exercise
            savedExerciseForIndex = -1
            exerciseFormSum = 0f
            exerciseSetCount = 0
            exerciseVolume = 0f
            ensureExerciseSaved()
            _workoutState.value = _workoutState.value.copy(
                weight = plan.items[currentExerciseIndex].weight,
            )
        }

        samples.clear()
        _workoutState.value = _workoutState.value.copy(
            phase = WorkoutPhase.Idle,
            currentSetNumber = currentSetNumber,
            currentExerciseIndex = currentExerciseIndex,
            sampleCount = 0,
            recordingSeconds = 0f,
            review = null,
        )
    }

    fun stopWorkout() {
        samples.clear()
        _workoutState.value = _workoutState.value.copy(
            isTracking = false,
            phase = WorkoutPhase.Idle,
            review = null,
        )
    }

    fun setError(message: String?) {
        _error.value = message
    }

    private fun mutateReview(transform: (List<DetectedRep>) -> List<DetectedRep>) {
        val review = _workoutState.value.review ?: return
        val updated = transform(review.reps)
        _workoutState.value = _workoutState.value.copy(
            review = review.copy(reps = updated, edited = true),
        )
    }

    private fun List<DetectedRep>.reindex(): List<DetectedRep> =
        mapIndexed { i, rep -> rep.copy(index = i) }

    private fun averageOrNull(a: Float?, b: Float?): Float? = when {
        a != null && b != null -> (a + b) / 2f
        else -> a ?: b
    }

    private fun List<Float>.averageOrZero(): Float =
        if (isEmpty()) 0f else average().toFloat()

    private fun currentExerciseId(): String {
        val index = currentExerciseIndex
        return exerciseIds.getOrPut(index) { UUID.randomUUID().toString() }
    }

    private fun ensureExerciseSaved() {
        val type = currentExerciseType ?: return
        val plan = currentPlan ?: return
        if (savedExerciseForIndex == currentExerciseIndex) return
        val item = plan.items.getOrNull(currentExerciseIndex) ?: return
        val exercise = Exercise(
            id = currentExerciseId(),
            workoutId = workoutId,
            type = type,
            profileId = ExerciseProfiles.forType(type).id,
            totalSets = item.sets,
            weight = item.weight,
        )
        savedExerciseForIndex = currentExerciseIndex
        launchCatching {
            workoutRepository.saveExercise(exercise)
        }
    }

    private fun saveConfirmedSet(review: SetReviewState, accepted: List<DetectedRep>) {
        val type = currentExerciseType ?: return
        val records = accepted.map { RepRecord.fromDetected(it) }
        val rom = accepted.map { it.romScore }.averageOrZero()
        val stability = accepted.mapNotNull { it.stabilityScore }.averageOrZero()
        val tempo = accepted.mapNotNull { it.tempoScore }.averageOrZero()
        val form = review.meanFormScore
        val avgTime = if (accepted.isEmpty()) 0f else accepted.map { it.durationMs.toFloat() }.average().toFloat()
        val duration = if (samples.size < 2) 0L else {
            (samples.last().tNanos - samples.first().tNanos) / 1_000_000L
        }
        val weight = _workoutState.value.weight
        val set = WorkoutSet(
            id = UUID.randomUUID().toString(),
            exerciseId = currentExerciseId(),
            workoutId = workoutId,
            setNumber = currentSetNumber,
            reps = accepted.size,
            userCorrected = review.edited,
            weight = weight,
            romScore = rom,
            stabilityScore = stability,
            tempoScore = tempo,
            formScore = form,
            avgRepTime = avgTime,
            totalDurationMillis = duration,
            xTiltScore = 0f,
            zTiltScore = 0f,
            flags = review.analysis.flags,
            repRecords = records,
        )
        sessionFormSum += form
        sessionSetCount += 1
        sessionVolume += weight * accepted.size
        sessionDurationMs += duration
        exerciseFormSum += form
        exerciseSetCount += 1
        exerciseVolume += weight * accepted.size
        launchCatching {
            workoutRepository.saveSet(set)
        }
    }

    private fun finishExercise() {
        val type = currentExerciseType ?: return
        val plan = currentPlan ?: return
        val item = plan.items.getOrNull(currentExerciseIndex) ?: return
        val avgForm = if (exerciseSetCount == 0) 0f else exerciseFormSum / exerciseSetCount
        val exercise = Exercise(
            id = currentExerciseId(),
            workoutId = workoutId,
            type = type,
            profileId = ExerciseProfiles.forType(type).id,
            totalSets = item.sets,
            weight = item.weight,
            avgFormScore = avgForm,
            totalVolume = exerciseVolume,
        )
        launchCatching { workoutRepository.saveExercise(exercise) }
    }

    private fun finishWorkout() {
        val plan = currentPlan ?: return
        val overall = if (sessionSetCount == 0) 0f else sessionFormSum / sessionSetCount
        val workout = Workout(
            id = workoutId,
            date = Instant.now().toString(),
            name = workoutName,
            schemaVersion = WORKOUT_SCHEMA_VERSION,
            totalExercises = plan.items.size,
            totalSets = plan.items.sumOf { it.sets },
            overallScore = overall,
            totalVolume = sessionVolume,
            totalDurationMillis = sessionDurationMs,
        )
        launchCatching {
            workoutRepository.saveWorkout(workout)
            updateDailyStreak()
            stopWorkout()
            _navigationEvent.emit(workoutId)
        }
    }

    private fun updateDailyStreak() {
        launchCatching {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val today = dateFormat.format(Date())
            val lastStreakUpdateDate = userRepository.getUserField("last_streak_update_date") as? String ?: ""
            if (lastStreakUpdateDate != today) {
                userRepository.updateStreak(increment = true)
                userRepository.updateUserData(mapOf("last_streak_update_date" to today))
            }
        }
    }

    private fun triggerSetFeedback(score: Float) {
        val feedback = when {
            score >= 90 -> WorkoutFeedback.PERFECT
            score >= 80 -> WorkoutFeedback.EXCELLENT
            score >= 70 -> WorkoutFeedback.GREAT
            score >= 50 -> WorkoutFeedback.GOOD
            else -> WorkoutFeedback.MISS
        }
        viewModelScope.launch { _feedbackEvent.send(feedback) }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                userRepository.getUserData().collect { data ->
                    _userData.value = data
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load user data"
            }
        }
    }

    private fun loadCharacterData() {
        viewModelScope.launch {
            userRepository.getCharacterData().collect { data ->
                _characterData.value = data ?: CharacterData()
            }
        }
    }

    data class WorkoutState(
        val isTracking: Boolean = false,
        val phase: WorkoutPhase = WorkoutPhase.Idle,
        val currentSetNumber: Int = 1,
        val totalSets: Int = 0,
        val currentExerciseIndex: Int = 0,
        val weight: Float = 0f,
        val sampleCount: Int = 0,
        val recordingSeconds: Float = 0f,
        val review: SetReviewState? = null,
        val notes: String? = null,
    )
}
