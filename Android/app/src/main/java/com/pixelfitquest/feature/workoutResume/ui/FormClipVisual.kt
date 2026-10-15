package com.pixelfitquest.feature.workoutResume.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pixelfitquest.components.atoms.SpriteSheetPlayer
import com.pixelfitquest.feature.workoutResume.model.CoachingVisuals

/**
 * 16-bit SNES Pixel Art Visual for bench press form mistakes & reference posture.
 * Uses the authentic PixelFit Quest lifter avatar sprite sheets generated via the
 * pixelfit-avatar-art 10x block grid standard (8 frames @ 8 fps, 480x480 resolution).
 *
 * P1-3: decode only the resolved tag sheet, and [remember]-cache the bitmap so
 * recomposition does not re-decode ~7 MB ARGB coaching clips.
 */
@Composable
fun FormClipVisual(
    tag: String,
    modifier: Modifier = Modifier,
    isFemale: Boolean = false,
    size: Dp = 120.dp,
    fps: Int = 8,
) {
    val sheetRes = remember(tag, isFemale) {
        CoachingVisuals.resolveClipDrawable(tag, isFemale)
    }
    val resources = LocalContext.current.resources
    val sheet = remember(sheetRes) {
        ImageBitmap.imageResource(resources, sheetRes)
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        SpriteSheetPlayer(
            sheet = sheet,
            frameCount = 8,
            fps = fps,
            playing = true,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
