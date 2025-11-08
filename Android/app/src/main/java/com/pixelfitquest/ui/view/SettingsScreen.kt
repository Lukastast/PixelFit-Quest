package com.pixelfitquest.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.pixelfitquest.Helpers.DisplayNameCard
import com.pixelfitquest.Helpers.ExitAppCard
import com.pixelfitquest.Helpers.RemoveAccountCard
import com.pixelfitquest.Helpers.card
import com.pixelfitquest.R
import com.pixelfitquest.model.User
import com.pixelfitquest.ui.components.PixelArtButton
import com.pixelfitquest.viewmodel.SettingsViewModel
import java.util.Locale


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
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
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
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        SetHeight(viewModel)
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

@Composable
fun SetHeight(
    viewModel: SettingsViewModel
) {
    val userSettings by viewModel.userSettings.collectAsState()
    var heightInput by remember { mutableStateOf("") }

    LaunchedEffect(userSettings?.height) {
        heightInput = userSettings?.height?.toString() ?: ""
    }

    Card(modifier = Modifier.card()) {
        Column(
            modifier = Modifier
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding( end = 8.dp)
            ) {
                Text("Current height:")

                Spacer(modifier = Modifier.weight(1f))

                userSettings?.height?.let { currentHeight ->
                    Text("$currentHeight cm")
                }
            }

            OutlinedTextField(
                value = heightInput,
                onValueChange = { heightInput = it },
                label = { Text("Enter height (e.g., 175)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                PixelArtButton(
                    onClick = {
                        val heightCm = heightInput.toIntOrNull()
                        if (heightCm != null && heightCm in 1..272) {
                            viewModel.setHeight(heightCm)
                        }
                    },
                    imageRes = R.drawable.button_unclicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = Modifier.width(180.dp).height(60.dp)
                ){
                    Text("Set Height")
                }
            }
        }
    }
}
