package com.pixelfitquest.components.atoms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import com.pixelfitquest.R
import com.pixelfitquest.feature.home.model.CharacterPose

@Composable
fun DwellingCharacterSprite(
    pose: CharacterPose,
    gender: String,
    variant: String = "basic",
    modifier: Modifier = Modifier,
    isAnimating: Boolean = true,
) {
    val isFemale = gender.lowercase() in listOf("female", "woman", "character_woman_idle")

    when (pose) {
        CharacterPose.STANDING -> {
            CharacterIdleAnimation(
                gender = if (isFemale) "female" else "male",
                variant = variant,
                isAnimating = isAnimating,
                modifier = modifier,
            )
        }
        CharacterPose.SITTING -> {
            val sheetRes = if (isFemale) {
                R.drawable.character_woman_sitting
            } else {
                R.drawable.character_male_sitting
            }
            SpriteSheetPlayer(
                sheet = ImageBitmap.imageResource(sheetRes),
                frameCount = 8,
                fps = 8,
                playing = isAnimating,
                modifier = modifier,
            )
        }
        CharacterPose.LYING -> {
            val sheetRes = if (isFemale) {
                R.drawable.character_woman_lying
            } else {
                R.drawable.character_male_lying
            }
            SpriteSheetPlayer(
                sheet = ImageBitmap.imageResource(sheetRes),
                frameCount = 6,
                fps = 4,
                playing = isAnimating,
                modifier = modifier,
            )
        }
    }
}
