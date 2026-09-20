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
import androidx.compose.ui.text.style.TextOverflow
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
    isTwoPane: Boolean = false,
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

    @Composable
    fun HeroStage(stageModifier: Modifier = Modifier) {
        Box(
            modifier = stageModifier
                .clip(RoundedCornerShape(spacing.cornerMd))
                .border(BorderStroke(2.dp, QuestBrown), RoundedCornerShape(spacing.cornerMd)),
        ) {
            Image(
                painter = painterResource(id = selectedItem.drawableRes),
                contentDescription = selectedItem.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            if (!isUnlocked) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(Color(0xD92A0E0E))
                        .padding(vertical = 3.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = when {
                            selectedItem.coinPrice != null -> "🔒 Locked · ${selectedItem.coinPrice} Coins"
                            selectedItem.minLevel != null -> "🔒 Locked · Level ${selectedItem.minLevel} Required"
                            else -> "🔒 Locked"
                        },
                        color = Color(0xFFFF8A80),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = spacing.sm, vertical = spacing.xxs),
            ) {
                Column {
                    Text(
                        text = selectedItem.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isTwoPane) 12.sp else 14.sp,
                    )
                    Text(
                        text = selectedItem.description,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = if (isTwoPane) 9.sp else 10.sp,
                        maxLines = if (isTwoPane) 1 else 2,
                    )
                }
            }
        }
    }

    @Composable
    fun ActionButton(
        actionModifier: Modifier = Modifier,
        buttonWidth: androidx.compose.ui.unit.Dp = spacing.scale(200),
        buttonHeight: androidx.compose.ui.unit.Dp = spacing.buttonHeight,
    ) {
        when {
            isEquipped -> {
                PixelArtButton(
                    onClick = {},
                    imageRes = R.drawable.button_green_clicked,
                    pressedRes = R.drawable.button_green_clicked,
                    modifier = actionModifier.size(buttonWidth, buttonHeight),
                ) {
                    Text(
                        text = "EQUIPPED",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isTwoPane) 12.sp else 14.sp,
                    )
                }
            }
            isUnlocked -> {
                PixelArtButton(
                    onClick = { onEquipBackground(selectedItem.id) },
                    imageRes = R.drawable.button_unclicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = actionModifier.size(buttonWidth, buttonHeight),
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
                    modifier = actionModifier.size(buttonWidth, buttonHeight),
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
                    modifier = actionModifier.size(buttonWidth, buttonHeight),
                ) {
                    Text("🔒 Level ${selectedItem.minLevel}", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }
        }
    }

    @Composable
    fun BackgroundGrid(gridModifier: Modifier = Modifier, columns: GridCells = GridCells.Fixed(2)) {
        LazyVerticalGrid(
            columns = columns,
            modifier = gridModifier,
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

    if (isTwoPane) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            // Left Pane (Preview & Action)
            Column(
                modifier = Modifier
                    .weight(0.38f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                HeroStage(
                    stageModifier = Modifier
                        .fillMaxWidth()
                        .height(spacing.scale(145))
                )
                ActionButton(
                    buttonWidth = spacing.scale(180),
                    buttonHeight = spacing.scale(42),
                )
            }

            // Right Pane (Items Grid)
            BackgroundGrid(
                gridModifier = Modifier
                    .weight(0.62f)
                    .fillMaxHeight(),
                columns = GridCells.Adaptive(minSize = 165.dp),
            )
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HeroStage(
                stageModifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )
            Spacer(modifier = Modifier.height(spacing.sm))
            ActionButton()
            Spacer(modifier = Modifier.height(spacing.md))
            BackgroundGrid(gridModifier = Modifier.weight(1f))
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(modifier = Modifier.width(spacing.xxs))
                when {
                    isEquipped -> Text("EQUIPPED", color = RewardGold, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    isUnlocked -> Text("UNLOCKED", color = Color(0xFF81C784), fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    item.coinPrice != null -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text("🔒 ${item.coinPrice}", color = Color(0xFFFFD54F), fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Image(
                                painter = painterResource(R.drawable.coin),
                                contentDescription = null,
                                modifier = Modifier.size(10.dp),
                            )
                        }
                    }
                    item.minLevel != null -> Text("🔒 LVL ${item.minLevel}", color = Color(0xFFFF8A80), fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
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
