package com.pixelfitquest.feature.achievements

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.pixelfitquest.R
import com.pixelfitquest.feature.achievements.model.AchievementCategory
import com.pixelfitquest.feature.achievements.model.AchievementItem
import com.pixelfitquest.feature.achievements.model.AchievementTier
import com.pixelfitquest.ui.theme.DarkStone
import com.pixelfitquest.ui.theme.FireOrange
import com.pixelfitquest.ui.theme.QuestBrown
import com.pixelfitquest.ui.theme.RewardGold
import androidx.compose.material3.MaterialTheme
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
            Spacer(modifier = Modifier.height(spacing.sm))
        }
        CategoryRow(
            selected = uiState.selectedCategory,
            onSelected = viewModel::onCategorySelected,
        )
        Spacer(modifier = Modifier.height(spacing.sm))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            items(
                items = uiState.visibleItems,
                key = { it.definition.id },
            ) { item ->
                AchievementTile(
                    item = item,
                    selected = uiState.selectedId == item.definition.id,
                    onClick = { viewModel.onAchievementTapped(item.definition.id) },
                )
            }
        }
        uiState.selectedItem?.let { selected ->
            Spacer(modifier = Modifier.height(spacing.xs))
            AchievementDetail(item = selected)
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
    ) {
        Image(
            painter = painterResource(id = R.drawable.info_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.back_desc),
                    tint = Color.White,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.achievements_title),
                    style = typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(
                        R.string.achievements_progress_count,
                        unlockedCount,
                        totalCount,
                    ),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                )
            }
            Spacer(modifier = Modifier.width(48.dp))
        }
    }
}

@Composable
private fun CategoryRow(
    selected: AchievementCategory?,
    onSelected: (AchievementCategory?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryChip(
            label = stringResource(R.string.achievements_category_all),
            selected = selected == null,
            onClick = { onSelected(null) },
        )
        AchievementCategory.entries.forEach { category ->
            CategoryChip(
                label = stringResource(category.labelRes()),
                selected = selected == category,
                onClick = { onSelected(category) },
            )
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) RewardGold else QuestBrown
    val textColor = if (selected) DarkStone else Color.White
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun AchievementTile(
    item: AchievementItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) RewardGold else Color.Transparent
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(168.dp)
            .border(2.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(id = R.drawable.questloginboard_wider),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = item.iconRes()),
                contentDescription = item.definition.name,
                modifier = Modifier.size(56.dp),
                alpha = if (item.isUnlocked) 1f else 0.55f,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.definition.name,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(6.dp))
            ProgressBar(fraction = item.progressFraction)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(
                    R.string.achievements_progress_value,
                    item.progress.currentValue.coerceAtMost(item.definition.threshold),
                    item.definition.threshold,
                ),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 10.sp,
            )
            RewardRow(item = item, compact = true)
        }
    }
}

@Composable
private fun AchievementDetail(item: AchievementItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.questloginboard_wider),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = item.definition.name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = item.definition.description,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (item.isUnlocked) {
                    stringResource(R.string.achievements_unlocked)
                } else {
                    stringResource(R.string.achievements_locked)
                },
                color = if (item.isUnlocked) RewardGold else Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            RewardRow(item = item, compact = false)
            if (item.isUnlocked && !item.progress.rewardGranted && item.definition.reward.hasValue) {
                Text(
                    text = stringResource(R.string.achievements_reward_pending),
                    color = FireOrange,
                    fontSize = 10.sp,
                )
            }
        }
    }
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
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.achievements_reward_coins, reward.coins),
                color = RewardGold,
                fontSize = if (compact) 10.sp else 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        if (reward.coins > 0 && reward.xp > 0) {
            Spacer(modifier = Modifier.width(8.dp))
        }
        if (reward.xp > 0) {
            Text(
                text = stringResource(R.string.achievements_reward_xp, reward.xp),
                color = RewardGold,
                fontSize = if (compact) 10.sp else 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ProgressBar(fraction: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(DarkStone.copy(alpha = 0.7f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(6.dp)
                .background(FireOrange)
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
