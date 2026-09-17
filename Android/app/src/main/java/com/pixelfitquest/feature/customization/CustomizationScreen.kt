package com.pixelfitquest.feature.customization

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.pixelfitquest.R
import com.pixelfitquest.components.atoms.IdleAnimation
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.components.atoms.PixelCharacterMotion
import com.pixelfitquest.components.atoms.SpriteSheetPlayer
import com.pixelfitquest.feature.customization.CustomizationViewModel.Companion.ARM_CM_DEFAULT
import com.pixelfitquest.feature.customization.CustomizationViewModel.Companion.ARM_CM_MAX
import com.pixelfitquest.feature.customization.CustomizationViewModel.Companion.ARM_CM_MIN
import com.pixelfitquest.feature.customization.CustomizationViewModel.Companion.HEIGHT_CM_MAX
import com.pixelfitquest.feature.customization.CustomizationViewModel.Companion.HEIGHT_CM_MIN
import com.pixelfitquest.feature.levels.LevelsViewModel
import com.pixelfitquest.feature.levels.cosmetics.AvatarSkinBridge
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import com.pixelfitquest.ui.theme.spacing
import com.pixelfitquest.ui.theme.typography
import kotlin.math.roundToInt

private enum class CustomizationTab { Character, Home, Stats }

@Composable
fun CustomizationScreen(
    viewModel: CustomizationViewModel = hiltViewModel(),
    levelsViewModel: LevelsViewModel = hiltViewModel(),
) {
    val characterData by viewModel.characterData.collectAsState()
    val userData by viewModel.userData.collectAsState()
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
    var selectedTab by remember { mutableStateOf(CustomizationTab.Character) }

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
    ) {
        CustomizationTabBar(
            selected = selectedTab,
            onSelect = { selectedTab = it },
        )

        Spacer(modifier = Modifier.height(spacing.md))

        when (selectedTab) {
            CustomizationTab.Character -> CharacterCustomizationPanel(
                gender = gender,
                displaySprite = displaySprite,
                isPremium = isPremium,
                isFitness = isFitness,
                isShadow = isShadow,
                isUnlocked = isUnlocked,
                levelRequired = levelRequired,
                onMale = { viewModel.updateGender("male") },
                onFemale = { viewModel.updateGender("female") },
                onPrevious = {
                    currentVariantIndex = (currentVariantIndex - 1 + variants.size) % variants.size
                },
                onNext = {
                    currentVariantIndex = (currentVariantIndex + 1) % variants.size
                },
                onSelect = {
                    viewModel.updateVariant(currentVariant)
                    levelsViewModel.equipByAvatarVariant(currentVariant)
                },
                onBuy = { viewModel.buyVariant(currentVariant, 100) },
            )
            CustomizationTab.Home -> HomeUpgradePanel(
                coins = userData?.coins ?: 0,
                isGymUnlocked = characterData.unlockedHomeUpgrades.contains(CustomizationViewModel.GYM_UPGRADE_ID),
                isGymEquipped = characterData.equippedHomeUpgrade == CustomizationViewModel.GYM_UPGRADE_ID,
                onBuyGym = { viewModel.buyHomeUpgrade(CustomizationViewModel.GYM_UPGRADE_ID, CustomizationViewModel.GYM_UPGRADE_PRICE) },
                onEquipGym = { viewModel.equipHomeUpgrade(CustomizationViewModel.GYM_UPGRADE_ID) },
                onEquipDefault = { viewModel.equipHomeUpgrade(null) },
            )
            CustomizationTab.Stats -> StatsPanel(
                heightCm = userData?.height ?: 178,
                armLengthCm = userData?.armLength?.roundToInt() ?: ARM_CM_DEFAULT,
                onHeightChange = viewModel::setHeight,
                onArmLengthChange = viewModel::setArmLength,
            )
        }
    }
}

