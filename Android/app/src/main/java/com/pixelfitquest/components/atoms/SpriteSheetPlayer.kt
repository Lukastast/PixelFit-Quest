package com.pixelfitquest.components.atoms

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import kotlinx.coroutines.delay

/**
 * Plays a horizontal sprite strip (equal-width frames).
 * Uses clipRect + translate (same path as [CharacterIdleAnimation]) so frame
 * edges never sample neighboring cells — avoids black flashes on faces (#168).
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
    // Prefer exact integer division when the sheet was authored to the bible
    // (sitting 3840/8, lying 2880/6, workout 3840/8). Fall back to rounded float.
    val frameWidth = if (sheet.width % count == 0) {
        sheet.width / count
    } else {
        (sheet.width.toFloat() / count).toInt().coerceAtLeast(1)
    }
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
        val dstW = size.width
        val dstH = size.height
        if (dstW <= 0f || dstH <= 0f || frameWidth <= 0 || frameHeight <= 0) return@Canvas
        val scaleX = dstW / frameWidth.toFloat()
        val scaleY = dstH / frameHeight.toFloat()
        val srcX = (frame * frameWidth).toFloat()
        // Scale then clip to one frame, then translate — nearest-neighbor via FilterQuality.None.
        scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset.Zero) {
            clipRect(left = 0f, top = 0f, right = frameWidth.toFloat(), bottom = frameHeight.toFloat()) {
                translate(left = -srcX, top = 0f) {
                    drawImage(
                        image = sheet,
                        filterQuality = FilterQuality.None,
                    )
                }
            }
        }
    }
}
