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
import com.pixelfitquest.ui.components.PixelArtButton
import com.pixelfitquest.viewmodel.CustomizationViewModel
import com.pixelfitquest.R

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
            PixelArtButton(
                onClick = { viewModel.updateGender("male") },
                imageRes = R.drawable.button_unclicked,  // Your normal PNG
                pressedRes = R.drawable.button_clicked,  // Your pressed PNG
                modifier = Modifier.size(80.dp, 40.dp)  // Compact size for toggles
            ) {
                Text("Male")
            }
            PixelArtButton(
                onClick = { viewModel.updateGender("female") },
                imageRes = R.drawable.button_unclicked,
                pressedRes = R.drawable.button_clicked,
                modifier = Modifier.size(80.dp, 40.dp)
            ) {
                Text("Female")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Animated Character Preview
        IdleAnimation(
            modifier = Modifier.size(200.dp),
            gender = characterData.gender,
            isAnimating = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        PixelArtButton(
            onClick = { openScreen("home") },  // Save and return
            imageRes = R.drawable.button_unclicked,  // Your normal PNG
            pressedRes = R.drawable.button_clicked,  // Your pressed PNG
            modifier = Modifier.size(200.dp, 60.dp)  // Wider for emphasis
        ) {
            Text("Save and Continue")
        }
    }
}
