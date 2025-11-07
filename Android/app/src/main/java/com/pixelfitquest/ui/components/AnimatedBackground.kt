package com.pixelfitquest.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.util.Log
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

/**
 * Reusable animated background composable for sprite sheet animations.
 * Assumes a horizontal strip layout (frames side-by-side).
 * Place this as the first child in a root Box(Modifier.fillMaxSize()) to overlay other UI.
 *
 * @param drawableRes The resource ID of the sprite sheet (e.g., R.drawable.menu_background_sheet).
 * @param numFrames Number of frames in the sheet (e.g., 14; verify via Logcat "BackgroundLoad").
 * @param frameDurationMs Milliseconds per frame (e.g., 150 for ~6.7 FPS; adjust for smoothness).
 * @param modifier Optional modifier (defaults to fillMaxSize() for full-screen backgrounds).
 */
@Composable
fun AnimatedBackground(
    drawableRes: Int,
    numFrames: Int = 14,  // Default to your 14-frame sheet
    frameDurationMs: Int = 150,  // Keeps it paced nicely
    modifier: Modifier = Modifier.fillMaxSize()
) {
    val context = LocalContext.current
    val bitmap = remember(drawableRes) {
        val loaded = BitmapFactory.decodeResource(context.resources, drawableRes)
        if (loaded != null) {
            Log.d("BackgroundLoad", "Loaded sprite sheet: ${loaded.width} x ${loaded.height} pixels")
            // Tip: Ensure width % numFrames == 0 for no jitter (e.g., 1792px / 14 = 128px/frame)
        }
        loaded
    }

    val infiniteTransition = rememberInfiniteTransition(label = "spriteAnimation")
    val animProgress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = numFrames.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(numFrames * frameDurationMs),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart  // Seamless loop
        ),
        label = "frameProgress"
    )

    // Pre-slice frames with uniform sizing to prevent jitter from uneven divisions
    val frames: List<ImageBitmap> = remember(bitmap, numFrames) {
        if (bitmap == null || bitmap.isRecycled || numFrames <= 0) {
            emptyList()
        } else {
            val totalWidth = bitmap.width
            val frameHeight = bitmap.height
            val baseFrameWidth = totalWidth / numFrames
            val remainder = totalWidth % numFrames  // Handles non-even divisions
            if (remainder != 0) {
                Log.w("BackgroundLoad", "Sheet width ($totalWidth) has remainder $remainder for $numFrames frames. Distributing evenly to avoid jitter.")
            }
            val frameBitmaps = mutableListOf<ImageBitmap>()
            var currentLeft = 0
            for (i in 0 until numFrames) {
                // Distribute remainder: Add 1px to first 'remainder' frames for even coverage
                val extraPx = if (i < remainder) 1 else 0
                val thisFrameWidth = baseFrameWidth + extraPx
                val srcRect = Rect(currentLeft, 0, currentLeft + thisFrameWidth, frameHeight)
                val frameBitmap = Bitmap.createBitmap(
                    bitmap,
                    srcRect.left,
                    srcRect.top,
                    srcRect.width(),
                    srcRect.height()
                )
                // ALWAYS rescale to uniform base size to eliminate any width-based shift
                val uniformFrame = Bitmap.createScaledBitmap(frameBitmap, baseFrameWidth, frameHeight, true)
                frameBitmaps.add(uniformFrame.asImageBitmap())
                currentLeft += thisFrameWidth
                // Recycle original slice to save memory
                if (frameBitmap != uniformFrame) frameBitmap.recycle()
            }
            frameBitmaps
        }
    }

    val currentFrameIndex = remember(animProgress.value) {
        (animProgress.value % numFrames).toInt().coerceAtMost(frames.size - 1)
    }

    // Render current frame, cropped to fill height & width (full coverage, centered)
    if (frames.isNotEmpty() && currentFrameIndex < frames.size) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center  // Ensures perfect centering within the bounds
        ) {
            Image(
                bitmap = frames[currentFrameIndex],
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop  // Full screen fill; crops proportionally to avoid bars/shifts
            )
        }
    } else {
        // Fallback: Transparent (no crash/blank screen)
        Box(modifier = modifier)
    }
}