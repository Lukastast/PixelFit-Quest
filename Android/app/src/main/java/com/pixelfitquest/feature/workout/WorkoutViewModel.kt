package com.pixelfitquest.feature.workout

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.pixelfitquest.feature.customization.model.CharacterData
import com.pixelfitquest.feature.workout.analysis.DetectedRep
import com.pixelfitquest.feature.workout.analysis.ExerciseProfiles
import com.pixelfitquest.feature.workout.analysis.FullRomStore
import com.pixelfitquest.feature.workout.analysis.RomUnit
import com.pixelfitquest.feature.workout.analysis.perSetRom
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
import com.pixelfitquest.feature.workout.sensor.ImuSample
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlan
import com.pixelfitquest.feature.progress.data.LiftHistoryDao
import com.pixelfitquest.feature.progress.data.toLiftHistoryEntity
import com.pixelfitquest.feature.streak.data.WeeklyStreakRepository
import com.pixelfitquest.firebase.repository.UserRepository
import com.pixelfitquest.firebase.repository.WorkoutRepository
import com.pixelfitquest.feature.achievements.AchievementSyncService
import com.pixelfitquest.viewmodel.PixelFitViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val achievementSyncService: AchievementSyncService,
    private val fullRomStore: FullRomStore,
    private val prefs: SharedPreferences,
    private val repEditLog: RepEditLog,
) : PixelFitViewModel() {

    private val _workoutState = MutableStateFlow(WorkoutState())
    val workoutState: StateFlow<WorkoutState> = _workoutState.asStateFlow()

    /** Hot path during Recording — do not fold into [workoutState] (avoids full-tree recomposition). */
    private val _recordingHud = MutableStateFlow(RecordingHud())
    val recordingHud: StateFlow<RecordingHud> = _recordingHud.asStateFlow()

    /** Hot path during Rest — second-granularity display; isolate from [workoutState]. */
    private val _restRemainingMs = MutableStateFlow(0L)
    val restRemainingMs: StateFlow<Long> = _restRemainingMs.asStateFlow()

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
    private var sessionRepCount = 0
    private var sessionVolume = 0f
    private var sessionDurationMs = 0L
    private var exerciseFormSum = 0f
    private var exerciseSetCount = 0
    private var exerciseVolume = 0f
    private val sideByExercise = mutableMapOf<Int, String>()
    /** True after setRom(100%)/replace in the current review; confirm must not raise above that baseline. */
    private var fullRomRecalibratedThisReview = false

    init {
        launchCatching {
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
        sessionRepCount = 0
        sessionVolume = 0f
        sessionDurationMs = 0L
        exerciseFormSum = 0f
        exerciseSetCount = 0
        exerciseVolume = 0f
        sideByExercise.clear()

        val initialWeight = plan.items.firstOrNull()?.weight ?: 0f
        _workoutState.value = WorkoutState(
            isTracking = true,
            phase = WorkoutPhase.Idle,
            currentSetNumber = 1,
            totalSets = plan.items.sumOf { it.sets },
            currentExerciseIndex = 0,
            weight = initialWeight,
        )

        ensureExerciseSaved()
    }

    fun startSet() {
        val phase = _workoutState.value.phase
        if (phase != WorkoutPhase.Idle && phase != WorkoutPhase.Resting) return
        ensureExerciseSaved()
        val type = currentExerciseType ?: return
        if (!ExerciseCatalog.hasImuSupport(type)) {
            samples.clear()
            showLogOnlyReview(sampleCount = 0)
            return
        }
        // No 3-2-1 countdown (#170): start IMU recording immediately.
        samples.clear()
        _recordingHud.value = RecordingHud()
        _restRemainingMs.value = 0L
        _workoutState.value = _workoutState.value.copy(
            phase = WorkoutPhase.Recording,
            review = null,
            restPaused = false,
            restAutostart = false,
        )
    }

    fun onRecordingTick(count: Int, firstNanos: Long, lastNanos: Long) {
        if (_workoutState.value.phase != WorkoutPhase.Recording) return
        if (count % 8 != 0) return
        val seconds = if (count < 2) 0f else (lastNanos - firstNanos) / 1_000_000_000f
        _recordingHud.value = RecordingHud(sampleCount = count, recordingSeconds = seconds)
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
        val analysis = setAnalyzer.analyzeSet(
            samples = samples.toList(),
            profile = profile,
            fullRom = fullRomStore.get(type.type),
        )
        fullRomRecalibratedThisReview = false
        val review = SetReviewState(
            analysis = analysis,
            reps = analysis.reps,
            sampleCount = samples.size,
            setNumber = currentSetNumber,
            exerciseName = ExerciseCatalog.definition(type).displayName,
            notes = "",
        )
        _recordingHud.value = RecordingHud(sampleCount = samples.size, recordingSeconds = _recordingHud.value.recordingSeconds)
        _workoutState.value = _workoutState.value.copy(
            phase = WorkoutPhase.Reviewing,
            review = review,
            notes = "",
        )
        logEdit(
            "detected",
            mapOf(
                "accepted" to analysis.acceptedReps.size.toString(),
                "shown" to review.reps.size.toString(),
                "reps" to repSummary(review.reps),
                "flags" to analysis.flags.joinToString(","),
            ),
        )
        Log.d("WorkoutVM", "Analyzed set $currentSetNumber: ${analysis.acceptedReps.size} accepted, ${analysis.candidateReps.size} candidates")
    }

    fun redoSet() {
        if (_workoutState.value.phase != WorkoutPhase.Reviewing) return
        fullRomRecalibratedThisReview = false
        samples.clear()
        _recordingHud.value = RecordingHud()
        _workoutState.value = _workoutState.value.copy(
            phase = WorkoutPhase.Idle,
            review = null,
            notes = null,
        )
    }

    fun acceptCandidate(repIndex: Int) = mutateReview { reps ->
        logEdit("accept", mapOf("rep" to repIndex.toString(), "before" to repSummary(reps)))
        reps.map {
            if (it.index == repIndex) it.copy(accepted = true, tags = it.tags - "candidate") else it
        }
    }

    fun removeRep(repIndex: Int) = mutateReview { reps ->
        logEdit("remove", mapOf("rep" to repIndex.toString(), "before" to repSummary(reps)))
        reps.filterNot { it.index == repIndex }.reindex()
    }

    fun mergeWithNext(repIndex: Int) = mutateReview { reps ->
        val i = reps.indexOfFirst { it.index == repIndex }
        if (i < 0 || i >= reps.lastIndex) return@mutateReview reps
        logEdit("merge", mapOf("rep" to repIndex.toString(), "before" to repSummary(reps)))
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
            assisted = a.assisted || b.assisted,
        )
        (reps.take(i) + merged + reps.drop(i + 2)).reindex()
    }

    fun addRep() = mutateReview { reps ->
        logEdit("add", mapOf("before" to repSummary(reps)))
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
        val current = reps.firstOrNull { it.index == repIndex }
        logEdit(
            "adjust_rom",
            mapOf(
                "rep" to repIndex.toString(),
                "from" to (current?.romScore?.toInt() ?: 0).toString(),
                "delta" to delta.toString(),
                "amp" to (current?.romEstimate ?: 0f).toString(),
            ),
        )
        reps.map { rep ->
            if (rep.index != repIndex) rep
            else rep.withRomPercent(rep.romScore + delta)
        }
    }

    fun setRom(repIndex: Int, percent: Int) = mutateReview { reps ->
        val current = reps.firstOrNull { it.index == repIndex } ?: return@mutateReview reps
        if (percent >= 100 && current.romEstimate > 1e-4f) {
            val baseline = current.romEstimate
            currentExerciseType?.let { fullRomStore.replace(it.type, baseline) }
            fullRomRecalibratedThisReview = true
            logEdit(
                "recalibrate_rom",
                mapOf(
                    "rep" to repIndex.toString(),
                    "baseline" to baseline.toString(),
                    "before" to repSummary(reps),
                ),
            )
            return@mutateReview reps.map { rep ->
                when {
                    rep.index == repIndex -> rep.withRomPercent(100f)
                    rep.isManual || "rom_override" in rep.tags -> rep
                    else -> rep.scoredAgainst(baseline)
                }
            }
        }
        logEdit(
            "set_rom",
            mapOf(
                "rep" to repIndex.toString(),
                "percent" to percent.toString(),
                "amp" to current.romEstimate.toString(),
            ),
        )
        reps.map { rep ->
            if (rep.index != repIndex) rep
            else rep.withRomPercent(percent.toFloat())
        }
    }

    fun toggleAssisted(repIndex: Int) = mutateReview { reps ->
        val next = reps.map { rep ->
            if (rep.index != repIndex) rep else rep.copy(assisted = !rep.assisted)
        }
        val assisted = next.firstOrNull { it.index == repIndex }?.assisted == true
        logEdit("assisted", mapOf("rep" to repIndex.toString(), "on" to assisted.toString()))
        next
    }

    fun setSide(side: String) {
        if (side != "L" && side != "R") return
        val type = currentExerciseType ?: return
        if (!ExerciseCatalog.definition(type).unilateral) return
        sideByExercise[currentExerciseIndex] = side
        _workoutState.value = _workoutState.value.copy(side = side)
        logEdit("side", mapOf("side" to side))
    }

    fun updateNotes(notes: String) {
        val review = _workoutState.value.review ?: return
        _workoutState.value = _workoutState.value.copy(
            review = review.copy(notes = notes),
            notes = notes,
        )
    }

    fun pauseRest() {
        val state = _workoutState.value
        if (state.phase != WorkoutPhase.Resting || state.restPaused) return
        _workoutState.value = state.copy(restPaused = true)
    }

    fun resumeRest() {
        val state = _workoutState.value
        if (state.phase != WorkoutPhase.Resting || !state.restPaused) return
        _workoutState.value = state.copy(restPaused = false)
    }

    fun stopRestAutostart() {
        val state = _workoutState.value
        if (state.phase != WorkoutPhase.Resting) return
        _workoutState.value = state.copy(restAutostart = false)
    }

    fun tickRest(deltaMs: Long): Long {
        val state = _workoutState.value
        if (state.phase != WorkoutPhase.Resting || state.restPaused) return _restRemainingMs.value
        val next = (_restRemainingMs.value - deltaMs).coerceAtLeast(0L)
        _restRemainingMs.value = next
        return next
    }

    fun onRestFinished() {
        val state = _workoutState.value
        if (state.phase != WorkoutPhase.Resting || state.restPaused || _restRemainingMs.value > 0L) return
        if (state.restAutostart) startSet()
    }

    fun confirmSet() {
        val state = _workoutState.value
        val review = state.review ?: return
        if (state.phase != WorkoutPhase.Reviewing) return
        // Drop Reviewing immediately so a second tap cannot save the set twice
        // while finishWorkout() is still in flight.
        _workoutState.value = state.copy(phase = WorkoutPhase.Idle)

        val accepted = review.reps.filter { it.accepted }
        _workoutState.value.side?.let { sideByExercise[currentExerciseIndex] = it }
        currentExerciseType?.let { type ->
            fullRomStore.raiseFromConfirmedSet(
                exerciseId = type.type,
                acceptedAmplitudes = accepted.map { it.romEstimate },
                userRecalibrated = fullRomRecalibratedThisReview,
            )
        }
        fullRomRecalibratedThisReview = false
        logEdit(
            "confirm",
            mapOf(
                "accepted" to accepted.size.toString(),
                "shown" to review.reps.size.toString(),
                "reps" to repSummary(accepted),
                "side" to (_workoutState.value.side ?: ""),
                "assisted" to accepted.count { it.assisted }.toString(),
            ),
        )
        saveConfirmedSet(review, accepted)
        val plan = currentPlan ?: return
        val currentItem = plan.items.getOrNull(currentExerciseIndex) ?: return
        val wasLastSet = currentSetNumber == currentItem.sets
        currentSetNumber++

        if (currentSetNumber > currentItem.sets) {
            if (wasLastSet) finishExercise()
            currentExerciseIndex++
            currentSetNumber = 1
            if (currentExerciseIndex >= plan.items.size) {
                _workoutState.value = _workoutState.value.copy(review = null, notes = null)
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
        val resting = RestTimerPrefs.isEnabled(prefs)
        val nextSide = rememberedSide()
        _recordingHud.value = RecordingHud()
        _restRemainingMs.value = if (resting) RestTimerPrefs.getSeconds(prefs) * 1000L else 0L
        _workoutState.value = _workoutState.value.copy(
            phase = if (resting) WorkoutPhase.Resting else WorkoutPhase.Idle,
            currentSetNumber = currentSetNumber,
            currentExerciseIndex = currentExerciseIndex,
            review = null,
            notes = null,
            side = nextSide,
            restPaused = false,
            restAutostart = resting && RestTimerPrefs.isAutostartEnabled(prefs),
        )
    }

    fun stopWorkout() {
        samples.clear()
        val wasTracking = _workoutState.value.isTracking
        val shouldDiscard = wasTracking && (sessionSetCount == 0 || sessionRepCount == 0)
        _recordingHud.value = RecordingHud()
        _restRemainingMs.value = 0L
        _workoutState.value = _workoutState.value.copy(
            isTracking = false,
            phase = WorkoutPhase.Idle,
            review = null,
            notes = null,
            restPaused = false,
            restAutostart = false,
        )
        if (shouldDiscard && workoutId.isNotBlank()) {
            launchCatching {
                workoutRepository.deleteWorkout(workoutId)
            }
        }
    }

    /** Logged sets with reps open the summary. A workout with no reps goes home. */
    fun leaveWorkout() {
        if (sessionSetCount > 0 && sessionRepCount > 0) {
            finishWorkout()
        } else {
            stopWorkout()
            launchCatching { _navigationEvent.emit("") }
        }
    }

    fun setError(message: String?) {
        _error.value = message
    }

    fun adjustWeight(deltaKg: Float) {
        val current = _workoutState.value.weight
        val newWeight = (current + deltaKg).coerceAtLeast(0f)
        val rounded = Math.round(newWeight * 100f) / 100f
        _workoutState.value = _workoutState.value.copy(weight = rounded)
    }

    fun setWeight(newWeightKg: Float) {
        val rounded = Math.round(newWeightKg.coerceAtLeast(0f) * 100f) / 100f
        _workoutState.value = _workoutState.value.copy(weight = rounded)
    }

    private fun rememberedSide(): String? {
        val type = currentExerciseType ?: return null
        if (!ExerciseCatalog.definition(type).unilateral) return null
        return sideByExercise[currentExerciseIndex]
    }

    private fun repSummary(reps: List<DetectedRep>): String =
        reps.joinToString("|") { rep ->
            val flag = when {
                rep.assisted && rep.accepted -> "as"
                rep.accepted -> "a"
                else -> "c"
            }
            "${rep.index}:$flag:${rep.romEstimate}:${rep.romScore.toInt()}:${rep.durationMs}:${rep.tags.joinToString(",")}"
        }

    private fun logEdit(action: String, fields: Map<String, String> = emptyMap()) {
        try {
            repEditLog.append(
                action,
                fields + mapOf(
                    "exercise" to (currentExerciseType?.type ?: ""),
                    "set" to currentSetNumber.toString(),
                ),
            )
        } catch (e: Exception) {
            Log.w("WorkoutVM", "Rep edit log skipped", e)
        }
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
            weight = _workoutState.value.weight.takeIf { it > 0f } ?: item.weight,
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
        val measuredRom = perSetRom(accepted)
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
            romEstimate = measuredRom?.estimate,
            romUnit = measuredRom?.unit?.name,
            stabilityScore = stability,
            tempoScore = tempo,
            formScore = form,
            avgRepTime = avgTime,
            totalDurationMillis = duration,
            twistDeg = twist,
            levelDeg = level,
            flags = review.analysis.flags,
            repRecords = records,
            side = _workoutState.value.side,
            notes = review.notes.trim().ifEmpty { null },
        )
        val repsCount = accepted.size
        sessionRepCount += repsCount
        sessionFormSum += form
        sessionSetCount += 1
        sessionVolume += weight * repsCount
        sessionDurationMs += duration
        exerciseFormSum += form
        exerciseSetCount += 1
        exerciseVolume += weight * repsCount
        if (repsCount > 0) {
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
        if (plan.items.isEmpty() || sessionSetCount == 0 || sessionRepCount == 0) {
            launchCatching {
                if (workoutId.isNotBlank()) {
                    workoutRepository.deleteWorkout(workoutId)
                }
                stopWorkout()
                _navigationEvent.emit("")
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
            totalSets = sessionSetCount,
            overallScore = overall,
            totalVolume = sessionVolume,
            totalDurationMillis = sessionDurationMs,
        )
        launchCatching {
            workoutRepository.saveWorkout(workout)
            recordWeeklyStreak(workout.id)
            try {
                achievementSyncService.sync()
            } catch (e: Exception) {
                Log.w("WorkoutVM", "Achievement sync failed", e)
            }
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
        fullRomRecalibratedThisReview = false
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
            notes = "",
        )
        _recordingHud.value = RecordingHud(sampleCount = sampleCount, recordingSeconds = 0f)
        _workoutState.value = _workoutState.value.copy(
            phase = WorkoutPhase.Reviewing,
            review = review,
            notes = "",
        )
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
        val review: SetReviewState? = null,
        val notes: String? = null,
        val side: String? = null,
        val restPaused: Boolean = false,
        val restAutostart: Boolean = false,
    )

    /** Narrow Recording HUD — updated on throttled sensor ticks only. */
    data class RecordingHud(
        val sampleCount: Int = 0,
        val recordingSeconds: Float = 0f,
    )
}
