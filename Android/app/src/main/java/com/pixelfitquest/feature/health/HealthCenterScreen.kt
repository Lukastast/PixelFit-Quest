package com.pixelfitquest.feature.health

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pixelfitquest.R
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.feature.achievements.AchievementsScreen
import com.pixelfitquest.feature.achievements.AchievementsViewModel
import com.pixelfitquest.feature.missions.MissionMetric
import com.pixelfitquest.feature.missions.MissionProgress
import com.pixelfitquest.feature.missions.WeeklyMissionBoard
import com.pixelfitquest.feature.missions.groupedCount
import com.pixelfitquest.health.HealthConnectIntents
import com.pixelfitquest.health.HealthConnectStatus
import com.pixelfitquest.health.HealthMetrics
import com.pixelfitquest.health.HealthRewards
import com.pixelfitquest.ui.theme.DarkStone
import com.pixelfitquest.ui.theme.FireOrange
import com.pixelfitquest.ui.theme.QuestBrown
import com.pixelfitquest.ui.theme.PixelFitWidthClass
import com.pixelfitquest.ui.theme.RewardGold
import com.pixelfitquest.ui.theme.VitalGreen
import com.pixelfitquest.ui.theme.spacing

@Composable
fun HealthCenterScreen(
    viewModel: HealthCenterViewModel = hiltViewModel(),
    achievementsViewModel: AchievementsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val spacing = MaterialTheme.spacing
    val useTwoPane = isLandscape || spacing.widthClass != PixelFitWidthClass.Compact
    val healthStatus by viewModel.healthStatus.collectAsState()
    val permissionsGranted by viewModel.permissionsGranted.collectAsState()
    val metrics by viewModel.healthMetrics.collectAsState()
    val section by viewModel.section.collectAsState()
    val claims by viewModel.healthClaims.collectAsState()
    val board by viewModel.weeklyBoard.collectAsState()
    val achievements by achievementsViewModel.uiState.collectAsState()

    val permissionContract = remember {
        PermissionController.createRequestPermissionResultContract()
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = permissionContract
    ) { granted ->
        viewModel.onPermissionsResult(granted)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = if (useTwoPane) spacing.md else spacing.screen,
                vertical = if (useTwoPane) spacing.xs else spacing.sm,
            ),
    ) {
        Text(
            text = stringResource(R.string.quest_center_title),
            fontSize = if (useTwoPane) 18.sp else 22.sp,
            fontWeight = FontWeight.Bold,
            color = RewardGold,
        )
        Text(
            text = stringResource(R.string.quest_center_subtitle),
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.75f),
        )
        Spacer(modifier = Modifier.height(spacing.sm))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            QuestTab(
                label = stringResource(R.string.quest_tab_missions),
                count = stringResource(
                    R.string.quest_tab_count,
                    board.completedCount,
                    board.missions.size,
                ),
                selected = section == QuestSection.MISSIONS,
                onClick = { viewModel.onSectionSelected(QuestSection.MISSIONS) },
                modifier = Modifier.weight(1f),
            )
            QuestTab(
                label = stringResource(R.string.quest_tab_achievements),
                count = stringResource(
                    R.string.quest_tab_count,
                    achievements.unlockedCount,
                    achievements.totalCount,
                ),
                selected = section == QuestSection.ACHIEVEMENTS,
                onClick = { viewModel.onSectionSelected(QuestSection.ACHIEVEMENTS) },
                modifier = Modifier.weight(1f),
            )
            QuestTab(
                label = stringResource(R.string.quest_tab_health),
                count = stringResource(
                    R.string.quest_tab_count,
                    metrics.rewardedGoalsMet,
                    HealthRewards.REWARDED_GOAL_COUNT,
                ),
                selected = section == QuestSection.HEALTH,
                onClick = { viewModel.onSectionSelected(QuestSection.HEALTH) },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(spacing.sm))
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (section) {
                QuestSection.MISSIONS -> MissionsPane(
                    board = board,
                    healthStatus = healthStatus,
                    permissionsGranted = permissionsGranted,
                    useTwoPane = useTwoPane,
                    onOpenHealth = { viewModel.onSectionSelected(QuestSection.HEALTH) },
                    modifier = Modifier.fillMaxSize(),
                )
                QuestSection.ACHIEVEMENTS -> AchievementsScreen(
                    onBack = {},
                    embedded = true,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = achievementsViewModel,
                )
                QuestSection.HEALTH -> HealthPane(
                    healthStatus = healthStatus,
                    permissionsGranted = permissionsGranted,
                    metrics = metrics,
                    claims = claims,
                    useTwoPane = useTwoPane,
                    onConnect = {
                        when (healthStatus) {
                            HealthConnectStatus.AVAILABLE -> {
                                runCatching {
                                    permissionLauncher.launch(viewModel.healthPermissions)
                                }
                            }
                            HealthConnectStatus.UPDATE_REQUIRED -> {
                                HealthConnectIntents.openPlayStore(context)
                            }
                            HealthConnectStatus.UNAVAILABLE -> Unit
                        }
                    },
                    onManage = {
                        runCatching { HealthConnectIntents.openHealthConnectSettings(context) }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun QuestTab(
    label: String,
    count: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = if (selected) RewardGold else QuestBrown
    val textColor = if (selected) DarkStone else Color.White
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = count,
            color = textColor,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun MissionsPane(
    board: WeeklyMissionBoard,
    healthStatus: HealthConnectStatus,
    permissionsGranted: Boolean,
    useTwoPane: Boolean,
    onOpenHealth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    val stepsHint = when {
        permissionsGranted -> null
        healthStatus == HealthConnectStatus.UNAVAILABLE ->
            stringResource(R.string.health_connect_unavailable_hint)
        healthStatus == HealthConnectStatus.UPDATE_REQUIRED ->
            stringResource(R.string.health_connect_update_hint)
        else -> stringResource(R.string.quest_steps_disconnected)
    }
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Text(
            text = stringResource(R.string.quest_week_resets),
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 12.sp,
        )
        if (stepsHint != null) {
            Text(
                text = stepsHint,
                color = RewardGold,
                fontSize = 12.sp,
                modifier = Modifier.clickable(onClick = onOpenHealth),
            )
        }
        if (useTwoPane) {
            board.missions.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    row.forEach { mission ->
                        Box(modifier = Modifier.weight(1f)) {
                            MissionCard(mission)
                        }
                    }
                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            board.missions.forEach { mission ->
                MissionCard(mission)
            }
        }
        Spacer(modifier = Modifier.height(spacing.xs))
    }
}

@Composable
private fun MissionCard(mission: MissionProgress) {
    val definition = mission.definition
    val target = definition.target.coerceAtLeast(0L)
    val done = mission.isComplete || mission.claimed
    val blurb = if (definition.metric == MissionMetric.WEEKLY_STEPS) {
        stringResource(R.string.quest_mission_steps_blurb)
    } else {
        stringResource(R.string.quest_mission_training_blurb)
    }
    QuestCard(
        title = definition.title,
        value = stringResource(
            R.string.quest_mission_progress,
            groupedCount(mission.displayedCurrent()),
            groupedCount(target),
        ),
        subtitle = blurb,
        badge = if (done) {
            stringResource(R.string.quest_mission_done)
        } else {
            stringResource(R.string.quest_percent, (mission.fraction * 100).toInt())
        },
        badgeColor = if (done) VitalGreen else RewardGold,
        fraction = if (done) 1f else mission.fraction,
        complete = done,
        reward = stringResource(
            R.string.health_mission_reward_line,
            definition.xp,
            definition.coins,
        ),
    )
}

@Composable
private fun HealthPane(
    healthStatus: HealthConnectStatus,
    permissionsGranted: Boolean,
    metrics: HealthMetrics,
    claims: HealthGoalClaims,
    useTwoPane: Boolean,
    onConnect: () -> Unit,
    onManage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    val connected = healthStatus == HealthConnectStatus.AVAILABLE && permissionsGranted
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Text(
            text = stringResource(R.string.health_goals_intro),
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 12.sp,
        )
        if (!connected) {
            ConnectCard(
                healthStatus = healthStatus,
                onClick = onConnect,
            )
        }
        if (useTwoPane) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                Box(modifier = Modifier.weight(1f)) { StepsGoalCard(metrics, claims.steps) }
                Box(modifier = Modifier.weight(1f)) { SleepGoalCard(metrics, claims.sleep) }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                Box(modifier = Modifier.weight(1f)) { HeartGoalCard(metrics, claims.heart) }
                Box(modifier = Modifier.weight(1f)) { RestingHeartCard(metrics) }
            }
        } else {
            StepsGoalCard(metrics, claims.steps)
            SleepGoalCard(metrics, claims.sleep)
            HeartGoalCard(metrics, claims.heart)
            RestingHeartCard(metrics)
        }
        if (connected) {
            PixelArtButton(
                onClick = onManage,
                imageRes = R.drawable.button_unclicked,
                pressedRes = R.drawable.button_clicked,
                modifier = Modifier
                    .fillMaxWidth(if (useTwoPane) 0.5f else 1f)
                    .height(if (useTwoPane) spacing.scale(42) else spacing.buttonHeight),
            ) {
                Text(
                    text = stringResource(R.string.health_manage),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
        Spacer(modifier = Modifier.height(spacing.xs))
    }
}

@Composable
private fun ConnectCard(
    healthStatus: HealthConnectStatus,
    onClick: () -> Unit,
) {
    val spacing = MaterialTheme.spacing
    val message = when (healthStatus) {
        HealthConnectStatus.UNAVAILABLE -> stringResource(R.string.health_connect_unavailable_hint)
        HealthConnectStatus.UPDATE_REQUIRED -> stringResource(R.string.health_connect_update_hint)
        HealthConnectStatus.AVAILABLE -> stringResource(R.string.health_connect_card_body)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Image(
            painter = painterResource(id = R.drawable.info_background_higher),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = spacing.scale(90))
                .padding(spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.health_connect_card_title),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = RewardGold,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = message,
                fontSize = 12.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun StepsGoalCard(metrics: HealthMetrics, claimed: Boolean) {
    val goal = metrics.stepGoal.coerceAtLeast(0)
    val met = goal > 0 && metrics.steps >= goal
    val done = met || claimed
    val remaining = (goal.toLong() - metrics.steps).coerceIn(0L, goal.toLong())
    QuestCard(
        title = stringResource(R.string.health_mission_steps_title),
        value = stringResource(
            R.string.quest_mission_progress,
            groupedCount(if (done) goal.toLong() else metrics.steps.coerceAtLeast(0L).coerceAtMost(goal.toLong())),
            groupedCount(goal.toLong()),
        ),
        subtitle = if (met || claimed) {
            stringResource(R.string.health_mission_steps_done)
        } else {
            stringResource(R.string.health_mission_steps_left, remaining.toInt())
        },
        badge = goalBadge(done = done, claimed = claimed, percent = metrics.progressPercent),
        badgeColor = if (done) VitalGreen else RewardGold,
        fraction = if (done) 1f else metrics.progressPercent / 100f,
        complete = done,
        reward = stringResource(
            R.string.health_mission_reward_line,
            HealthRewards.STEPS_REWARD_EXP,
            HealthRewards.STEPS_REWARD_COINS,
        ),
    )
}

@Composable
private fun SleepGoalCard(metrics: HealthMetrics, claimed: Boolean) {
    val minutes = metrics.sleepMinutes
    val inRange = minutes != null &&
        minutes in HealthRewards.SLEEP_MINUTES_MIN..HealthRewards.SLEEP_MINUTES_MAX
    val done = inRange || claimed
    val fraction = when {
        done -> 1f
        minutes == null || minutes <= 0L -> 0f
        minutes < HealthRewards.SLEEP_MINUTES_MIN ->
            minutes.toFloat() / HealthRewards.SLEEP_MINUTES_MIN.toFloat()
        else -> 1f
    }
    val subtitle = when {
        claimed || inRange -> stringResource(R.string.health_mission_sleep_ready)
        minutes == null -> stringResource(R.string.health_mission_sleep_empty)
        minutes < HealthRewards.SLEEP_MINUTES_MIN -> stringResource(
            R.string.health_mission_sleep_short,
            (HealthRewards.SLEEP_MINUTES_MIN - minutes).toInt(),
        )
        else -> stringResource(R.string.health_mission_sleep_long)
    }
    val badge = when {
        claimed -> stringResource(R.string.health_mission_claimed)
        inRange -> stringResource(R.string.health_mission_sleep_badge)
        minutes == null -> stringResource(R.string.health_mission_sleep_badge_empty)
        minutes > HealthRewards.SLEEP_MINUTES_MAX -> stringResource(R.string.health_mission_sleep_over)
        else -> stringResource(
            R.string.quest_percent,
            ((minutes * 100) / HealthRewards.SLEEP_MINUTES_MIN).toInt(),
        )
    }
    QuestCard(
        title = stringResource(R.string.health_mission_sleep_title),
        value = if (minutes == null) "—" else formatSleep(minutes),
        subtitle = subtitle,
        badge = badge,
        badgeColor = if (done) VitalGreen else Color(0xFF5C6BC0),
        fraction = fraction,
        complete = done,
        reward = stringResource(
            R.string.health_mission_reward_line,
            HealthRewards.SLEEP_REWARD_EXP,
            HealthRewards.SLEEP_REWARD_COINS,
        ),
    )
}

@Composable
private fun HeartGoalCard(metrics: HealthMetrics, claimed: Boolean) {
    val goal = metrics.weeklyHeartGoal.coerceAtLeast(0)
    val met = goal > 0 && metrics.weeklyHeartPoints >= goal
    val done = met || claimed
    val remaining = (goal - metrics.weeklyHeartPoints).coerceAtLeast(0)
    QuestCard(
        title = stringResource(R.string.health_mission_heart_title),
        value = stringResource(
            R.string.health_mission_heart_value,
            if (done) goal else metrics.weeklyHeartPoints.coerceAtLeast(0).coerceAtMost(goal),
            goal,
        ),
        subtitle = if (done) {
            stringResource(R.string.health_mission_heart_done)
        } else {
            stringResource(R.string.health_mission_heart_left, remaining)
        },
        badge = goalBadge(
            done = done,
            claimed = claimed,
            percent = metrics.weeklyHeartProgressPercent,
        ),
        badgeColor = if (done) VitalGreen else Color(0xFFE53935),
        fraction = if (done) 1f else metrics.weeklyHeartProgressPercent / 100f,
        complete = done,
        reward = stringResource(
            R.string.health_mission_reward_line,
            HealthRewards.WEEKLY_HEART_REWARD_EXP,
            HealthRewards.WEEKLY_HEART_REWARD_COINS,
        ),
    )
}

@Composable
private fun RestingHeartCard(metrics: HealthMetrics) {
    val bpm = metrics.heartRateBpm
    QuestCard(
        title = stringResource(R.string.health_metric_resting_title),
        value = if (bpm != null && bpm > 0L) {
            stringResource(R.string.health_metric_resting_value, bpm)
        } else {
            stringResource(R.string.health_metric_resting_empty)
        },
        subtitle = stringResource(R.string.health_metric_resting_hint),
        badge = stringResource(R.string.health_metric_resting_badge),
        badgeColor = Color(0xFFE53935),
        fraction = null,
        complete = false,
        reward = null,
    )
}

@Composable
private fun goalBadge(done: Boolean, claimed: Boolean, percent: Int): String {
    return when {
        claimed -> stringResource(R.string.health_mission_claimed)
        done -> stringResource(R.string.quest_mission_done)
        else -> stringResource(R.string.quest_percent, percent.coerceIn(0, 100))
    }
}

@Composable
private fun QuestCard(
    title: String,
    value: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    fraction: Float?,
    complete: Boolean,
    reward: String?,
) {
    val spacing = MaterialTheme.spacing
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = spacing.scale(96)),
    ) {
        Image(
            painter = painterResource(id = R.drawable.info_background_higher),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.md, vertical = spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.xxs),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = badge,
                    color = badgeColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(spacing.cornerXs))
                        .background(badgeColor.copy(alpha = 0.2f))
                        .padding(horizontal = spacing.xs, vertical = spacing.xxs),
                )
            }
            Text(
                text = value,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (fraction != null) {
                QuestProgressBar(fraction = fraction, complete = complete)
            }
            if (reward != null) {
                Text(
                    text = reward,
                    color = RewardGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun QuestProgressBar(fraction: Float, complete: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(DarkStone.copy(alpha = 0.55f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(6.dp)
                .background(if (complete) VitalGreen else FireOrange),
        )
    }
}

private fun formatSleep(minutes: Long): String {
    val safe = minutes.coerceAtLeast(0L)
    return "${safe / 60}h ${safe % 60}m"
}
