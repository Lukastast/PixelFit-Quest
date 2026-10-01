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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.pixelfitquest.feature.workout.WorkoutWeightPrefs
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
import kotlin.math.roundToInt

@Composable
fun WeightProgressionSettingsCard(
    suggestionEnabled: Boolean,
    onSuggestionToggle: (Boolean) -> Unit,
    repThreshold: Int,
    onThresholdChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val canDecrease = repThreshold > WorkoutWeightPrefs.MIN_REP_THRESHOLD
    val canIncrease = repThreshold < WorkoutWeightPrefs.MAX_REP_THRESHOLD

    val preset = WorkoutWeightPrefs.ProgressionPreset.fromThreshold(repThreshold)
    val isStrength = preset == WorkoutWeightPrefs.ProgressionPreset.STRENGTH
    val isHypertrophy = preset == WorkoutWeightPrefs.ProgressionPreset.HYPERTROPHY

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

            // Stepper & Presets (Visible when enabled)
            if (suggestionEnabled) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = SlateBorderSubtle.copy(alpha = 0.4f),
                    modifier = Modifier.padding(horizontal = spacing.xxs)
                )

                // Presets Header & Target Status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacing.xxs),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_rep_threshold_title),
                        color = SilverSteel,
                        fontFamily = determination,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                    )

                    // Target indicator badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(spacing.cornerXs))
                            .background(SlateGroove)
                            .border(
                                1.dp,
                                if (isStrength || isHypertrophy) ImperialGold.copy(alpha = 0.6f) else SlateBorder,
                                RoundedCornerShape(spacing.cornerXs)
                            )
                            .padding(horizontal = spacing.xs, vertical = spacing.scale(2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when {
                                isStrength -> stringResource(R.string.settings_rep_badge_strength, repThreshold)
                                isHypertrophy -> stringResource(R.string.settings_rep_badge_hypertrophy, repThreshold)
                                else -> stringResource(R.string.settings_rep_badge_custom, repThreshold)
                            },
                            color = ImperialGold,
                            fontFamily = determination,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                // Presets Row (Strength vs Hypertrophy)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    PresetOptionChip(
                        title = stringResource(R.string.settings_progression_preset_strength),
                        subtitle = stringResource(R.string.settings_progression_preset_strength_sub),
                        isSelected = isStrength,
                        onClick = { onThresholdChange(WorkoutWeightPrefs.PRESET_STRENGTH_REPS) },
                        modifier = Modifier.weight(1f)
                    )

                    PresetOptionChip(
                        title = stringResource(R.string.settings_progression_preset_hypertrophy),
                        subtitle = if (isHypertrophy) {
                            stringResource(R.string.settings_progression_preset_hypertrophy_active, repThreshold)
                        } else {
                            stringResource(R.string.settings_progression_preset_hypertrophy_sub)
                        },
                        isSelected = isHypertrophy,
                        onClick = {
                            val next = if (repThreshold == WorkoutWeightPrefs.PRESET_HYPERTROPHY_DEFAULT_REPS) {
                                WorkoutWeightPrefs.PRESET_HYPERTROPHY_HIGH_REPS
                            } else {
                                WorkoutWeightPrefs.PRESET_HYPERTROPHY_DEFAULT_REPS
                            }
                            onThresholdChange(next)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Custom Slider Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.xxs)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.settings_progression_custom_slider),
                            color = SilverSlate,
                            fontFamily = determination,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$repThreshold reps",
                            color = ImperialGold,
                            fontFamily = determination,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // Stepper + Slider Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
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
                                    onThresholdChange((repThreshold - 1).coerceAtLeast(WorkoutWeightPrefs.MIN_REP_THRESHOLD))
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

                        // Slider
                        Slider(
                            value = repThreshold.toFloat().coerceIn(
                                WorkoutWeightPrefs.MIN_REP_THRESHOLD.toFloat(),
                                WorkoutWeightPrefs.MAX_REP_THRESHOLD.toFloat()
                            ),
                            onValueChange = { newValue ->
                                onThresholdChange(
                                    newValue.roundToInt().coerceIn(
                                        WorkoutWeightPrefs.MIN_REP_THRESHOLD,
                                        WorkoutWeightPrefs.MAX_REP_THRESHOLD
                                    )
                                )
                            },
                            valueRange = WorkoutWeightPrefs.MIN_REP_THRESHOLD.toFloat()..WorkoutWeightPrefs.MAX_REP_THRESHOLD.toFloat(),
                            steps = (WorkoutWeightPrefs.MAX_REP_THRESHOLD - WorkoutWeightPrefs.MIN_REP_THRESHOLD) - 1,
                            colors = SliderDefaults.colors(
                                thumbColor = ImperialGold,
                                activeTrackColor = ImperialGold,
                                inactiveTrackColor = SlateGroove,
                                activeTickColor = SlateDeep,
                                inactiveTickColor = SlateBorderSubtle.copy(alpha = 0.5f),
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = spacing.xxs)
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
                                    onThresholdChange((repThreshold + 1).coerceAtMost(WorkoutWeightPrefs.MAX_REP_THRESHOLD))
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

                    // Min / Max labels row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.scale(32)),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${WorkoutWeightPrefs.MIN_REP_THRESHOLD} reps",
                            color = SilverSlate.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                        )
                        Text(
                            text = "${WorkoutWeightPrefs.MAX_REP_THRESHOLD} reps",
                            color = SilverSlate.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                        )
                    }
                }

                // Dynamic Hint
                Text(
                    text = stringResource(R.string.settings_rep_threshold_hint, repThreshold),
                    color = SilverSlate,
                    fontSize = 10.sp,
                    lineHeight = 13.sp
                )
            }
        }
    }
}

@Composable
private fun PresetOptionChip(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val borderColor = if (isSelected) ImperialGold else SlateBorderSubtle
    val bgColor = if (isSelected) ImperialGold.copy(alpha = 0.12f) else SlateGroove
    val textColor = if (isSelected) ImperialGold else SilverSteel
    val subtextColor = if (isSelected) ImperialGold.copy(alpha = 0.85f) else SilverSlate

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(spacing.cornerXs))
            .background(bgColor)
            .border(
                BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
                RoundedCornerShape(spacing.cornerXs)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.xs, vertical = spacing.scale(6)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isSelected) {
                    Text(
                        text = "▶ ",
                        color = ImperialGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = title,
                    color = textColor,
                    fontFamily = determination,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(spacing.scale(2)))
            Text(
                text = subtitle,
                color = subtextColor,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}
