package com.pixelfitquest.feature.streak

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pixelfitquest.R
import com.pixelfitquest.feature.streak.model.MAX_WEEKLY_TARGET
import com.pixelfitquest.feature.streak.model.MIN_WEEKLY_TARGET
import com.pixelfitquest.feature.streak.model.WeeklyStreakRewards
import com.pixelfitquest.feature.streak.model.WeeklyStreakSkin
import com.pixelfitquest.feature.streak.model.WeeklyStreakSnapshot
import com.pixelfitquest.ui.theme.FireOrange
import com.pixelfitquest.ui.theme.RewardGold
import com.pixelfitquest.ui.theme.VitalGreen
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WeeklyStreakDialog(
    snapshot: WeeklyStreakSnapshot,
    onDismiss: () -> Unit,
    onTargetChange: (Int) -> Unit,
) {
    val weekEnd = snapshot.weekStart.plusDays(6)
    val dateFmt = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
    val progress = if (snapshot.targetSessionsPerWeek > 0) {
        (snapshot.sessionsThisWeek.toFloat() / snapshot.targetSessionsPerWeek.toFloat())
            .coerceIn(0f, 1f)
    } else {
        0f
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(360.dp)
                .height(420.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.questloginboard_wider),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.weekly_streak_title),
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(
                        R.string.weekly_streak_range,
                        snapshot.weekStart.format(dateFmt),
                        weekEnd.format(dateFmt),
                    ),
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.streak),
                        contentDescription = stringResource(R.string.streak_icon_desc),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(
                            R.string.weekly_streak_weeks,
                            snapshot.currentStreakWeeks
                        ),
                        fontSize = 20.sp,
                        color = FireOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = stringResource(
                        R.string.weekly_streak_longest,
                        snapshot.longestStreakWeeks
                    ),
                    fontSize = 12.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.weekly_streak_this_week),
                    fontSize = 13.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(
                        R.string.weekly_streak_progress,
                        snapshot.sessionsThisWeek,
                        snapshot.targetSessionsPerWeek
                    ),
                    fontSize = 14.sp,
                    color = if (snapshot.weekGoalMet) VitalGreen else Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = if (snapshot.weekGoalMet) VitalGreen else FireOrange,
                    trackColor = Color.White.copy(alpha = 0.25f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (snapshot.weekGoalMet && snapshot.lastXpAwardedThisWeek) {
                    Text(
                        text = stringResource(
                            R.string.weekly_streak_xp_bonus,
                            snapshot.lastXpAwardAmount
                        ),
                        color = RewardGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.weekly_streak_goal_met),
                        color = VitalGreen,
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        text = stringResource(
                            R.string.weekly_streak_xp_next,
                            snapshot.xpForNextCompletion
                        ),
                        color = RewardGold,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.weekly_streak_target_label),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            onTargetChange(
                                (snapshot.targetSessionsPerWeek - 1).coerceAtLeast(MIN_WEEKLY_TARGET)
                            )
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = stringResource(R.string.weekly_streak_decrease_goal),
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "${snapshot.targetSessionsPerWeek}",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = {
                            onTargetChange(
                                (snapshot.targetSessionsPerWeek + 1).coerceAtMost(MAX_WEEKLY_TARGET)
                            )
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.weekly_streak_increase_goal),
                            tint = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.weekly_streak_skins_title),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                WeeklyStreakRewards.skins.forEach { skin ->
                    SkinRow(
                        skin = skin,
                        unlocked = snapshot.unlockedSkins.any { it.id == skin.id }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.weekly_streak_close),
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable(onClick = onDismiss)
                        .padding(8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SkinRow(
    skin: WeeklyStreakSkin,
    unlocked: Boolean,
) {
    val name = when (skin.id) {
        WeeklyStreakRewards.SKIN_EMBER -> stringResource(R.string.weekly_streak_skin_ember)
        WeeklyStreakRewards.SKIN_PHOENIX -> stringResource(R.string.weekly_streak_skin_phoenix)
        WeeklyStreakRewards.SKIN_LEGEND -> stringResource(R.string.weekly_streak_skin_legend)
        else -> skin.displayName
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (unlocked) {
                Image(
                    painter = painterResource(id = R.drawable.streak),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = name, color = Color.White, fontSize = 12.sp)
        }
        Text(
            text = if (unlocked) {
                stringResource(R.string.weekly_streak_skin_unlocked)
            } else {
                stringResource(R.string.weekly_streak_skin_locked, skin.requiredWeeks)
            },
            color = if (unlocked) VitalGreen else Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp
        )
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
        color = RewardGold
    )
}