@Composable
private fun CustomizationTabBar(
    selected: CustomizationTab,
    onSelect: (CustomizationTab) -> Unit,
) {
    val spacing = MaterialTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.md),
        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        PixelArtButton(
            onClick = { onSelect(CustomizationTab.Character) },
            imageRes = if (selected == CustomizationTab.Character) {
                R.drawable.button_clicked
            } else {
                R.drawable.button_unclicked
            },
            pressedRes = R.drawable.button_clicked,
            modifier = Modifier
                .weight(1f)
                .height(spacing.scale(50)),
        ) {
            Text(
                text = stringResource(R.string.customization_tab_character),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            )
        }
        PixelArtButton(
            onClick = { onSelect(CustomizationTab.Home) },
            imageRes = if (selected == CustomizationTab.Home) {
                R.drawable.button_clicked
            } else {
                R.drawable.button_unclicked
            },
            pressedRes = R.drawable.button_clicked,
            modifier = Modifier
                .weight(1f)
                .height(spacing.scale(50)),
        ) {
            Text(
                text = stringResource(R.string.customization_tab_home),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            )
        }
        PixelArtButton(
            onClick = { onSelect(CustomizationTab.Stats) },
            imageRes = if (selected == CustomizationTab.Stats) {
                R.drawable.button_clicked
            } else {
                R.drawable.button_unclicked
            },
            pressedRes = R.drawable.button_clicked,
            modifier = Modifier
                .weight(1f)
                .height(spacing.scale(50)),
        ) {
            Text(
                text = stringResource(R.string.customization_tab_stats),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun HomeUpgradePanel(
    coins: Int,
    isGymUnlocked: Boolean,
    isGymEquipped: Boolean,
    onBuyGym: () -> Unit,
    onEquipGym: () -> Unit,
    onEquipDefault: () -> Unit,
) {
    val spacing = MaterialTheme.spacing
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CustomizationCard {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.gym_upgrade_title),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp,
                )

                Spacer(modifier = Modifier.height(spacing.xs))

                Text(
                    text = stringResource(R.string.gym_upgrade_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = spacing.sm),
                )

                Spacer(modifier = Modifier.height(spacing.sm))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(spacing.cornerSm)),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.dwelling_gym_landscape),
                        contentDescription = "Iron Gym Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }

                Spacer(modifier = Modifier.height(spacing.sm))

                val buttonModifier = Modifier.size(spacing.scale(200), spacing.buttonHeight)
                if (isGymUnlocked) {
                    if (isGymEquipped) {
                        PixelArtButton(
                            onClick = onEquipDefault,
                            imageRes = R.drawable.button_unclicked,
                            pressedRes = R.drawable.button_clicked,
                            modifier = buttonModifier,
                        ) {
                            Text(stringResource(R.string.gym_upgrade_use_default), color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(spacing.xxs))
                        Text(
                            text = stringResource(R.string.gym_upgrade_equipped),
                            color = Color(0xFFFFD700),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    } else {
                        PixelArtButton(
                            onClick = onEquipGym,
                            imageRes = R.drawable.button_unclicked,
                            pressedRes = R.drawable.button_clicked,
                            modifier = buttonModifier,
                        ) {
                            Text(stringResource(R.string.gym_upgrade_equip), color = Color.White)
                        }
                    }
                } else {
                    val canAfford = coins >= CustomizationViewModel.GYM_UPGRADE_PRICE
                    PixelArtButton(
                        onClick = { if (canAfford) onBuyGym() },
                        imageRes = if (canAfford) R.drawable.button_unclicked else R.drawable.button_clicked,
                        pressedRes = R.drawable.button_clicked,
                        modifier = buttonModifier,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${CustomizationViewModel.GYM_UPGRADE_PRICE} ")
                            Image(
                                painter = painterResource(R.drawable.coin),
                                contentDescription = null,
                                modifier = Modifier.size(MaterialTheme.spacing.md),
                            )
                            Text(stringResource(R.string.coins_label))
                        }
                    }
                    if (!canAfford) {
                        Spacer(modifier = Modifier.height(spacing.xxs))
                        Text(
                            text = stringResource(R.string.gym_upgrade_not_enough_coins),
                            color = Color.Red.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CharacterCustomizationPanel(
    gender: String,
    displaySprite: String,
    isPremium: Boolean,
    isFitness: Boolean,
    isShadow: Boolean,
    isUnlocked: Boolean,
    levelRequired: Int?,
    onMale: () -> Unit,
    onFemale: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSelect: () -> Unit,
    onBuy: () -> Unit,
) {
    val spacing = MaterialTheme.spacing
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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
                    GenderToggleButton(stringResource(R.string.male), isSelected = gender == "male", onClick = onMale)
                    GenderToggleButton(stringResource(R.string.female), isSelected = gender == "female", onClick = onFemale)
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
                    onPrevious = onPrevious,
                    onNext = onNext,
                )

                Spacer(modifier = Modifier.height(spacing.xs))

                ActionButtons(
                    isPremium = isPremium,
                    isUnlocked = isUnlocked,
                    lockedLevel = if (!isUnlocked && isShadow) levelRequired else null,
                    onSelect = onSelect,
                    onBuy = onBuy,
                )
            }
        }
    }
}

@Composable
private fun StatsPanel(
    heightCm: Int,
    armLengthCm: Int,
    onHeightChange: (Int) -> Unit,
    onArmLengthChange: (Int) -> Unit,
) {
    val spacing = MaterialTheme.spacing
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CustomizationCard {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(spacing.md),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.stats_title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(spacing.md))
                MetricSlider(
                    label = stringResource(R.string.height_label),
                    valueCm = heightCm.coerceIn(HEIGHT_CM_MIN, HEIGHT_CM_MAX),
                    range = HEIGHT_CM_MIN.toFloat()..HEIGHT_CM_MAX.toFloat(),
                    onCommit = onHeightChange,
                )
                Spacer(modifier = Modifier.height(spacing.lg))
                MetricSlider(
                    label = stringResource(R.string.stats_arm_length),
                    valueCm = armLengthCm.coerceIn(ARM_CM_MIN, ARM_CM_MAX),
                    range = ARM_CM_MIN.toFloat()..ARM_CM_MAX.toFloat(),
                    onCommit = onArmLengthChange,
                )
                Spacer(modifier = Modifier.height(spacing.md))
                Text(
                    text = stringResource(R.string.body_metrics_help),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = spacing.xs),
                )
            }
        }
    }
}

@Composable
private fun MetricSlider(
    label: String,
    valueCm: Int,
    range: ClosedFloatingPointRange<Float>,
    onCommit: (Int) -> Unit,
) {
    var sliderValue by remember { mutableFloatStateOf(valueCm.toFloat()) }
    LaunchedEffect(valueCm) { sliderValue = valueCm.toFloat() }
    val steps = (range.endInclusive - range.start).roundToInt() - 1

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.metric_cm_value, label, sliderValue.roundToInt()),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onCommit(sliderValue.roundToInt()) },
            valueRange = range,
            steps = steps.coerceAtLeast(0),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFFD700),
                activeTrackColor = Color(0xFFFFD700),
                inactiveTrackColor = Color.White.copy(alpha = 0.3f),
            ),
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


