package com.pixelfitquest.feature.customization.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelfitquest.R
import com.pixelfitquest.components.atoms.IdleAnimation
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.feature.customization.model.CharacterItem
import com.pixelfitquest.feature.customization.model.CustomizationCatalog
import com.pixelfitquest.feature.customization.model.UnlockType
import com.pixelfitquest.feature.levels.cosmetics.AvatarSkinBridge
import com.pixelfitquest.ui.theme.DarkStone
import com.pixelfitquest.ui.theme.ImperialGold
import com.pixelfitquest.ui.theme.PlatinumWhite
import com.pixelfitquest.ui.theme.RewardGold
import com.pixelfitquest.ui.theme.SilverSlate
import com.pixelfitquest.ui.theme.SilverSteel
import com.pixelfitquest.ui.theme.SlateBorder
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.SlateGroove
import com.pixelfitquest.ui.theme.SlateSurface
import com.pixelfitquest.ui.theme.TorchAmber
import com.pixelfitquest.ui.theme.VitalGreen
import com.pixelfitquest.ui.theme.spacing
import com.pixelfitquest.ui.theme.typography

@Composable
fun CharacterCustomizationPanel(
    gender: String,
    selectedCharacterId: String,
    equippedVariant: String,
    unlockedVariants: Set<String>,
    userCoins: Int,
    userLevel: Int,
    heightCm: Int,
    armLengthCm: Int,
    unlockedLevelSkinIds: Set<String>,
    onSelectGender: (String) -> Unit,
    onSelectCharacter: (String) -> Unit,
    onEquipCharacter: (String) -> Unit,
    onBuyCharacter: (String, Int) -> Unit,
    onOpenStats: () -> Unit,
    modifier: Modifier = Modifier,
    isTwoPane: Boolean = false,
) {
    val spacing = MaterialTheme.spacing
    val selectedItem = remember(selectedCharacterId) {
        CustomizationCatalog.characters.find { it.id == selectedCharacterId }
            ?: CustomizationCatalog.characters.first()
    }

    val resolvedVariant = remember(selectedItem.id, gender) {
        when (selectedItem.id) {
            "basic" -> "basic"
            "fitness" -> if (gender == "female") "female_fitness" else "male_fitness"
            "shadow" -> "shadow"
            "premium" -> if (gender == "female") "female_premium" else "male_premium"
            else -> selectedItem.id
        }
    }

    val isEquipped = equippedVariant == resolvedVariant
    val unlockedByLevel = AvatarSkinBridge.isUnlockedByLevel(resolvedVariant, unlockedLevelSkinIds)
    val isUnlocked = selectedItem.unlockType == UnlockType.DEFAULT ||
        unlockedVariants.contains(resolvedVariant) ||
        unlockedByLevel ||
        (selectedItem.minLevel != null && userLevel >= selectedItem.minLevel)

    val spriteKey = remember(resolvedVariant, gender, isUnlocked) {
        AvatarSkinBridge.spriteKey(resolvedVariant, gender, isUnlocked)
    }

    @Composable
    fun GenderToggle(
        modifier: Modifier = Modifier,
        isVertical: Boolean = false,
        buttonWidth: androidx.compose.ui.unit.Dp = spacing.scale(90),
        buttonHeight: androidx.compose.ui.unit.Dp = spacing.scale(38),
    ) {
        if (isVertical) {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(spacing.xxs),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PixelArtButton(
                    onClick = { onSelectGender("male") },
                    imageRes = if (gender == "male") R.drawable.button_clicked else R.drawable.button_unclicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = Modifier.size(buttonWidth, buttonHeight),
                ) {
                    Text(
                        text = stringResource(R.string.male),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                    )
                }
                PixelArtButton(
                    onClick = { onSelectGender("female") },
                    imageRes = if (gender == "female") R.drawable.button_clicked else R.drawable.button_unclicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = Modifier.size(buttonWidth, buttonHeight),
                ) {
                    Text(
                        text = stringResource(R.string.female),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                    )
                }
            }
        } else {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                PixelArtButton(
                    onClick = { onSelectGender("male") },
                    imageRes = if (gender == "male") R.drawable.button_clicked else R.drawable.button_unclicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = Modifier.size(buttonWidth, buttonHeight),
                ) {
                    Text(
                        text = stringResource(R.string.male),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isTwoPane) 11.sp else 13.sp,
                    )
                }
                PixelArtButton(
                    onClick = { onSelectGender("female") },
                    imageRes = if (gender == "female") R.drawable.button_clicked else R.drawable.button_unclicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = Modifier.size(buttonWidth, buttonHeight),
                ) {
                    Text(
                        text = stringResource(R.string.female),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isTwoPane) 11.sp else 13.sp,
                    )
                }
            }
        }
    }

    @Composable
    fun HeroStage(
        modifier: Modifier = Modifier,
        height: androidx.compose.ui.unit.Dp = if (isTwoPane) spacing.scale(165) else spacing.scale(218),
        spriteSize: androidx.compose.ui.unit.Dp = if (isTwoPane) spacing.scale(85) else spacing.scale(88),
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(spacing.cornerMd))
                .background(SlateDeep.copy(alpha = 0.92f))
                .border(BorderStroke(2.dp, SlateBorder), RoundedCornerShape(spacing.cornerMd)),
            contentAlignment = Alignment.Center,
        ) {
            if (isTwoPane) {
                // Landscape: Inside preview box:
                // - Title of avatar in the center
                // - Under it: Avatar to the left and male/female buttons stacked on the right
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = spacing.sm, vertical = spacing.xs),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    // 1. Title centered at the top
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = selectedItem.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ImperialGold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )

                            if (!isUnlocked) {
                                Spacer(modifier = Modifier.width(spacing.xs))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(spacing.cornerXs))
                                        .background(SlateGroove.copy(alpha = 0.90f))
                                        .border(1.dp, SlateBorder, RoundedCornerShape(spacing.cornerXs))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = when {
                                            selectedItem.isPremium -> "🔒 Soon"
                                            selectedItem.coinPrice != null -> "🔒 ${selectedItem.coinPrice}"
                                            selectedItem.minLevel != null -> "🔒 Lvl ${selectedItem.minLevel}"
                                            else -> "🔒 Locked"
                                        },
                                        color = TorchAmber,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }

                        selectedItem.bonusDescription?.let { bonus ->
                            Text(
                                text = bonus,
                                color = TorchAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    // 2. Under it: Avatar to the left and Male/Female stacked on top of each other to the right
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Avatar on left
                        Box(
                            contentAlignment = Alignment.Center,
                        ) {
                            IdleAnimation(
                                modifier = Modifier
                                    .size(spriteSize)
                                    .offset(x = spacing.scale(-6)),
                                gender = spriteKey,
                                isAnimating = true,
                            )
                        }

                        // Male and Female stacked on top of each other on right
                        GenderToggle(
                            isVertical = true,
                            buttonWidth = spacing.scale(70),
                            buttonHeight = spacing.scale(28),
                        )
                    }
                }
            } else {
                // Portrait: Centered sprite, name, bonus, and GenderToggle placed below avatar name
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = spacing.sm,
                            end = spacing.sm,
                            top = if (!isUnlocked) spacing.scale(30) else spacing.xs,
                            bottom = spacing.xs,
                        ),
                ) {
                    IdleAnimation(
                        modifier = Modifier
                            .size(spriteSize)
                            .offset(x = spacing.scale(-8)),
                        gender = spriteKey,
                        isAnimating = true,
                    )

                    Spacer(modifier = Modifier.height(spacing.scale(14)))

                    Text(
                        text = selectedItem.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp,
                    )

                    selectedItem.bonusDescription?.let { bonus ->
                        Text(
                            text = bonus,
                            color = RewardGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.scale(4)))

                    // Male / Female toggle placed below name inside preview box
                    GenderToggle(
                        buttonWidth = spacing.scale(75),
                        buttonHeight = spacing.scale(30),
                    )
                }

                if (!isUnlocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .background(SlateGroove.copy(alpha = 0.90f))
                            .border(BorderStroke(1.dp, SlateBorder))
                            .padding(vertical = 3.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = when {
                                selectedItem.isPremium -> "🔒 Coming Soon"
                                selectedItem.coinPrice != null -> "🔒 Locked · ${selectedItem.coinPrice} Coins"
                                selectedItem.minLevel != null -> "🔒 Locked · Level ${selectedItem.minLevel} Required"
                                else -> "🔒 Locked"
                            },
                            color = TorchAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun BodyCalibrationCard(
        modifier: Modifier = Modifier,
        height: androidx.compose.ui.unit.Dp = spacing.scale(46),
    ) {
        val cardModifier = modifier.fillMaxWidth().height(height)

        Box(
            modifier = cardModifier
                .clip(RoundedCornerShape(spacing.cornerSm))
                .background(SlateDeep.copy(alpha = 0.92f))
                .border(BorderStroke(1.dp, SlateBorder), RoundedCornerShape(spacing.cornerSm))
                .clickable(onClick = onOpenStats)
                .padding(horizontal = spacing.xs, vertical = spacing.xxs),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.xxs),
                ) {
                    Text("📏", fontSize = if (isTwoPane) 13.sp else 14.sp)
                    Column {
                        Text(
                            text = "Body Stats",
                            color = SilverSteel,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isTwoPane) 11.sp else 12.sp,
                            maxLines = 1,
                        )
                        Text(
                            text = "${heightCm} cm · Arm ${armLengthCm} cm",
                            color = SilverSlate,
                            fontSize = if (isTwoPane) 9.sp else 9.5.sp,
                            maxLines = 1,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(spacing.xxs))
                Text(
                    text = "EDIT ⚙",
                    color = ImperialGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isTwoPane) 9.sp else 9.5.sp,
                    maxLines = 1,
                )
            }
        }
    }

    @Composable
    fun ActionButton(
        modifier: Modifier = Modifier,
        buttonWidth: androidx.compose.ui.unit.Dp? = null,
        buttonHeight: androidx.compose.ui.unit.Dp = spacing.buttonHeight,
    ) {
        val btnModifier = if (buttonWidth != null) {
            modifier.size(buttonWidth, buttonHeight)
        } else {
            modifier.fillMaxWidth().height(buttonHeight)
        }

        when {
            selectedItem.isPremium -> {
                PixelArtButton(
                    onClick = {},
                    imageRes = R.drawable.button_unclicked,
                    pressedRes = R.drawable.button_unclicked,
                    modifier = btnModifier,
                ) {
                    Text(stringResource(R.string.coming_soon), color = Color.White.copy(alpha = 0.6f), fontSize = if (isTwoPane) 11.sp else 13.sp)
                }
            }
            isEquipped -> {
                PixelArtButton(
                    onClick = {},
                    imageRes = R.drawable.button_green_clicked,
                    pressedRes = R.drawable.button_green_clicked,
                    modifier = btnModifier,
                ) {
                    Text(
                        text = "EQUIPPED",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isTwoPane) 11.sp else 13.sp,
                    )
                }
            }
            isUnlocked -> {
                PixelArtButton(
                    onClick = { onEquipCharacter(selectedItem.id) },
                    imageRes = R.drawable.button_unclicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = btnModifier,
                ) {
                    Text(stringResource(R.string.select), color = Color.White, fontWeight = FontWeight.Bold, fontSize = if (isTwoPane) 12.sp else 14.sp)
                }
            }
            selectedItem.coinPrice != null -> {
                val canAfford = userCoins >= selectedItem.coinPrice
                PixelArtButton(
                    onClick = { if (canAfford) onBuyCharacter(selectedItem.id, selectedItem.coinPrice) },
                    imageRes = if (canAfford) R.drawable.button_unclicked else R.drawable.button_clicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = btnModifier,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = spacing.xxs),
                    ) {
                        Text("${selectedItem.coinPrice} ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = if (isTwoPane) 12.sp else 14.sp)
                        Image(
                            painter = painterResource(R.drawable.coin),
                            contentDescription = null,
                            modifier = Modifier.size(if (isTwoPane) 14.dp else spacing.md),
                        )
                        Text(" " + stringResource(R.string.coins_label), color = Color.White, fontSize = if (isTwoPane) 11.sp else 13.sp)
                    }
                }
            }
            selectedItem.minLevel != null -> {
                PixelArtButton(
                    onClick = {},
                    imageRes = R.drawable.button_clicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = btnModifier,
                ) {
                    Text("🔒 Level ${selectedItem.minLevel}", color = Color.White.copy(alpha = 0.7f), fontSize = if (isTwoPane) 10.sp else 11.sp)
                }
            }
        }
    }

    @Composable
    fun CharacterGrid(modifier: Modifier = Modifier, columns: GridCells = GridCells.Fixed(2)) {
        LazyVerticalGrid(
            columns = columns,
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
            contentPadding = PaddingValues(bottom = spacing.md),
        ) {
            items(CustomizationCatalog.characters, key = { it.id }) { item ->
                val itemVariant = when (item.id) {
                    "basic" -> "basic"
                    "fitness" -> if (gender == "female") "female_fitness" else "male_fitness"
                    "shadow" -> "shadow"
                    "premium" -> if (gender == "female") "female_premium" else "male_premium"
                    else -> item.id
                }
                val itemUnlockedByLevel = AvatarSkinBridge.isUnlockedByLevel(itemVariant, unlockedLevelSkinIds)
                val itemUnlocked = item.unlockType == UnlockType.DEFAULT ||
                    unlockedVariants.contains(itemVariant) ||
                    itemUnlockedByLevel ||
                    (item.minLevel != null && userLevel >= item.minLevel)
                val itemEquipped = equippedVariant == itemVariant
                val isSelected = selectedCharacterId == item.id

                CharacterCard(
                    item = item,
                    isSelected = isSelected,
                    isEquipped = itemEquipped,
                    isUnlocked = itemUnlocked,
                    onClick = { onSelectCharacter(item.id) },
                )
            }
        }
    }

    if (isTwoPane) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            // Left Pane (Preview & Controls): HeroStage with GenderToggle inside, then Action & Calibration side-by-side
            Column(
                modifier = Modifier
                    .weight(0.48f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                HeroStage(
                    height = spacing.scale(165),
                    spriteSize = spacing.scale(90),
                )

                Spacer(modifier = Modifier.height(spacing.xs))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ActionButton(
                        buttonHeight = spacing.scale(46),
                        modifier = Modifier.weight(0.42f),
                    )
                    BodyCalibrationCard(
                        modifier = Modifier.weight(0.58f),
                    )
                }
            }

            // Right Pane (Items Grid)
            CharacterGrid(
                modifier = Modifier
                    .weight(0.52f)
                    .fillMaxHeight(),
                columns = GridCells.Adaptive(minSize = 145.dp),
            )
        }
    } else {
        // Portrait single column
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HeroStage()
            Spacer(modifier = Modifier.height(spacing.xs))
            // Equip and Body Stats on one line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ActionButton(
                    buttonHeight = spacing.scale(46),
                    modifier = Modifier.weight(0.44f),
                )
                BodyCalibrationCard(
                    modifier = Modifier.weight(0.56f),
                )
            }
            Spacer(modifier = Modifier.height(spacing.sm))
            CharacterGrid(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun CharacterCard(
    item: CharacterItem,
    isSelected: Boolean,
    isEquipped: Boolean,
    isUnlocked: Boolean,
    onClick: () -> Unit,
) {
    val spacing = MaterialTheme.spacing
    val borderColor = when {
        isEquipped -> ImperialGold
        isSelected -> PlatinumWhite
        !isUnlocked -> SlateBorder.copy(alpha = 0.6f)
        else -> SlateBorder
    }

    val backgroundColor = if (!isUnlocked) {
        SlateGroove.copy(alpha = 0.88f)
    } else if (isEquipped) {
        SlateDeep.copy(alpha = 0.94f)
    } else {
        SlateSurface.copy(alpha = 0.94f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cornerSm))
            .background(backgroundColor)
            .border(BorderStroke(if (isSelected || isEquipped) 2.dp else 1.dp, borderColor), RoundedCornerShape(spacing.cornerSm))
            .clickable(onClick = onClick)
            .padding(spacing.sm),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.name,
                    color = if (!isUnlocked && !isSelected) SilverSlate else SilverSteel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(modifier = Modifier.width(spacing.xxs))
                when {
                    isEquipped -> Text("EQUIPPED", color = ImperialGold, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    isUnlocked -> Text("UNLOCKED", color = VitalGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    item.coinPrice != null -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text("🔒 ${item.coinPrice}", color = ImperialGold, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Image(
                                painter = painterResource(R.drawable.coin),
                                contentDescription = null,
                                modifier = Modifier.size(10.dp),
                            )
                        }
                    }
                    item.minLevel != null -> Text("🔒 LVL ${item.minLevel}", color = TorchAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    item.isPremium -> Text("🔒 SOON", color = SilverSlate, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }

            Spacer(modifier = Modifier.height(spacing.xxs))

            Text(
                text = item.description,
                color = if (!isUnlocked) SilverSlate.copy(alpha = 0.65f) else SilverSteel.copy(alpha = 0.85f),
                fontSize = 10.sp,
                maxLines = 2,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
