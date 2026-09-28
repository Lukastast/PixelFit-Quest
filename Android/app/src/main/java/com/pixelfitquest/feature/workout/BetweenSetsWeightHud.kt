package com.pixelfitquest.feature.workout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelfitquest.R
import com.pixelfitquest.ui.theme.ImperialGold
import com.pixelfitquest.ui.theme.LocalSpacing
import com.pixelfitquest.ui.theme.SilverSlate
import com.pixelfitquest.ui.theme.SilverSteel
import com.pixelfitquest.ui.theme.SlateBorder
import com.pixelfitquest.ui.theme.SlateBorderSubtle
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.SlateGroove
import com.pixelfitquest.ui.theme.VitalGreen
import com.pixelfitquest.ui.theme.determination
import java.util.Locale

@Composable
fun BetweenSetsWeightHud(
    weight: Float,
    onAdjustWeight: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current

    Box(
        modifier = modifier
            .widthIn(max = spacing.scale(360))
            .clip(RoundedCornerShape(spacing.cornerSm))
            .background(SlateDeep.copy(alpha = 0.92f))
            .border(BorderStroke(1.5.dp, SlateBorder), RoundedCornerShape(spacing.cornerSm))
            .padding(horizontal = spacing.sm, vertical = spacing.xs)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.xxs),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.workout_set_weight_label).uppercase(),
                    fontFamily = determination,
                    fontSize = 11.sp,
                    color = SilverSlate,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (weight % 1f == 0f) "${weight.toInt()} KG" else String.format(Locale.US, "%.1f KG", weight),
                    fontFamily = determination,
                    fontSize = 14.sp,
                    color = ImperialGold,
                    fontWeight = FontWeight.Bold
                )
            }

            // Quick plate adjustment row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.scale(4), Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HudPlateChip(label = "-5", delta = -5f, onClick = onAdjustWeight)
                HudPlateChip(label = "-2.5", delta = -2.5f, onClick = onAdjustWeight)
                HudPlateChip(label = "-1.25", delta = -1.25f, onClick = onAdjustWeight)
                HudPlateChip(label = "+1.25", delta = 1.25f, onClick = onAdjustWeight)
                HudPlateChip(label = "+2.5", delta = 2.5f, onClick = onAdjustWeight)
                HudPlateChip(label = "+5", delta = 5f, onClick = onAdjustWeight)
            }
        }
    }
}

@Composable
private fun HudPlateChip(
    label: String,
    delta: Float,
    onClick: (Float) -> Unit,
) {
    val spacing = LocalSpacing.current
    val isPositive = delta > 0
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(spacing.cornerXs))
            .background(SlateGroove)
            .border(1.dp, SlateBorderSubtle, RoundedCornerShape(spacing.cornerXs))
            .clickable { onClick(delta) }
            .padding(horizontal = spacing.scale(7), vertical = spacing.scale(4)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = determination,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPositive) VitalGreen else SilverSteel
        )
    }
}
