package com.pixelfitquest.components.molecules

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.pixelfitquest.ui.theme.SilverSteel
import com.pixelfitquest.ui.theme.SlateBorder
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.determination

@Composable
fun RomCalibrationSettingsCard(
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cornerSm))
            .background(SlateDeep.copy(alpha = 0.94f))
            .border(BorderStroke(1.5.dp, SlateBorder), RoundedCornerShape(spacing.cornerSm))
            .padding(horizontal = spacing.sm, vertical = spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.settings_rom_reset_title),
                color = SilverSteel,
                fontFamily = determination,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
            Text(
                text = stringResource(R.string.settings_rom_reset_subtitle),
                color = SilverSteel.copy(alpha = 0.75f),
                fontSize = 11.sp,
            )
        }
        Spacer(Modifier.width(spacing.sm))
        Text(
            text = stringResource(R.string.settings_rom_reset_action),
            color = ImperialGold,
            fontFamily = determination,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(spacing.cornerXs))
                .border(1.dp, ImperialGold.copy(alpha = 0.6f), RoundedCornerShape(spacing.cornerXs))
                .clickable(onClick = onReset)
                .padding(horizontal = spacing.sm, vertical = spacing.xxs),
        )
    }
}
