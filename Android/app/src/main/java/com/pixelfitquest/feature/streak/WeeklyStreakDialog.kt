package com.pixelfitquest.feature.streak

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pixelfitquest.R
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.feature.streak.model.MAX_WEEKLY_TARGET
import com.pixelfitquest.feature.streak.model.MIN_WEEKLY_TARGET
import com.pixelfitquest.feature.streak.model.WeeklyStreakRewards
import com.pixelfitquest.feature.streak.model.WeeklyStreakSkin
import com.pixelfitquest.feature.streak.model.WeeklyStreakSnapshot
import com.pixelfitquest.ui.theme.ImperialGold
import com.pixelfitquest.ui.theme.PlatinumWhite
import com.pixelfitquest.ui.theme.RewardGold
import com.pixelfitquest.ui.theme.SilverSlate
import com.pixelfitquest.ui.theme.SilverSteel
import com.pixelfitquest.ui.theme.SlateBorder
import com.pixelfitquest.ui.theme.SlateBorderSubtle
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.SlateGroove
import com.pixelfitquest.ui.theme.SlateSurface
import com.pixelfitquest.ui.theme.TorchAmber
import com.pixelfitquest.ui.theme.VitalGreen
import com.pixelfitquest.ui.theme.determination
import com.pixelfitquest.ui.theme.spacing
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WeeklyStreakDialog(
    snapshot: WeeklyStreakSnapshot,
    onDismiss: () -> Unit,
    onTargetChange: (Int) -> Unit,
) {
    val spacing = MaterialTheme.spacing
    val weekEnd = snapshot.weekStart.plusDays(6)
    val dateFmt = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
    val rawProgress = if (snapshot.targetSessionsPerWeek > 0) {
        (snapshot.sessionsThisWeek.toFloat() / snapshot.targetSessionsPerWeek.toFloat())
            .coerceIn(0f, 1f)
    } else {
        0f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "StreakProgressAnimation",
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.76f)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .width(360.dp)
                    .heightIn(max = 620.dp)
                    .padding(horizontal = spacing.md, vertical = spacing.sm)
                    .clip(RoundedCornerShape(spacing.cornerMd))
                    .background(SlateDeep.copy(alpha = 0.98f))
                    .border(BorderStroke(2.dp, SlateBorder), RoundedCornerShape(spacing.cornerMd)),
            ) {
                // 16-bit SNES corner brackets
                StreakCornerBrackets(modifier = Modifier.fillMaxSize())

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Header with title, date range, and close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.weekly_streak_title).uppercase(),
                                fontFamily = determination,
                                color = ImperialGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                letterSpacing = 1.sp,
                            )
                            Text(
                                text = stringResource(
                                    R.string.weekly_streak_range,
                                    snapshot.weekStart.format(dateFmt),
                                    weekEnd.format(dateFmt),
                                ),
                                fontFamily = determination,
                                color = SilverSlate,
                                fontSize = 11.sp,
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(SlateSurface.copy(alpha = 0.8f))
                                .border(1.dp, SlateBorderSubtle, CircleShape),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.weekly_streak_close),
                                tint = SilverSteel,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }

                    // Hero Streak Card
                    val isGoalMet = snapshot.weekGoalMet ||
                        snapshot.sessionsThisWeek >= snapshot.targetSessionsPerWeek
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(spacing.cornerSm))
                            .background(SlateSurface.copy(alpha = 0.85f))
                            .border(
                                BorderStroke(1.dp, if (isGoalMet) ImperialGold.copy(alpha = 0.45f) else SlateBorderSubtle),
                                RoundedCornerShape(spacing.cornerSm),
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.streak),
                                contentDescription = stringResource(R.string.streak_icon_desc),
                                modifier = Modifier.size(36.dp),
                                contentScale = ContentScale.Fit,
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(
                                        R.string.weekly_streak_weeks,
                                        snapshot.currentStreakWeeks,
                                    ).uppercase(),
                                    fontFamily = determination,
                                    color = if (isGoalMet) ImperialGold else TorchAmber,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = stringResource(
                                        R.string.weekly_streak_longest,
                                        snapshot.longestStreakWeeks,
                                    ),
                                    fontFamily = determination,
                                    color = SilverSteel,
                                    fontSize = 11.sp,
                                )
                            }
                        }
                    }

                    // This Week's Progress Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(spacing.cornerSm))
                            .background(SlateSurface.copy(alpha = 0.85f))
                            .border(BorderStroke(1.dp, SlateBorderSubtle), RoundedCornerShape(spacing.cornerSm))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(R.string.weekly_streak_this_week).uppercase(),
                                    fontFamily = determination,
                                    color = ImperialGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = if (snapshot.weekGoalMet) {
                                        stringResource(R.string.weekly_streak_goal_met).uppercase() + " ✓"
                                    } else {
                                        stringResource(
                                            R.string.weekly_streak_progress,
                                            snapshot.sessionsThisWeek,
                                            snapshot.targetSessionsPerWeek,
                                        )
                                    },
                                    fontFamily = determination,
                                    color = if (snapshot.weekGoalMet) VitalGreen else SilverSteel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            // 16-bit Progress Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(spacing.cornerXs))
                                    .background(SlateGroove)
                                    .border(
                                        BorderStroke(1.dp, SlateBorderSubtle),
                                        RoundedCornerShape(spacing.cornerXs),
                                    ),
                            ) {
                                if (animatedProgress > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(animatedProgress)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(spacing.cornerXs))
                                            .background(
                                                if (snapshot.weekGoalMet) {
                                                    Brush.horizontalGradient(listOf(VitalGreen, VitalGreen.copy(alpha = 0.8f)))
                                                } else {
                                                    Brush.horizontalGradient(listOf(TorchAmber, ImperialGold))
                                                }
                                            ),
                                    )
                                }
                            }

                            // XP Reward / Bonus Callout
                            if (snapshot.weekGoalMet && snapshot.lastXpAwardedThisWeek) {
                                Text(
                                    text = stringResource(
                                        R.string.weekly_streak_xp_bonus,
                                        snapshot.lastXpAwardAmount,
                                    ),
                                    fontFamily = determination,
                                    color = RewardGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            } else {
                                Text(
                                    text = stringResource(
                                        R.string.weekly_streak_xp_next,
                                        snapshot.xpForNextCompletion,
                                    ),
                                    fontFamily = determination,
                                    color = RewardGold,
                                    fontSize = 11.sp,
                                )
                            }
                        }
                    }

                    // Target Sessions Stepper Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(spacing.cornerSm))
                            .background(SlateSurface.copy(alpha = 0.85f))
                            .border(BorderStroke(1.dp, SlateBorderSubtle), RoundedCornerShape(spacing.cornerSm))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.weekly_streak_target_label).uppercase(),
                                    fontFamily = determination,
                                    color = SilverSteel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = stringResource(R.string.weekly_streak_target_hint),
                                    fontFamily = determination,
                                    color = SilverSlate,
                                    fontSize = 10.sp,
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                val canDecrease = snapshot.targetSessionsPerWeek > MIN_WEEKLY_TARGET
                                val canIncrease = snapshot.targetSessionsPerWeek < MAX_WEEKLY_TARGET

                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(spacing.cornerXs))
                                        .background(SlateGroove)
                                        .border(
                                            BorderStroke(1.dp, if (canDecrease) SlateBorder else SlateBorderSubtle.copy(alpha = 0.4f)),
                                            RoundedCornerShape(spacing.cornerXs),
                                        )
                                        .clickable(enabled = canDecrease) {
                                            onTargetChange((snapshot.targetSessionsPerWeek - 1).coerceAtLeast(MIN_WEEKLY_TARGET))
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = stringResource(R.string.weekly_streak_decrease_goal),
                                        tint = if (canDecrease) SilverSteel else SilverSlate.copy(alpha = 0.35f),
                                        modifier = Modifier.size(16.dp),
                                    )
                                }

                                Text(
                                    text = "${snapshot.targetSessionsPerWeek}",
                                    fontFamily = determination,
                                    color = ImperialGold,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp),
                                )

                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(spacing.cornerXs))
                                        .background(SlateGroove)
                                        .border(
                                            BorderStroke(1.dp, if (canIncrease) SlateBorder else SlateBorderSubtle.copy(alpha = 0.4f)),
                                            RoundedCornerShape(spacing.cornerXs),
                                        )
                                        .clickable(enabled = canIncrease) {
                                            onTargetChange((snapshot.targetSessionsPerWeek + 1).coerceAtMost(MAX_WEEKLY_TARGET))
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = stringResource(R.string.weekly_streak_increase_goal),
                                        tint = if (canIncrease) SilverSteel else SilverSlate.copy(alpha = 0.35f),
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }

                    // Streak Rewards / Outfits
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.weekly_streak_skins_title).uppercase(),
                            fontFamily = determination,
                            color = ImperialGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                        )

                        WeeklyStreakRewards.skins.forEach { skin ->
                            SkinRow(
                                skin = skin,
                                unlocked = snapshot.unlockedSkins.any { it.id == skin.id },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Close Button
                    PixelArtButton(
                        onClick = onDismiss,
                        imageRes = R.drawable.button_unclicked,
                        pressedRes = R.drawable.button_clicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.weekly_streak_close).uppercase(),
                            fontFamily = determination,
                            color = ImperialGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SkinRow(
    skin: WeeklyStreakSkin,
    unlocked: Boolean,
) {
    val spacing = MaterialTheme.spacing
    val name = when (skin.id) {
        WeeklyStreakRewards.SKIN_EMBER -> stringResource(R.string.weekly_streak_skin_ember)
        WeeklyStreakRewards.SKIN_PHOENIX -> stringResource(R.string.weekly_streak_skin_phoenix)
        WeeklyStreakRewards.SKIN_LEGEND -> stringResource(R.string.weekly_streak_skin_legend)
        else -> skin.displayName
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cornerSm))
            .background(SlateSurface.copy(alpha = if (unlocked) 0.85f else 0.5f))
            .border(
                BorderStroke(1.dp, if (unlocked) ImperialGold.copy(alpha = 0.35f) else SlateBorderSubtle),
                RoundedCornerShape(spacing.cornerSm),
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (unlocked) {
                    Image(
                        painter = painterResource(id = R.drawable.streak),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = SilverSlate.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp),
                    )
                }
                Text(
                    text = name,
                    fontFamily = determination,
                    color = if (unlocked) ImperialGold else SilverSteel,
                    fontSize = 12.sp,
                    fontWeight = if (unlocked) FontWeight.Bold else FontWeight.Normal,
                )
            }
            Text(
                text = if (unlocked) {
                    stringResource(R.string.weekly_streak_skin_unlocked).uppercase()
                } else {
                    stringResource(R.string.weekly_streak_skin_locked, skin.requiredWeeks)
                },
                fontFamily = determination,
                color = if (unlocked) VitalGreen else SilverSlate,
                fontSize = 11.sp,
                fontWeight = if (unlocked) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}

/**
 * 16-bit SNES corner brackets in ImperialGold and PlatinumWhite.
 */
@Composable
private fun StreakCornerBrackets(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cornerSize = 12.dp.toPx()
        val thickness = 2.5.dp.toPx()
        val gold = ImperialGold
        val white = PlatinumWhite

        // Top-left
        drawRect(gold, Offset(0f, 0f), Size(cornerSize, thickness))
        drawRect(gold, Offset(0f, 0f), Size(thickness, cornerSize))
        drawRect(white, Offset(0f, 0f), Size(thickness, thickness))

        // Top-right
        drawRect(gold, Offset(size.width - cornerSize, 0f), Size(cornerSize, thickness))
        drawRect(gold, Offset(size.width - thickness, 0f), Size(thickness, cornerSize))
        drawRect(white, Offset(size.width - thickness, 0f), Size(thickness, thickness))

        // Bottom-left
        drawRect(gold, Offset(0f, size.height - thickness), Size(cornerSize, thickness))
        drawRect(gold, Offset(0f, size.height - cornerSize), Size(thickness, cornerSize))
        drawRect(white, Offset(0f, size.height - thickness), Size(thickness, thickness))

        // Bottom-right
        drawRect(gold, Offset(size.width - cornerSize, size.height - thickness), Size(cornerSize, thickness))
        drawRect(gold, Offset(size.width - thickness, size.height - cornerSize), Size(thickness, cornerSize))
        drawRect(white, Offset(size.width - thickness, size.height - thickness), Size(thickness, thickness))
    }
}

@Composable
fun WeeklyStreakBonusBanner(snapshot: WeeklyStreakSnapshot) {
    if (!snapshot.weekGoalMet || !snapshot.lastXpAwardedThisWeek || snapshot.lastXpAwardAmount <= 0) {
        return
    }
    Text(
        text = stringResource(R.string.weekly_streak_resume_bonus, snapshot.lastXpAwardAmount),
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = RewardGold,
    )
}
