package com.pixelfitquest.feature.customization

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.pixelfitquest.R
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.feature.customization.model.CustomizationTab
import com.pixelfitquest.feature.customization.ui.*
import com.pixelfitquest.feature.levels.LevelsViewModel
import com.pixelfitquest.ui.theme.DarkStone
import com.pixelfitquest.ui.theme.QuestBrown
import com.pixelfitquest.ui.theme.RewardGold
import com.pixelfitquest.ui.theme.spacing

@Composable
fun CustomizationScreen(
    viewModel: CustomizationViewModel = hiltViewModel(),
    levelsViewModel: LevelsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val levelsState by levelsViewModel.uiState.collectAsState()
    val unlockedLevelSkinIds = remember(levelsState.items) {
        levelsState.items
            .filter { it.unlocked }
            .map { it.definition.id }
            .toSet()
    }

    val spacing = MaterialTheme.spacing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // --- Header with Coins, Level, and Body Stats quick action ---
        CustomizationHeader(
            coins = uiState.userCoins,
            level = uiState.userLevel,
            onOpenStats = viewModel::openStatsDialog,
        )

        Spacer(modifier = Modifier.height(spacing.sm))

        // --- 4 Equal Tabs (No horizontal scrolling required) ---
        CustomizationTabBar(
            selected = uiState.selectedTab,
            onSelect = viewModel::selectTab,
        )

        Spacer(modifier = Modifier.height(spacing.sm))

        // --- Tab Content ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when (uiState.selectedTab) {
                CustomizationTab.Character -> CharacterCustomizationPanel(
                    gender = uiState.gender,
                    selectedCharacterId = uiState.selectedCharacterId,
                    equippedVariant = uiState.equippedVariant,
                    unlockedVariants = uiState.unlockedVariants,
                    userCoins = uiState.userCoins,
                    userLevel = uiState.userLevel,
                    heightCm = uiState.heightCm,
                    armLengthCm = uiState.armLengthCm,
                    unlockedLevelSkinIds = unlockedLevelSkinIds,
                    onSelectGender = viewModel::selectGender,
                    onSelectCharacter = viewModel::selectCharacter,
                    onEquipCharacter = { id ->
                        viewModel.equipCharacter(id)
                        levelsViewModel.equipByAvatarVariant(uiState.equippedVariant)
                    },
                    onBuyCharacter = viewModel::buyCharacter,
                    onOpenStats = viewModel::openStatsDialog,
                )
                CustomizationTab.Home -> HomeCustomizationPanel(
                    selectedHomeId = uiState.selectedHomeId,
                    equippedHomeId = uiState.equippedHomeId,
                    unlockedHomeUpgrades = uiState.unlockedHomeUpgrades,
                    userCoins = uiState.userCoins,
                    userLevel = uiState.userLevel,
                    onSelectHome = viewModel::selectHome,
                    onEquipHome = viewModel::equipHome,
                    onBuyHome = viewModel::buyHome,
                )
                CustomizationTab.Gym -> GymCustomizationPanel(
                    selectedGymId = uiState.selectedGymId,
                    equippedGymId = uiState.equippedGymId,
                    unlockedGyms = uiState.unlockedGyms,
                    userCoins = uiState.userCoins,
                    userLevel = uiState.userLevel,
                    onSelectGym = viewModel::selectGym,
                    onEquipGym = viewModel::equipGym,
                    onBuyGym = viewModel::buyGym,
                )
                CustomizationTab.Background -> BackgroundCustomizationPanel(
                    selectedBackgroundId = uiState.selectedBackgroundId,
                    equippedBackgroundId = uiState.equippedBackgroundId,
                    unlockedBackgrounds = uiState.unlockedBackgrounds,
                    userCoins = uiState.userCoins,
                    userLevel = uiState.userLevel,
                    onSelectBackground = viewModel::selectBackground,
                    onEquipBackground = viewModel::equipBackground,
                    onBuyBackground = viewModel::buyBackground,
                )
            }
        }
    }

    // --- Body Calibration Dialog ---
    if (uiState.isStatsDialogOpen) {
        StatsDialog(
            heightCm = uiState.heightCm,
            armLengthCm = uiState.armLengthCm,
            onHeightChange = viewModel::setHeight,
            onArmLengthChange = viewModel::setArmLength,
            onDismiss = viewModel::closeStatsDialog,
        )
    }
}

@Composable
private fun CustomizationHeader(
    coins: Int,
    level: Int,
    onOpenStats: () -> Unit,
) {
    val spacing = MaterialTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Armory",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Stats quick action
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(spacing.cornerSm))
                    .background(DarkStone.copy(alpha = 0.85f))
                    .border(1.dp, QuestBrown, RoundedCornerShape(spacing.cornerSm))
                    .clickable(onClick = onOpenStats)
                    .padding(horizontal = spacing.sm, vertical = spacing.xxs),
            ) {
                Text(
                    text = "📏 Stats",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                )
            }

            // Level Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(spacing.cornerSm))
                    .background(DarkStone.copy(alpha = 0.85f))
                    .border(1.dp, QuestBrown, RoundedCornerShape(spacing.cornerSm))
                    .padding(horizontal = spacing.sm, vertical = spacing.xxs),
            ) {
                Text(
                    text = "Lvl $level",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                )
            }

            // Coins Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(spacing.cornerSm))
                    .background(DarkStone.copy(alpha = 0.85f))
                    .border(1.dp, RewardGold, RoundedCornerShape(spacing.cornerSm))
                    .padding(horizontal = spacing.sm, vertical = spacing.xxs),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.coin),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(spacing.xxs))
                    Text(
                        text = "$coins",
                        color = RewardGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                    )
                }
            }
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
        CustomizationTab.entries.forEach { tab ->
            val isSelected = selected == tab
            val title = when (tab) {
                CustomizationTab.Character -> stringResource(R.string.customization_tab_character)
                CustomizationTab.Home -> stringResource(R.string.customization_tab_home)
                CustomizationTab.Gym -> stringResource(R.string.customization_tab_gym)
                CustomizationTab.Background -> stringResource(R.string.customization_tab_background)
            }

            PixelArtButton(
                onClick = { onSelect(tab) },
                imageRes = if (isSelected) R.drawable.button_clicked else R.drawable.button_unclicked,
                pressedRes = R.drawable.button_clicked,
                modifier = Modifier
                    .weight(1f)
                    .height(spacing.scale(42)),
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                )
            }
        }
    }
}
