package com.pixelfitquest.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixelfitquest.ui.components.IdleAnimation
import com.pixelfitquest.viewmodel.CustomizationViewModel

@Composable
fun CustomizationScreen(
    openScreen: (String) -> Unit,
    viewModel: CustomizationViewModel = hiltViewModel()
) {
    val characterData by viewModel.characterData.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Customize Your Character")

        Spacer(modifier = Modifier.height(16.dp))

        // Gender Toggle Buttons
        Row {
            Button(
                onClick = { viewModel.updateGender("male") },
                enabled = characterData.gender != "male"
            ) { Text("Male") }
            Button(
                onClick = { viewModel.updateGender("female") },
                enabled = characterData.gender != "female"
            ) { Text("Female") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Animated Character Preview
        IdleAnimation(
            modifier = Modifier.size(200.dp),
            gender = characterData.gender,
            isAnimating = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { openScreen("home") }  // Save and return
        ) { Text("Save and Continue") }
    }
}