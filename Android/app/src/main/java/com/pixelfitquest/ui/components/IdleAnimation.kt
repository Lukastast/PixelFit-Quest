package com.pixelfitquest.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import com.pixelfitquest.R
import kotlinx.coroutines.delay

@Composable
fun IdleAnimation(
    modifier: Modifier = Modifier,
    gender: String = "female",
    isAnimating: Boolean = false
) {
    val spriteSheet = ImageBitmap.imageResource(
        if (gender == "male") R.drawable.character_male_idle else R.drawable.character_woman_idle
    )
    var currentFrame by remember { mutableStateOf(0) }
    val frameCount = 13  // Adjust to your number of frames
    val frameWidth = 6240f / frameCount  // Auto-calculate width based on new sheet width (6240 / frames)
    val frameHeight = 480f  // Full height of sheet (assuming single row)

    if (isAnimating) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(100L)  // Faster loop (50 FPS)
                currentFrame = (currentFrame + 1) % frameCount
            }
        }
    }

    Canvas(modifier = modifier) {
        val x = currentFrame * frameWidth
        clipRect(
            left = 0f,
            top = 0f,
            right = frameWidth,
            bottom = frameHeight
        ) {
            translate(left = -x, top = 0f) {
                drawImage(spriteSheet)
            }
        }
    }
}