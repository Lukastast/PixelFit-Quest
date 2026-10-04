package com.pixelfitquest.components.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelfitquest.R
import com.pixelfitquest.feature.workout.sensor.MountSide
import com.pixelfitquest.ui.theme.ImperialGold
import com.pixelfitquest.ui.theme.LocalSpacing
import com.pixelfitquest.ui.theme.SilverSlate
import com.pixelfitquest.ui.theme.SlateBorder
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.determination

@Composable
fun BarSleeveCard(
    side: MountSide,
    onSelect: (MountSide) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cornerSm))
            .background(SlateDeep.copy(alpha = 0.94f))
            .border(1.dp, SlateBorder, RoundedCornerShape(spacing.cornerSm))
            .padding(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        Text(
            text = stringResource(R.string.settings_sleeve_title),
            color = ImperialGold,
            fontFamily = determination,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
        )
        Text(
            text = stringResource(R.string.settings_sleeve_subtitle),
            color = SilverSlate,
            fontSize = 11.sp,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
            SleeveChip(stringResource(R.string.settings_sleeve_unset), side == MountSide.UNKNOWN) {
                onSelect(MountSide.UNKNOWN)
            }
            SleeveChip(stringResource(R.string.settings_sleeve_left), side == MountSide.LEFT) {
                onSelect(MountSide.LEFT)
            }
            SleeveChip(stringResource(R.string.settings_sleeve_right), side == MountSide.RIGHT) {
                onSelect(MountSide.RIGHT)
            }
        }
    }
}

@Composable
fun DeveloperTraceCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cornerSm))
            .background(SlateDeep.copy(alpha = 0.94f))
            .border(1.dp, if (enabled) ImperialGold else SlateBorder, RoundedCornerShape(spacing.cornerSm))
            .clickable { onToggle(!enabled) }
            .padding(spacing.sm),
    ) {
        Text(
            text = stringResource(R.string.settings_trace_export_title),
            color = if (enabled) ImperialGold else SilverSlate,
            fontFamily = determination,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
        )
        Text(
            text = stringResource(
                if (enabled) R.string.settings_trace_export_on else R.string.settings_trace_export_off,
            ),
            color = SilverSlate,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun SleeveChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val spacing = LocalSpacing.current
    Text(
        text = label,
        color = if (selected) SlateDeep else SilverSlate,
        fontFamily = determination,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(spacing.cornerXs))
            .background(if (selected) ImperialGold else SlateDeep)
            .border(1.dp, if (selected) ImperialGold else SlateBorder, RoundedCornerShape(spacing.cornerXs))
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.xs, vertical = spacing.xxs),
    )
}
