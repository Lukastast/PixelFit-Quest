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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelfitquest.R
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.feature.customization.model.AppBackgroundItem
import com.pixelfitquest.feature.customization.model.CustomizationCatalog
import com.pixelfitquest.feature.customization.model.UnlockType
import com.pixelfitquest.ui.theme.DarkStone
import com.pixelfitquest.ui.theme.QuestBrown
import com.pixelfitquest.ui.theme.RewardGold
import com.pixelfitquest.ui.theme.spacing

@Composable
fun BackgroundCustomizationPanel(
    selectedBackgroundId: String,
    equippedBackgroundId: String,
    unlockedBackgrounds: Set<String>,
    userCoins: Int,
    userLevel: Int,
    onSelectBackground: (String) -> Unit,
    onEquipBackground: (String) -> Unit,
    onBuyBackground: (String, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    val selectedItem = remember(selectedBackgroundId) {
        CustomizationCatalog.appBackgrounds.find { it.id == selectedBackgroundId }
            ?: CustomizationCatalog.appBackgrounds.first()
    }

    val isEquipped = equippedBackgroundId == selectedItem.id
    val isUnlocked = when (selectedItem.unlockType) {
        UnlockType.DEFAULT -> true
        UnlockType.LEVEL -> selectedItem.minLevel != null && userLevel >= selectedItem.minLevel
        UnlockType.COINS -> unlockedBackgrounds.contains(selectedItem.id)
        UnlockType.COMING_SOON -> false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // --- Hero App Background Preview Stage ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(spacing.cornerMd))
                .border(BorderStroke(2.dp, QuestBrown), RoundedCornerShape(spacing.cornerMd)),
        ) {
            Image(
                painter = painterResource(id = selectedItem.drawableRes),
                contentDescription = selectedItem.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            // Prominent Lock Banner if background not unlocked
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

            // Bottom overlay bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = spacing.sm, vertical = spacing.xs),
            ) {
                Column {
                    Text(
                        text = selectedItem.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                    Text(
                        text = selectedItem.description,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(spacing.sm))

        // --- Action Button for Selected Background ---
        when {
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
                    onClick = { onEquipBackground(selectedItem.id) },
                    imageRes = R.drawable.button_unclicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = Modifier.size(spacing.scale(200), spacing.buttonHeight),
                ) {
                    Text("Equip", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            selectedItem.coinPrice != null -> {
                val canAfford = userCoins >= selectedItem.coinPrice
                PixelArtButton(
                    onClick = { if (canAfford) onBuyBackground(selectedItem.id, selectedItem.coinPrice) },
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

        Spacer(modifier = Modifier.height(spacing.md))

        // --- Background Selection Cards ---
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
            contentPadding = PaddingValues(bottom = spacing.md),
        ) {
            items(CustomizationCatalog.appBackgrounds, key = { it.id }) { item ->
                val itemUnlocked = when (item.unlockType) {
                    UnlockType.DEFAULT -> true
                    UnlockType.LEVEL -> item.minLevel != null && userLevel >= item.minLevel
                    UnlockType.COINS -> unlockedBackgrounds.contains(item.id)
                    UnlockType.COMING_SOON -> false
                }
                val itemEquipped = equippedBackgroundId == item.id
                val isSelected = selectedBackgroundId == item.id

                BackgroundCard(
                    item = item,
                    isSelected = isSelected,
                    isEquipped = itemEquipped,
                    isUnlocked = itemUnlocked,
                    onClick = { onSelectBackground(item.id) },
                )
            }
        }
    }
}

@Composable
private fun BackgroundCard(
    item: AppBackgroundItem,
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
                    fontSize = 12.sp,
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
