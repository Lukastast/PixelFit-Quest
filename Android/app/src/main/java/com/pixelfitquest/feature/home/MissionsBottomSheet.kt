package com.pixelfitquest.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pixelfitquest.R
import com.pixelfitquest.feature.missions.MissionProgress
import com.pixelfitquest.feature.missions.groupedCount
import com.pixelfitquest.ui.theme.RewardGold
import com.pixelfitquest.ui.theme.VitalGreen
import com.pixelfitquest.ui.theme.spacing

@Composable
fun MissionsDialog(
    missions: List<MissionProgress>,
    onDismiss: () -> Unit,
) {
    val spacing = MaterialTheme.spacing

    Dialog(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.questloginboard_wider),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = spacing.scale(440))
                    .padding(spacing.md),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.daily_missions_title),
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "✕",
                        fontSize = 18.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(spacing.xxs),
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                ) {
                    missions.forEach { mission ->
                        MissionRow(mission)
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionRow(mission: MissionProgress) {
    val spacing = MaterialTheme.spacing
    val definition = mission.definition
    val target = definition.target.coerceAtLeast(0L)
    val done = mission.isComplete || mission.claimed
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.xxs),
    ) {
        Text(
            text = definition.title,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(
                    R.string.quest_mission_progress,
                    groupedCount(mission.displayedCurrent()),
                    groupedCount(target),
                ),
                color = if (done) VitalGreen else Color.White,
                fontSize = 12.sp,
                fontWeight = if (done) FontWeight.Bold else FontWeight.Normal,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (definition.xp > 0) {
                    Text(
                        text = "+${definition.xp} ${stringResource(R.string.exp_label)}",
                        color = RewardGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                if (definition.coins > 0) {
                    if (definition.xp > 0) {
                        Spacer(modifier = Modifier.width(spacing.xs))
                    }
                    Text(
                        text = "+${definition.coins}",
                        color = RewardGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.width(spacing.xxs))
                    Image(
                        painter = painterResource(id = R.drawable.coin),
                        contentDescription = stringResource(R.string.coins_reward_desc),
                        modifier = Modifier.size(spacing.md),
                    )
                }
            }
        }
    }
}
