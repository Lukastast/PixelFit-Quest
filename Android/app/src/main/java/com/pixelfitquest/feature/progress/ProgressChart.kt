package com.pixelfitquest.feature.progress

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.hypot

data class ChartPoint(
    val xLabel: String,
    val value: Float,
    val markerLabel: String = "",
)

@Composable
fun ProgressLineChart(
    points: List<ChartPoint>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    yUnit: String = "",
    stub: Boolean = false,
) {
    var selectedIndex by remember(points) { mutableStateOf<Int?>(null) }
    val labelPaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 28f
            isAntiAlias = true
        }
    }
    val tickPaint = remember {
        Paint().apply {
            color = android.graphics.Color.argb(180, 255, 255, 255)
            textSize = 26f
            isAntiAlias = true
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(points) {
                detectTapGestures { tap ->
                    if (points.isEmpty()) return@detectTapGestures
                    selectedIndex = nearestPointIndex(tap, size.width.toFloat(), size.height.toFloat(), points)
                }
            },
    ) {
        val leftPad = 56.dp.toPx()
        val rightPad = 12.dp.toPx()
        val topPad = 16.dp.toPx()
        val bottomPad = 36.dp.toPx()
        val chartWidth = (size.width - leftPad - rightPad).coerceAtLeast(1f)
        val chartHeight = (size.height - topPad - bottomPad).coerceAtLeast(1f)
        val origin = Offset(leftPad, topPad + chartHeight)

        val values = points.map { it.value }
        val minVal = values.minOrNull() ?: 0f
        val maxVal = values.maxOrNull() ?: 1f
        val paddedMin = if (values.isEmpty()) 0f else (minVal * 0.9f).coerceAtLeast(0f)
        val paddedMax = if (values.isEmpty() || maxVal <= paddedMin) paddedMin + 1f else maxVal * 1.08f
        val range = (paddedMax - paddedMin).coerceAtLeast(1f)

        fun xFor(index: Int): Float {
            if (points.size <= 1) return leftPad + chartWidth / 2f
            return leftPad + chartWidth * index / (points.size - 1).toFloat()
        }

        fun yFor(value: Float): Float {
            val t = ((value - paddedMin) / range).coerceIn(0f, 1f)
            return topPad + chartHeight * (1f - t)
        }

        val gridColor = Color.White.copy(alpha = 0.12f)
        val tickCount = 4
        for (i in 0..tickCount) {
            val frac = i / tickCount.toFloat()
            val y = topPad + chartHeight * (1f - frac)
            drawLine(
                color = gridColor,
                start = Offset(leftPad, y),
                end = Offset(leftPad + chartWidth, y),
                strokeWidth = 1.dp.toPx(),
            )
            val tickValue = paddedMin + range * frac
            drawContext.canvas.nativeCanvas.drawText(
                formatAxisValue(tickValue, yUnit),
                4.dp.toPx(),
                y + 8.dp.toPx(),
                tickPaint,
            )
        }

        drawLine(
            color = Color.White.copy(alpha = 0.4f),
            start = origin,
            end = Offset(leftPad + chartWidth, origin.y),
            strokeWidth = 2.dp.toPx(),
        )
        drawLine(
            color = Color.White.copy(alpha = 0.4f),
            start = origin,
            end = Offset(leftPad, topPad),
            strokeWidth = 2.dp.toPx(),
        )

        if (points.isEmpty()) return@Canvas

        val path = Path()
        points.forEachIndexed { index, point ->
            val x = xFor(index)
            val y = yFor(point.value)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        val stroke = Stroke(
            width = 3.dp.toPx(),
            cap = StrokeCap.Round,
            pathEffect = if (stub) PathEffect.dashPathEffect(floatArrayOf(12f, 10f)) else null,
        )
        drawPath(path = path, color = lineColor.copy(alpha = if (stub) 0.45f else 1f), style = stroke)

        val highlight = selectedIndex
        points.forEachIndexed { index, point ->
            val x = xFor(index)
            val y = yFor(point.value)
            val radius = if (index == highlight) 6.dp.toPx() else 4.dp.toPx()
            drawCircle(color = lineColor.copy(alpha = if (stub) 0.5f else 1f), radius = radius, center = Offset(x, y))
            drawCircle(color = Color(0xFF1A1A1A), radius = radius * 0.4f, center = Offset(x, y))
        }

        val labelIndices = xLabelIndices(points.size)
        labelIndices.forEach { index ->
            val label = points[index].xLabel
            val x = xFor(index)
            drawContext.canvas.nativeCanvas.drawText(
                label,
                x - tickPaint.measureText(label) / 2f,
                size.height - 8.dp.toPx(),
                tickPaint,
            )
        }

        if (highlight != null && highlight in points.indices) {
            val point = points[highlight]
            val x = xFor(highlight)
            val y = yFor(point.value)
            val text = point.markerLabel.ifBlank { formatAxisValue(point.value, yUnit) }
            val textWidth = labelPaint.measureText(text)
            val tx = (x - textWidth / 2f).coerceIn(leftPad, size.width - textWidth - 4.dp.toPx())
            val ty = (y - 12.dp.toPx()).coerceAtLeast(topPad + 14.dp.toPx())
            drawContext.canvas.nativeCanvas.drawText(text, tx, ty, labelPaint)
        }
    }
}

private fun nearestPointIndex(
    tap: Offset,
    width: Float,
    height: Float,
    points: List<ChartPoint>,
): Int? {
    val leftPad = width * 0.16f
    val rightPad = width * 0.04f
    val topPad = height * 0.12f
    val bottomPad = height * 0.2f
    val chartWidth = (width - leftPad - rightPad).coerceAtLeast(1f)
    val chartHeight = (height - topPad - bottomPad).coerceAtLeast(1f)
    val values = points.map { it.value }
    val minVal = values.min()
    val maxVal = values.max()
    val paddedMin = (minVal * 0.9f).coerceAtLeast(0f)
    val paddedMax = if (maxVal <= paddedMin) paddedMin + 1f else maxVal * 1.08f
    val range = (paddedMax - paddedMin).coerceAtLeast(1f)

    var bestIndex = 0
    var bestDist = Float.MAX_VALUE
    points.forEachIndexed { index, point ->
        val x = if (points.size <= 1) leftPad + chartWidth / 2f else leftPad + chartWidth * index / (points.size - 1)
        val t = ((point.value - paddedMin) / range).coerceIn(0f, 1f)
        val y = topPad + chartHeight * (1f - t)
        val dist = hypot(tap.x - x, tap.y - y)
        if (dist < bestDist) {
            bestDist = dist
            bestIndex = index
        }
    }
    return bestIndex
}

private fun xLabelIndices(count: Int): List<Int> {
    if (count <= 0) return emptyList()
    if (count <= 5) return (0 until count).toList()
    return listOf(0, count / 2, count - 1).distinct()
}

internal fun formatAxisValue(value: Float, unit: String): String {
    val number = if (abs(value - value.toInt()) < 0.05f) {
        value.toInt().toString()
    } else {
        String.format("%.1f", value)
    }
    return if (unit.isBlank()) number else number
}

internal fun formatKg(value: Float): String {
    return if (abs(value - value.toInt()) < 0.05f) value.toInt().toString() else String.format("%.1f", value)
}
