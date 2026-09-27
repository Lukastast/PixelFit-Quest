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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelfitquest.ui.theme.ImperialGold
import com.pixelfitquest.ui.theme.LocalSpacing
import com.pixelfitquest.ui.theme.SilverSlate
import com.pixelfitquest.ui.theme.SilverSteel
import com.pixelfitquest.ui.theme.SlateBorder
import com.pixelfitquest.ui.theme.SlateBorderSubtle
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.SlateGroove
import com.pixelfitquest.ui.theme.determination

@Composable
fun VolumeCard(
    musicVolume: Int,
    onVolumeChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val canDecrease = musicVolume > 0
    val canIncrease = musicVolume < 100

    val volumeIcon = when {
        musicVolume == 0 -> Icons.AutoMirrored.Filled.VolumeMute
        musicVolume < 50 -> Icons.AutoMirrored.Filled.VolumeDown
        else -> Icons.AutoMirrored.Filled.VolumeUp
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = spacing.scale(56))
            .clip(RoundedCornerShape(spacing.cornerSm))
            .background(SlateDeep.copy(alpha = 0.94f))
            .border(BorderStroke(1.5.dp, SlateBorder), RoundedCornerShape(spacing.cornerSm))
            .padding(horizontal = spacing.sm, vertical = spacing.xs)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Recessed icon well
            Box(
                modifier = Modifier
                    .size(spacing.scale(36))
                    .clip(RoundedCornerShape(spacing.cornerXs))
                    .background(SlateGroove)
                    .border(1.dp, SlateBorder, RoundedCornerShape(spacing.cornerXs)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = volumeIcon,
                    contentDescription = "Volume Icon",
                    tint = SilverSteel,
                    modifier = Modifier.size(spacing.scale(18))
                )
            }

            Spacer(modifier = Modifier.width(spacing.sm))

            // Title & Subtitle
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Music Volume",
                    color = SilverSteel,
                    fontFamily = determination,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    lineHeight = 16.sp
                )
                Text(
                    text = "Cavern quest soundtrack",
                    color = SilverSlate,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(spacing.xs))

            // Stepper controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.xxs)
            ) {
                // Minus button
                Box(
                    modifier = Modifier
                        .size(spacing.scale(28))
                        .clip(RoundedCornerShape(spacing.cornerXs))
                        .background(SlateGroove)
                        .border(
                            BorderStroke(
                                1.dp,
                                if (canDecrease) SlateBorder else SlateBorderSubtle.copy(alpha = 0.4f)
                            ),
                            RoundedCornerShape(spacing.cornerXs)
                        )
                        .clickable(enabled = canDecrease) {
                            val newVolume = (musicVolume - 10).coerceAtLeast(0)
                            onVolumeChange(newVolume)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease Volume",
                        tint = if (canDecrease) SilverSteel else SilverSlate.copy(alpha = 0.35f),
                        modifier = Modifier.size(spacing.scale(16))
                    )
                }

                // Volume text
                Text(
                    text = "${musicVolume}%",
                    fontFamily = determination,
                    color = ImperialGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(min = spacing.scale(38))
                )

                // Plus button
                Box(
                    modifier = Modifier
                        .size(spacing.scale(28))
                        .clip(RoundedCornerShape(spacing.cornerXs))
                        .background(SlateGroove)
                        .border(
                            BorderStroke(
                                1.dp,
                                if (canIncrease) SlateBorder else SlateBorderSubtle.copy(alpha = 0.4f)
                            ),
                            RoundedCornerShape(spacing.cornerXs)
                        )
                        .clickable(enabled = canIncrease) {
                            val newVolume = (musicVolume + 10).coerceAtMost(100)
                            onVolumeChange(newVolume)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase Volume",
                        tint = if (canIncrease) SilverSteel else SilverSlate.copy(alpha = 0.35f),
                        modifier = Modifier.size(spacing.scale(16))
                    )
                }
            }
        }
    }
}
