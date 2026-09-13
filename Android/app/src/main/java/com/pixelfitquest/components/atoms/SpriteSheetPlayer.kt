package com.pixelfitquest.components.atoms

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Plays a horizontal sprite strip (equal-width frames).
 * Used for Gemini Cape Hero walk sheet (#141 / #142).
 */
@Composable
fun SpriteSheetPlayer(
    sheet: ImageBitmap,
    modifier: Modifier = Modifier,
    frameCount: Int,
    fps: Int = 10,
    playing: Boolean = true,
) {
    val count = frameCount.coerceAtLeast(1)
    val frameWidth = (sheet.width.toFloat() / count).roundToInt().coerceAtLeast(1)
    val frameHeight = sheet.height.coerceAtLeast(1)
    var frame by remember { mutableIntStateOf(0) }

    if (playing) {
        LaunchedEffect(count, fps) {
            val delayMs = (1000L / fps.coerceAtLeast(1)).coerceAtLeast(16L)
            while (true) {
                delay(delayMs)
                frame = (frame + 1) % count
            }
        }
    }

    Canvas(modifier = modifier) {
        val srcX = frame * frameWidth
        drawImage(
            image = sheet,
            srcOffset = IntOffset(srcX, 0),
            srcSize = IntSize(frameWidth, frameHeight),
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
            filterQuality = FilterQuality.None,
        )
    }
}
