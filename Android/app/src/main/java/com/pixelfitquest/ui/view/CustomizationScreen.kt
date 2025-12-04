package com.pixelfitquest.ui.view

import android.util.Log
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
import com.pixelfitquest.ui.theme.typography
import com.pixelfitquest.viewmodel.CustomizationViewModel
import com.pixelfitquest.viewmodel.SettingsViewModel


@Composable
fun CustomizationScreen(
    openScreen: (String) -> Unit,
    viewModel: CustomizationViewModel = hiltViewModel(),
) {
    val characterData by viewModel.characterData.collectAsState()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val offset = -18
    val price = 100
    val painter = painterResource(id = R.drawable.info_background_even_even_higher)
    val intrinsicSize = painter.intrinsicSize
    val aspectRatio = if (intrinsicSize.isSpecified) {
        intrinsicSize.height / intrinsicSize.width
    } else {
        280f / 400f
    }

    var currentVariantIndex by remember { mutableStateOf(0) }

    val fitnessVariant = if (characterData.gender == "female") "female_fitness" else "male_fitness"
    val premiumVariant = if (characterData.gender == "female") "female_premium" else "male_premium"
    val variants = listOf("basic", fitnessVariant, premiumVariant)
    val currentVariant = variants[currentVariantIndex]
    val isUnlocked = characterData.unlockedVariants.contains(currentVariant)
    val isPremium = currentVariant == premiumVariant
    val isFitness = currentVariant == fitnessVariant

    LaunchedEffect(characterData.variant, characterData.gender) {
        val savedVariant = characterData.variant
        when (savedVariant) {
            fitnessVariant -> currentVariantIndex = 1
            premiumVariant -> currentVariantIndex = 2
            else -> currentVariantIndex = 0
        }
    }

    // FIXED: Correct sprite names with explicit gender check
    val displayGender = when {
        isPremium -> {
            // Premium is always locked
            if (characterData.gender == "female") "locked_woman" else "locked_male"
        }
        isFitness && isUnlocked -> {
            // Fitness variant unlocked - show the fitness sprite
            if (characterData.gender == "female") {
                "fitness_character_woman_idle"
            } else {
                "fitness_character_male_idle"
            }
        }
        isFitness && !isUnlocked -> {
            // Fitness variant locked - show locked sprite
            if (characterData.gender == "female") "locked_woman" else "locked_male"
        }
        else -> {
            // Basic variant - show basic sprite
            // FIXED: Use direct equality check instead of just else
            when (characterData.gender) {
                "female" -> "character_woman_idle"
                "male" -> "character_male_idle"
                else -> {
                    // Fallback for unexpected values - log and default to male
                    Log.w("CustomizationScreen", "Unexpected gender value: ${characterData.gender}, defaulting to male")
                    "character_male_idle"
                }
            }
        }
    }

    // Add debug logging
    LaunchedEffect(characterData.gender, displayGender) {
        Log.d("CustomizationScreen", "Gender: ${characterData.gender}, Display: $displayGender, Variant: $currentVariant, IsUnlocked: $isUnlocked")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
            ) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Choose Your Character",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row {
                        PixelArtButton(
                            onClick = {
                                Log.d("CustomizationScreen", "Male button clicked")
                                viewModel.updateGender("male")
                            },
                            imageRes = R.drawable.button_unclicked,
                            pressedRes = R.drawable.button_clicked,
                            modifier = Modifier.size(80.dp, 40.dp)
                        ) {
                            Text("Male")
                        }
                        PixelArtButton(
                            onClick = {
                                Log.d("CustomizationScreen", "Female button clicked")
                                viewModel.updateGender("female")
                            },
                            imageRes = R.drawable.button_unclicked,
                            pressedRes = R.drawable.button_clicked,
                            modifier = Modifier.size(80.dp, 40.dp)
                        ) {
                            Text("Female")
                        }
                    }

                    if (isUnlocked && isFitness) {
                        Text(
                            text = "+2 coins & +2 exp per reward",
                            color = Color.White,
                            fontSize = 12.sp,
                            style = typography.bodyMedium
                        )
                    }

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

                        IdleAnimation(
                            modifier = Modifier
                                .size(120.dp)
                                .offset(x = (offset).dp),
                            gender = displayGender,
                            isAnimating = true
                        )

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

                    if (isPremium) {
                        PixelArtButton(
                            onClick = { },
                            imageRes = R.drawable.button_unclicked,
                            pressedRes = R.drawable.button_unclicked,
                            modifier = Modifier.size(200.dp, 60.dp)
                        ) {
                            Text("Coming Soon")
                        }
                    } else if (isUnlocked) {
                        PixelArtButton(
                            onClick = {
                                viewModel.updateVariant(currentVariant)
                            },
                            imageRes = R.drawable.button_unclicked,
                            pressedRes = R.drawable.button_clicked,
                            modifier = Modifier.size(200.dp, 60.dp)
                        ) {
                            Text("Select")
                        }
                    } else {
                        PixelArtButton(
                            onClick = { viewModel.buyVariant(currentVariant, price) },
                            imageRes = R.drawable.button_unclicked,
                            pressedRes = R.drawable.button_clicked,
                            modifier = Modifier.size(200.dp, 60.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("$price ")
                                Image(
                                    painter = painterResource(R.drawable.coin),
                                    contentDescription = "Coin icon",
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(" coins")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            SetHeight(settingsViewModel)
        }
    }
}



@Composable
fun SetHeight(
    viewModel: SettingsViewModel
) {
    val userSettings by viewModel.userSettings.collectAsState(initial = null)
    var heightInput by remember { mutableStateOf("") }

    LaunchedEffect(userSettings?.height) {
        heightInput = userSettings?.height?.toString() ?: ""
    }

    val painter = painterResource(id = R.drawable.info_background_even_even_higher)
    val intrinsicSize = painter.intrinsicSize
    val aspectRatio = if (intrinsicSize.isSpecified) {
        intrinsicSize.height / intrinsicSize.width
    } else {
        280f / 400f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
    ) {
        Image(
            painter = painter,
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

            Text(
                text = "Enter height (e.g., 175)",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

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
                ) {
                    Text("Set Height", fontSize = 14.sp)
                }
            }
        }
    }
}