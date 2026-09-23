package com.pixelfitquest.feature.workout

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.pixelfitquest.feature.customization.model.CharacterData
import com.pixelfitquest.feature.workout.analysis.AnalyzerUser
import com.pixelfitquest.feature.workout.analysis.DetectedRep
import com.pixelfitquest.feature.workout.analysis.ExerciseProfiles
import com.pixelfitquest.feature.workout.analysis.FullRomStore
import com.pixelfitquest.feature.workout.analysis.RomUnit
import com.pixelfitquest.feature.workout.analysis.SetAnalysis
import com.pixelfitquest.feature.workout.analysis.SetAnalyzer
import com.pixelfitquest.feature.workout.analysis.formScoreFrom
import com.pixelfitquest.feature.workout.catalog.ExerciseCatalog
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
import com.pixelfitquest.feature.progress.data.LiftHistoryDao
import com.pixelfitquest.feature.progress.data.toLiftHistoryEntity
import com.pixelfitquest.feature.streak.data.WeeklyStreakRepository
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
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val workoutRepository: WorkoutRepository,
    private val weeklyStreakRepository: WeeklyStreakRepository,
    private val setAnalyzer: SetAnalyzer,
    private val liftHistoryDao: LiftHistoryDao,
    private val fullRomStore: FullRomStore,
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
        if (_workoutState.value.isTracking) return
        if (plan.items.isEmpty()) {
            _error.value = "Cannot start a workout with no exercises"
            return
        }
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
        val type = currentExerciseType ?: return
        if (!ExerciseCatalog.hasImuSupport(type)) {
            samples.clear()
            showLogOnlyReview(sampleCount = 0)
            return
        }
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
        if (!ExerciseCatalog.hasImuSupport(type)) {
            showLogOnlyReview(sampleCount = samples.size)
            return
        }
        val profile = ExerciseProfiles.forType(type)
        val user = _userData.value
        val analysis = setAnalyzer.analyzeSet(
            samples = samples.toList(),
            profile = profile,
            user = AnalyzerUser(
                heightCm = user?.height ?: 178,
                armLengthCm = user?.armLength,
            ),
            fullRom = fullRomStore.get(type.type),
        )
        val review = SetReviewState(
            analysis = analysis,
            reps = analysis.reps,
            sampleCount = samples.size,
            setNumber = currentSetNumber,
            exerciseName = ExerciseCatalog.definition(type).displayName,
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
        val tempo = averageOrNull(a.tempoScore, b.tempoScore)
        val rom = maxOf(a.romScore, b.romScore)
        val barQuality = averageOrNull(a.stabilityScore, b.stabilityScore)
        val merged = a.copy(
            tEndNanos = b.tEndNanos,
            durationMs = a.durationMs + b.durationMs,
            romEstimate = maxOf(a.romEstimate, b.romEstimate),
            concentricMs = a.concentricMs + b.concentricMs,
            eccentricMs = a.eccentricMs + b.eccentricMs,
            romScore = rom,
            stabilityScore = barQuality,
            tempoScore = tempo,
            levelDeg = averageOrNull(a.levelDeg, b.levelDeg),
            twistDeg = averageOrNull(a.twistDeg, b.twistDeg),
            formScore = formScoreFrom(
                romScore = rom,
                tempoScore = tempo,
                barQuality = barQuality,
            ),
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
        val current = reps.firstOrNull { it.index == repIndex }
        if (percent >= 100 && current != null) {
            currentExerciseType?.let { fullRomStore.raise(it.type, current.romEstimate) }
        }
        reps.map { rep ->
            if (rep.index != repIndex) rep
            else rep.withRomPercent(percent.toFloat())
        }
    }

    fun confirmSet() {
        val review = _workoutState.value.review ?: return
        if (_workoutState.value.phase != WorkoutPhase.Reviewing) return

        val accepted = review.reps.filter { it.accepted }
        currentExerciseType?.let { type ->
            val peak = accepted.maxOfOrNull { it.romEstimate } ?: 0f
            fullRomStore.raise(type.type, peak)
        }
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
        val wasTracking = _workoutState.value.isTracking
        val shouldDiscard = wasTracking && sessionSetCount == 0
        _workoutState.value = _workoutState.value.copy(
            isTracking = false,
            phase = WorkoutPhase.Idle,
            review = null,
        )
        if (shouldDiscard && workoutId.isNotBlank()) {
            launchCatching {
                workoutRepository.deleteWorkout(workoutId)
            }
        }
    }

    /** Logged sets open the summary. A workout with no sets goes home. */
    fun leaveWorkout() {
        if (sessionSetCount > 0) {
            finishWorkout()
        } else {
            stopWorkout()
            launchCatching { _navigationEvent.emit("") }
        }
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
            profileId = type.type,
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
        val twist = accepted.mapNotNull { it.twistDeg }.averageOrZero()
        val level = accepted.mapNotNull { it.levelDeg }.averageOrZero()
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
            twistDeg = twist,
            levelDeg = level,
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
            try {
                liftHistoryDao.insert(set.toLiftHistoryEntity(type))
            } catch (e: Exception) {
                Log.w("WorkoutVM", "Local lift history save skipped", e)
            }
            try {
                workoutRepository.saveSet(set)
            } catch (e: Exception) {
                Log.w("WorkoutVM", "Cloud set save skipped", e)
            }
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
            profileId = type.type,
            totalSets = item.sets,
            weight = item.weight,
            avgFormScore = avgForm,
            totalVolume = exerciseVolume,
        )
        launchCatching { workoutRepository.saveExercise(exercise) }
    }

    private fun finishWorkout() {
        val plan = currentPlan ?: return
        if (plan.items.isEmpty() || sessionSetCount == 0) {
            launchCatching {
                if (workoutId.isNotBlank()) {
                    workoutRepository.deleteWorkout(workoutId)
                }
                stopWorkout()
            }
            return
        }
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
            recordWeeklyStreak(workout.id)
            stopWorkout()
            _navigationEvent.emit(workoutId)
        }
    }

    private suspend fun recordWeeklyStreak(completedWorkoutId: String) {
        try {
            weeklyStreakRepository.recordCompletedSession(completedWorkoutId)
            weeklyStreakRepository.withConsumedPendingXp { amount ->
                userRepository.updateExp(amount)
            }
        } catch (e: Exception) {
            Log.i("WorkoutVM", "Weekly streak XP kept on device until account is available", e)
        }
    }

    /**
     * Log-only lifts skip IMU analysis. Do not fall back to another exercise's
     * profile — that would invent form scores. User adds reps on the review overlay.
     */
    private fun showLogOnlyReview(sampleCount: Int) {
        val type = currentExerciseType ?: return
        val review = SetReviewState(
            analysis = SetAnalysis(
                reps = emptyList(),
                meanFormScore = 0f,
                flags = listOf("log_only"),
            ),
            reps = emptyList(),
            sampleCount = sampleCount,
            setNumber = currentSetNumber,
            exerciseName = ExerciseCatalog.definition(type).displayName,
        )
        _workoutState.value = _workoutState.value.copy(
            phase = WorkoutPhase.Reviewing,
            sampleCount = sampleCount,
            recordingSeconds = 0f,
            review = review,
        )
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
