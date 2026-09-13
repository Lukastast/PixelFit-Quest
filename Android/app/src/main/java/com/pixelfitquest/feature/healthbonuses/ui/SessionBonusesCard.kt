package com.pixelfitquest.feature.healthbonuses.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelfitquest.R
import com.pixelfitquest.feature.healthbonuses.model.BonusKind
import com.pixelfitquest.feature.healthbonuses.model.HealthDataOrigin
import com.pixelfitquest.feature.healthbonuses.model.HealthSnapshot
import com.pixelfitquest.feature.healthbonuses.model.SessionBonus
import com.pixelfitquest.ui.theme.RewardGold
import com.pixelfitquest.ui.theme.VitalGreen

@Composable
fun SessionBonusesCard(
    bonuses: List<SessionBonus>,
    snapshot: HealthSnapshot,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.health_bonus_section_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.health_bonus_disclaimer),
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(10.dp))

            if (bonuses.isEmpty()) {
                Text(
                    text = stringResource(R.string.health_bonus_none),
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    bonuses.forEach { bonus ->
                        BonusRow(bonus)
                    }
                }
                val extraXp = bonuses.sumOf { it.xp }
                val extraCoins = bonuses.sumOf { it.coins }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.health_bonus_total, extraXp, extraCoins),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = VitalGreen
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = sourceCaption(snapshot),
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
private fun BonusRow(bonus: SessionBonus) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = iconFor(bonus.kind),
            contentDescription = null,
            tint = VitalGreen,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titleFor(bonus.kind),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                text = bodyFor(bonus.kind),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = stringResource(R.string.xp_reward, bonus.xp),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = VitalGreen
            )
            Text(
                text = stringResource(R.string.coins_reward, bonus.coins),
                fontSize = 12.sp,
                color = RewardGold
            )
        }
    }
}

@Composable
private fun titleFor(kind: BonusKind): String = stringResource(
    when (kind) {
        BonusKind.GOOD_SLEEP -> R.string.health_bonus_sleep_title
        BonusKind.STEP_GOAL -> R.string.health_bonus_steps_title
        BonusKind.RUNNING -> R.string.health_bonus_run_title
        BonusKind.ENERGY -> R.string.health_bonus_energy_title
    }
)

@Composable
private fun bodyFor(kind: BonusKind): String = stringResource(
    when (kind) {
        BonusKind.GOOD_SLEEP -> R.string.health_bonus_sleep_body
        BonusKind.STEP_GOAL -> R.string.health_bonus_steps_body
        BonusKind.RUNNING -> R.string.health_bonus_run_body
        BonusKind.ENERGY -> R.string.health_bonus_energy_body
    }
)

private fun iconFor(kind: BonusKind): ImageVector = when (kind) {
    BonusKind.GOOD_SLEEP -> Icons.Outlined.Bedtime
    BonusKind.STEP_GOAL -> Icons.AutoMirrored.Outlined.DirectionsWalk
    BonusKind.RUNNING -> Icons.AutoMirrored.Outlined.DirectionsRun
    BonusKind.ENERGY -> Icons.Outlined.Bolt
}

@Composable
private fun sourceCaption(snapshot: HealthSnapshot): String {
    return when (snapshot.origin) {
        HealthDataOrigin.HEALTH_CONNECT, HealthDataOrigin.MIXED ->
            stringResource(R.string.health_bonus_source_on_device)
        HealthDataOrigin.NONE ->
            stringResource(R.string.health_bonus_source_unavailable)
    }
}
