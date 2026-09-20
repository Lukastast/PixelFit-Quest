package com.pixelfitquest.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pixelfitquest.R
import com.pixelfitquest.ui.theme.spacing

@Composable
fun MissionsDialog(
    weeklyMissions: List<Pair<String, String>>,
    completedMissions: Set<String>,
    todaySteps: Long,
    todaysWorkouts: Int,
    onDismiss: () -> Unit,
) {
    val spacing = MaterialTheme.spacing

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Image(
                painter = painterResource(id = R.drawable.questloginboard_wider),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(spacing.scale(260)),
                contentScale = ContentScale.FillBounds
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.scale(260))
                    .padding(spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.daily_missions_title),
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "✕",
                        fontSize = 18.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(spacing.xxs)
                    )
                }

                Spacer(modifier = Modifier.height(spacing.xs))

                weeklyMissions.forEach { (mission, reward) ->
                    val isCompleted = completedMissions.contains(mission)
                    val effectiveCompleted = isCompleted || when {
                        mission.startsWith("Walk") -> {
                            val target = mission.split(" ")[1].toLongOrNull() ?: 0
                            todaySteps >= target
                        }
                        mission.startsWith("Complete") -> {
                            val target = mission.split(" ")[1].toIntOrNull() ?: 0
                            todaysWorkouts >= target
                        }
                        else -> false
                    }

                    val progressText = if (effectiveCompleted) {
                        if (mission.startsWith("Walk")) {
                            val target = mission.split(" ")[1].toLongOrNull() ?: 0
                            "$target / $target"
                        } else {
                            val target = mission.split(" ")[1].toIntOrNull() ?: 0
                            "$target / $target"
                        }
                    } else {
                        if (mission.startsWith("Walk")) {
                            val target = mission.split(" ")[1].toLongOrNull() ?: 0
                            "$todaySteps / $target"
                        } else {
                            val target = mission.split(" ")[1].toIntOrNull() ?: 0
                            "$todaysWorkouts / $target"
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = spacing.scale(5)),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = mission,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = progressText,
                            color = if (effectiveCompleted) Color(0xFF4CAF50) else Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (effectiveCompleted) FontWeight.Bold else FontWeight.Normal
                        )

                        val rewardParts = reward.split(":", limit = 2).map { it.trim() }
                        val rewardType = if (rewardParts.size == 2) rewardParts[0].lowercase() else ""
                        val rewardAmount = if (rewardParts.size == 2) rewardParts[1].toIntOrNull() ?: 0 else 0

                        val isCoins = rewardType == "coins"
                        val isExp = rewardType == "exp"

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "+$rewardAmount",
                                color = Color(0xFFFFD700),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(spacing.xs))
                            if (isCoins) {
                                Image(
                                    painter = painterResource(id = R.drawable.coin),
                                    contentDescription = stringResource(R.string.coins_reward_desc),
                                    modifier = Modifier.size(spacing.md)
                                )
                            } else if (isExp) {
                                Text(
                                    text = stringResource(R.string.exp_label),
                                    color = Color(0xFFFFD700),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
