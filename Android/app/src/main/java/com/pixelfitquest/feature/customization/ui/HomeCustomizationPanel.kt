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
import com.pixelfitquest.feature.customization.model.CustomizationCatalog
import com.pixelfitquest.feature.customization.model.HomeDwellingItem
import com.pixelfitquest.feature.customization.model.UnlockType
import com.pixelfitquest.feature.home.model.DwellingTier
import com.pixelfitquest.ui.theme.DarkStone
import com.pixelfitquest.ui.theme.QuestBrown
import com.pixelfitquest.ui.theme.RewardGold
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
) {
    val spacing = MaterialTheme.spacing
    val selectedItem = remember(selectedHomeId) {
        CustomizationCatalog.homeDwellings.find { it.id == selectedHomeId }
            ?: CustomizationCatalog.homeDwellings.first()
    }

    val isEquipped = equippedHomeId == selectedItem.id
    val isUnlocked = when (selectedItem.unlockType) {
        UnlockType.DEFAULT -> true
        UnlockType.LEVEL -> selectedItem.minLevel != null && userLevel >= selectedItem.minLevel
        UnlockType.COINS -> selectedItem.id != null && unlockedHomeUpgrades.contains(selectedItem.id)
        UnlockType.COMING_SOON -> false
    }

    // Determine preview image
    val previewRes = remember(selectedItem.id, userLevel) {
        if (selectedItem.id == null) {
            val tier = DwellingTier.forLevel(userLevel)
            when (tier) {
                DwellingTier.TARP -> R.drawable.dwelling_tarp_landscape
                DwellingTier.TENT -> R.drawable.dwelling_tent_landscape
                DwellingTier.SHACK -> R.drawable.dwelling_shack_landscape
                DwellingTier.COTTAGE -> R.drawable.dwelling_cottage_landscape
                DwellingTier.CASTLE -> R.drawable.dwelling_castle_landscape
                DwellingTier.GYM -> R.drawable.dwelling_gym_landscape
            }
        } else {
            selectedItem.landscapeRes
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // --- Hero Dwelling Stage ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(spacing.cornerMd))
                .border(BorderStroke(2.dp, QuestBrown), RoundedCornerShape(spacing.cornerMd)),
        ) {
            Image(
                painter = painterResource(id = previewRes),
                contentDescription = selectedItem.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            // Prominent Lock Banner if dwelling not unlocked
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

            // Gradient / overlay info at the bottom of the stage
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = spacing.sm, vertical = spacing.xs),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
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
        }

        Spacer(modifier = Modifier.height(spacing.sm))

        // --- Action Button for Selected Home ---
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
                    onClick = { onEquipHome(selectedItem.id) },
                    imageRes = R.drawable.button_unclicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = Modifier.size(spacing.scale(200), spacing.buttonHeight),
                ) {
                    Text("Equip", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            selectedItem.coinPrice != null && selectedItem.id != null -> {
                val canAfford = userCoins >= selectedItem.coinPrice
                PixelArtButton(
                    onClick = { if (canAfford) onBuyHome(selectedItem.id, selectedItem.coinPrice) },
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

        // --- Home Dwelling Selection Cards ---
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
            contentPadding = PaddingValues(bottom = spacing.md),
        ) {
            items(CustomizationCatalog.homeDwellings, key = { it.id ?: "auto" }) { item ->
                val itemUnlocked = when (item.unlockType) {
                    UnlockType.DEFAULT -> true
                    UnlockType.LEVEL -> item.minLevel != null && userLevel >= item.minLevel
                    UnlockType.COINS -> item.id != null && unlockedHomeUpgrades.contains(item.id)
                    UnlockType.COMING_SOON -> false
                }
                val itemEquipped = equippedHomeId == item.id
                val isSelected = selectedHomeId == item.id

                HomeCard(
                    item = item,
                    isSelected = isSelected,
                    isEquipped = itemEquipped,
                    isUnlocked = itemUnlocked,
                    onClick = { onSelectHome(item.id) },
                )
            }
        }
    }
}

@Composable
private fun HomeCard(
    item: HomeDwellingItem,
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
