package com.pixelfitquest.feature.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.pixelfitquest.R
import com.pixelfitquest.components.molecules.ExitAppCard
import com.pixelfitquest.components.molecules.LandscapeWorkoutCard
import com.pixelfitquest.components.molecules.RemoveAccountCard
import com.pixelfitquest.components.molecules.SettingsActionCard
import com.pixelfitquest.local.export.ui.LocalExportCard
import com.pixelfitquest.components.molecules.VolumeCard
import com.pixelfitquest.components.molecules.launchCredManButtonUI
import com.pixelfitquest.feature.streak.WeeklyGoalCard
import com.pixelfitquest.feature.streak.WeeklyStreakViewModel
import com.pixelfitquest.firebase.model.User
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
    val weeklyStreak by weeklyStreakViewModel.snapshot.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        onScreenReady()
    }

    val spacing = MaterialTheme.spacing

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(spacing.screen),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.scale(50))
                    .padding(horizontal = spacing.md)
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
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(spacing.xs))

            VolumeCard(
                musicVolume = musicVolume,
                onVolumeChange = { viewModel.setMusicVolume(it) }
            )

            Spacer(modifier = Modifier.height(spacing.xxs))

            WeeklyGoalCard(
                targetSessions = weeklyStreak.targetSessionsPerWeek,
                onTargetChange = { weeklyStreakViewModel.setTargetSessionsPerWeek(it) }
            )

            Spacer(modifier = Modifier.height(spacing.xxs))
            LandscapeWorkoutCard(
                enabled = workoutLandscapeEnabled,
                onToggle = { viewModel.setWorkoutLandscapeEnabled(it) }
            )

            Spacer(modifier = Modifier.height(spacing.xxs))

            if (!signedIn) {
                SettingsActionCard(
                    title = stringResource(R.string.sign_in_with_google),
                    subtitle = stringResource(R.string.sign_in_with_google_subtitle),
                    icon = Icons.AutoMirrored.Filled.Login,
                ) {
                    scope.launch {
                        launchCredManButtonUI(context) { credential ->
                            viewModel.onGoogleSignIn(credential)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(spacing.xxs))
            }

            SettingsActionCard(
                title = stringResource(R.string.backup_sync_pro_title),
                subtitle = stringResource(R.string.backup_sync_pro_subtitle),
                icon = Icons.Filled.CloudOff,
            ) {
                viewModel.onBackupSyncClick()
            }

            Spacer(modifier = Modifier.height(spacing.xxs))

            LocalExportCard()

            if (signedIn) {
                Spacer(modifier = Modifier.height(spacing.xxs))
                ExitAppCard { viewModel.onSignOutClick(restartApp) }
                Spacer(modifier = Modifier.height(spacing.xxs))
                RemoveAccountCard { viewModel.onDeleteAccountClick(restartApp) }
            }

            Spacer(modifier = Modifier.height(spacing.md))
        }
    }
}
