package com.pixelfitquest.feature.levels

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
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.feature.levels.cosmetics.HomeThemeVisuals
import com.pixelfitquest.feature.levels.model.CosmeticItem
import com.pixelfitquest.feature.levels.model.CosmeticKind
import com.pixelfitquest.feature.levels.model.LevelProgress
import com.pixelfitquest.ui.theme.DarkStone
import com.pixelfitquest.ui.theme.QuestBrown
import com.pixelfitquest.ui.theme.RewardGold
import com.pixelfitquest.ui.theme.typography

@Composable
fun LevelsScreen(
    onBack: () -> Unit,
    viewModel: LevelsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        LevelsHeader(
            progress = uiState.progress,
            titleName = uiState.equippedTitleName,
            unlockedCount = uiState.unlockedCount,
            totalCount = uiState.totalCount,
            onBack = onBack,
        )
        Spacer(modifier = Modifier.height(12.dp))
        KindRow(
            selected = uiState.selectedKind,
            onSelected = viewModel::onFilterSelected,
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = uiState.visibleItems,
                key = { it.definition.id },
            ) { item ->
                CosmeticTile(
                    item = item,
                    selected = uiState.selectedId == item.definition.id,
                    onClick = { viewModel.onCosmeticTapped(item.definition.id) },
                )
            }
        }
        uiState.selectedItem?.let { selected ->
            Spacer(modifier = Modifier.height(8.dp))
            CosmeticDetail(
                item = selected,
                onEquip = viewModel::equipSelected,
            )
        }
    }
}

@Composable
private fun LevelsHeader(
    progress: LevelProgress,
    titleName: String,
    unlockedCount: Int,
    totalCount: Int,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
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
                val levelLabel = if (progress.isMaxLevel) {
                    stringResource(R.string.max_level)
                } else {
                    progress.level.toString()
                }
                Text(
                    text = stringResource(R.string.levels_header_level, levelLabel),
                    style = typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                if (titleName.isNotBlank()) {
                    Text(
                        text = titleName,
                        color = RewardGold,
                        fontSize = 12.sp,
                    )
                }
                Text(
                    text = stringResource(
                        R.string.levels_progress_count,
                        unlockedCount,
                        totalCount,
                    ),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                )
            }
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(width = 80.dp, height = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = HomeThemeVisuals.xpBarDrawable(progress.xpBarIndex)),
                    contentDescription = stringResource(R.string.xp_bar_desc),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun KindRow(
    selected: CosmeticKind?,
    onSelected: (CosmeticKind?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KindChip(
            label = stringResource(R.string.levels_filter_all),
            selected = selected == null,
            onClick = { onSelected(null) },
        )
        CosmeticKind.entries.forEach { kind ->
            KindChip(
                label = stringResource(kind.labelRes()),
                selected = selected == kind,
                onClick = { onSelected(kind) },
            )
        }
    }
}

@Composable
private fun KindChip(
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
private fun CosmeticTile(
    item: CosmeticItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = when {
        selected -> RewardGold
        item.equipped -> RewardGold.copy(alpha = 0.55f)
        else -> Color.Transparent
    }
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
                painter = painterResource(id = HomeThemeVisuals.previewRes(item.definition)),
                contentDescription = item.definition.name,
                modifier = Modifier.size(56.dp),
                contentScale = ContentScale.Crop,
                alpha = if (item.unlocked) 1f else 0.45f,
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
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (item.unlocked) {
                    if (item.equipped) {
                        stringResource(R.string.levels_equipped)
                    } else {
                        stringResource(R.string.levels_unlocked)
                    }
                } else {
                    stringResource(R.string.levels_locked_level, item.definition.unlockLevel)
                },
                color = if (item.unlocked) RewardGold else Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
            )
        }
    }
}

@Composable
private fun CosmeticDetail(
    item: CosmeticItem,
    onEquip: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
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
            Spacer(modifier = Modifier.height(8.dp))
            when {
                item.equipped -> {
                    Text(
                        text = stringResource(R.string.levels_equipped),
                        color = RewardGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                item.unlocked -> {
                    PixelArtButton(
                        onClick = onEquip,
                        imageRes = R.drawable.button_unclicked,
                        pressedRes = R.drawable.button_clicked,
                        modifier = Modifier.size(160.dp, 40.dp),
                    ) {
                        Text(stringResource(R.string.levels_equip), color = Color.White)
                    }
                }
                else -> {
                    Text(
                        text = stringResource(
                            R.string.levels_locked_level,
                            item.definition.unlockLevel,
                        ),
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}

private fun CosmeticKind.labelRes(): Int = when (this) {
    CosmeticKind.HOME_THEME -> R.string.levels_filter_home
    CosmeticKind.CHARACTER_SKIN -> R.string.levels_filter_skins
    CosmeticKind.TITLE -> R.string.levels_filter_titles
}
