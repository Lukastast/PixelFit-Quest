package com.pixelfitquest.feature.workoutResume

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pixelfitquest.R
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.feature.healthbonuses.ui.SessionBonusesCard
import com.pixelfitquest.feature.streak.WeeklyStreakBonusBanner
import com.pixelfitquest.feature.streak.WeeklyStreakViewModel
import com.pixelfitquest.feature.workout.model.enums.displayName
import com.pixelfitquest.ui.navigation.PROGRESS_SCREEN
import com.pixelfitquest.feature.workout.analysis.BAR_COACH_DEG
import com.pixelfitquest.feature.workout.analysis.BAR_EVEN_DEG
import com.pixelfitquest.feature.workout.analysis.BAR_METER_SPAN_DEG
import com.pixelfitquest.ui.theme.LocalSpacing
import com.pixelfitquest.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutResumeScreen(
    openScreen: (String) -> Unit,
    onWorkoutDeleted: () -> Unit,
    viewModel: WorkoutResumeViewModel,
    weeklyStreakViewModel: WeeklyStreakViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val summary by viewModel.summary.collectAsState()
    val exercisesWithSets by viewModel.exercisesWithSets.collectAsState()
    val bonusUi by viewModel.bonusUi.collectAsState()
    val isDeleting by viewModel.isDeleting.collectAsState()
    val spacing = MaterialTheme.spacing
    val weeklyStreak by weeklyStreakViewModel.snapshot.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.deleted.collect { onWorkoutDeleted() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.resume_title)) },
                navigationIcon = {
                    IconButton(onClick = { openScreen("home") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back_desc)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6C7A88)
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .paint(
                    painter = painterResource(id = R.drawable.logsigninbackground),
                    contentScale = ContentScale.Crop
                )
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
            contentPadding = PaddingValues(spacing.md)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(spacing.md),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                ) {
                    Column(modifier = Modifier.padding(spacing.md)) {
                        Text(
                            stringResource(R.string.session_complete),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(spacing.xs))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(spacing.lg),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(Modifier.width(spacing.scale(6)))
                                Text(
                                    stringResource(R.string.xp_reward, summary.totalXp),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Green
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(painterResource(R.drawable.coin), null, Modifier.size(spacing.scale(28)))
                                Spacer(Modifier.width(spacing.scale(6)))
                                Text(
                                    stringResource(R.string.coins_reward, summary.totalCoins),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )
                            }
                        }

                        if (summary.rewardClipped) {
                            Spacer(Modifier.height(spacing.xs))
                            Text(
                                text = stringResource(R.string.daily_reward_clipped),
                                color = Color(0xFFFFD700),
                                fontSize = 13.sp,
                            )
                        }
                        Spacer(Modifier.height(spacing.xs))
                        WeeklyStreakBonusBanner(snapshot = weeklyStreak)

                        Spacer(Modifier.height(spacing.scale(20)))

                        Text(
                            text = stringResource(R.string.exercise_feedback_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        Spacer(Modifier.height(spacing.xs))

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = spacing.scale(400)),
                            verticalArrangement = Arrangement.spacedBy(spacing.sm)
                        ) {
                            items(exercisesWithSets) { item ->
                                val exercise = item.exercise
                                val avgRom = item.avgFormScore.toInt()
                                val avgLevel = item.sets.map { it.levelDeg }.average().toFloat()
                                val avgTwist = item.sets.map { it.twistDeg }.average().toFloat()

                                ExerciseMiniFeedback(
                                    exerciseName = exercise.type.displayName(),
                                    avgRomScore = avgRom,
                                    avgLevelDeg = avgLevel,
                                    avgTwistDeg = avgTwist,
                                )
                            }
                        }
                    }
                }
            }

            if (bonusUi.loaded) {
                item {
                    SessionBonusesCard(
                        bonuses = bonusUi.bonuses,
                        snapshot = bonusUi.snapshot,
                    )
                }
            }

            item {
                PixelArtButton(
                    onClick = { openScreen(PROGRESS_SCREEN) },
                    imageRes = R.drawable.button_unclicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(spacing.scale(56)),
                ) {
                    Text(
                        text = stringResource(R.string.progress_resume_cta),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            item {
                PixelArtButton(
                    onClick = { if (!isDeleting) showDeleteDialog = true },
                    imageRes = R.drawable.button_unclicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(spacing.scale(56)),
                ) {
                    Text(
                        text = stringResource(
                            if (isDeleting) R.string.deleting_workout else R.string.delete_workout,
                        ),
                        color = Color(0xFFFF8A80),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            items(exercisesWithSets) { exerciseWithSets ->
                val exercise = exerciseWithSets.exercise
                val sets = exerciseWithSets.sets

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(spacing.cornerMd),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF8A9AA8))
                ) {
                    Column(
                        modifier = Modifier.padding(spacing.md)
                    ) {
                        Text(
                            text = exercise.type.displayName(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.sets_completed_count, sets.size),
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(bottom = spacing.xs)
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(spacing.xs),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            sets.forEach { set ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(spacing.cornerSm),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF566474)
                                    )
                                ) {
                                    Column {

                                        Row(
                                            modifier = Modifier.fillMaxWidth()
                                                .padding(spacing.md),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically

                                        ) {
                                            Text(
                                                text = stringResource(R.string.set_label, set.setNumber),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium
                                            )

                                        }

                                        Spacer(modifier = Modifier.size(spacing.xxs))

                                        Row(modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(stringResource(R.string.rom_score_title), fontSize = 12.sp, color = Color.Gray)
                                                Text(
                                                    stringResource(R.string.score_out_of_100, set.romScore.toInt()),
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = when {
                                                        set.romScore >= 90 -> Color.Green
                                                        set.romScore >= 70 -> Color.Yellow
                                                        else -> Color(0xFFFFA500)
                                                    }
                                                )
                                            }
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(stringResource(R.string.set_score_title), fontSize = 12.sp, color = Color.Gray)
                                                Text(
                                                    text = stringResource(R.string.score_out_of_100, set.formScore.toInt()),
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when {
                                                        set.formScore >= 90 -> Color.Green
                                                        set.formScore >= 70 -> Color.Yellow
                                                        set.formScore >= 50 -> Color(0xFFFFA500)
                                                        else -> Color.Red
                                                    }
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.size(spacing.xxs))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {

                                            Column(
                                                modifier = Modifier.weight(1f),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(stringResource(R.string.reps_label), fontSize = 12.sp, color = Color.Gray)
                                                Text("${set.reps}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                            }

                                            Column(
                                                modifier = Modifier.weight(1f),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(stringResource(R.string.kg_label), fontSize = 12.sp, color = Color.Gray)
                                                Text("${set.weight.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                            }
                                        }

                                        if (set.repRecords.isNotEmpty()) {
                                            Text(
                                                text = set.repRecords.joinToString("  ") { rec ->
                                                    "R${rec.index + 1}:${rec.formScore.toInt()}"
                                                },
                                                fontSize = 11.sp,
                                                color = Color.White.copy(alpha = 0.8f),
                                                modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.xxs),
                                            )
                                        }


                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = spacing.md, end = spacing.md),
                                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                                        ) {
                                            ImbalanceMeter(
                                                title = stringResource(R.string.z_tilt_score_title),
                                                degrees = set.levelDeg,
                                                caption = levelCaption(set.levelDeg),
                                                modifier = Modifier.weight(1f),
                                            )
                                            ImbalanceMeter(
                                                title = stringResource(R.string.x_tilt_score_title),
                                                degrees = set.twistDeg,
                                                caption = twistCaption(set.twistDeg),
                                                modifier = Modifier.weight(1f),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.size(spacing.xs))
            }


            if (exercisesWithSets.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_exercises_completed),
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

    }

    if (showDeleteDialog) {
        DeleteWorkoutDialog(
            onDismiss = { if (!isDeleting) showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteWorkout()
            },
        )
    }
}

@Composable
private fun DeleteWorkoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.questloginboard),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(spacing.dialogHeight),
                contentScale = ContentScale.FillBounds
            )
            Column(
                modifier = Modifier
                    .padding(spacing.xl)
                    .fillMaxWidth(0.95f)
                    .heightIn(max = spacing.scale(300))
            ) {
                Text(
                    text = stringResource(R.string.delete_workout_title),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(spacing.xs))
                Text(
                    text = stringResource(R.string.delete_workout_description),
                    color = Color.White
                )
                Spacer(Modifier.height(spacing.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PixelArtButton(
                        onClick = onDismiss,
                        imageRes = R.drawable.button_unclicked,
                        pressedRes = R.drawable.button_clicked,
                        modifier = Modifier
                            .height(spacing.scale(50))
                            .width(spacing.buttonWidthSm)
                    ) {
                        Text(stringResource(R.string.cancel), color = Color.Black)
                    }
                    PixelArtButton(
                        onClick = onConfirm,
                        imageRes = R.drawable.button_unclicked,
                        pressedRes = R.drawable.button_clicked,
                        modifier = Modifier
                            .height(spacing.scale(50))
                            .width(spacing.buttonWidthSm)
                    ) {
                        Text(
                            text = stringResource(R.string.delete_workout_confirm),
                            color = Color(0xFFB71C1C),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ImbalanceMeter(
    title: String,
    degrees: Float,
    caption: String,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(spacing.scale(6)))
        TiltScoreBar(degrees)
        Text(
            caption,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
        )
    }
}

@Composable
private fun levelCaption(deg: Float): String {
    val mag = kotlin.math.abs(deg).toInt()
    return when {
        deg > BAR_EVEN_DEG -> stringResource(R.string.imbalance_right, mag)
        deg < -BAR_EVEN_DEG -> stringResource(R.string.imbalance_left, mag)
        else -> stringResource(R.string.imbalance_even)
    }
}

@Composable
private fun twistCaption(deg: Float): String {
    val mag = kotlin.math.abs(deg).toInt()
    return when {
        deg > BAR_EVEN_DEG -> stringResource(R.string.imbalance_head, mag)
        deg < -BAR_EVEN_DEG -> stringResource(R.string.imbalance_hip, mag)
        else -> stringResource(R.string.imbalance_even)
    }
}

@Composable
fun TiltScoreBar(
    degrees: Float,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(LocalSpacing.current.scale(14))
) {
    val spacing = LocalSpacing.current
    val span = BAR_METER_SPAN_DEG
    val clamped = degrees.coerceIn(-span, span)
    val positionFraction = (clamped + span) / (2f * span)

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(spacing.scale(7)))
    ) {

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Red,
                            Color.Green,
                            Color.Red
                        )
                    )
                )
        )

        val markerX = (constraints.maxWidth * positionFraction).toInt()

        Box(
            modifier = Modifier
                .offset { IntOffset(markerX - 6, 0) }
                .size(spacing.sm)
                .clip(CircleShape)
                .background(Color.White)
                .border(spacing.xxxs, Color.Black.copy(alpha = 0.6f), CircleShape)
        )
    }
}

    @Composable
    fun ExerciseMiniFeedback(
        exerciseName: String,
        avgRomScore: Int,
        avgLevelDeg: Float,
        avgTwistDeg: Float,
    ) {
        val spacing = LocalSpacing.current
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(spacing.cornerMd))
                .padding(spacing.sm)
        ) {
            Text(
                text = exerciseName,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                fontSize = 15.sp
            )

            Spacer(Modifier.height(spacing.scale(6)))

            if (avgRomScore < 90) {
                FeedbackLine(
                    icon = painterResource(R.drawable.ic_pushup_person),
                    text = stringResource(R.string.feedback_rom_low),
                    color = Color(0xFFFF8800)
                )
            } else {
                FeedbackLine(
                    icon = painterResource(R.drawable.ic_check_circle),
                    text = stringResource(R.string.feedback_rom_perfect),
                    color = Color.Green
                )
            }

            when {
                avgLevelDeg > BAR_COACH_DEG -> FeedbackLine(
                    icon = painterResource(R.drawable.ic_balance_scale),
                    text = stringResource(R.string.feedback_tilt_right),
                    color = Color(0xFFFF8800)
                )
                avgLevelDeg < -BAR_COACH_DEG -> FeedbackLine(
                    icon = painterResource(R.drawable.ic_balance_scale),
                    text = stringResource(R.string.feedback_tilt_left),
                    color = Color(0xFFFF8800)
                )
                else -> FeedbackLine(
                    icon = painterResource(R.drawable.ic_check_circle),
                    text = stringResource(R.string.feedback_tilt_perfect),
                    color = Color.Green
                )
            }

            if (avgTwistDeg > BAR_COACH_DEG) {
                FeedbackLine(
                    text = stringResource(R.string.feedback_x_tilt_right),
                    color = Color(0xFFFF8800)
                )
            } else if (avgTwistDeg < -BAR_COACH_DEG) {
                FeedbackLine(
                    text = stringResource(R.string.feedback_x_tilt_left),
                    color = Color(0xFFFF8800)
                )
            }
        }
    }

    @Composable
    fun FeedbackLine(
        icon: Painter? = null,
        text: String,
        color: Color
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon?.let {
                Image(painter = it, contentDescription = null, modifier = Modifier.size(LocalSpacing.current.scale(20)))
                Spacer(Modifier.width(LocalSpacing.current.scale(6)))
            }
            Text(text, color = color, fontSize = 13.sp)
        }
    }
