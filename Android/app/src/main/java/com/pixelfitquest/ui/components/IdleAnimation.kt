package com.pixelfitquest.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.imageResource
import com.pixelfitquest.R
import kotlinx.coroutines.delay

@Composable
fun IdleAnimation(
    modifier: Modifier = Modifier,
    gender: String = "female",
    isAnimating: Boolean = false
) {
    val spriteSheetId = when (gender) {
        "male" -> R.drawable.character_male_idle
        "female" -> R.drawable.character_woman_idle
        "character_male_idle" -> R.drawable.character_male_idle
        "character_woman_idle" -> R.drawable.character_woman_idle
        "locked_male" -> R.drawable.locked_male_character_idle
        "locked_woman" -> R.drawable.locked_woman_character_idle
        "fitness_character_male_idle" -> R.drawable.fitness_character_male_idle
        "fitness_character_woman_idle" -> R.drawable.fitness_character_woman_idle
        else -> R.drawable.character_woman_idle
    }

    val spriteSheet = ImageBitmap.imageResource(spriteSheetId)
    var currentFrame by remember { mutableStateOf(0) }
    val frameCount = 13
    val frameWidth = 6240f / frameCount
    val frameHeight = 480f

    if (isAnimating) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(100L)
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

@Composable
fun CharacterIdleAnimation(
    modifier: Modifier = Modifier,
    gender: String = "female",
    variant: String = "basic",
    isAnimating: Boolean = true
) {
    val spriteSheetId = when (variant) {
        "male_fitness" -> R.drawable.fitness_character_male_idle
        "female_fitness" -> R.drawable.fitness_character_woman_idle
        else -> {
            if (gender == "male") R.drawable.character_male_idle
            else R.drawable.character_woman_idle
        }
    }

    val spriteSheet = ImageBitmap.imageResource(spriteSheetId)
    var currentFrame by remember { mutableStateOf(0) }
    val frameCount = 13
    val frameWidth = 6240f / frameCount
    val frameHeight = 480f

    if (isAnimating) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(100L)
                currentFrame = (currentFrame + 1) % frameCount
            }
        }
    }

    Canvas(modifier = modifier) {
        val x = currentFrame * frameWidth
        clipRect(left = 0f, top = 0f, right = frameWidth, bottom = frameHeight) {
            translate(left = -x) {
                drawImage(spriteSheet)
            }
        }
    }
}