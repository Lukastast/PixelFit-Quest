package com.pixelfitquest.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.pixelfitquest.helpers.ExitAppCard
import com.pixelfitquest.helpers.RemoveAccountCard
import com.pixelfitquest.helpers.VolumeCard
import com.pixelfitquest.R
import com.pixelfitquest.model.User
import com.pixelfitquest.ui.theme.typography
import com.pixelfitquest.viewmodel.SettingsViewModel
import java.util.Locale


@Composable
fun SettingsScreen(
    restartApp: (String) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    onScreenReady: () -> Unit = {}
) {
    val user by viewModel.user.collectAsState(initial = User())
    val userSettings by viewModel.userData.collectAsState(initial = null)
    val musicVolume = userSettings?.musicVolume ?: 50
    val provider = user.provider.replaceFirstChar { it.titlecase(Locale.getDefault()) }

    LaunchedEffect(Unit) {
        onScreenReady()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    text = "Settings",
                    style = typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

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
                    Text(
                        text = String.format(stringResource(R.string.profile_email), user.email),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            VolumeCard(
                musicVolume = musicVolume,
                onVolumeChange = { viewModel.setMusicVolume(it) }
            )

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            ExitAppCard { viewModel.onSignOutClick(restartApp) }

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            RemoveAccountCard { viewModel.onDeleteAccountClick(restartApp) }
        }
    }
}