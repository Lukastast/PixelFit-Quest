package com.pixelfitquest.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.pixelfitquest.Helpers.DisplayNameCard
import com.pixelfitquest.Helpers.ExitAppCard
import com.pixelfitquest.Helpers.RemoveAccountCard
import com.pixelfitquest.Helpers.card
import com.pixelfitquest.model.User
import com.pixelfitquest.viewmodel.SettingsViewModel
import java.util.Locale
import com.pixelfitquest.R


@Composable
fun SettingsScreen(restartApp: (String) -> Unit,
                   viewModel: SettingsViewModel = hiltViewModel()) {

    val user by viewModel.user.collectAsState(initial = User())
    val provider = user.provider.replaceFirstChar { it.titlecase(Locale.getDefault()) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Customize your PixelFit Quest experience.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        ProfileImage(viewModel)

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        )

        DisplayNameCard(user.displayName) { viewModel.onUpdateDisplayNameClick(it) }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        )

        Card(modifier = Modifier.card()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = String.format(stringResource(R.string.profile_email), user.email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                ExitAppCard { viewModel.onSignOutClick(restartApp) }
                RemoveAccountCard { viewModel.onDeleteAccountClick(restartApp) }
            }
        }
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