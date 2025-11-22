package com.pixelfitquest.ui.view

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixelfitquest.Helpers.ExitAppCard
import com.pixelfitquest.Helpers.RemoveAccountCard
import com.pixelfitquest.Helpers.TypewriterText
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
    val musicVolume by remember { mutableStateOf(userSettings?.musicVolume ?: 50) }
    val provider = user.provider.replaceFirstChar { it.titlecase(Locale.getDefault()) }

    Box(modifier = Modifier.fillMaxSize()) {
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
        // NEW: Tutorial overlay for first time
        val context = LocalContext.current
        val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
        var showTutorial by remember { mutableStateOf(prefs.getBoolean("first_time_settings", true)) }
        if (showTutorial) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                TypewriterText(
                    text = "Welcome to Settings! Here you can adjust music volume. Sign out or delete your account if needed.",
                    onComplete = {
                        prefs.edit().putBoolean("first_time_settings", false).apply()
                        showTutorial = false
                    },
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}