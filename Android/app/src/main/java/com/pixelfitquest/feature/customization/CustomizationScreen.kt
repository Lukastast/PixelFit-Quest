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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import com.pixelfitquest.components.atoms.IdleAnimation
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.components.atoms.PixelCharacterMotion
import com.pixelfitquest.components.atoms.SpriteSheetPlayer
import com.pixelfitquest.feature.levels.LevelsViewModel
import com.pixelfitquest.feature.levels.cosmetics.AvatarSkinBridge
import com.pixelfitquest.ui.theme.spacing
import com.pixelfitquest.ui.theme.typography
import com.pixelfitquest.feature.settings.SettingsViewModel

@Composable
fun CustomizationScreen(
    viewModel: CustomizationViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    levelsViewModel: LevelsViewModel = hiltViewModel(),
) {
    val characterData by viewModel.characterData.collectAsState()
    val userData by settingsViewModel.userData.collectAsState()
    val levelsState by levelsViewModel.uiState.collectAsState()
    val unlockedSkinIds = remember(levelsState.items) {
        levelsState.items
            .filter { it.unlocked }
            .map { it.definition.id }
            .toSet()
    }

    val gender = characterData.gender
    val fitnessVariant = if (gender == "female") "female_fitness" else "male_fitness"
    val premiumVariant = if (gender == "female") "female_premium" else "male_premium"
    val variants = remember(gender) {
        listOf("basic", fitnessVariant, AvatarSkinBridge.VARIANT_SHADOW, premiumVariant)
    }

    var currentVariantIndex by remember { mutableIntStateOf(0) }

    // Synchronize UI index when character data or gender changes
    LaunchedEffect(characterData.variant, gender) {
        val index = variants.indexOf(characterData.variant)
        if (index != -1) currentVariantIndex = index
    }

    val currentVariant = variants[currentVariantIndex]
    val isPremium = currentVariant == premiumVariant
    val isFitness = currentVariant == fitnessVariant
    val isShadow = currentVariant == AvatarSkinBridge.VARIANT_SHADOW
    val unlockedByLevel = AvatarSkinBridge.isUnlockedByLevel(currentVariant, unlockedSkinIds)
    val isUnlocked = characterData.unlockedVariants.contains(currentVariant) || unlockedByLevel
    val levelRequired = AvatarSkinBridge.unlockLevel(currentVariant)

    val displaySprite = remember(currentVariant, gender, isUnlocked, isPremium, isShadow) {
        AvatarSkinBridge.spriteKey(currentVariant, gender, isUnlocked)
    }

    val spacing = MaterialTheme.spacing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        // Character Customization Section
        CustomizationCard {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.choose_character),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(spacing.md))

                Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
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
                        modifier = Modifier.padding(top = spacing.xs)
                    )
                }
                if (isShadow) {
                    Text(
                        text = stringResource(R.string.levels_shadow_skin),
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

                Spacer(modifier = Modifier.height(spacing.xs))

                ActionButtons(
                    isPremium = isPremium,
                    isUnlocked = isUnlocked,
                    lockedLevel = if (!isUnlocked && isShadow) levelRequired else null,
                    onSelect = {
                        viewModel.updateVariant(currentVariant)
                        levelsViewModel.equipByAvatarVariant(currentVariant)
                    },
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
        modifier = Modifier.size(MaterialTheme.spacing.scale(80), MaterialTheme.spacing.scale(40))
    ) {
        Text(text)
    }
}

@Composable
private fun VariantCarousel(spriteKey: String, onPrevious: () -> Unit, onNext: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        val spacing = MaterialTheme.spacing
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            PixelArtButton(
                onClick = onPrevious,
                imageRes = R.drawable.unclicked_customization_button_left,
                pressedRes = R.drawable.clicked_customization_button_left,
                modifier = Modifier.size(spacing.scale(40))
            ) {}

            Spacer(modifier = Modifier.width(spacing.md))

            IdleAnimation(
                modifier = Modifier
                    .size(spacing.scale(120))
                    .offset(x = spacing.scale(-18)),
                gender = spriteKey,
                isAnimating = true
            )

            Spacer(modifier = Modifier.width(spacing.md))

            PixelArtButton(
                onClick = onNext,
                imageRes = R.drawable.unclicked_customization_button_right,
                pressedRes = R.drawable.clicked_customization_button_right,
                modifier = Modifier.size(spacing.scale(40))
            ) {}
        }

        // Hybrid bob+slide preview (#141) — IdleAnimation kept above; flag toggles prototype.
        if (SHOW_BOB_SLIDE_PREVIEW) {
            Spacer(modifier = Modifier.height(spacing.xs))
            Text(
                text = "Bob+slide preview",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 10.sp
            )
            BobSlideCharacterPreview(spriteKey = spriteKey)
            Spacer(modifier = Modifier.height(spacing.xs))
            Text(
                text = "Cape Hero walk (Gemini)",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 10.sp
            )
            CapeHeroWalkPreview()
        }
    }
}

