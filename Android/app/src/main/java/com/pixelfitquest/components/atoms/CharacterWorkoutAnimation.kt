package com.pixelfitquest.components.atoms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import com.pixelfitquest.R

/**
 * Resolves the appropriate 8-frame workout sprite sheet based on character gender and customization variant.
 * - Basic Wanderer male -> R.drawable.character_male_workout (authentic classic bald Wanderer)
 * - Fitness male -> R.drawable.fitness_character_male_workout (athletic headband)
 * - Basic Wanderer female -> R.drawable.character_woman_workout (ponytail)
 * - Fitness female -> R.drawable.fitness_character_woman_workout (athletic headband + ponytail)
 */
fun resolveWorkoutSpriteSheet(gender: String, variant: String = "basic"): Int {
    val isFemale = gender.lowercase() in listOf("female", "woman", "character_woman_idle", "fitness_character_woman_idle")
    val isFitness = variant.contains("fitness")
    return when {
        isFemale && isFitness -> R.drawable.fitness_character_woman_workout
        isFemale -> R.drawable.character_woman_workout
        isFitness -> R.drawable.fitness_character_male_workout
        else -> R.drawable.character_male_workout
    }
}

/**
 * Animated sprite player for the workout bench press animation.
 * Features an 8-frame horizontal strip (480x480 per frame) matching
 * the retro pixel art style of PixelFit-Quest.
 */
@Composable
fun CharacterWorkoutAnimation(
    modifier: Modifier = Modifier,
    gender: String = "male",
    variant: String = "basic",
    isAnimating: Boolean = true,
    fps: Int = 8,
) {
    val sheetRes = resolveWorkoutSpriteSheet(gender, variant)

    SpriteSheetPlayer(
        sheet = ImageBitmap.imageResource(sheetRes),
        frameCount = 8,
        fps = fps,
        playing = isAnimating,
        modifier = modifier,
    )
}
