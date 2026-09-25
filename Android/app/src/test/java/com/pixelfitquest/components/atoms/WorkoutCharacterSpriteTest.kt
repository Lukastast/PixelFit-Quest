package com.pixelfitquest.components.atoms

import com.pixelfitquest.R
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutCharacterSpriteTest {

    @Test
    fun resolveWorkoutSpriteSheet_maleBasic_resolvesBaseMaleClassicWorkout() {
        val res = resolveWorkoutSpriteSheet(gender = "male", variant = "basic")
        assertEquals(R.drawable.character_male_workout, res)
    }

    @Test
    fun resolveWorkoutSpriteSheet_maleDefaultVariant_resolvesBaseMaleClassicWorkout() {
        val res = resolveWorkoutSpriteSheet(gender = "male")
        assertEquals(R.drawable.character_male_workout, res)
    }

    @Test
    fun resolveWorkoutSpriteSheet_maleShadow_resolvesBaseMaleWorkout() {
        val res = resolveWorkoutSpriteSheet(gender = "male", variant = "shadow")
        assertEquals(R.drawable.character_male_workout, res)
    }

    @Test
    fun resolveWorkoutSpriteSheet_maleFitness_resolvesFitnessMaleWorkout() {
        val res1 = resolveWorkoutSpriteSheet(gender = "male", variant = "fitness")
        assertEquals(R.drawable.fitness_character_male_workout, res1)

        val res2 = resolveWorkoutSpriteSheet(gender = "male", variant = "male_fitness")
        assertEquals(R.drawable.fitness_character_male_workout, res2)
    }

    @Test
    fun resolveWorkoutSpriteSheet_femaleBasic_resolvesBaseFemaleWorkout() {
        val res = resolveWorkoutSpriteSheet(gender = "female", variant = "basic")
        assertEquals(R.drawable.character_woman_workout, res)

        val resAlias = resolveWorkoutSpriteSheet(gender = "woman", variant = "basic")
        assertEquals(R.drawable.character_woman_workout, resAlias)
    }

    @Test
    fun resolveWorkoutSpriteSheet_femaleFitness_resolvesFitnessFemaleWorkout() {
        val res1 = resolveWorkoutSpriteSheet(gender = "female", variant = "fitness")
        assertEquals(R.drawable.fitness_character_woman_workout, res1)

        val res2 = resolveWorkoutSpriteSheet(gender = "female", variant = "female_fitness")
        assertEquals(R.drawable.fitness_character_woman_workout, res2)
    }

    @Test
    fun benchProjection_landscape_alignsWithBackgroundRackHooks() {
        // Landscape phone 2400x1080
        val screenW = 2400f
        val screenH = 1080f
        val bgW = 1376f
        val bgH = 768f

        val scale = maxOf(screenW / bgW, screenH / bgH)
        val bgLeft = (screenW - bgW * scale) / 2f
        val bgTop = (screenH - bgH * scale) / 2f

        val frameX = bgLeft + 580f * scale
        val frameY = bgTop + 230f * scale

        // Background left hook is at (672, 395)
        val bgHookLeftX = bgLeft + 672f * scale
        val bgHookLeftY = bgTop + 395f * scale

        // Frame left hook is at (92, 165)
        val frameHookLeftX = frameX + 92f * scale
        val frameHookLeftY = frameY + 165f * scale

        assertEquals(bgHookLeftX, frameHookLeftX, 0.001f)
        assertEquals(bgHookLeftY, frameHookLeftY, 0.001f)
    }
}
