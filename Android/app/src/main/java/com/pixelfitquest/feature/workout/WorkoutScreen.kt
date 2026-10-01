package com.pixelfitquest.feature.workout

import android.content.Context
import android.hardware.SensorManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.pixelfitquest.R
import com.pixelfitquest.components.atoms.CharacterIdleAnimation
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.feature.workout.catalog.ExerciseCatalog
import com.pixelfitquest.feature.workout.model.WorkoutPhase
import com.pixelfitquest.feature.workout.orientation.WorkoutOrientationLock
import com.pixelfitquest.feature.workout.orientation.WorkoutOrientationPrefs
import com.pixelfitquest.feature.workout.sensor.SensorSession
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlan
import com.pixelfitquest.ui.navigation.HOME_SCREEN
import com.pixelfitquest.ui.theme.spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

@Composable
fun WorkoutScreen(
    plan: WorkoutPlan,
    templateName: String = "workout",
    openScreen: (String) -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val viewModel: WorkoutViewModel = hiltViewModel()
    val state by viewModel.workoutState.collectAsState()

    BackHandler {
        viewModel.leaveWorkout()
    }

    val context = LocalContext.current
    val landscapeEnabled = remember {
        WorkoutOrientationPrefs.isEnabled(
            context.getSharedPreferences(WorkoutOrientationPrefs.PREFS_NAME, Context.MODE_PRIVATE),
        )
    }
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    val session = remember(sensorManager) {
        SensorSession(
            sensorManager = sensorManager,
            onAccelTick = viewModel::onRecordingTick,
            onMissingAccelerometer = {
                viewModel.setError(context.getString(R.string.no_accelerometer_error))
            },
        )
    }

    val characterData by viewModel.characterData.collectAsState()

    val currentItem = plan.items.getOrNull(state.currentExerciseIndex)
    val currentDefinition = currentItem?.exercise?.let { ExerciseCatalog.definition(it) }
    val currentExercise = currentDefinition?.displayName ?: "Unknown"
    val currentImu = currentDefinition?.imuSupported == true
    val currentSets = currentItem?.sets ?: 0
    val currentWeight = state.weight

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { workoutId ->
            val route = if (workoutId.isBlank()) HOME_SCREEN else "workout_resume/$workoutId"
            navController.navigate(route) {
                popUpTo(HOME_SCREEN) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(Unit) {
        if (plan.items.isEmpty()) {
            viewModel.setError(context.getString(R.string.no_plan_error))
            navController.navigate(HOME_SCREEN) {
                popUpTo(HOME_SCREEN) { inclusive = false }
                launchSingleTop = true
            }
            return@LaunchedEffect
        }
        viewModel.startWorkoutFromPlan(plan, templateName)
    }

    if (plan.items.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.invalid_plan_error))
        }
        return
    }

    WorkoutOrientationLock(
        phase = state.phase,
        landscapeEnabled = landscapeEnabled,
    )

    LaunchedEffect(state.phase, state.restPaused) {
        if (state.phase != WorkoutPhase.Resting || state.restPaused) return@LaunchedEffect
        while (true) {
            // Display is second-granularity; 500ms is enough (P1-1). Delay before first
            // tick so a 90s rest is not short by one interval.
            delay(500)
            val left = viewModel.tickRest(500)
            if (left <= 0L) break
        }
        viewModel.onRestFinished()
    }

    LaunchedEffect(state.phase) {
        if (state.phase == WorkoutPhase.Recording) {
            session.clear()
            session.register()
        } else {
            session.unregister()
        }
    }

    DisposableEffect(session) {
        onDispose { session.unregister() }
    }

    val spacing = MaterialTheme.spacing

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val landscape = maxWidth > maxHeight
        val buttonSize = if (landscape) spacing.scale(64) else spacing.barMd
        val characterSize = if (landscape) spacing.scale(72) else spacing.scale(120)

        val gymBackgroundRes = remember(characterData.equippedGym) {
            com.pixelfitquest.feature.customization.model.CustomizationCatalog.gymDrawable(
                characterData.equippedGym
            )
        }

        Image(
            painter = painterResource(id = gymBackgroundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        WorkoutStatusHud(
            phase = state.phase,
            currentSetNumber = state.currentSetNumber,
            currentSets = currentSets,
            currentWeight = currentWeight,
            currentImu = currentImu,
            recordingHud = viewModel.recordingHud,
            modifier = Modifier
                .statusBarsPadding()
                .displayCutoutPadding()
                .padding(top = spacing.xs, start = spacing.md, end = spacing.md)
                .align(Alignment.TopCenter),
        )

        if (state.phase != WorkoutPhase.Reviewing) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .displayCutoutPadding()
                    .padding(top = spacing.xl + spacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    when (state.phase) {
                        WorkoutPhase.Recording -> PixelArtButton(
                            onClick = {
                                val recorded = session.snapshotInterpolated()
                                session.unregister()
                                viewModel.finishSet(recorded)
                            },
                            imageRes = R.drawable.pause_button_unclicked,
                            pressedRes = R.drawable.pause_button_clicked,
                            modifier = Modifier.size(buttonSize),
                        )
                        WorkoutPhase.Idle, WorkoutPhase.Resting -> PixelArtButton(
                            onClick = { viewModel.startSet() },
                            imageRes = R.drawable.play_button_unclicked,
                            pressedRes = R.drawable.play_button_clicked,
                            modifier = Modifier.size(buttonSize),
                        )
                        else -> Box(Modifier.size(buttonSize))
                    }

                    Box(modifier = Modifier.padding(start = spacing.md, end = spacing.md)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(spacing.cornerSm))
                                .padding(horizontal = spacing.sm, vertical = spacing.xxs),
                        ) {
                            Text(
                                text = currentExercise,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                            Text(
                                text = stringResource(
                                    if (currentImu) R.string.exercise_badge_imu else R.string.exercise_badge_log,
                                ),
                                color = if (currentImu) Color(0xFFA5D6A7) else Color(0xFFBDBDBD),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = spacing.xs),
                            )
                        }
                    }
                    PixelArtButton(
                        onClick = { viewModel.leaveWorkout() },
                        imageRes = R.drawable.stop_button_unclicked,
                        pressedRes = R.drawable.stop_button_clicked,
                        modifier = Modifier.size(buttonSize),
                    )
                }

                if (state.phase == WorkoutPhase.Idle || state.phase == WorkoutPhase.Resting) {
                    Spacer(modifier = Modifier.height(spacing.xs))
                    BetweenSetsWeightHud(
                        weight = state.weight,
                        onAdjustWeight = viewModel::adjustWeight,
                    )
                    if (currentDefinition?.unilateral == true) {
                        Spacer(modifier = Modifier.height(spacing.xs))
                        SidePicker(
                            side = state.side,
                            onSide = viewModel::setSide,
                        )
                    }
                }
                if (state.phase == WorkoutPhase.Resting) {
                    Spacer(modifier = Modifier.height(spacing.sm))
                    RestTimerHud(
                        restRemainingMs = viewModel.restRemainingMs,
                        paused = state.restPaused,
                        autostart = state.restAutostart,
                        onPause = viewModel::pauseRest,
                        onResume = viewModel::resumeRest,
                        onStopAutostart = viewModel::stopRestAutostart,
                        modifier = Modifier.padding(horizontal = spacing.md),
                    )
                }
            }
        }

        val isRecording = state.phase == WorkoutPhase.Recording
        // P0-2: no infinite bob during Recording — IMU→HUD updates already stress the frame budget.
        // Light bob only while idle / resting (not reviewing).
        val workoutBob = if (!isRecording && state.phase != WorkoutPhase.Reviewing) {
            val transition = rememberInfiniteTransition(label = "workoutMotion")
            val bob by transition.animateFloat(
                initialValue = -6f,
                targetValue = 6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(700, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "workoutBob",
            )
            bob
        } else {
            0f
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(bottom = spacing.lg),
            contentAlignment = Alignment.BottomCenter,
        ) {
            CharacterIdleAnimation(
                modifier = Modifier
                    .size(characterSize)
                    .graphicsLayer { translationY = workoutBob },
                gender = characterData.gender,
                variant = characterData.variant,
                isAnimating = !isRecording,
            )
        }

        if (state.phase == WorkoutPhase.Recording) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .displayCutoutPadding()
                    .padding(end = spacing.sm)
                    .background(Color(0xCC1B5E20), RoundedCornerShape(spacing.cornerSm))
                    .padding(horizontal = spacing.scale(10), vertical = spacing.xs),
            ) {
                Text(
                    text = stringResource(R.string.workout_recording_badge),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                )
            }
        }

        state.review?.let { review ->
            if (state.phase == WorkoutPhase.Reviewing) {
                SetReviewOverlay(
                    review = review,
                    currentWeight = state.weight,
                    onAdjustWeight = viewModel::adjustWeight,
                    onAcceptCandidate = viewModel::acceptCandidate,
                    onRemove = viewModel::removeRep,
                    onMerge = viewModel::mergeWithNext,
                    onAdd = viewModel::addRep,
                    onAdjustRom = viewModel::adjustRom,
                    onSetRom = viewModel::setRom,
                    onToggleAssisted = viewModel::toggleAssisted,
                    unilateral = currentDefinition?.unilateral == true,
                    side = state.side,
                    onSide = viewModel::setSide,
                    onRedo = viewModel::redoSet,
                    onConfirm = viewModel::confirmSet,
                )
            }
        }
    }
}


