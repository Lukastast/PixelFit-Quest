package com.pixelfitquest.feature.health

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pixelfitquest.R
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.health.HealthConnectIntents
import com.pixelfitquest.health.HealthConnectStatus
import com.pixelfitquest.ui.LockToPortrait
import com.pixelfitquest.ui.theme.spacing

@Composable
fun HealthCenterScreen(
    viewModel: HealthCenterViewModel = hiltViewModel(),
) {
    LockToPortrait()

    val context = LocalContext.current
    val healthStatus by viewModel.healthStatus.collectAsState()
    val permissionsGranted by viewModel.permissionsGranted.collectAsState()
    val metrics by viewModel.healthMetrics.collectAsState()
    val spacing = MaterialTheme.spacing

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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.screen, vertical = spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        Text(
            text = "Health Center",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD700),
            modifier = Modifier.padding(vertical = spacing.xs)
        )

        // Health Connect Status & Permission Card
        if (healthStatus != HealthConnectStatus.AVAILABLE || !permissionsGranted) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.scale(90))
                    .clickable {
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
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.info_background_higher),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(spacing.sm),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val message = when {
                        healthStatus == HealthConnectStatus.UNAVAILABLE ->
                            stringResource(R.string.health_connect_unavailable_hint)
                        healthStatus == HealthConnectStatus.UPDATE_REQUIRED ->
                            stringResource(R.string.health_connect_update_hint)
                        else ->
                            "Tap to connect Health Connect for automatic step & sleep rewards!"
                    }
                    Text(
                        text = "Connect Health Data",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = message,
                        fontSize = 12.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Steps Progress Card
        HealthMetricCard(
            title = "Daily Steps",
            valueText = "${metrics.steps} / ${metrics.stepGoal}",
            subtitle = if (metrics.steps >= metrics.stepGoal) "Goal reached! +50 EXP & +10 Coins" else "${(metrics.stepGoal - metrics.steps).coerceAtLeast(0)} steps remaining",
            badgeText = "${metrics.progressPercent}%",
            badgeColor = if (metrics.steps >= metrics.stepGoal) Color(0xFF4CAF50) else Color(0xFFFFD700)
        )

        // Heart Rate Card
        HealthMetricCard(
            title = "Resting Heart Rate (7d Avg)",
            valueText = if (metrics.heartRateBpm != null && metrics.heartRateBpm!! > 0) "${metrics.heartRateBpm} BPM" else "No recent data",
            subtitle = if (metrics.heartRateBpm != null && metrics.heartRateBpm!! > 0) "7-day rolling baseline (filters outliers)" else "Wear your fitness tracker during rest and sleep",
            badgeText = "❤ Resting",
            badgeColor = Color(0xFFE53935)
        )

        // Sleep Duration Card
        val isOptimalSleep = metrics.sleepMinutes != null && metrics.sleepMinutes!! in 420..540
        HealthMetricCard(
            title = "Sleep Duration (7–9h Goal)",
            valueText = if (metrics.sleepMinutes != null) "${metrics.sleepMinutes!! / 60}h ${metrics.sleepMinutes!! % 60}m" else "No sleep data logged",
            subtitle = when {
                isOptimalSleep -> "Optimal 7–9h sleep goal reached! +50 EXP & +15 Coins"
                metrics.sleepMinutes != null && metrics.sleepMinutes!! < 420 -> "${(420 - metrics.sleepMinutes!!)}m more needed for optimal 7h rest reward"
                metrics.sleepMinutes != null -> "Logged ${metrics.sleepMinutes!! / 60}h ${metrics.sleepMinutes!! % 60}m (Optimal range: 7–9h)"
                else -> "Track sleep with Health Connect to earn +50 EXP & +15 Coins"
            },
            badgeText = if (isOptimalSleep) "⭐ 7–9h Rest" else "🌙 Sleep",
            badgeColor = if (isOptimalSleep) Color(0xFF4CAF50) else Color(0xFF5C6BC0)
        )

        // Weekly Heart Points Card
        val isHeartGoalReached = metrics.weeklyHeartPoints >= metrics.weeklyHeartGoal
        HealthMetricCard(
            title = "Weekly Heart Goal (Google Fit)",
            valueText = "${metrics.weeklyHeartPoints} / ${metrics.weeklyHeartGoal} pts",
            subtitle = if (isHeartGoalReached) {
                "Weekly goal reached! +150 EXP & +30 Coins"
            } else {
                "${(metrics.weeklyHeartGoal - metrics.weeklyHeartPoints).coerceAtLeast(0)} points remaining this week"
            },
            badgeText = if (isHeartGoalReached) "❤ Goal Met" else "${metrics.weeklyHeartProgressPercent}%",
            badgeColor = if (isHeartGoalReached) Color(0xFF4CAF50) else Color(0xFFE53935)
        )

        // Manage Health Connect Button (only shown when connected, opens Health Connect to view/revoke permissions)
        if (healthStatus == HealthConnectStatus.AVAILABLE && permissionsGranted) {
            Spacer(modifier = Modifier.height(spacing.sm))

            PixelArtButton(
                onClick = {
                    runCatching { HealthConnectIntents.openHealthConnectSettings(context) }
                },
                imageRes = R.drawable.button_unclicked,
                pressedRes = R.drawable.button_clicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.buttonHeight)
            ) {
                Text(
                    text = "Manage Health Connect",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun HealthMetricCard(
    title: String,
    valueText: String,
    subtitle: String,
    badgeText: String,
    badgeColor: Color,
) {
    val spacing = MaterialTheme.spacing

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(spacing.scale(85))
    ) {
        Image(
            painter = painterResource(id = R.drawable.info_background_higher),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.md, vertical = spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = valueText,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
            Box(
                modifier = Modifier
                    .background(badgeColor.copy(alpha = 0.2f), RoundedCornerShape(spacing.cornerXs))
                    .padding(horizontal = spacing.xs, vertical = spacing.xxs)
            ) {
                Text(
                    text = badgeText,
                    color = badgeColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
