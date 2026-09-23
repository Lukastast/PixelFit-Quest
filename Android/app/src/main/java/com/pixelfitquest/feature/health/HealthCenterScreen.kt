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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import com.pixelfitquest.ui.theme.BronzeCopper
import com.pixelfitquest.ui.theme.BronzeRust
import com.pixelfitquest.ui.theme.CrystalCyan
import com.pixelfitquest.ui.theme.EmberOrange
import com.pixelfitquest.ui.theme.HeartRuby
import com.pixelfitquest.ui.theme.ImperialGold
import com.pixelfitquest.ui.theme.PixelFitWidthClass
import com.pixelfitquest.ui.theme.PlatinumWhite
import com.pixelfitquest.ui.theme.SilverSlate
import com.pixelfitquest.ui.theme.SilverSteel
import com.pixelfitquest.ui.theme.SlateBorder
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.SlateGroove
import com.pixelfitquest.ui.theme.SlateSurface
import com.pixelfitquest.ui.theme.SleepAmethyst
import com.pixelfitquest.ui.theme.TorchAmber
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
    val userData by viewModel.userData.collectAsState()
    val level = userData?.level ?: 1
    val coins = userData?.coins ?: 0

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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (useTwoPane) spacing.scale(50) else spacing.scale(54))
                .clip(RoundedCornerShape(spacing.cornerMd))
                .background(SlateDeep.copy(alpha = 0.94f))
                .border(BorderStroke(2.dp, SlateBorder), RoundedCornerShape(spacing.cornerMd)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.level_5),
                    contentDescription = null,
                    modifier = Modifier.size(if (useTwoPane) 28.dp else 34.dp),
                )
                Spacer(modifier = Modifier.width(spacing.xs))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.quest_center_title),
                        fontSize = if (useTwoPane) 15.sp else 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = ImperialGold,
                    )
                    Text(
                        text = stringResource(R.string.quest_center_subtitle),
                        fontSize = 11.sp,
                        color = SilverSteel,
                    )
                }

                // Level and Coins in top right of Quest Center
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.xxs),
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(spacing.cornerXs))
                            .background(SlateDeep.copy(alpha = 0.88f))
                            .border(BorderStroke(1.dp, SlateBorder), RoundedCornerShape(spacing.cornerXs))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = "Lvl $level",
                            color = ImperialGold,
                            fontSize = if (useTwoPane) 11.sp else 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(spacing.cornerXs))
                            .background(SlateDeep.copy(alpha = 0.88f))
                            .border(BorderStroke(1.dp, SlateBorder), RoundedCornerShape(spacing.cornerXs))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.coin),
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = "$coins",
                            color = ImperialGold,
                            fontSize = if (useTwoPane) 11.sp else 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
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
    PixelArtButton(
        onClick = onClick,
        imageRes = if (selected) R.drawable.button_clicked else R.drawable.button_unclicked,
        pressedRes = R.drawable.button_clicked,
        modifier = modifier.height(52.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(
                start = 2.dp,
                end = 2.dp,
                top = if (selected) 4.dp else 1.dp,
                bottom = if (selected) 3.dp else 7.dp,
            ),
        ) {
            Text(
                text = label,
                color = if (selected) ImperialGold else SilverSteel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = count,
                color = if (selected) TorchAmber else SilverSlate,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun MissionsHeroStage(
    board: WeeklyMissionBoard,
    useTwoPane: Boolean,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    val completed = board.completedCount
    val total = board.missions.size.coerceAtLeast(1)
    val fraction = (completed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    val allDone = completed >= total && total > 0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cornerMd))
            .background(SlateDeep.copy(alpha = 0.92f))
            .border(BorderStroke(2.dp, SlateBorder), RoundedCornerShape(spacing.cornerMd))
            .padding(spacing.sm),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.xxs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "WEEKLY MISSIONS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ImperialGold,
                    fontSize = if (useTwoPane) 14.sp else 15.sp,
                )
                Text(
                    text = if (allDone) "ALL COMPLETED" else "$completed/$total DONE",
                    color = if (allDone) VitalGreen else TorchAmber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(spacing.cornerXs))
                        .background((if (allDone) VitalGreen else TorchAmber).copy(alpha = 0.18f))
                        .border(1.dp, (if (allDone) VitalGreen else TorchAmber).copy(alpha = 0.4f), RoundedCornerShape(spacing.cornerXs))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(if (useTwoPane) 50.dp else 54.dp)
                        .clip(RoundedCornerShape(spacing.cornerXs))
                        .background(SlateGroove.copy(alpha = 0.9f))
                        .border(1.dp, SlateBorder, RoundedCornerShape(spacing.cornerXs)),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.level_5),
                        contentDescription = null,
                        modifier = Modifier.size(if (useTwoPane) 40.dp else 44.dp),
                    )
                }

                Spacer(modifier = Modifier.width(spacing.sm))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = stringResource(R.string.quest_week_resets),
                        color = SilverSlate,
                        fontSize = if (useTwoPane) 10.5.sp else 11.5.sp,
                        maxLines = 2,
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(SlateGroove)
                            .border(1.dp, SlateBorder, RoundedCornerShape(2.dp)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .fillMaxHeight()
                                .background(if (allDone) VitalGreen else TorchAmber),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${(fraction * 100).toInt()}% Complete",
                            color = SilverSteel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        val totalCoins = board.missions.sumOf { it.definition.coins }
                        val totalXp = board.missions.sumOf { it.definition.xp }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.coin),
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                            )
                            Text(
                                text = "+$totalCoins",
                                color = ImperialGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "+$totalXp XP",
                                color = ImperialGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
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
        MissionsHeroStage(board = board, useTwoPane = useTwoPane)

        if (stepsHint != null) {
            Text(
                text = stepsHint,
                color = TorchAmber,
                fontSize = 11.5.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(spacing.cornerXs))
                    .background(SlateSurface.copy(alpha = 0.94f))
                    .border(1.dp, SlateBorder, RoundedCornerShape(spacing.cornerXs))
                    .clickable(onClick = onOpenHealth)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
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
    val isSteps = definition.metric == MissionMetric.WEEKLY_STEPS
    val blurb = if (isSteps) {
        stringResource(R.string.quest_mission_steps_blurb)
    } else {
        stringResource(R.string.quest_mission_training_blurb)
    }
    val iconRes = if (isSteps) R.drawable.steps_5000 else R.drawable.bronze_workout_1
    QuestCard(
        iconRes = iconRes,
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
        badgeColor = if (done) VitalGreen else TorchAmber,
        fraction = if (done) 1f else mission.fraction,
        complete = done,
        rewardCoins = definition.coins,
        rewardXp = definition.xp,
    )
}

@Composable
private fun HealthHeroStage(
    metrics: HealthMetrics,
    healthStatus: HealthConnectStatus,
    permissionsGranted: Boolean,
    useTwoPane: Boolean,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    val connected = healthStatus == HealthConnectStatus.AVAILABLE && permissionsGranted
    val metGoals = metrics.rewardedGoalsMet
    val totalGoals = HealthRewards.REWARDED_GOAL_COUNT
    val fraction = (metGoals.toFloat() / totalGoals.toFloat()).coerceIn(0f, 1f)
    val allDone = metGoals >= totalGoals

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cornerMd))
            .background(SlateDeep.copy(alpha = 0.92f))
            .border(BorderStroke(2.dp, SlateBorder), RoundedCornerShape(spacing.cornerMd))
            .padding(spacing.sm),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.xxs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "DAILY VITALITY",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ImperialGold,
                    fontSize = if (useTwoPane) 14.sp else 15.sp,
                )
                Text(
                    text = if (connected) "$metGoals/$totalGoals GOALS MET" else "CONNECT SYNC",
                    color = if (connected && allDone) VitalGreen else TorchAmber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(spacing.cornerXs))
                        .background((if (connected && allDone) VitalGreen else TorchAmber).copy(alpha = 0.18f))
                        .border(1.dp, (if (connected && allDone) VitalGreen else TorchAmber).copy(alpha = 0.4f), RoundedCornerShape(spacing.cornerXs))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(if (useTwoPane) 50.dp else 54.dp)
                        .clip(RoundedCornerShape(spacing.cornerXs))
                        .background(SlateGroove.copy(alpha = 0.9f))
                        .border(1.dp, SlateBorder, RoundedCornerShape(spacing.cornerXs)),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.steps_5000),
                        contentDescription = null,
                        modifier = Modifier.size(if (useTwoPane) 40.dp else 44.dp),
                    )
                }

                Spacer(modifier = Modifier.width(spacing.sm))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = stringResource(R.string.health_goals_intro),
                        color = SilverSlate,
                        fontSize = if (useTwoPane) 10.5.sp else 11.5.sp,
                        maxLines = 2,
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(SlateGroove)
                            .border(1.dp, SlateBorder, RoundedCornerShape(2.dp)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .fillMaxHeight()
                                .background(if (allDone) VitalGreen else TorchAmber),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (connected) "$metGoals of $totalGoals complete" else "Sync to track daily stats",
                            color = SilverSteel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = if (connected) "Active" else "Offline Ready",
                            color = if (connected) VitalGreen else SilverSlate,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
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
        HealthHeroStage(
            metrics = metrics,
            healthStatus = healthStatus,
            permissionsGranted = permissionsGranted,
            useTwoPane = useTwoPane,
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
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SilverSteel,
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
            .clip(RoundedCornerShape(spacing.cornerSm))
            .background(SlateDeep.copy(alpha = 0.94f))
            .border(BorderStroke(1.5.dp, TorchAmber.copy(alpha = 0.85f)), RoundedCornerShape(spacing.cornerSm))
            .clickable(onClick = onClick)
            .padding(spacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(spacing.cornerXs))
                    .background(SlateGroove.copy(alpha = 0.9f))
                    .border(1.dp, SlateBorder, RoundedCornerShape(spacing.cornerXs)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.steps_5000),
                    contentDescription = null,
                    modifier = Modifier.size(42.dp),
                )
            }
            Spacer(modifier = Modifier.width(spacing.sm))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = stringResource(R.string.health_connect_card_title),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImperialGold,
                )
                Text(
                    text = message,
                    fontSize = 10.5.sp,
                    color = SilverSteel.copy(alpha = 0.85f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
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
        iconRes = R.drawable.steps_5000,
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
        badgeColor = if (done) VitalGreen else TorchAmber,
        fraction = if (done) 1f else metrics.progressPercent / 100f,
        complete = done,
        rewardCoins = HealthRewards.STEPS_REWARD_COINS,
        rewardXp = HealthRewards.STEPS_REWARD_EXP,
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
        iconRes = R.drawable.streak_30,
        title = stringResource(R.string.health_mission_sleep_title),
        value = if (minutes == null) "—" else formatSleep(minutes),
        subtitle = subtitle,
        badge = badge,
        badgeColor = if (done) VitalGreen else SleepAmethyst,
        fraction = fraction,
        complete = done,
        rewardCoins = HealthRewards.SLEEP_REWARD_COINS,
        rewardXp = HealthRewards.SLEEP_REWARD_EXP,
    )
}