@Composable
private fun WorkoutStatusHud(
    phase: WorkoutPhase,
    currentSetNumber: Int,
    currentSets: Int,
    currentWeight: Float,
    currentImu: Boolean,
    recordingHud: StateFlow<WorkoutViewModel.RecordingHud>,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    // Only this leaf recomposes on throttled recording ticks.
    val hud by recordingHud.collectAsState()
    val status = when (phase) {
        WorkoutPhase.Recording -> stringResource(
            R.string.workout_status_recording,
            currentSetNumber,
            currentSets,
            currentWeight,
            hud.recordingSeconds,
        )
        WorkoutPhase.Reviewing -> stringResource(
            R.string.workout_status_review,
            currentSetNumber,
            currentSets,
        )
        WorkoutPhase.Resting -> stringResource(
            R.string.workout_status_resting,
            currentSetNumber,
            currentSets,
        )
        WorkoutPhase.Idle -> stringResource(
            if (currentImu) R.string.workout_status_idle else R.string.workout_status_idle_log,
            currentSetNumber,
            currentSets,
            currentWeight,
        )
    }
    Row(modifier = modifier) {
        Text(
            text = status,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(spacing.cornerSm))
                .padding(horizontal = spacing.scale(10)),
        )
    }
}

@Composable
private fun RestTimerHud(
    restRemainingMs: StateFlow<Long>,
    paused: Boolean,
    autostart: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStopAutostart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val remainingMs by restRemainingMs.collectAsState()
    RestTimerCard(
        remainingMs = remainingMs,
        paused = paused,
        autostart = autostart,
        onPause = onPause,
        onResume = onResume,
        onStopAutostart = onStopAutostart,
        modifier = modifier,
    )
}

@Composable
private fun SidePicker(
    side: String?,
    onSide: (String) -> Unit,
) {
    val spacing = MaterialTheme.spacing
    Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
        SideChip(stringResource(R.string.set_side_left), side == "L") { onSide("L") }
        SideChip(stringResource(R.string.set_side_right), side == "R") { onSide("R") }
    }
}

@Composable
private fun SideChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val spacing = MaterialTheme.spacing
    Text(
        text = label,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        modifier = Modifier
            .background(
                if (selected) Color(0xFF1565C0) else Color.Black.copy(alpha = 0.7f),
                RoundedCornerShape(spacing.cornerSm),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.sm, vertical = spacing.xxs),
    )
}
