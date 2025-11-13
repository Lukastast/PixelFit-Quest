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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.pixelfitquest.Helpers.DisplayNameCard
import com.pixelfitquest.Helpers.ExitAppCard
import com.pixelfitquest.Helpers.RemoveAccountCard
import com.pixelfitquest.Helpers.VolumeCard
import com.pixelfitquest.R
import com.pixelfitquest.model.User
import com.pixelfitquest.ui.theme.typography
import com.pixelfitquest.viewmodel.SettingsViewModel
import java.util.Locale


@Composable
fun SettingsScreen(restartApp: (String) -> Unit,
                   viewModel: SettingsViewModel = hiltViewModel()) {

    val user by viewModel.user.collectAsState(initial = User())
    val userSettings by viewModel.userSettings.collectAsState(initial = null)
    val musicVolume by remember { derivedStateOf { userSettings?.musicVolume ?: 50 } }
    val provider = user.provider.replaceFirstChar { it.titlecase(Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Updated title with background image at top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)  // Adjust height for image
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

        ProfileImage(viewModel)

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        DisplayNameCard(user.displayName) { viewModel.onUpdateDisplayNameClick(it) }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        // Email section with background image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(16.dp, 0.dp, 16.dp, 8.dp)
        ) {
            // Background image
            Image(
                painter = painterResource(id = R.drawable.info_background_higher),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Email text on top
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

        // UPDATED: Volume section (now uses VolumeCard from Helpers for consistent width/styling)
        VolumeCard(
            musicVolume = musicVolume,
            onVolumeChange = { viewModel.setMusicVolume(it) }
        )

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        // ExitAppCard as standalone
        ExitAppCard { viewModel.onSignOutClick(restartApp) }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        // RemoveAccountCard as standalone
        RemoveAccountCard { viewModel.onDeleteAccountClick(restartApp) }

        //if (!user.isLinkedWithGoogle) {
        //                    //needs to be changed with link button
        //                    AuthenticationButton(
        //                        buttonText = R.string.link_google_account,
        //                        modifier = Modifier
        //                            .fillMaxWidth()
        //                            .padding(16.dp)
        //                    ) { credential ->
        //                        viewModel.linkAccountWithGoogle(credential)
        //                    }
        //                }
    }
}

@Composable
fun ProfileImage(viewModel: SettingsViewModel) {
    val profileModel by remember { derivedStateOf { viewModel.getProfilePictureModel() } }

    AsyncImage(
        model = profileModel,
        contentDescription = "Profile picture",
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape),  // Optional: Round it like your app icon
        placeholder = painterResource(R.drawable.pixelfiticon),  // Show default while loading
        error = painterResource(R.drawable.pixelfiticon)  // Fallback on error
    )
}