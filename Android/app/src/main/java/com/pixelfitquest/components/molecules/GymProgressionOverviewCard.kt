package com.pixelfitquest.components.molecules

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelfitquest.R
import com.pixelfitquest.feature.progress.formatKg
import com.pixelfitquest.feature.progress.model.ProgressOverview
import com.pixelfitquest.feature.workout.model.enums.displayName
import com.pixelfitquest.ui.theme.BronzeCopper
import com.pixelfitquest.ui.theme.ImperialGold
import com.pixelfitquest.ui.theme.LocalSpacing
import com.pixelfitquest.ui.theme.SilverSlate
import com.pixelfitquest.ui.theme.SilverSteel
import com.pixelfitquest.ui.theme.SlateBorder
import com.pixelfitquest.ui.theme.SlateBorderSubtle
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.SlateGroove
import com.pixelfitquest.ui.theme.TorchAmber
import com.pixelfitquest.ui.theme.VitalGreen
import com.pixelfitquest.ui.theme.determination
import kotlin.math.abs

@Composable
fun GymProgressionOverviewCard(
    overview: ProgressOverview?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val primarySeries = overview?.primarySeries
    val sessions = primarySeries?.sessions.orEmpty()
    val weightDelta = primarySeries?.weightDeltaKg
    val exerciseName = primarySeries?.exerciseType?.displayName() ?: "Bench Press"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cornerSm))
            .background(SlateDeep.copy(alpha = 0.94f))
            .border(BorderStroke(1.5.dp, SlateBorder), RoundedCornerShape(spacing.cornerSm))
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.sm, vertical = spacing.xs)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.xs)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
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
                    Image(
                        painter = painterResource(id = R.drawable.streak),
                        contentDescription = "Progression icon",
                        modifier = Modifier.size(spacing.scale(20))
                    )
                }

                Spacer(modifier = Modifier.width(spacing.sm))

                // Title & Exercise info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.progress_overview_title),
                        color = ImperialGold,
                        fontFamily = determination,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        lineHeight = 16.sp
                    )
                    Text(
                        text = if (primarySeries != null && sessions.isNotEmpty()) {
                            "$exerciseName · ${sessions.size} ${if (sessions.size == 1) "session" else "sessions"}"
                        } else {
                            stringResource(R.string.progress_overview_subtitle)
                        },
                        color = SilverSlate,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(spacing.xs))

                // Delta badge (if available)
                if (weightDelta != null && abs(weightDelta) > 0.01f) {
                    val isPositive = weightDelta >= 0f
                    val deltaColor = if (isPositive) VitalGreen else BronzeCopper
                    val deltaText = if (isPositive) "+${formatKg(weightDelta)} kg" else "${formatKg(weightDelta)} kg"

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(spacing.cornerXs))
                            .background(deltaColor.copy(alpha = 0.16f))
                            .border(1.dp, deltaColor.copy(alpha = 0.6f), RoundedCornerShape(spacing.cornerXs))
                            .padding(horizontal = spacing.scale(6), vertical = spacing.scale(2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = deltaText,
                            color = deltaColor,
                            fontFamily = determination,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(spacing.xs))
                }

                // Chevron
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = ImperialGold.copy(alpha = 0.75f),
                    modifier = Modifier.size(spacing.scale(12))
                )
            }

            // Quick Metrics Pill Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.xxs)
            ) {
                MetricPill(
                    label = stringResource(R.string.progress_overview_latest),
                    value = "${formatKg(primarySeries?.latestWeightKg ?: 0f)} kg",
                    valueColor = SilverSteel,
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    label = stringResource(R.string.progress_overview_est_1rm),
                    value = "${formatKg(primarySeries?.bestEst1RmKg ?: 0f)} kg",
                    valueColor = ImperialGold,
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    label = stringResource(R.string.progress_overview_volume),
                    value = "${formatKg(primarySeries?.totalVolumeKg ?: 0f)} kg",
                    valueColor = TorchAmber,
                    modifier = Modifier.weight(1f)
                )
            }

            // Mini Progression Sparkline Graph
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.scale(62))
                    .clip(RoundedCornerShape(spacing.cornerXs))
                    .background(SlateGroove)
                    .border(1.dp, SlateBorderSubtle, RoundedCornerShape(spacing.cornerXs)),
                contentAlignment = Alignment.Center
            ) {
                if (sessions.isEmpty()) {
                    Text(
                        text = stringResource(R.string.progress_overview_empty),
                        color = SilverSlate.copy(alpha = 0.75f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = spacing.sm)
                    )
                } else {
                    ProgressionSparkline(
                        values = sessions.map { it.workingWeightKg },
                        lineColor = TorchAmber,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Footer Callout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "1RM · Volume · Quality Analysis",
                    color = SilverSlate.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
                Text(
                    text = stringResource(R.string.progress_overview_cta),
                    color = ImperialGold,
                    fontFamily = determination,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(spacing.cornerXs))
            .background(SlateGroove)
            .border(1.dp, SlateBorderSubtle, RoundedCornerShape(spacing.cornerXs))
            .padding(horizontal = spacing.scale(6), vertical = spacing.scale(4))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = SilverSlate,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                text = value,
                color = valueColor,
                fontFamily = determination,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ProgressionSparkline(
    values: List<Float>,
    lineColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        if (values.isEmpty()) return@Canvas

        val w = size.width
        val h = size.height
        val padH = 12.dp.toPx()
        val padTop = 10.dp.toPx()
        val padBottom = 8.dp.toPx()
        val drawW = (w - 2 * padH).coerceAtLeast(1f)
        val drawH = (h - padTop - padBottom).coerceAtLeast(1f)

        val minVal = values.minOrNull() ?: 0f
        val maxVal = values.maxOrNull() ?: 1f
        val range = (maxVal - minVal).coerceAtLeast(1f)

        // Subtle horizontal guide lines
        val guideColor = Color.White.copy(alpha = 0.07f)
        drawLine(
            color = guideColor,
            start = Offset(padH, padTop + drawH * 0.5f),
            end = Offset(padH + drawW, padTop + drawH * 0.5f),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
        )

        fun xAt(i: Int): Float {
            return if (values.size <= 1) padH + drawW / 2f
            else padH + drawW * (i.toFloat() / (values.size - 1))
        }

        fun yAt(v: Float): Float {
            val fraction = ((v - minVal) / range).coerceIn(0f, 1f)
            return padTop + drawH * (1f - fraction)
        }

        // Fill path under the curve
        val fillPath = Path()
        fillPath.moveTo(xAt(0), padTop + drawH)
        values.forEachIndexed { i, v ->
            fillPath.lineTo(xAt(i), yAt(v))
        }
        fillPath.lineTo(xAt(values.size - 1), padTop + drawH)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.28f), Color.Transparent),
                startY = padTop,
                endY = padTop + drawH
            )
        )

        // Line path
        val strokePath = Path()
        values.forEachIndexed { i, v ->
            val x = xAt(i)
            val y = yAt(v)
            if (i == 0) strokePath.moveTo(x, y) else strokePath.lineTo(x, y)
        }
        drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Points
        values.forEachIndexed { i, v ->
            val x = xAt(i)
            val y = yAt(v)
            val isLast = i == values.size - 1
            val radius = if (isLast) 4.5.dp.toPx() else 3.dp.toPx()
            val pointColor = if (isLast) ImperialGold else lineColor

            drawCircle(
                color = pointColor,
                radius = radius,
                center = Offset(x, y)
            )
            drawCircle(
                color = SlateGroove,
                radius = radius * 0.5f,
                center = Offset(x, y)
            )
        }
    }
}
