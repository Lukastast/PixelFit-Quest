package com.pixelfitquest.feature.settings

import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.pixelfitquest.R
import com.pixelfitquest.components.molecules.ExitAppCard
import com.pixelfitquest.components.molecules.HealthConnectCard
import com.pixelfitquest.components.molecules.RemoveAccountCard
import com.pixelfitquest.components.molecules.SettingsActionCard
import com.pixelfitquest.components.molecules.VolumeCard
import com.pixelfitquest.ui.navigation.BODY_METRICS_SCREEN
import com.pixelfitquest.components.molecules.launchCredManButtonUI
import com.pixelfitquest.firebase.model.User
import com.pixelfitquest.health.HealthConnectIntents
import com.pixelfitquest.health.HealthConnectStatus
import com.pixelfitquest.ui.theme.typography
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    restartApp: (String) -> Unit,
    openScreen: (String) -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
    onScreenReady: () -> Unit = {}
) {
    val user by viewModel.user.collectAsState(initial = User())
    val userSettings by viewModel.userData.collectAsState(initial = null)
    val musicVolume = userSettings?.musicVolume ?: 50
    val signedIn = user.id.isNotBlank()
    val healthStatus by viewModel.healthStatus.collectAsState()
    val healthGranted by viewModel.healthPermissionsGranted.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val healthPermissionContract = remember {
        PermissionController.createRequestPermissionResultContract()
    }
    val healthPermissionLauncher = rememberLauncherForActivityResult(
        contract = healthPermissionContract
    ) { granted ->
        viewModel.onHealthPermissionsResult(granted)
    }

    LaunchedEffect(Unit) {
        viewModel.refreshHealthStatus()
        onScreenReady()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 16.dp)
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

            Spacer(modifier = Modifier.fillMaxWidth().padding(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(16.dp, 0.dp, 16.dp, 8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.info_background_higher),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (signedIn) {
                        Text(
                            text = stringResource(
                                R.string.profile_email,
                                user.email.ifBlank { user.displayName }
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.guest_profile_title),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.guest_profile_subtitle),
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.fillMaxWidth().padding(8.dp))

            VolumeCard(
                musicVolume = musicVolume,
                onVolumeChange = { viewModel.setMusicVolume(it) }
            )

            Spacer(modifier = Modifier.fillMaxWidth().padding(8.dp))

            HealthConnectCard(
                status = healthStatus,
                permissionsGranted = healthGranted,
                onClick = {
                    when (healthStatus) {
                        HealthConnectStatus.AVAILABLE -> {
                            if (healthGranted) {
                                runCatching { HealthConnectIntents.openHealthConnectSettings(context) }
                            } else {
                                runCatching {
                                    healthPermissionLauncher.launch(viewModel.healthPermissions)
                                }
                            }
                        }
                        HealthConnectStatus.UPDATE_REQUIRED -> {
                            HealthConnectIntents.openPlayStore(context)
                        }
                        HealthConnectStatus.UNAVAILABLE -> Unit
                    }
                }
            )

            Spacer(modifier = Modifier.fillMaxWidth().padding(8.dp))

            SettingsActionCard(
                title = stringResource(R.string.body_metrics_card_title),
                subtitle = stringResource(R.string.body_metrics_card_subtitle),
                icon = Icons.Filled.Person,
            ) {
                openScreen(BODY_METRICS_SCREEN)
            }

            Spacer(modifier = Modifier.fillMaxWidth().padding(8.dp))

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
                Spacer(modifier = Modifier.fillMaxWidth().padding(8.dp))
            }

            SettingsActionCard(
                title = stringResource(R.string.backup_sync_pro_title),
                subtitle = stringResource(R.string.backup_sync_pro_subtitle),
                icon = Icons.Filled.CloudOff,
            ) {
                viewModel.onBackupSyncClick()
            }

            Spacer(modifier = Modifier.fillMaxWidth().padding(8.dp))

            SettingsActionCard(
                title = stringResource(R.string.export_json_title),
                subtitle = stringResource(R.string.export_json_subtitle),
                icon = Icons.Filled.Share,
            ) {
                viewModel.exportJson(context)
            }

            Spacer(modifier = Modifier.fillMaxWidth().padding(8.dp))

            SettingsActionCard(
                title = stringResource(R.string.export_csv_title),
                subtitle = stringResource(R.string.export_csv_subtitle),
                icon = Icons.Filled.Share,
            ) {
                viewModel.exportCsv(context)
            }

            if (signedIn) {
                Spacer(modifier = Modifier.fillMaxWidth().padding(8.dp))
                ExitAppCard { viewModel.onSignOutClick(restartApp) }
                Spacer(modifier = Modifier.fillMaxWidth().padding(8.dp))
                RemoveAccountCard { viewModel.onDeleteAccountClick(restartApp) }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
