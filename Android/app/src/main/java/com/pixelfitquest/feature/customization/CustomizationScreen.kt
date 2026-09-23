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
import com.pixelfitquest.ui.theme.ImperialGold
import com.pixelfitquest.ui.theme.RewardGold
import com.pixelfitquest.ui.theme.SilverSteel
import com.pixelfitquest.ui.theme.SlateBorder
import com.pixelfitquest.ui.theme.SlateDeep
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
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val useTwoPane = isLandscape || spacing.widthClass != com.pixelfitquest.ui.theme.PixelFitWidthClass.Compact

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = if (useTwoPane) spacing.xxs else spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // --- Header with Coins, Level, and Body Stats quick action ---
        CustomizationHeader(
            coins = uiState.userCoins,
            level = uiState.userLevel,
            onOpenStats = viewModel::openStatsDialog,
            isCompact = useTwoPane,
        )

        Spacer(modifier = Modifier.height(if (useTwoPane) spacing.xxs else spacing.sm))

        // --- 4 Equal Tabs (No horizontal scrolling required) ---
        CustomizationTabBar(
            selected = uiState.selectedTab,
            onSelect = viewModel::selectTab,
            isCompact = useTwoPane,
        )

        Spacer(modifier = Modifier.height(if (useTwoPane) spacing.xxs else spacing.sm))

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
                    isTwoPane = useTwoPane,
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
                    isTwoPane = useTwoPane,
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
                    isTwoPane = useTwoPane,
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
                    isTwoPane = useTwoPane,
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
    isCompact: Boolean = false,
) {
    val spacing = MaterialTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isCompact) spacing.sm else spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Armory",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = if (isCompact) 17.sp else 20.sp,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Stats quick action
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(spacing.cornerSm))
                    .background(SlateDeep.copy(alpha = 0.88f))
                    .border(1.dp, SlateBorder, RoundedCornerShape(spacing.cornerSm))
                    .clickable(onClick = onOpenStats)
                    .padding(horizontal = spacing.sm, vertical = spacing.xxs),
            ) {
                Text(
                    text = "📏 Stats",
                    color = SilverSteel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                )
            }

            // Level Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(spacing.cornerSm))
                    .background(SlateDeep.copy(alpha = 0.88f))
                    .border(1.dp, SlateBorder, RoundedCornerShape(spacing.cornerSm))
                    .padding(horizontal = spacing.sm, vertical = spacing.xxs),
            ) {
                Text(
                    text = "Lvl $level",
                    color = ImperialGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                )
            }

            // Coins Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(spacing.cornerSm))
                    .background(SlateDeep.copy(alpha = 0.88f))
                    .border(1.dp, SlateBorder, RoundedCornerShape(spacing.cornerSm))
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
                        color = ImperialGold,
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
    isCompact: Boolean = false,
) {
    val spacing = MaterialTheme.spacing

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isCompact) spacing.sm else spacing.md),
        horizontalArrangement = Arrangement.spacedBy(if (isCompact) spacing.xxs else spacing.xs),
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
                    .height(if (isCompact) spacing.scale(35) else spacing.scale(42)),
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isCompact) 11.sp else 12.sp,
                )
            }
        }
    }
}
