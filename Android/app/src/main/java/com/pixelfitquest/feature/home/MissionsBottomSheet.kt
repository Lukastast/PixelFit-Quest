package com.pixelfitquest.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import com.pixelfitquest.R
import com.pixelfitquest.feature.missions.MissionProgress
import com.pixelfitquest.feature.missions.groupedCount
import com.pixelfitquest.ui.theme.ImperialGold
import com.pixelfitquest.ui.theme.ParchmentBorder
import com.pixelfitquest.ui.theme.RewardGold
import com.pixelfitquest.ui.theme.SilverSlate
import com.pixelfitquest.ui.theme.SilverSteel
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.VitalGreen
import com.pixelfitquest.ui.theme.spacing

@Composable
fun MissionsDialog(
    missions: List<MissionProgress>,
    rerollAvailable: Boolean = false,
    onReroll: (String) -> Unit = {},
    onDismiss: () -> Unit,
) {
    val spacing = MaterialTheme.spacing

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(spacing.cornerMd))
                .background(SlateDeep.copy(alpha = 0.98f))
                .border(BorderStroke(2.dp, ParchmentBorder), RoundedCornerShape(spacing.cornerMd))
                .padding(spacing.md),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = spacing.scale(440)),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.daily_missions_title),
                        fontSize = 16.sp,
                        color = ImperialGold,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "✕",
                        fontSize = 18.sp,
                        color = SilverSteel,
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
                        MissionRow(mission, rerollAvailable, onReroll)
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionRow(
    mission: MissionProgress,
    rerollAvailable: Boolean,
    onReroll: (String) -> Unit,
) {
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
            color = SilverSteel,
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
                color = if (done) VitalGreen else SilverSlate,
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
        if (rerollAvailable && !done) {
            Text(
                text = stringResource(R.string.mission_reroll),
                color = ImperialGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = spacing.xxs)
                    .clickable { onReroll(definition.id) },
            )
        }
    }
}
