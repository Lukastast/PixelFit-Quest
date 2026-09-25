package com.pixelfitquest.feature.settings

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.pixelfitquest.BuildConfig
import com.pixelfitquest.R
import com.pixelfitquest.components.molecules.ExitAppCard
import com.pixelfitquest.components.molecules.GodModeCard
import com.pixelfitquest.components.molecules.LandscapeWorkoutCard
import com.pixelfitquest.components.molecules.RemoveAccountCard
import com.pixelfitquest.components.molecules.SettingsActionCard
import com.pixelfitquest.local.export.ui.LocalExportCard
import com.pixelfitquest.components.molecules.VolumeCard
import com.pixelfitquest.components.molecules.launchCredManButtonUI
import com.pixelfitquest.feature.streak.WeeklyGoalCard
import com.pixelfitquest.feature.streak.WeeklyStreakViewModel
import com.pixelfitquest.firebase.model.User
import com.pixelfitquest.ui.theme.PixelFitWidthClass
import com.pixelfitquest.ui.theme.spacing
import com.pixelfitquest.ui.theme.typography
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
    val godModeEnabled by viewModel.godModeEnabled.collectAsState()
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
            .padding(start = spacing.xl, end = spacing.xl, bottom = spacing.xs)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = if (useTwoPane) spacing.md else spacing.screen,
                    vertical = if (useTwoPane) spacing.xs else spacing.screen
                ),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (useTwoPane) 0.42f else 1f)
                    .height(spacing.scale(if (useTwoPane) 42 else 50))
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
                    style = typography.bodyMedium,
                    fontSize = if (useTwoPane) 14.sp else 16.sp,
                    color = Color.White,
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
                    // Left Column: Gameplay & Preferences
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Top
                    ) {
                        Text(
                            text = "Gameplay & Preferences",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700),
                            modifier = Modifier.padding(bottom = spacing.xs, start = spacing.xs)
                        )

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
                        Text(
                            text = "Account & Data",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700),
                            modifier = Modifier.padding(bottom = spacing.xs, start = spacing.xs)
                        )

                        if (!signedIn) {
                            SettingsActionCard(
                                title = stringResource(R.string.sign_in_with_google),
                                subtitle = stringResource(R.string.sign_in_with_google_subtitle),
                                icon = Icons.AutoMirrored.Filled.Login,
                                modifier = cardModifier,
                            ) {
                                scope.launch {
                                    launchCredManButtonUI(context) { credential ->
                                        viewModel.onGoogleSignIn(credential)
                                    }
                                }
                            }
                        }

                        SettingsActionCard(
                            title = stringResource(R.string.backup_sync_pro_title),
                            subtitle = stringResource(R.string.backup_sync_pro_subtitle),
                            icon = Icons.Filled.CloudOff,
                            modifier = cardModifier,
                        ) {
                            viewModel.onBackupSyncClick()
                        }

                        LocalExportCard(modifier = cardModifier)

                        if (signedIn) {
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
                // Portrait single column
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

                if (BuildConfig.DEBUG) {
                    GodModeCard(
                        enabled = godModeEnabled,
                        onToggle = { viewModel.setGodModeEnabled(it) },
                        modifier = cardModifier,
                    )
                }

                if (!signedIn) {
                    SettingsActionCard(
                        title = stringResource(R.string.sign_in_with_google),
                        subtitle = stringResource(R.string.sign_in_with_google_subtitle),
                        icon = Icons.AutoMirrored.Filled.Login,
                        modifier = cardModifier,
                    ) {
                        scope.launch {
                            launchCredManButtonUI(context) { credential ->
                                viewModel.onGoogleSignIn(credential)
                            }
                        }
                    }
                }

                SettingsActionCard(
                    title = stringResource(R.string.backup_sync_pro_title),
                    subtitle = stringResource(R.string.backup_sync_pro_subtitle),
                    icon = Icons.Filled.CloudOff,
                    modifier = cardModifier,
                ) {
                    viewModel.onBackupSyncClick()
                }

                LocalExportCard(modifier = cardModifier)

                if (signedIn) {
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
        }
    }
}
