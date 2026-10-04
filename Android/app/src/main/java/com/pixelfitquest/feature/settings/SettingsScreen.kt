package com.pixelfitquest.feature.settings

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.pixelfitquest.BuildConfig
import com.pixelfitquest.R
import com.pixelfitquest.components.molecules.BarSleeveCard
import com.pixelfitquest.components.molecules.DeveloperTraceCard
import com.pixelfitquest.components.molecules.ExitAppCard
import com.pixelfitquest.components.molecules.GodModeCard
import com.pixelfitquest.components.molecules.LandscapeWorkoutCard
import com.pixelfitquest.components.molecules.PreWorkoutWeightCheckCard
import com.pixelfitquest.components.molecules.WeightProgressionSettingsCard
import com.pixelfitquest.components.molecules.RemoveAccountCard
import com.pixelfitquest.components.molecules.RestTimerSettingsCard
import com.pixelfitquest.components.molecules.RomCalibrationSettingsCard
import com.pixelfitquest.components.molecules.SettingsActionCard
import com.pixelfitquest.components.molecules.VolumeCard
import com.pixelfitquest.components.molecules.launchCredManButtonUI
import com.pixelfitquest.feature.streak.WeeklyGoalCard
import com.pixelfitquest.feature.streak.WeeklyStreakViewModel
import com.pixelfitquest.firebase.model.User
import com.pixelfitquest.local.export.ui.LocalExportCard
import com.pixelfitquest.ui.theme.ImperialGold
import com.pixelfitquest.ui.theme.PixelFitWidthClass
import com.pixelfitquest.ui.theme.SilverSlate
import com.pixelfitquest.ui.theme.VitalGreen
import com.pixelfitquest.ui.theme.determination
import com.pixelfitquest.ui.theme.spacing
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    restartApp: (String) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    weeklyStreakViewModel: WeeklyStreakViewModel = hiltViewModel(),
    onScreenReady: () -> Unit = {}
) {
    val user by viewModel.user.collectAsState(initial = User())
    val userSettings by viewModel.userData.collectAsState(initial = null)
    val musicVolume = userSettings?.musicVolume ?: 50
    val signedIn = user.id.isNotBlank()
    val workoutLandscapeEnabled by viewModel.workoutLandscapeEnabled.collectAsState()
    val preWorkoutWeightCheckEnabled by viewModel.preWorkoutWeightCheckEnabled.collectAsState()
    val weightSuggestionEnabled by viewModel.weightSuggestionEnabled.collectAsState()
    val weightSuggestionRepThreshold by viewModel.weightSuggestionRepThreshold.collectAsState()
    val godModeEnabled by viewModel.godModeEnabled.collectAsState()
    val mountSide by viewModel.mountSide.collectAsState()
    val traceExportEnabled by viewModel.traceExportEnabled.collectAsState()
    val restTimerEnabled by viewModel.restTimerEnabled.collectAsState()
    val restTimerSeconds by viewModel.restTimerSeconds.collectAsState()
    val restAutostart by viewModel.restAutostart.collectAsState()
    val weeklyStreak by weeklyStreakViewModel.snapshot.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        onScreenReady()
    }

    val spacing = MaterialTheme.spacing
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val useTwoPane = spacing.widthClass != PixelFitWidthClass.Compact || isLandscape

    val cardModifier = if (useTwoPane) {
        Modifier
            .fillMaxWidth()
            .padding(bottom = spacing.xs)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(bottom = spacing.xs)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = if (useTwoPane) spacing.md else spacing.screen,
                    vertical = if (useTwoPane) spacing.xs else spacing.sm
                ),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Plaque
            Box(
                modifier = Modifier
                    .widthIn(max = spacing.scale(if (useTwoPane) 400 else 420))
                    .fillMaxWidth(if (useTwoPane) 0.42f else 1f)
                    .height(spacing.scale(if (useTwoPane) 42 else 48))
                    .padding(horizontal = if (useTwoPane) spacing.xs else spacing.md)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.info_background),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
                Text(
                    text = stringResource(R.string.settings_title),
                    fontFamily = determination,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (useTwoPane) 15.sp else 17.sp,
                    color = ImperialGold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(spacing.xs))

            if (useTwoPane) {
                // Two-Column Landscape / Foldables Layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.md)
                ) {
                    // Left Column: Gameplay & Audio
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Top
                    ) {
                        SettingsSectionHeader(title = "GAMEPLAY & AUDIO")

                        VolumeCard(
                            musicVolume = musicVolume,
                            onVolumeChange = { viewModel.setMusicVolume(it) },
                            modifier = cardModifier,
                        )

                        WeeklyGoalCard(
                            targetSessions = weeklyStreak.targetSessionsPerWeek,
                            onTargetChange = { weeklyStreakViewModel.setTargetSessionsPerWeek(it) },
                            modifier = cardModifier,
                        )

                        LandscapeWorkoutCard(
                            enabled = workoutLandscapeEnabled,
                            onToggle = { viewModel.setWorkoutLandscapeEnabled(it) },
                            modifier = cardModifier,
                        )

                        PreWorkoutWeightCheckCard(
                            enabled = preWorkoutWeightCheckEnabled,
                            onToggle = { viewModel.setPreWorkoutWeightCheckEnabled(it) },
                            modifier = cardModifier,
                        )

                        WeightProgressionSettingsCard(
                            suggestionEnabled = weightSuggestionEnabled,
                            onSuggestionToggle = { viewModel.setWeightSuggestionEnabled(it) },
                            repThreshold = weightSuggestionRepThreshold,
                            onThresholdChange = { viewModel.setWeightSuggestionRepThreshold(it) },
                            modifier = cardModifier,
                        )

                        RestTimerSettingsCard(
                            enabled = restTimerEnabled,
                            seconds = restTimerSeconds,
                            autostart = restAutostart,
                            onEnabled = viewModel::setRestTimerEnabled,
                            onSeconds = viewModel::setRestTimerSeconds,
                            onAutostart = viewModel::setRestAutostart,
                            modifier = cardModifier,
                        )

                        RomCalibrationSettingsCard(
                            onReset = viewModel::resetLearnedRange,
                            modifier = cardModifier,
                        )

                        BarSleeveCard(
                            side = mountSide,
                            onSelect = viewModel::setMountSide,
                            modifier = cardModifier,
                        )

                        DeveloperTraceCard(
                            enabled = traceExportEnabled,
                            onToggle = viewModel::setTraceExportEnabled,
                            modifier = cardModifier,
                        )

                        if (BuildConfig.DEBUG) {
                            GodModeCard(
                                enabled = godModeEnabled,
                                onToggle = { viewModel.setGodModeEnabled(it) },
                                modifier = cardModifier,
                            )
                        }
                    }

                    // Right Column: Account & Data
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Top
                    ) {
                        SettingsSectionHeader(title = "ACCOUNT & CLOUD")

                        if (!signedIn) {
                            SettingsActionCard(
                                title = stringResource(R.string.sign_in_with_google),
                                subtitle = stringResource(R.string.sign_in_with_google_subtitle),
                                iconPainter = painterResource(id = R.drawable.google_g),
                                modifier = cardModifier,
                                trailingContent = {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(spacing.cornerXs))
                                            .background(ImperialGold.copy(alpha = 0.15f))
                                            .border(1.dp, ImperialGold.copy(alpha = 0.5f), RoundedCornerShape(spacing.cornerXs))
                                            .padding(horizontal = spacing.xs, vertical = spacing.xxs)
                                    ) {
                                        Text(
                                            text = "SIGN IN",
                                            fontFamily = determination,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = ImperialGold
                                        )
                                    }
                                }
                            ) {
                                scope.launch {
                                    launchCredManButtonUI(context) { credential ->
                                        viewModel.onGoogleSignIn(credential)
                                    }
                                }
                            }
                        } else {
                            val userLabel = when {
                                user.email.isNotBlank() -> user.email
                                user.displayName.isNotBlank() -> user.displayName
                                else -> "Cloud Account"
                            }
                            SettingsActionCard(
                                title = "Google Account",
                                subtitle = userLabel,
                                iconPainter = painterResource(id = R.drawable.google_g),
                                modifier = cardModifier,
                                trailingContent = {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(spacing.cornerXs))
                                            .background(VitalGreen.copy(alpha = 0.16f))
                                            .border(1.dp, VitalGreen.copy(alpha = 0.5f), RoundedCornerShape(spacing.cornerXs))
                                            .padding(horizontal = spacing.xs, vertical = spacing.xxs)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(VitalGreen)
                                            )
                                            Text(
                                                text = "SYNCED",
                                                fontFamily = determination,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = VitalGreen
                                            )
                                        }
                                    }
                                }
                            ) {
                                // Already signed in
                            }
                        }

                        SettingsActionCard(
                            title = stringResource(R.string.backup_sync_pro_title),
                            subtitle = stringResource(R.string.backup_sync_pro_subtitle),
                            icon = Icons.Filled.CloudSync,
                            modifier = cardModifier,
                        ) {
                            viewModel.onBackupSyncClick()
                        }

                        SettingsSectionHeader(title = "DATA & STORAGE")

                        LocalExportCard(modifier = cardModifier)

                        if (signedIn) {
                            SettingsSectionHeader(title = "ACCOUNT ACTIONS")

                            ExitAppCard(
                                onSignOutClick = { viewModel.onSignOutClick(restartApp) },
                                modifier = cardModifier,
                            )
                            RemoveAccountCard(
                                onRemoveAccountClick = { viewModel.onDeleteAccountClick(restartApp) },
                                modifier = cardModifier,
                            )
                        }
                    }
                }
            } else {
                // Portrait single column with structured sections
                SettingsSectionHeader(title = "GAMEPLAY & AUDIO")

                VolumeCard(
                    musicVolume = musicVolume,
                    onVolumeChange = { viewModel.setMusicVolume(it) },
                    modifier = cardModifier,
                )

                WeeklyGoalCard(
                    targetSessions = weeklyStreak.targetSessionsPerWeek,
                    onTargetChange = { weeklyStreakViewModel.setTargetSessionsPerWeek(it) },
                    modifier = cardModifier,
                )

                LandscapeWorkoutCard(
                    enabled = workoutLandscapeEnabled,
                    onToggle = { viewModel.setWorkoutLandscapeEnabled(it) },
                    modifier = cardModifier,
                )

                PreWorkoutWeightCheckCard(
                    enabled = preWorkoutWeightCheckEnabled,
                    onToggle = { viewModel.setPreWorkoutWeightCheckEnabled(it) },
                    modifier = cardModifier,
                )

                WeightProgressionSettingsCard(
                    suggestionEnabled = weightSuggestionEnabled,
                    onSuggestionToggle = { viewModel.setWeightSuggestionEnabled(it) },
                    repThreshold = weightSuggestionRepThreshold,
                    onThresholdChange = { viewModel.setWeightSuggestionRepThreshold(it) },
                    modifier = cardModifier,
                )

                RestTimerSettingsCard(
                    enabled = restTimerEnabled,
                    seconds = restTimerSeconds,
                    autostart = restAutostart,
                    onEnabled = viewModel::setRestTimerEnabled,
                    onSeconds = viewModel::setRestTimerSeconds,
                    onAutostart = viewModel::setRestAutostart,
                    modifier = cardModifier,
                )

                RomCalibrationSettingsCard(
                    onReset = viewModel::resetLearnedRange,
                    modifier = cardModifier,
                )

                BarSleeveCard(
                    side = mountSide,
                    onSelect = viewModel::setMountSide,
                    modifier = cardModifier,
                )

                DeveloperTraceCard(
                    enabled = traceExportEnabled,
                    onToggle = viewModel::setTraceExportEnabled,
                    modifier = cardModifier,
                )

                if (BuildConfig.DEBUG) {
                    GodModeCard(
                        enabled = godModeEnabled,
                        onToggle = { viewModel.setGodModeEnabled(it) },
                        modifier = cardModifier,
                    )
                }

                SettingsSectionHeader(title = "ACCOUNT & CLOUD")

                if (!signedIn) {
                    SettingsActionCard(
                        title = stringResource(R.string.sign_in_with_google),
                        subtitle = stringResource(R.string.sign_in_with_google_subtitle),
                        iconPainter = painterResource(id = R.drawable.google_g),
                        modifier = cardModifier,
                        trailingContent = {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(spacing.cornerXs))
                                    .background(ImperialGold.copy(alpha = 0.15f))
                                    .border(1.dp, ImperialGold.copy(alpha = 0.5f), RoundedCornerShape(spacing.cornerXs))
                                    .padding(horizontal = spacing.xs, vertical = spacing.xxs)
                            ) {
                                Text(
                                    text = "SIGN IN",
                                    fontFamily = determination,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = ImperialGold
                                )
                            }
                        }
                    ) {
                        scope.launch {
                            launchCredManButtonUI(context) { credential ->
                                viewModel.onGoogleSignIn(credential)
                            }
                        }
                    }
                } else {
                    val userLabel = when {
                        user.email.isNotBlank() -> user.email
                        user.displayName.isNotBlank() -> user.displayName
                        else -> "Cloud Account"
                    }
                    SettingsActionCard(
                        title = "Google Account",
                        subtitle = userLabel,
                        iconPainter = painterResource(id = R.drawable.google_g),
                        modifier = cardModifier,
                        trailingContent = {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(spacing.cornerXs))
                                    .background(VitalGreen.copy(alpha = 0.16f))
                                    .border(1.dp, VitalGreen.copy(alpha = 0.5f), RoundedCornerShape(spacing.cornerXs))
                                    .padding(horizontal = spacing.xs, vertical = spacing.xxs)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(VitalGreen)
                                    )
                                    Text(
                                        text = "SYNCED",
                                        fontFamily = determination,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = VitalGreen
                                    )
                                }
                            }
                        }
                    ) {
                        // Already signed in
                    }
                }

                SettingsActionCard(
                    title = stringResource(R.string.backup_sync_pro_title),
                    subtitle = stringResource(R.string.backup_sync_pro_subtitle),
                    icon = Icons.Filled.CloudSync,
                    modifier = cardModifier,
                ) {
                    viewModel.onBackupSyncClick()
                }

                SettingsSectionHeader(title = "DATA & STORAGE")

                LocalExportCard(modifier = cardModifier)

                if (signedIn) {
                    SettingsSectionHeader(title = "ACCOUNT ACTIONS")

                    ExitAppCard(
                        onSignOutClick = { viewModel.onSignOutClick(restartApp) },
                        modifier = cardModifier,
                    )
                    RemoveAccountCard(
                        onRemoveAccountClick = { viewModel.onDeleteAccountClick(restartApp) },
                        modifier = cardModifier,
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.md))

            // Footer with App Version
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.xxxs),
                modifier = Modifier.padding(bottom = spacing.sm)
            ) {
                Text(
                    text = "PixelFit Quest v${BuildConfig.VERSION_NAME}",
                    fontFamily = determination,
                    fontSize = 11.sp,
                    color = SilverSlate.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Stay consistent. Level up.",
                    fontFamily = determination,
                    fontSize = 10.sp,
                    color = SilverSlate.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = spacing.xs, bottom = spacing.xxs, start = spacing.xxs)
    ) {
        Text(
            text = "◈ $title",
            fontFamily = determination,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp,
            color = ImperialGold
        )
    }
}
