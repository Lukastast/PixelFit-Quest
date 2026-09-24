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
import com.pixelfitquest.feature.customization.model.CustomizationCatalog
import com.pixelfitquest.feature.customization.model.HomeDwellingItem
import com.pixelfitquest.feature.customization.model.isShopItemUnlocked
import com.pixelfitquest.feature.customization.model.purchaseBlockedByLevel
import com.pixelfitquest.feature.home.model.DwellingTier
import com.pixelfitquest.feature.home.model.DwellingVisuals
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

@Composable
fun HomeCustomizationPanel(
    selectedHomeId: String?,
    equippedHomeId: String?,
    unlockedHomeUpgrades: Set<String>,
    userCoins: Int,
    userLevel: Int,
    onSelectHome: (String?) -> Unit,
    onEquipHome: (String?) -> Unit,
    onBuyHome: (String, Int) -> Unit,
    modifier: Modifier = Modifier,
    isTwoPane: Boolean = false,
) {
    val spacing = MaterialTheme.spacing
    val selectedItem = remember(selectedHomeId) {
        CustomizationCatalog.homeDwellings.find { it.id == selectedHomeId }
            ?: CustomizationCatalog.homeDwellings.first()
    }

    val isEquipped = equippedHomeId == selectedItem.id
    val isUnlocked = isShopItemUnlocked(
        unlockType = selectedItem.unlockType,
        minLevel = selectedItem.minLevel,
        userLevel = userLevel,
        owned = selectedItem.id != null && unlockedHomeUpgrades.contains(selectedItem.id),
    )
    val blockedByLevel = purchaseBlockedByLevel(isUnlocked, selectedItem.minLevel, userLevel)

    val previewRes = remember(selectedItem.id, unlockedHomeUpgrades) {
        if (selectedItem.id == null) {
            val tier = DwellingTier.bestOwned(unlockedHomeUpgrades)
            DwellingVisuals.landscapeBgRes(tier)
        } else {
            selectedItem.landscapeRes
        }
    }

    @Composable
    fun HeroStage(stageModifier: Modifier = Modifier) {
        Box(
            modifier = stageModifier
                .clip(RoundedCornerShape(spacing.cornerMd))
                .border(BorderStroke(2.dp, SlateBorder), RoundedCornerShape(spacing.cornerMd)),
        ) {
            Image(
                painter = painterResource(id = previewRes),
                contentDescription = selectedItem.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

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
                            blockedByLevel -> "🔒 Locked · Level ${selectedItem.minLevel} Required"
                            selectedItem.coinPrice != null -> "🔒 Locked · ${selectedItem.coinPrice} Coins"
                            selectedItem.minLevel != null -> "🔒 Locked · Level ${selectedItem.minLevel} Required"
                            else -> "🔒 Locked"
                        },
                        color = TorchAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(SlateDeep.copy(alpha = 0.88f))
                    .border(BorderStroke(1.dp, SlateBorder))
                    .padding(horizontal = spacing.sm, vertical = spacing.xxs),
            ) {
                Column {
                    Text(
                        text = selectedItem.name,
                        color = ImperialGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isTwoPane) 12.sp else 14.sp,
                    )
                    Text(
                        text = selectedItem.description,
                        color = SilverSteel,
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
                    onClick = { onEquipHome(selectedItem.id) },
                    imageRes = R.drawable.button_unclicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = actionModifier.size(buttonWidth, buttonHeight),
                ) {
                    Text("Equip", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            blockedByLevel -> {
                PixelArtButton(
                    onClick = {},
                    imageRes = R.drawable.button_clicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = actionModifier.size(buttonWidth, buttonHeight),
                ) {
                    Text("🔒 Level ${selectedItem.minLevel}", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }
            selectedItem.coinPrice != null && selectedItem.id != null -> {
                val canAfford = userCoins >= selectedItem.coinPrice
                PixelArtButton(
                    onClick = { if (canAfford) onBuyHome(selectedItem.id, selectedItem.coinPrice) },
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
    fun HomeGrid(gridModifier: Modifier = Modifier, columns: GridCells = GridCells.Fixed(2)) {
        LazyVerticalGrid(
            columns = columns,
            modifier = gridModifier,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
            contentPadding = PaddingValues(bottom = spacing.md),
        ) {
            items(CustomizationCatalog.homeDwellings, key = { it.id ?: "auto" }) { item ->
                val itemUnlocked = isShopItemUnlocked(
                    unlockType = item.unlockType,
                    minLevel = item.minLevel,
                    userLevel = userLevel,
                    owned = item.id != null && unlockedHomeUpgrades.contains(item.id),
                )
                val itemEquipped = equippedHomeId == item.id
                val isSelected = selectedHomeId == item.id

                HomeCard(
                    item = item,
                    isSelected = isSelected,
                    isEquipped = itemEquipped,
                    isUnlocked = itemUnlocked,
                    blockedByLevel = purchaseBlockedByLevel(itemUnlocked, item.minLevel, userLevel),
                    onClick = { onSelectHome(item.id) },
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
            HomeGrid(
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
            HomeGrid(gridModifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun HomeCard(
    item: HomeDwellingItem,
    isSelected: Boolean,
    isEquipped: Boolean,
    isUnlocked: Boolean,
    blockedByLevel: Boolean,
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
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(modifier = Modifier.width(spacing.xxs))
                when {
                    isEquipped -> Text("EQUIPPED", color = ImperialGold, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    isUnlocked -> Text("UNLOCKED", color = VitalGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    blockedByLevel -> Text("🔒 LVL ${item.minLevel}", color = TorchAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
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
