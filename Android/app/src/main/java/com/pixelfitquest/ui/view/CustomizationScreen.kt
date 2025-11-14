package com.pixelfitquest.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixelfitquest.R
import com.pixelfitquest.ui.components.IdleAnimation
import com.pixelfitquest.ui.components.PixelArtButton
import com.pixelfitquest.viewmodel.CustomizationViewModel
import com.pixelfitquest.viewmodel.SettingsViewModel


@Composable
fun CustomizationScreen(
    openScreen: (String) -> Unit,
    viewModel: CustomizationViewModel = hiltViewModel()
) {
    val characterData by viewModel.characterData.collectAsState()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val offset = -18
    val painter = painterResource(id = R.drawable.info_background_even_even_higher)
    val intrinsicSize = painter.intrinsicSize
    val aspectRatio = if (intrinsicSize.isSpecified) {
        intrinsicSize.height / intrinsicSize.width
    } else {
        280f / 400f // fallback aspect ratio, adjust based on your image if needed
    }

    var currentVariantIndex by remember { mutableIntStateOf(0) }
    val variants = listOf("basic", "premium") // Add more variants as needed
    val currentVariant = variants[currentVariantIndex]
    val isUnlocked = characterData.unlockedVariants.contains(currentVariant)
    val isLocked = !isUnlocked

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Custom card with background image for the entire customization screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
        ) {
            // Background image
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Content on top
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,

                ) {
                Text(
                    text = "Choose Your Character",
                    style = MaterialTheme.typography.bodyMedium,  // Same font as PixelArtButton
                    color = Color.White  // White color
                )

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

                // Left and Right Navigation Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PixelArtButton(
                        onClick = {
                            currentVariantIndex = (currentVariantIndex - 1 + variants.size) % variants.size
                        },
                        imageRes = R.drawable.unclicked_customization_button_left,
                        pressedRes = R.drawable.clicked_customization_button_left,
                        modifier = Modifier.size(40.dp)
                    ) {}

                    Spacer(modifier = Modifier.width(16.dp))

                    // Animated Character Preview
                    if (isLocked) {
                        IdleAnimation(
                            modifier = Modifier
                                .size(120.dp)
                                .offset(x = (offset).dp),
                            gender = if (characterData.gender == "female") "locked_woman" else "locked_male",
                            isAnimating = true
                        )
                    } else {
                        IdleAnimation(
                            modifier = Modifier
                                .size(120.dp)
                                .offset(x = (offset).dp),
                            gender = characterData.gender,
                            isAnimating = true
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    PixelArtButton(
                        onClick = {
                            currentVariantIndex = (currentVariantIndex + 1) % variants.size
                        },
                        imageRes = R.drawable.unclicked_customization_button_right,
                        pressedRes = R.drawable.clicked_customization_button_right,
                        modifier = Modifier.size(40.dp)
                    ) {}
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Premium character option
                if (isUnlocked) {
                    PixelArtButton(
                        onClick = { viewModel.updateVariant(currentVariant) },
                        imageRes = R.drawable.button_unclicked,
                        pressedRes = R.drawable.button_clicked,
                        modifier = Modifier.size(200.dp, 60.dp)
                    ) {
                        Text("Select")
                    }
                } else {
                    PixelArtButton(
                        onClick = { viewModel.buyVariant(currentVariant, 100) },
                        imageRes = R.drawable.button_unclicked,
                        pressedRes = R.drawable.button_clicked,
                        modifier = Modifier.size(200.dp, 60.dp)
                    ) {
                        Text("Buy for 100 coins")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        SetHeight(settingsViewModel)
    }
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

    val painter = painterResource(id = R.drawable.info_background_even_even_higher)
    val intrinsicSize = painter.intrinsicSize
    val aspectRatio = if (intrinsicSize.isSpecified) {
        intrinsicSize.height / intrinsicSize.width
    } else {
        280f / 400f // fallback aspect ratio, adjust based on your image if needed
    }

    // Custom card with background image for set height
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
    ) {
        // Background image
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // Content on top
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Centered row for current height text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current height:",
                        color = Color.White,
                        fontSize = 18.sp
                    )

                    userSettings?.height?.let { currentHeight ->
                        Text(
                            text = " $currentHeight cm",
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Label for input field
            Text(
                text = "Enter height (e.g., 175)",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Custom input field with background image
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.inputfield),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
                TextField(
                    singleLine = true,
                    value = heightInput,
                    onValueChange = { heightInput = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.96f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,
                        unfocusedTextColor = Color.Black,
                        focusedTextColor = Color.Black
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    modifier = Modifier.width(220.dp).height(60.dp)
                ){
                    Text("Set Height", fontSize = 14.sp)
                }
            }
        }
    }
}