package com.pixelfitquest.feature.customization.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pixelfitquest.R
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.feature.customization.CustomizationViewModel.Companion.ARM_CM_MAX
import com.pixelfitquest.feature.customization.CustomizationViewModel.Companion.ARM_CM_MIN
import com.pixelfitquest.feature.customization.CustomizationViewModel.Companion.HEIGHT_CM_MAX
import com.pixelfitquest.feature.customization.CustomizationViewModel.Companion.HEIGHT_CM_MIN
import com.pixelfitquest.ui.theme.DarkStone
import com.pixelfitquest.ui.theme.ImperialGold
import com.pixelfitquest.ui.theme.LeatherDark
import com.pixelfitquest.ui.theme.ParchmentBorder
import com.pixelfitquest.ui.theme.QuestBrown
import com.pixelfitquest.ui.theme.RewardGold
import com.pixelfitquest.ui.theme.SilverSlate
import com.pixelfitquest.ui.theme.SilverSteel
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.spacing
import kotlin.math.roundToInt

@Composable
fun StatsDialog(
    heightCm: Int,
    armLengthCm: Int,
    onHeightChange: (Int) -> Unit,
    onArmLengthChange: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val spacing = MaterialTheme.spacing

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(spacing.cornerMd))
                .background(SlateDeep.copy(alpha = 0.96f))
                .border(BorderStroke(2.dp, ParchmentBorder), RoundedCornerShape(spacing.cornerMd))
                .padding(spacing.md),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "📏 Body Calibration",
                        style = MaterialTheme.typography.titleMedium,
                        color = ImperialGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }

                Spacer(modifier = Modifier.height(spacing.sm))

                Text(
                    text = "Calibrates accelerometer & gyroscope sensors to compute precise reps and range of motion.",
                    color = SilverSlate,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(spacing.md))

                // Height Slider
                DialogMetricSlider(
                    label = stringResource(R.string.height_label),
                    valueCm = heightCm.coerceIn(HEIGHT_CM_MIN, HEIGHT_CM_MAX),
                    range = HEIGHT_CM_MIN.toFloat()..HEIGHT_CM_MAX.toFloat(),
                    onCommit = onHeightChange,
                )

                Spacer(modifier = Modifier.height(spacing.md))

                // Arm Length Slider
                DialogMetricSlider(
                    label = stringResource(R.string.stats_arm_length),
                    valueCm = armLengthCm.coerceIn(ARM_CM_MIN, ARM_CM_MAX),
                    range = ARM_CM_MIN.toFloat()..ARM_CM_MAX.toFloat(),
                    onCommit = onArmLengthChange,
                )

                Spacer(modifier = Modifier.height(spacing.lg))

                // Done Button
                PixelArtButton(
                    onClick = onDismiss,
                    imageRes = R.drawable.button_unclicked,
                    pressedRes = R.drawable.button_clicked,
                    modifier = Modifier.size(spacing.scale(160), spacing.buttonHeight),
                ) {
                    Text(
                        text = "DONE",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogMetricSlider(
    label: String,
    valueCm: Int,
    range: ClosedFloatingPointRange<Float>,
    onCommit: (Int) -> Unit,
) {
    var sliderValue by remember { mutableFloatStateOf(valueCm.toFloat()) }
    LaunchedEffect(valueCm) { sliderValue = valueCm.toFloat() }
    val steps = (range.endInclusive - range.start).roundToInt() - 1

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                color = SilverSteel,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "${sliderValue.roundToInt()} cm",
                color = ImperialGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onCommit(sliderValue.roundToInt()) },
            valueRange = range,
            steps = steps.coerceAtLeast(0),
            colors = SliderDefaults.colors(
                thumbColor = ImperialGold,
                activeTrackColor = ImperialGold,
                inactiveTrackColor = LeatherDark,
            ),
        )
    }
}
