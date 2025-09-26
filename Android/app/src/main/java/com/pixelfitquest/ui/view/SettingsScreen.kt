package com.pixelfitquest.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixelfitquest.ui.theme.typography
import com.pixelfitquest.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(restartApp: (String) -> Unit,
                   viewModel: SettingsViewModel = hiltViewModel()) {


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

        // Example settings options
        Button(
            onClick = { /* TODO: Implement profile settings navigation or action */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Edit Profile")
        }

        Button(
            onClick = { /* TODO: Implement notification settings action */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Notification Settings")
        }

        Button(
            onClick = { /* TODO: Implement device sync settings action */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Sync Devices")
        }
        Button(

            onClick = {
                viewModel.onSignOutClick(restartApp) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Out", style = typography.labelLarge)
        }
    }
}