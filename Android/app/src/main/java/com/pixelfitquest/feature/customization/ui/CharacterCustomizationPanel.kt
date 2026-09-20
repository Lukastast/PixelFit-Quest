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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.pixelfitquest.ui.theme.QuestBrown
import com.pixelfitquest.ui.theme.RewardGold
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // --- Gender Toggle ---
        Row(
            modifier = Modifier.padding(bottom = spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            PixelArtButton(
                onClick = { onSelectGender("male") },
                imageRes = if (gender == "male") R.drawable.button_clicked else R.drawable.button_unclicked,
                pressedRes = R.drawable.button_clicked,
                modifier = Modifier.size(spacing.scale(90), spacing.scale(38)),
            ) {
                Text(
                    text = stringResource(R.string.male),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                )
            }
            PixelArtButton(
                onClick = { onSelectGender("female") },
                imageRes = if (gender == "female") R.drawable.button_clicked else R.drawable.button_unclicked,
                pressedRes = R.drawable.button_clicked,
                modifier = Modifier.size(spacing.scale(90), spacing.scale(38)),
            ) {
                Text(
                    text = stringResource(R.string.female),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                )
            }
        }

        // --- Hero Character Stage ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing.scale(170))
                .clip(RoundedCornerShape(spacing.cornerMd))
                .background(Color.Black.copy(alpha = 0.5f))
                .border(BorderStroke(2.dp, QuestBrown), RoundedCornerShape(spacing.cornerMd)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize().padding(spacing.sm),
            ) {
                IdleAnimation(
                    modifier = Modifier
                        .size(spacing.scale(95))
                        .offset(x = spacing.scale(-12)),
                    gender = spriteKey,
                    isAnimating = true,
                )

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
            }

            // Prominent Lock Banner if character not unlocked
            if (!isUnlocked) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(Color(0xD92A0E0E))
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = when {
                            selectedItem.isPremium -> "🔒 Coming Soon"
                            selectedItem.coinPrice != null -> "🔒 Locked · ${selectedItem.coinPrice} Coins"
                            selectedItem.minLevel != null -> "🔒 Locked · Level ${selectedItem.minLevel} Required"
                            else -> "🔒 Locked"
                        },
                        color = Color(0xFFFF8A80),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(spacing.xs))

        // --- Embedded Body Calibration Quick-Card ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(spacing.cornerSm))
                .background(DarkStone.copy(alpha = 0.85f))
                .border(BorderStroke(1.dp, QuestBrown), RoundedCornerShape(spacing.cornerSm))
                .clickable(onClick = onOpenStats)
                .padding(horizontal = spacing.sm, vertical = spacing.xs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                ) {
                    Text("📏", fontSize = 14.sp)
                    Column {
                        Text(
                            text = "Body Calibration",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                        )
                        Text(
                            text = "Height: ${heightCm} cm · Arm: ${armLengthCm} cm",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                        )
                    }
                }
                Text(
                    text = "EDIT ⚙",
                    color = RewardGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.xs))

        // --- Action Button for Selected Character ---
        when {
            selectedItem.isPremium -> {
                PixelArtButton(
                    onClick = {},
                    imageRes = R.drawable.button_unclicked,
                    pressedRes = R.drawable.button_unclicked,
                    modifier = Modifier.size(spacing.scale(200), spacing.buttonHeight),
                ) {
                    Text(stringResource(R.string.coming_soon), color = Color.White.copy(alpha = 0.6f))
                }
            }
            isEquipped -> {
                Box(
                    modifier = Modifier
                        .size(spacing.scale(200), spacing.buttonHeight)
                        .clip(RoundedCornerShape(spacing.cornerSm))
                        .background(Color(0xFF2E7D32).copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "EQUIPPED",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                }
            }
            isUnlocked -> {
                PixelArtButton(
                    onClick = { onEquipCharacter(selectedItem.id) },
                    imageRes = R.drawable.button_unclicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = Modifier.size(spacing.scale(200), spacing.buttonHeight),
                ) {
                    Text(stringResource(R.string.select), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            selectedItem.coinPrice != null -> {
                val canAfford = userCoins >= selectedItem.coinPrice
                PixelArtButton(
                    onClick = { if (canAfford) onBuyCharacter(selectedItem.id, selectedItem.coinPrice) },
                    imageRes = if (canAfford) R.drawable.button_unclicked else R.drawable.button_clicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = Modifier.size(spacing.scale(200), spacing.buttonHeight),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${selectedItem.coinPrice} ", color = Color.White, fontWeight = FontWeight.Bold)
                        Image(
                            painter = painterResource(R.drawable.coin),
                            contentDescription = null,
                            modifier = Modifier.size(spacing.md),
                        )
                        Text(stringResource(R.string.coins_label), color = Color.White)
                    }
                }
            }
            selectedItem.minLevel != null -> {
                PixelArtButton(
                    onClick = {},
                    imageRes = R.drawable.button_clicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = Modifier.size(spacing.scale(200), spacing.buttonHeight),
                ) {
                    Text("🔒 Unlock at Level ${selectedItem.minLevel}", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(spacing.sm))

        // --- Character Variant Selection Cards ---
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
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
        isEquipped -> RewardGold
        isSelected -> Color.White
        !isUnlocked -> Color(0xFF383333)
        else -> QuestBrown
    }

    val backgroundColor = if (!isUnlocked) {
        Color(0xFF141212).copy(alpha = 0.92f)
    } else {
        DarkStone.copy(alpha = 0.85f)
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
                    color = if (!isUnlocked && !isSelected) Color.White.copy(alpha = 0.65f) else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                )
                when {
                    isEquipped -> Text("EQUIPPED", color = RewardGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    isUnlocked -> Text("UNLOCKED", color = Color(0xFF81C784), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    item.coinPrice != null -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔒 ${item.coinPrice}", color = Color(0xFFFFD54F), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(2.dp))
                            Image(
                                painter = painterResource(R.drawable.coin),
                                contentDescription = null,
                                modifier = Modifier.size(10.dp),
                            )
                        }
                    }
                    item.minLevel != null -> Text("🔒 LVL ${item.minLevel}", color = Color(0xFFFF8A80), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    item.isPremium -> Text("🔒 SOON", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(spacing.xxs))

            Text(
                text = item.description,
                color = if (!isUnlocked) Color.White.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
                maxLines = 2,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
