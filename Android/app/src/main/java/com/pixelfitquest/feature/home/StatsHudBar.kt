package com.pixelfitquest.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelfitquest.R
import com.pixelfitquest.ui.theme.ImperialGold
import com.pixelfitquest.ui.theme.LeatherDark
import com.pixelfitquest.ui.theme.ParchmentBorder
import com.pixelfitquest.ui.theme.SilverSteel
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.spacing

@Composable
fun StatsHudBar(
    coins: Int,
    streak: Int,
    displayLevel: String,
    progressIndex: Int,
    onStreakClick: () -> Unit,
    onLevelClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false,
) {
    val spacing = MaterialTheme.spacing
    val barHeight = if (isLandscape) spacing.scale(42) else spacing.barSm

    Box(
        modifier = modifier
            .then(
                if (isLandscape) Modifier.widthIn(max = spacing.scale(500))
                else Modifier.fillMaxWidth()
            )
            .height(barHeight)
            .clip(RoundedCornerShape(spacing.cornerSm))
            .clickable { onLevelClick() }
    ) {
        Image(
            painter = painterResource(id = R.drawable.info_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = if (isLandscape) spacing.md else spacing.sm),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Coins
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.coin),
                    contentDescription = stringResource(R.string.coin_icon_desc),
                    modifier = Modifier.size(if (isLandscape) spacing.scale(18) else spacing.scale(20))
                )
                Spacer(modifier = Modifier.padding(horizontal = spacing.xxs))
                Text(
                    text = stringResource(R.string.coins_count, coins),
                    fontSize = if (isLandscape) 13.sp else 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImperialGold
                )
            }

            // Streak
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onStreakClick() }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.streak),
                    contentDescription = stringResource(R.string.weekly_streak_hud_desc, streak),
                    modifier = Modifier.size(if (isLandscape) spacing.scale(18) else spacing.scale(20))
                )
                Spacer(modifier = Modifier.padding(horizontal = spacing.xxxs))
                Text(
                    text = "$streak",
                    fontSize = if (isLandscape) 13.sp else 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SilverSteel
                )
            }

            // Level
            Text(
                text = stringResource(R.string.level_display, displayLevel),
                fontSize = if (isLandscape) 13.sp else 14.sp,
                fontWeight = FontWeight.Bold,
                color = ImperialGold,
            )

            // XP Bar
            Box(
                modifier = Modifier
                    .size(
                        width = if (isLandscape) spacing.scale(68) else spacing.scale(76),
                        height = if (isLandscape) spacing.scale(14) else spacing.md
                    ),
                contentAlignment = Alignment.Center
            ) {
                val xpPainter = when (progressIndex) {
                    1 -> painterResource(id = R.drawable.xp_20_percent)
                    2 -> painterResource(id = R.drawable.xp_40_percent)
                    3 -> painterResource(id = R.drawable.xp_60_percent)
                    4 -> painterResource(id = R.drawable.xp_80_percent)
                    5 -> painterResource(id = R.drawable.xp_100_percent)
                    else -> painterResource(id = R.drawable.xp_0_percent)
                }
                Image(
                    painter = xpPainter,
                    contentDescription = stringResource(R.string.xp_bar_desc),
                    modifier = Modifier.matchParentSize()
                )
            }
        }
    }
}

@Composable
fun StatsHudColumn(
    coins: Int,
    streak: Int,
    displayLevel: String,
    progressIndex: Int,
    onStreakClick: () -> Unit,
    onLevelClick: () -> Unit,
    onMissionsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(spacing.landscapeHudWidth)
            .padding(vertical = spacing.xs, horizontal = spacing.xxs)
            .background(
                color = SlateDeep.copy(alpha = 0.94f),
                shape = RoundedCornerShape(spacing.cornerSm)
            )
            .border(
                width = 2.dp,
                color = ParchmentBorder,
                shape = RoundedCornerShape(spacing.cornerSm)
            )
            .clip(RoundedCornerShape(spacing.cornerSm))
            .clickable { onLevelClick() }
            .padding(spacing.xs),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Coins
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.coin),
                contentDescription = stringResource(R.string.coin_icon_desc),
                modifier = Modifier.size(spacing.scale(22))
            )
            Spacer(modifier = Modifier.width(spacing.xxs))
            Text(
                text = "$coins",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ImperialGold
            )
        }

        // Streak
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onStreakClick() }
        ) {
            Image(
                painter = painterResource(id = R.drawable.streak),
                contentDescription = stringResource(R.string.weekly_streak_hud_desc, streak),
                modifier = Modifier.size(spacing.scale(22))
            )
            Spacer(modifier = Modifier.width(spacing.xxs))
            Text(
                text = "$streak",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SilverSteel
            )
        }

        // Level & XP
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Lv. $displayLevel",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ImperialGold
            )
            Spacer(modifier = Modifier.height(spacing.xxs))
            val xpPainter = when (progressIndex) {
                1 -> painterResource(id = R.drawable.xp_20_percent)
                2 -> painterResource(id = R.drawable.xp_40_percent)
                3 -> painterResource(id = R.drawable.xp_60_percent)
                4 -> painterResource(id = R.drawable.xp_80_percent)
                5 -> painterResource(id = R.drawable.xp_100_percent)
                else -> painterResource(id = R.drawable.xp_0_percent)
            }
            Image(
                painter = xpPainter,
                contentDescription = stringResource(R.string.xp_bar_desc),
                modifier = Modifier.size(width = spacing.scale(70), height = spacing.xs * 1.5f)
            )
        }

        // Missions Trigger
        Box(
            modifier = Modifier
                .clickable { onMissionsClick() }
                .background(LeatherDark.copy(alpha = 0.9f), RoundedCornerShape(spacing.cornerXs))
                .border(1.dp, ParchmentBorder, RoundedCornerShape(spacing.cornerXs))
                .padding(horizontal = spacing.xs, vertical = spacing.xxs),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Missions",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ImperialGold
            )
        }
    }
}
