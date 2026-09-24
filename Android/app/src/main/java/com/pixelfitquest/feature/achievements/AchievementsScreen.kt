package com.pixelfitquest.feature.achievements

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.pixelfitquest.R
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.feature.achievements.model.AchievementCategory
import com.pixelfitquest.feature.achievements.model.AchievementItem
import com.pixelfitquest.feature.achievements.model.AchievementStatusFilter
import com.pixelfitquest.feature.achievements.model.AchievementTier
import com.pixelfitquest.ui.theme.BronzeCopper
import com.pixelfitquest.ui.theme.CrystalCyan
import com.pixelfitquest.ui.theme.ImperialGold
import com.pixelfitquest.ui.theme.PixelFitWidthClass
import com.pixelfitquest.ui.theme.PlatinumWhite
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
fun AchievementsScreen(
    onBack: () -> Unit,
    embedded: Boolean = false,
    modifier: Modifier = Modifier,
    viewModel: AchievementsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val spacing = MaterialTheme.spacing
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val useTwoPane = isLandscape || spacing.widthClass != PixelFitWidthClass.Compact
    var showCategoryDialog by remember { mutableStateOf(false) }

    val categoryCounts = remember(uiState.items) {
        AchievementCategory.entries.associateWith { cat ->
            uiState.items.count { it.definition.category == cat }
        }
    }

    if (showCategoryDialog) {
        CategoryPickerDialog(
            selected = uiState.selectedCategory,
            totalCount = uiState.totalCount,
            categoryCounts = categoryCounts,
            onSelect = viewModel::onCategorySelected,
            onDismiss = { showCategoryDialog = false },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (embedded) {
                    Modifier
                } else {
                    Modifier
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = spacing.screen, vertical = spacing.sm)
                }
            )
    ) {
        if (!embedded) {
            AchievementsHeader(
                unlockedCount = uiState.unlockedCount,
                totalCount = uiState.totalCount,
                onBack = onBack,
            )
            Spacer(modifier = Modifier.height(spacing.xs))
        }

        if (useTwoPane) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = if (embedded) 0.dp else spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                // Left Pane: HeroStage + Filter Controls
                Column(
                    modifier = Modifier
                        .weight(0.45f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs),
                ) {
                    uiState.selectedItem?.let {
                        AchievementHeroStage(item = it, isTwoPane = true)
                    }

                    CategoryCarousel(
                        selected = uiState.selectedCategory,
                        totalCount = uiState.totalCount,
                        categoryCounts = categoryCounts,
                        onPrevious = viewModel::onPreviousCategory,
                        onNext = viewModel::onNextCategory,
                        onOpenDialog = { showCategoryDialog = true },
                    )

                    StatusFilterRow(
                        selected = uiState.statusFilter,
                        onSelect = viewModel::onStatusFilterSelected,
                    )
                }

                // Right Pane: Grid
                AchievementGrid(
                    items = uiState.visibleItems,
                    selectedId = uiState.selectedItem?.definition?.id,
                    onSelect = viewModel::onAchievementTapped,
                    columns = GridCells.Adaptive(minSize = 135.dp),
                    modifier = Modifier
                        .weight(0.55f)
                        .fillMaxHeight(),
                )
            }
        } else {
            // Portrait
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                uiState.selectedItem?.let {
                    AchievementHeroStage(item = it, isTwoPane = false)
                }

                CategoryCarousel(
                    selected = uiState.selectedCategory,
                    totalCount = uiState.totalCount,
                    categoryCounts = categoryCounts,
                    onPrevious = viewModel::onPreviousCategory,
                    onNext = viewModel::onNextCategory,
                    onOpenDialog = { showCategoryDialog = true },
                )

                StatusFilterRow(
                    selected = uiState.statusFilter,
                    onSelect = viewModel::onStatusFilterSelected,
                )

                AchievementGrid(
                    items = uiState.visibleItems,
                    selectedId = uiState.selectedItem?.definition?.id,
                    onSelect = viewModel::onAchievementTapped,
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun AchievementsHeader(
    unlockedCount: Int,
    totalCount: Int,
    onBack: () -> Unit,
) {
    val spacing = MaterialTheme.spacing
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(spacing.headerHeight)
            .clip(RoundedCornerShape(spacing.cornerMd))
            .background(SlateDeep.copy(alpha = 0.94f))
            .border(BorderStroke(2.dp, SlateBorder), RoundedCornerShape(spacing.cornerMd))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.back_desc),
                    tint = SilverSteel,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.achievements_title),
                    style = typography.bodyMedium,
                    color = ImperialGold,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(
                        R.string.achievements_progress_count,
                        unlockedCount,
                        totalCount,
                    ),
                    color = SilverSteel,
                    fontSize = 12.sp,
                )
            }
            Spacer(modifier = Modifier.width(48.dp))
        }
    }
}

