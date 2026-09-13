package com.pixelfitquest.components.atoms

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Hybrid home-character motion prototype (#141): single still frame +
 * InfiniteTransition bob (vertical) and optional horizontal slide.
 * Avoids multi-frame walk sprite sheets for v0 motion feel.
 */
@Composable
fun PixelCharacterMotion(
    bitmap: ImageBitmap,
    modifier: Modifier = Modifier,
    bobAmplitudeDp: Dp = 4.dp,
    bobDurationMs: Int = 900,
    walkEnabled: Boolean = true,
    walkSpeed: Float = 1f,
    walkAmplitudeDp: Dp = 14.dp,
    /** When set, draws only this source width from x=0 (idle sheet frame 0). */
    frameWidthPx: Float? = null,
    frameHeightPx: Float? = null,
) {
    val motionModifier = characterMotionModifier(
        modifier = modifier,
        bobAmplitudeDp = bobAmplitudeDp,
        bobDurationMs = bobDurationMs,
        walkEnabled = walkEnabled,
        walkSpeed = walkSpeed,
        walkAmplitudeDp = walkAmplitudeDp,
    )

    val srcW = (frameWidthPx ?: bitmap.width.toFloat()).roundToInt().coerceAtLeast(1)
    val srcH = (frameHeightPx ?: bitmap.height.toFloat()).roundToInt().coerceAtLeast(1)

    Canvas(modifier = motionModifier) {
        drawImage(
            image = bitmap,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(srcW, srcH),
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
        )
    }
}

/**
 * Painter overload for a true single-frame drawable (not a strip).
 */
@Composable
fun PixelCharacterMotion(
    painter: Painter,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    bobAmplitudeDp: Dp = 4.dp,
    bobDurationMs: Int = 900,
    walkEnabled: Boolean = true,
    walkSpeed: Float = 1f,
    walkAmplitudeDp: Dp = 14.dp,
) {
    val motionModifier = characterMotionModifier(
        modifier = modifier,
        bobAmplitudeDp = bobAmplitudeDp,
        bobDurationMs = bobDurationMs,
        walkEnabled = walkEnabled,
        walkSpeed = walkSpeed,
        walkAmplitudeDp = walkAmplitudeDp,
    )

    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = motionModifier,
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun characterMotionModifier(
    modifier: Modifier,
    bobAmplitudeDp: Dp,
    bobDurationMs: Int,
    walkEnabled: Boolean,
    walkSpeed: Float,
    walkAmplitudeDp: Dp,
): Modifier {
    val density = LocalDensity.current
    val bobAmpPx = with(density) { bobAmplitudeDp.toPx() }
    val walkAmpPx = with(density) { walkAmplitudeDp.toPx() }

    val transition = rememberInfiniteTransition(label = "pixelCharacterMotion")

    val bobOffset by transition.animateFloat(
        initialValue = -bobAmpPx,
        targetValue = bobAmpPx,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = bobDurationMs.coerceAtLeast(1),
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bob",
    )

    // walkSpeed 1f ≈ 2000ms for a full left↔right cycle; higher = faster.
    val walkCycleMs = ((2000f / walkSpeed.coerceAtLeast(0.05f))).toInt().coerceAtLeast(1)
    val slideOffset by transition.animateFloat(
        initialValue = -walkAmpPx,
        targetValue = walkAmpPx,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = walkCycleMs,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "slide",
    )

    return modifier.graphicsLayer {
        translationY = bobOffset
        translationX = if (walkEnabled) slideOffset else 0f
    }
}
