package com.pixelfitquest.feature.workout

import android.content.Context
import android.hardware.SensorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
import com.pixelfitquest.feature.workout.model.enums.WorkoutFeedback
import com.pixelfitquest.feature.workout.orientation.WorkoutOrientationLock
import com.pixelfitquest.feature.workout.orientation.WorkoutOrientationPrefs
import com.pixelfitquest.feature.workout.sensor.SensorSession
import com.pixelfitquest.feature.workoutBuilder.model.WorkoutPlan
import com.pixelfitquest.ui.navigation.HOME_SCREEN
import com.pixelfitquest.ui.theme.determination
import com.pixelfitquest.ui.theme.spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

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
    var currentFeedback by remember { mutableStateOf<WorkoutFeedback?>(null) }
    val animState = remember { Animatable(0f) }
    var countdownNumber by remember { mutableStateOf<Int?>(null) }

    val currentItem = plan.items.getOrNull(state.currentExerciseIndex)
    val currentDefinition = currentItem?.exercise?.let { ExerciseCatalog.definition(it) }
    val currentExercise = currentDefinition?.displayName ?: "Unknown"
    val currentImu = currentDefinition?.imuSupported == true
    val currentSets = currentItem?.sets ?: 0
    val currentWeight = currentItem?.weight ?: 0.0

    LaunchedEffect(Unit) {
        viewModel.countdownEvent.collectLatest {
            countdownNumber = 3
            repeat(3) { i ->
                delay(1000L)
                countdownNumber = 3 - i - 1
            }
            countdownNumber = -1
            viewModel.onCountdownFinished()
            delay(1000L)
            countdownNumber = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.feedbackEvent.collect { feedback ->
            if (animState.isRunning) {
                animState.snapTo(1f)
                animState.animateTo(0f)
            }
            currentFeedback = feedback
            animState.snapTo(0f)
            animState.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = 500f),
            )
            delay(300L)
            animState.animateTo(0f)
            currentFeedback = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { workoutId ->
            navController.navigate("workout_resume/$workoutId") {
                popUpTo(HOME_SCREEN) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(Unit) {
        if (plan.items.isEmpty()) {
            viewModel.setError(context.getString(R.string.no_plan_error))
            openScreen("workout_customization")
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

        Image(
            painter = painterResource(id = R.drawable.gym_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )

        Row(
            modifier = Modifier
                .padding(top = spacing.md, start = spacing.md, end = spacing.md)
                .align(Alignment.TopCenter),
        ) {
            val status = when (state.phase) {
                WorkoutPhase.Recording -> stringResource(
                    R.string.workout_status_recording,
                    state.currentSetNumber,
                    currentSets,
                    currentWeight,
                    state.recordingSeconds,
                )
                WorkoutPhase.Countdown -> stringResource(
                    R.string.workout_status_countdown,
                    state.currentSetNumber,
                    currentSets,
                )
                WorkoutPhase.Reviewing -> stringResource(
                    R.string.workout_status_review,
                    state.currentSetNumber,
                    currentSets,
                )
                WorkoutPhase.Idle -> stringResource(
                    if (currentImu) R.string.workout_status_idle else R.string.workout_status_idle_log,
                    state.currentSetNumber,
                    currentSets,
                    currentWeight,
                )
            }
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

        if (state.phase != WorkoutPhase.Reviewing) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = spacing.xl),
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
                    WorkoutPhase.Idle -> PixelArtButton(
                        onClick = { viewModel.startSet() },
                        imageRes = R.drawable.play_button_unclicked,
                        pressedRes = R.drawable.play_button_clicked,
                        modifier = Modifier.size(buttonSize),
                    )
                    else -> Box(Modifier.size(buttonSize))
                }

                Box(modifier = Modifier.padding(top = spacing.lg, start = spacing.md, end = spacing.md)) {
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
                    onClick = {
                        viewModel.stopWorkout()
                        openScreen("workout_customization")
                    },
                    imageRes = R.drawable.stop_button_unclicked,
                    pressedRes = R.drawable.stop_button_clicked,
                    modifier = Modifier.size(buttonSize),
                )
            }
        }

        val isRecording = state.phase == WorkoutPhase.Recording
        val workoutBob = if (isRecording) {
            val transition = rememberInfiniteTransition(label = "workoutMotion")
            val bob by transition.animateFloat(
                initialValue = -10f,
                targetValue = 10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = FastOutSlowInEasing),
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
                .padding(spacing.xl),
            contentAlignment = Alignment.BottomCenter,
        ) {
            CharacterIdleAnimation(
                modifier = Modifier
                    .size(characterSize)
                    .graphicsLayer { translationY = workoutBob },
                gender = characterData.gender,
                variant = characterData.variant,
                isAnimating = true,
            )
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            countdownNumber?.let { number ->
                val text = if (number >= 0) "$number" else stringResource(R.string.workout_go)
                val color = if (number >= 0) Color.Yellow else Color.Green
                Text(
                    text = text,
                    fontSize = if (landscape) 72.sp else 120.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontFamily = determination,
                    modifier = Modifier
                        .scale(1.2f)
                        .padding(bottom = spacing.scale(10))
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(spacing.lg))
                        .padding(horizontal = spacing.xxl, vertical = spacing.sm),
                )
            }

            currentFeedback?.let { feedback ->
                Text(
                    text = feedback.text,
                    fontFamily = determination,
                    fontSize = 48.sp,
                    color = feedback.color,
                    modifier = Modifier.graphicsLayer {
                        scaleX = animState.value * feedback.scale
                        scaleY = animState.value * feedback.scale
                        alpha = animState.value
                    },
                )
            }
        }

        if (state.phase == WorkoutPhase.Recording) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
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
                    onAcceptCandidate = viewModel::acceptCandidate,
                    onRemove = viewModel::removeRep,
                    onMerge = viewModel::mergeWithNext,
                    onAdd = viewModel::addRep,
                    onAdjustRom = viewModel::adjustRom,
                    onSetRom = viewModel::setRom,
                    onRedo = viewModel::redoSet,
                    onConfirm = viewModel::confirmSet,
                )
            }
        }
    }
}
