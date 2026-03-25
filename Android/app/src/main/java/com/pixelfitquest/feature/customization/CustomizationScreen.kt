package com.pixelfitquest.feature.customization

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.pixelfitquest.R
import com.pixelfitquest.components.atoms.IdleAnimation
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.ui.theme.typography
import com.pixelfitquest.feature.settings.SettingsViewModel

@Composable
fun CustomizationScreen(
    viewModel: CustomizationViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val characterData by viewModel.characterData.collectAsState()
    val userData by settingsViewModel.userData.collectAsState()

    val gender = characterData.gender
    val fitnessVariant = if (gender == "female") "female_fitness" else "male_fitness"
    val premiumVariant = if (gender == "female") "female_premium" else "male_premium"
    val variants = remember(gender) { listOf("basic", fitnessVariant, premiumVariant) }

    var currentVariantIndex by remember { mutableIntStateOf(0) }

    // Synchronize UI index when character data or gender changes
    LaunchedEffect(characterData.variant, gender) {
        val index = variants.indexOf(characterData.variant)
        if (index != -1) currentVariantIndex = index
    }

    val currentVariant = variants[currentVariantIndex]
    val isUnlocked = characterData.unlockedVariants.contains(currentVariant)
    val isPremium = currentVariant == premiumVariant
    val isFitness = currentVariant == fitnessVariant

    // Logic to determine which sprite key to pass to IdleAnimation
    val displaySprite = remember(currentVariant, gender, isUnlocked, isPremium) {
        when {
            currentVariant == "basic" -> gender
            isPremium || !isUnlocked -> if (gender == "female") "locked_woman" else "locked_male"
            else -> "fitness_character_${if (gender == "female") "woman" else "male"}_idle"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Character Customization Section
        CustomizationCard {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.choose_character),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GenderToggleButton(stringResource(R.string.male), isSelected = gender == "male") {
                        viewModel.updateGender("male")
                    }
                    GenderToggleButton(stringResource(R.string.female), isSelected = gender == "female") {
                        viewModel.updateGender("female")
                    }
                }

                if (isUnlocked && isFitness) {
                    Text(
                        text = stringResource(R.string.fitness_bonus),
                        color = Color.White,
                        fontSize = 12.sp,
                        style = typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                VariantCarousel(
                    spriteKey = displaySprite,
                    onPrevious = { currentVariantIndex = (currentVariantIndex - 1 + variants.size) % variants.size },
                    onNext = { currentVariantIndex = (currentVariantIndex + 1) % variants.size }
                )

                Spacer(modifier = Modifier.height(8.dp))

                ActionButtons(
                    isPremium = isPremium,
                    isUnlocked = isUnlocked,
                    onSelect = { viewModel.updateVariant(currentVariant) },
                    onBuy = { viewModel.buyVariant(currentVariant, 100) }
                )
            }
        }

        // User Stats / Height Section
        HeightSettingsCard(
            currentHeight = userData?.height,
            onSave = { settingsViewModel.setHeight(it) }
        )
    }
}

@Composable
private fun CustomizationCard(content: @Composable BoxScope.() -> Unit) {
    val painter = painterResource(id = R.drawable.info_background_even_even_higher)
    val aspectRatio = remember(painter) {
        val size = painter.intrinsicSize
        if (size.isSpecified) size.height / size.width else 0.7f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
        content()
    }
}

@Composable
private fun GenderToggleButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    PixelArtButton(
        onClick = onClick,
        imageRes = if (isSelected) R.drawable.button_clicked else R.drawable.button_unclicked,
        pressedRes = R.drawable.button_clicked,
        modifier = Modifier.size(80.dp, 40.dp)
    ) {
        Text(text)
    }
}

@Composable
private fun VariantCarousel(spriteKey: String, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        PixelArtButton(
            onClick = onPrevious,
            imageRes = R.drawable.unclicked_customization_button_left,
            pressedRes = R.drawable.clicked_customization_button_left,
            modifier = Modifier.size(40.dp)
        ) {}

        Spacer(modifier = Modifier.width(16.dp))

        IdleAnimation(
            modifier = Modifier
                .size(120.dp)
                .offset(x = (-18).dp),
            gender = spriteKey,
            isAnimating = true
        )

        Spacer(modifier = Modifier.width(16.dp))

        PixelArtButton(
            onClick = onNext,
            imageRes = R.drawable.unclicked_customization_button_right,
            pressedRes = R.drawable.clicked_customization_button_right,
            modifier = Modifier.size(40.dp)
        ) {}
    }
}

@Composable
private fun ActionButtons(
    isPremium: Boolean,
    isUnlocked: Boolean,
    onSelect: () -> Unit,
    onBuy: () -> Unit,
    price: Int = 100
) {
    val modifier = Modifier.size(200.dp, 60.dp)
    when {
        isPremium -> {
            PixelArtButton(onClick = {}, imageRes = R.drawable.button_unclicked, pressedRes = R.drawable.button_unclicked, modifier = modifier) {
                Text(stringResource(R.string.coming_soon))
            }
        }
        isUnlocked -> {
            PixelArtButton(onClick = onSelect, imageRes = R.drawable.button_unclicked, pressedRes = R.drawable.button_clicked, modifier = modifier) {
                Text(stringResource(R.string.select))
            }
        }
        else -> {
            PixelArtButton(onClick = onBuy, imageRes = R.drawable.button_unclicked, pressedRes = R.drawable.button_clicked, modifier = modifier) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$price ")
                    Image(painter = painterResource(R.drawable.coin), contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(stringResource(R.string.coins_label))
                }
            }
        }
    }
}

@Composable
private fun HeightSettingsCard(currentHeight: Int?, onSave: (Int) -> Unit) {
    var heightInput by remember { mutableStateOf("") }
    LaunchedEffect(currentHeight) { heightInput = currentHeight?.toString() ?: "" }

    CustomizationCard {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.current_height, currentHeight ?: "--"),
                color = Color.White,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = stringResource(R.string.enter_height_hint), color = Color.White, fontSize = 12.sp)

            Box(modifier = Modifier.size(200.dp, 60.dp), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.inputfield),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
                TextField(
                    value = heightInput,
                    onValueChange = { if (it.length <= 3) heightInput = it.filter { c -> c.isDigit() } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxHeight().fillMaxWidth(0.9f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        unfocusedTextColor = Color.Black,
                        focusedTextColor = Color.Black
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PixelArtButton(
                onClick = {
                    heightInput.toIntOrNull()?.let { if (it in 1..272) onSave(it) }
                },
                imageRes = R.drawable.button_unclicked,
                pressedRes = R.drawable.button_clicked,
                modifier = Modifier.size(220.dp, 60.dp)
            ) {
                Text(stringResource(R.string.set_height), fontSize = 14.sp)
            }
        }
    }
}
