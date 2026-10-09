package com.pixelfitquest.components.molecules

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
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
import com.pixelfitquest.ui.theme.LocalSpacing
import com.pixelfitquest.ui.theme.SilverSteel
import com.pixelfitquest.ui.theme.SlateBorder
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.SlateGroove
import com.pixelfitquest.ui.theme.VitalGreen
import com.pixelfitquest.ui.theme.determination
import java.util.Locale

@Composable
fun RestTimerSettingsCard(
    enabled: Boolean,
    seconds: Int,
    autostart: Boolean,
    onEnabled: (Boolean) -> Unit,
    onSeconds: (Int) -> Unit,
    onAutostart: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cornerSm))
            .background(SlateDeep.copy(alpha = 0.94f))
            .border(BorderStroke(1.5.dp, SlateBorder), RoundedCornerShape(spacing.cornerSm))
            .padding(horizontal = spacing.sm, vertical = spacing.xs),
        verticalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEnabled(!enabled) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(spacing.scale(36))
                    .clip(RoundedCornerShape(spacing.cornerXs))
                    .background(SlateGroove)
                    .border(1.dp, SlateBorder, RoundedCornerShape(spacing.cornerXs)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Timer,
                    contentDescription = stringResource(R.string.settings_rest_title),
                    tint = SilverSteel,
                    modifier = Modifier.size(spacing.scale(18)),
                )
            }
            Spacer(Modifier.width(spacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.settings_rest_title),
                    color = SilverSteel,
                    fontFamily = determination,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
                Text(
                    text = stringResource(R.string.settings_rest_subtitle),
                    color = SilverSteel.copy(alpha = 0.75f),
                    fontSize = 11.sp,
                )
            }
            Text(
                text = stringResource(if (enabled) R.string.settings_landscape_on else R.string.settings_landscape_off),
                color = if (enabled) VitalGreen else SilverSteel,
                fontFamily = determination,
                fontWeight = FontWeight.Bold,
            )
        }
        if (enabled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StepChip("−") { onSeconds(seconds - 15) }
                Text(
                    text = String.format(Locale.US, "%d:%02d", seconds / 60, seconds % 60),
                    color = SilverSteel,
                    fontFamily = determination,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
                StepChip("+") { onSeconds(seconds + 15) }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAutostart(!autostart) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_rest_autostart),
                        color = SilverSteel,
                        fontFamily = determination,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    )
                    Text(
                        text = stringResource(R.string.settings_rest_autostart_subtitle),
                        color = SilverSteel.copy(alpha = 0.75f),
                        fontSize = 11.sp,
                    )
                }
                Text(
                    text = stringResource(
                        if (autostart) R.string.settings_landscape_on else R.string.settings_landscape_off,
                    ),
                    color = if (autostart) VitalGreen else SilverSteel,
                    fontFamily = determination,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun StepChip(label: String, onClick: () -> Unit) {
    val spacing = LocalSpacing.current
    Text(
        text = label,
        color = SilverSteel,
        fontFamily = determination,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(spacing.cornerXs))
            .background(SlateGroove)
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.sm, vertical = spacing.xxs),
    )
}