@Composable
private fun CategoryCarousel(
    selected: AchievementCategory?,
    totalCount: Int,
    categoryCounts: Map<AchievementCategory, Int>,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onOpenDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    val categoryLabel = when (selected) {
        null -> stringResource(R.string.achievements_category_all) + " ($totalCount)"
        AchievementCategory.WORKOUTS -> "⚔ " + stringResource(R.string.achievements_category_workouts) + " (${categoryCounts[selected] ?: 0})"
        AchievementCategory.STREAK -> "🔥 " + stringResource(R.string.achievements_category_streak) + " (${categoryCounts[selected] ?: 0})"
        AchievementCategory.STEPS -> "🥾 " + stringResource(R.string.achievements_category_steps) + " (${categoryCounts[selected] ?: 0})"
        AchievementCategory.VOLUME -> "🏋 " + stringResource(R.string.achievements_category_volume) + " (${categoryCounts[selected] ?: 0})"
        AchievementCategory.MILESTONES -> "🏆 " + stringResource(R.string.achievements_category_milestones) + " (${categoryCounts[selected] ?: 0})"
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PixelArtButton(
            onClick = onPrevious,
            imageRes = R.drawable.unclicked_customization_button_left,
            pressedRes = R.drawable.clicked_customization_button_left,
            modifier = Modifier.size(width = 24.dp, height = 34.dp),
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(34.dp)
                .clip(RoundedCornerShape(spacing.cornerSm))
                .background(SlateDeep.copy(alpha = 0.92f))
                .border(BorderStroke(1.dp, SlateBorder), RoundedCornerShape(spacing.cornerSm))
                .clickable(onClick = onOpenDialog)
                .padding(horizontal = spacing.sm),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = categoryLabel,
                    color = SilverSteel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.width(spacing.xxs))
                Text(
                    text = "▾",
                    color = ImperialGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        PixelArtButton(
            onClick = onNext,
            imageRes = R.drawable.unclicked_customization_button_right,
            pressedRes = R.drawable.clicked_customization_button_right,
            modifier = Modifier.size(width = 24.dp, height = 34.dp),
        )
    }
}

@Composable
private fun CategoryPickerDialog(
    selected: AchievementCategory?,
    totalCount: Int,
    categoryCounts: Map<AchievementCategory, Int>,
    onSelect: (AchievementCategory?) -> Unit,
    onDismiss: () -> Unit,
) {
    val spacing = MaterialTheme.spacing
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(spacing.cornerMd))
                .background(SlateDeep.copy(alpha = 0.98f))
                .border(BorderStroke(2.dp, SlateBorder), RoundedCornerShape(spacing.cornerMd))
                .padding(spacing.md),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                Text(
                    text = stringResource(R.string.achievements_select_category),
                    color = ImperialGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    val allSelected = selected == null
                    PixelArtButton(
                        onClick = {
                            onSelect(null)
                            onDismiss()
                        },
                        imageRes = if (allSelected) R.drawable.button_clicked else R.drawable.button_unclicked,
                        pressedRes = R.drawable.button_clicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.achievements_category_all) + " ($totalCount)",
                            color = if (allSelected) ImperialGold else SilverSteel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                        )
                    }

                    AchievementCategory.entries.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            row.forEach { cat ->
                                val isSelected = selected == cat
                                val count = categoryCounts[cat] ?: 0
                                PixelArtButton(
                                    onClick = {
                                        onSelect(cat)
                                        onDismiss()
                                    },
                                    imageRes = if (isSelected) R.drawable.button_clicked else R.drawable.button_unclicked,
                                    pressedRes = R.drawable.button_clicked,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                ) {
                                    Text(
                                        text = stringResource(cat.labelRes()) + " ($count)",
                                        color = if (isSelected) ImperialGold else SilverSteel,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                    )
                                }
                            }
                            if (row.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                PixelArtButton(
                    onClick = onDismiss,
                    imageRes = R.drawable.button_unclicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = Modifier
                        .width(110.dp)
                        .height(34.dp),
                ) {
                    Text(
                        text = "CLOSE",
                        color = SilverSteel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusFilterRow(
    selected: AchievementStatusFilter,
    onSelect: (AchievementStatusFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.xxs),
    ) {
        AchievementStatusFilter.entries.forEach { filter ->
            val isSelected = selected == filter
            PixelArtButton(
                onClick = { onSelect(filter) },
                imageRes = if (isSelected) R.drawable.button_clicked else R.drawable.button_unclicked,
                pressedRes = R.drawable.button_clicked,
                modifier = Modifier
                    .weight(1f)
                    .height(30.dp),
            ) {
                Text(
                    text = stringResource(filter.labelRes),
                    color = if (isSelected) ImperialGold else SilverSteel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun AchievementHeroStage(
    item: AchievementItem,
    modifier: Modifier = Modifier,
    isTwoPane: Boolean = false,
) {
    val spacing = MaterialTheme.spacing
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cornerMd))
            .background(SlateDeep.copy(alpha = 0.92f))
            .border(BorderStroke(2.dp, SlateBorder), RoundedCornerShape(spacing.cornerMd))
            .padding(spacing.sm),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.xxs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false),
                ) {
                    Text(
                        text = item.definition.name,
                        fontWeight = FontWeight.Bold,
                        color = ImperialGold,
                        fontSize = if (isTwoPane) 14.sp else 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.width(spacing.xs))
                    Text(
                        text = item.definition.tier.name,
                        color = tierColor(item.definition.tier),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(spacing.cornerXs))
                            .background(tierColor(item.definition.tier).copy(alpha = 0.15f))
                            .border(1.dp, tierColor(item.definition.tier).copy(alpha = 0.4f), RoundedCornerShape(spacing.cornerXs))
                            .padding(horizontal = 5.dp, vertical = 1.dp),
                    )
                }

                when {
                    item.isUnlocked -> Text(
                        text = "UNLOCKED",
                        color = VitalGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    item.progressFraction > 0f -> Text(
                        text = "${(item.progressFraction * 100).toInt()}%",
                        color = TorchAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    else -> Text(
                        text = "LOCKED",
                        color = TorchAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = item.iconRes()),
                    contentDescription = item.definition.name,
                    modifier = Modifier.size(if (isTwoPane) 54.dp else 58.dp),
                    alpha = if (item.isUnlocked) 1f else 0.6f,
                )

                Spacer(modifier = Modifier.width(spacing.sm))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = item.definition.description,
                        color = SilverSteel,
                        fontSize = if (isTwoPane) 10.5.sp else 11.5.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    ProgressBar(fraction = item.progressFraction, isUnlocked = item.isUnlocked)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(
                                R.string.achievements_progress_value,
                                item.progress.currentValue.coerceAtMost(item.definition.threshold),
                                item.definition.threshold,
                            ),
                            color = SilverSteel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                        )

                        RewardRow(item = item, compact = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementCard(
    item: AchievementItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val spacing = MaterialTheme.spacing
    val borderColor = when {
        isSelected -> PlatinumWhite
        item.isUnlocked -> ImperialGold
        else -> SlateBorder
    }
    val backgroundColor = if (!item.isUnlocked) {
        SlateSurface.copy(alpha = 0.94f)
    } else {
        SlateDeep.copy(alpha = 0.92f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cornerSm))
            .background(backgroundColor)
            .border(
                BorderStroke(if (isSelected || item.isUnlocked) 2.dp else 1.dp, borderColor),
                RoundedCornerShape(spacing.cornerSm),
            )
            .clickable(onClick = onClick)
            .padding(spacing.xs),
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
                    text = item.definition.name,
                    color = if (!item.isUnlocked && !isSelected) SilverSlate else SilverSteel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(modifier = Modifier.width(spacing.xxs))
                when {
                    item.isUnlocked -> Text(
                        text = "UNLOCKED",
                        color = VitalGreen,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    item.progressFraction > 0f -> Text(
                        text = "${(item.progressFraction * 100).toInt()}%",
                        color = TorchAmber,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    else -> Text(
                        text = "LOCKED",
                        color = TorchAmber,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            Image(
                painter = painterResource(id = item.iconRes()),
                contentDescription = item.definition.name,
                modifier = Modifier.size(50.dp),
                alpha = if (item.isUnlocked) 1f else 0.55f,
            )

            Spacer(modifier = Modifier.height(3.dp))

            ProgressBar(fraction = item.progressFraction, isUnlocked = item.isUnlocked)
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${item.progress.currentValue.coerceAtMost(item.definition.threshold)}/${item.definition.threshold}",
                    color = SilverSteel,
                    fontSize = 8.5.sp,
                )
                Text(
                    text = item.definition.tier.name,
                    color = tierColor(item.definition.tier),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun AchievementGrid(
    items: List<AchievementItem>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    columns: GridCells = GridCells.Fixed(2),
) {
    val spacing = MaterialTheme.spacing
    if (items.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.achievements_no_items),
                color = SilverSlate,
                fontSize = 12.sp,
            )
        }
    } else {
        LazyVerticalGrid(
            columns = columns,
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
            contentPadding = PaddingValues(bottom = spacing.sm),
        ) {
            items(
                items = items,
                key = { it.definition.id },
            ) { item ->
                AchievementCard(
                    item = item,
                    isSelected = selectedId == item.definition.id,
                    onClick = { onSelect(item.definition.id) },
                )
            }
        }
    }
}

private fun tierColor(tier: AchievementTier): Color = when (tier) {
    AchievementTier.BRONZE -> BronzeCopper
    AchievementTier.SILVER -> SilverSteel
    AchievementTier.GOLD -> ImperialGold
    AchievementTier.PLATINUM -> CrystalCyan
}

@Composable
private fun RewardRow(item: AchievementItem, compact: Boolean) {
    val reward = item.definition.reward
    if (!reward.hasValue) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (reward.coins > 0) {
            Image(
                painter = painterResource(id = R.drawable.coin),
                contentDescription = stringResource(R.string.coins_reward_desc),
                modifier = Modifier.size(if (compact) 12.dp else 16.dp),
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = stringResource(R.string.achievements_reward_coins, reward.coins),
                color = ImperialGold,
                fontSize = if (compact) 10.sp else 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        if (reward.coins > 0 && reward.xp > 0) {
            Spacer(modifier = Modifier.width(6.dp))
        }
        if (reward.xp > 0) {
            Text(
                text = stringResource(R.string.achievements_reward_xp, reward.xp),
                color = ImperialGold,
                fontSize = if (compact) 10.sp else 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ProgressBar(fraction: Float, isUnlocked: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(SlateGroove)
            .border(1.dp, SlateBorder, RoundedCornerShape(2.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(if (isUnlocked) VitalGreen else TorchAmber)
        )
    }
}

internal fun achievementIconRes(id: String, tier: AchievementTier): Int = when (id) {
    "bronze_workout_1" -> R.drawable.bronze_workout_1
    "iron_workout_5" -> R.drawable.iron_workout_5
    "silver_workout_10" -> R.drawable.silver_workout_10
    "steel_workout_25" -> R.drawable.steel_workout_25
    "gold_workout_50" -> R.drawable.gold_workout_50
    "platinum_workout_100" -> R.drawable.platinum_workout_100
    "streak_3" -> R.drawable.streak_3
    "streak_7" -> R.drawable.streak_7
    "streak_14" -> R.drawable.streak_14
    "streak_30" -> R.drawable.streak_30
    "steps_5000" -> R.drawable.steps_5000
    "steps_10000" -> R.drawable.steps_10000
    "steps_50000" -> R.drawable.steps_50000
    "steps_100000" -> R.drawable.steps_100000
    "volume_1000" -> R.drawable.volume_1000
    "volume_10000" -> R.drawable.volume_10000
    "volume_50000" -> R.drawable.volume_50000
    "sets_10" -> R.drawable.sets_10
    "sets_50" -> R.drawable.sets_50
    "sets_200" -> R.drawable.sets_200
    "level_5" -> R.drawable.level_5
    "level_10" -> R.drawable.level_10
    "level_20" -> R.drawable.level_20
    "unique_3" -> R.drawable.unique_3
    "unique_8" -> R.drawable.unique_8
    "resting_heart_rate" -> R.drawable.resting_heart_rate
    else -> when (tier) {
        AchievementTier.BRONZE -> R.drawable.achievement_bronze_workout_1_time
        AchievementTier.SILVER -> R.drawable.achievement_silver_workout_10_times
        AchievementTier.GOLD,
        AchievementTier.PLATINUM,
        -> R.drawable.achievement_gold_workout_50_times
    }
}

private fun AchievementItem.iconRes(): Int {
    if (!isUnlocked) return R.drawable.locked_achievement
    return achievementIconRes(definition.id, definition.tier)
}

private fun AchievementCategory.labelRes(): Int = when (this) {
    AchievementCategory.WORKOUTS -> R.string.achievements_category_workouts
    AchievementCategory.STREAK -> R.string.achievements_category_streak
    AchievementCategory.STEPS -> R.string.achievements_category_steps
    AchievementCategory.VOLUME -> R.string.achievements_category_volume
    AchievementCategory.MILESTONES -> R.string.achievements_category_milestones
}