@Composable
private fun HeartGoalCard(metrics: HealthMetrics, claimed: Boolean) {
    val goal = metrics.weeklyHeartGoal.coerceAtLeast(0)
    val met = goal > 0 && metrics.weeklyHeartPoints >= goal
    val done = met || claimed
    val remaining = (goal - metrics.weeklyHeartPoints).coerceAtLeast(0)
    QuestCard(
        iconRes = R.drawable.streak_3,
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
        badgeColor = if (done) VitalGreen else HeartRuby,
        fraction = if (done) 1f else metrics.weeklyHeartProgressPercent / 100f,
        complete = done,
        rewardCoins = HealthRewards.WEEKLY_HEART_REWARD_COINS,
        rewardXp = HealthRewards.WEEKLY_HEART_REWARD_EXP,
    )
}

@Composable
private fun RestingHeartCard(metrics: HealthMetrics) {
    val bpm = metrics.heartRateBpm
    QuestCard(
        iconRes = R.drawable.streak_3,
        title = stringResource(R.string.health_metric_resting_title),
        value = if (bpm != null && bpm > 0L) {
            stringResource(R.string.health_metric_resting_value, bpm)
        } else {
            stringResource(R.string.health_metric_resting_empty)
        },
        subtitle = stringResource(R.string.health_metric_resting_hint),
        badge = stringResource(R.string.health_metric_resting_badge),
        badgeColor = HeartRuby,
        fraction = null,
        complete = false,
        rewardCoins = null,
        rewardXp = null,
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
    iconRes: Int?,
    title: String,
    value: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    fraction: Float?,
    complete: Boolean,
    rewardCoins: Int? = null,
    rewardXp: Int? = null,
) {
    val spacing = MaterialTheme.spacing
    val borderColor = if (complete) ImperialGold else SlateBorder
    val backgroundColor = if (complete) SlateDeep.copy(alpha = 0.92f) else SlateSurface.copy(alpha = 0.94f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cornerSm))
            .background(backgroundColor)
            .border(BorderStroke(if (complete) 2.dp else 1.dp, borderColor), RoundedCornerShape(spacing.cornerSm))
            .padding(spacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (iconRes != null) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(spacing.cornerXs))
                        .background(SlateGroove.copy(alpha = 0.9f))
                        .border(1.dp, SlateBorder, RoundedCornerShape(spacing.cornerXs)),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(42.dp),
                        alpha = if (complete) 1f else 0.75f,
                    )
                }
                Spacer(modifier = Modifier.width(spacing.sm))
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.weight(1f),
                        color = if (complete) ImperialGold else SilverSteel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = badge,
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(spacing.cornerXs))
                            .background(badgeColor.copy(alpha = 0.18f))
                            .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(spacing.cornerXs))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
                Text(
                    text = subtitle,
                    color = SilverSlate,
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (fraction != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = value,
                            color = SilverSteel,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    QuestProgressBar(fraction = fraction, complete = complete)
                } else {
                    Text(
                        text = value,
                        color = SilverSteel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                if (rewardCoins != null || rewardXp != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (rewardCoins != null && rewardCoins > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.coin),
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "+$rewardCoins",
                                    color = ImperialGold,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        if (rewardXp != null && rewardXp > 0) {
                            Text(
                                text = "+$rewardXp XP",
                                color = ImperialGold,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestProgressBar(fraction: Float, complete: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(SlateGroove)
            .border(1.dp, SlateBorder, RoundedCornerShape(2.dp)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(if (complete) VitalGreen else TorchAmber),
        )
    }
}

private fun formatSleep(minutes: Long): String {
    val safe = minutes.coerceAtLeast(0L)
    return "${safe / 60}h ${safe % 60}m"
}
