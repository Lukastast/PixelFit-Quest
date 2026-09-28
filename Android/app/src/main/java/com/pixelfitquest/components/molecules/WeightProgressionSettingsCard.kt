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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@Composable
fun WeightProgressionSettingsCard(
    suggestionEnabled: Boolean,
    onSuggestionToggle: (Boolean) -> Unit,
    repThreshold: Int,
    onThresholdChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val canDecrease = repThreshold > 5
    val canIncrease = repThreshold < 30

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cornerSm))
            .background(SlateDeep.copy(alpha = 0.94f))
            .border(BorderStroke(1.5.dp, SlateBorder), RoundedCornerShape(spacing.cornerSm))
            .padding(horizontal = spacing.sm, vertical = spacing.xs)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.xs)
        ) {
            // Main Toggle Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSuggestionToggle(!suggestionEnabled) }
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
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = stringResource(R.string.settings_weight_progression_title),
                        tint = if (suggestionEnabled) VitalGreen else SilverSteel,
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
                        text = stringResource(R.string.settings_weight_progression_title),
                        color = SilverSteel,
                        fontFamily = determination,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        lineHeight = 16.sp
                    )
                    Text(
                        text = stringResource(R.string.settings_weight_progression_subtitle),
                        color = SilverSlate,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(spacing.xs))

                // Pixel toggle badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(spacing.cornerXs))
                        .background(if (suggestionEnabled) VitalGreen.copy(alpha = 0.16f) else SlateGroove)
                        .border(
                            1.dp,
                            if (suggestionEnabled) VitalGreen.copy(alpha = 0.6f) else SlateBorderSubtle,
                            RoundedCornerShape(spacing.cornerXs)
                        )
                        .padding(horizontal = spacing.sm, vertical = spacing.scale(4)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (suggestionEnabled) "ON" else "OFF",
                        color = if (suggestionEnabled) VitalGreen else SilverSlate,
                        fontFamily = determination,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            // Stepper Row (Visible when enabled)
            if (suggestionEnabled) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = SlateBorderSubtle.copy(alpha = 0.4f),
                    modifier = Modifier.padding(horizontal = spacing.xxs)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacing.xxs)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.settings_rep_threshold_title),
                            color = SilverSteel,
                            fontFamily = determination,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                        )
                        Text(
                            text = stringResource(R.string.settings_rep_threshold_hint, repThreshold),
                            color = SilverSlate,
                            fontSize = 10.sp,
                            lineHeight = 13.sp
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
                                    onThresholdChange((repThreshold - 1).coerceAtLeast(5))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease rep target",
                                tint = if (canDecrease) SilverSteel else SilverSlate.copy(alpha = 0.35f),
                                modifier = Modifier.size(spacing.scale(16))
                            )
                        }

                        // Target reps text
                        Text(
                            text = "$repThreshold reps",
                            fontFamily = determination,
                            color = ImperialGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.widthIn(min = spacing.scale(54))
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
                                    onThresholdChange((repThreshold + 1).coerceAtMost(30))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase rep target",
                                tint = if (canIncrease) SilverSteel else SilverSlate.copy(alpha = 0.35f),
                                modifier = Modifier.size(spacing.scale(16))
                            )
                        }
                    }
                }
            }
        }
    }
}