/** Temporary in-app flag for the hybrid motion prototype. */
private const val SHOW_BOB_SLIDE_PREVIEW = true

private fun idleSpriteResId(spriteKey: String): Int = when (spriteKey) {
    "male" -> R.drawable.character_male_idle
    "female" -> R.drawable.character_woman_idle
    "character_male_idle" -> R.drawable.character_male_idle
    "character_woman_idle" -> R.drawable.character_woman_idle
    "locked_male" -> R.drawable.locked_male_character_idle
    "locked_woman" -> R.drawable.locked_woman_character_idle
    "fitness_character_male_idle" -> R.drawable.fitness_character_male_idle
    "fitness_character_woman_idle" -> R.drawable.fitness_character_woman_idle
    else -> R.drawable.character_woman_idle
}

@Composable
private fun BobSlideCharacterPreview(spriteKey: String) {
    // Reuse existing idle sheet; motion uses frame 0 only (no walk strip).
    val sheet = ImageBitmap.imageResource(idleSpriteResId(spriteKey))
    val frameCount = 13
    val frameWidth = sheet.width.toFloat() / frameCount
    val frameHeight = sheet.height.toFloat()

    PixelCharacterMotion(
        bitmap = sheet,
        modifier = Modifier
            .size(96.dp)
            .offset(x = (-12).dp),
        bobAmplitudeDp = 4.dp,
        bobDurationMs = 900,
        walkEnabled = true,
        walkSpeed = 1f,
        walkAmplitudeDp = 12.dp,
        frameWidthPx = frameWidth,
        frameHeightPx = frameHeight,
    )
}

/** Gemini Cape Hero 8-frame walk strip preview (matches former cape_hero.json). */
@Composable
private fun CapeHeroWalkPreview() {
    val sheet = ImageBitmap.imageResource(R.drawable.cape_hero_walk)
    SpriteSheetPlayer(
        sheet = sheet,
        frameCount = 8,
        fps = 10,
        modifier = Modifier
            .size(96.dp)
            .offset(x = (-12).dp),
    )
}

@Composable
private fun ActionButtons(
    isPremium: Boolean,
    isUnlocked: Boolean,
    lockedLevel: Int? = null,
    onSelect: () -> Unit,
    onBuy: () -> Unit,
    price: Int = 100
) {
    val spacing = MaterialTheme.spacing
    val modifier = Modifier.size(spacing.scale(200), spacing.buttonHeight)
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
        lockedLevel != null && lockedLevel > 1 -> {
            PixelArtButton(onClick = {}, imageRes = R.drawable.button_unclicked, pressedRes = R.drawable.button_unclicked, modifier = modifier) {
                Text(stringResource(R.string.levels_locked_level, lockedLevel))
            }
        }
        else -> {
            PixelArtButton(onClick = onBuy, imageRes = R.drawable.button_unclicked, pressedRes = R.drawable.button_clicked, modifier = modifier) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$price ")
                    Image(painter = painterResource(R.drawable.coin), contentDescription = null, modifier = Modifier.size(MaterialTheme.spacing.md))
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

    val spacing = MaterialTheme.spacing
    CustomizationCard {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing.md),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.current_height, currentHeight ?: "--"),
                color = Color.White,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(spacing.md))

            Text(text = stringResource(R.string.enter_height_hint), color = Color.White, fontSize = 12.sp)

            Box(modifier = Modifier.size(spacing.scale(200), spacing.inputHeight), contentAlignment = Alignment.Center) {
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

            Spacer(modifier = Modifier.height(spacing.md))

            PixelArtButton(
                onClick = {
                    heightInput.toIntOrNull()?.let { if (it in 1..272) onSave(it) }
                },
                imageRes = R.drawable.button_unclicked,
                pressedRes = R.drawable.button_clicked,
                modifier = Modifier.size(spacing.scale(220), spacing.buttonHeight)
            ) {
                Text(stringResource(R.string.set_height), fontSize = 14.sp)
            }
        }
    }
}
