package com.pixelfitquest.feature.home.model

import com.pixelfitquest.R

object DwellingVisuals {
    fun portraitBgRes(tier: DwellingTier): Int = when (tier) {
        DwellingTier.TARP -> R.drawable.dwelling_tarp_portrait
        DwellingTier.TENT -> R.drawable.dwelling_tent_portrait
        DwellingTier.SHACK -> R.drawable.dwelling_shack_portrait
        DwellingTier.COTTAGE -> R.drawable.dwelling_cottage_portrait
        DwellingTier.CASTLE -> R.drawable.dwelling_castle_portrait
        DwellingTier.GYM -> R.drawable.dwelling_gym_landscape
    }

    fun landscapeBgRes(tier: DwellingTier): Int = when (tier) {
        DwellingTier.TARP -> R.drawable.dwelling_tarp_landscape
        DwellingTier.TENT -> R.drawable.dwelling_tent_landscape
        DwellingTier.SHACK -> R.drawable.dwelling_shack_landscape
        DwellingTier.COTTAGE -> R.drawable.dwelling_cottage_landscape
        DwellingTier.CASTLE -> R.drawable.dwelling_castle_landscape
        DwellingTier.GYM -> R.drawable.dwelling_gym_landscape
    }

    fun furnitureRes(tier: DwellingTier, isLandscape: Boolean): Int? = null

    /**
     * Character anchor (x, y) normalized to [0..1] range within the dwelling container.
     */
    fun characterAnchor(
        tier: DwellingTier,
        pose: CharacterPose,
        isLandscape: Boolean,
    ): Pair<Float, Float> {
        if (tier == DwellingTier.GYM) {
            return when (pose) {
                CharacterPose.STANDING -> Pair(0.32f, 0.88f)
                CharacterPose.SITTING -> Pair(0.64f, 0.66f)
                CharacterPose.LYING -> Pair(0.65f, 0.63f)
            }
        }
        return if (isLandscape) {
            when (tier) {
                DwellingTier.TARP -> when (pose) {
                    CharacterPose.STANDING -> Pair(0.60f, 0.85f)
                    CharacterPose.SITTING -> Pair(0.60f, 0.88f)
                    CharacterPose.LYING -> Pair(0.24f, 0.79f)
                }
                DwellingTier.TENT -> when (pose) {
                    CharacterPose.STANDING -> Pair(0.55f, 0.85f)
                    CharacterPose.SITTING -> Pair(0.55f, 0.88f)
                    CharacterPose.LYING -> Pair(0.12f, 0.62f)
                }
                DwellingTier.SHACK -> when (pose) {
                    CharacterPose.STANDING -> Pair(0.58f, 0.88f)
                    CharacterPose.SITTING -> Pair(0.60f, 0.95f)
                    CharacterPose.LYING -> Pair(0.12f, 0.63f)
                }
                DwellingTier.COTTAGE -> when (pose) {
                    CharacterPose.STANDING -> Pair(0.55f, 0.85f)
                    CharacterPose.SITTING -> Pair(0.55f, 0.88f)
                    CharacterPose.LYING -> Pair(0.12f, 0.62f)
                }
                DwellingTier.CASTLE -> when (pose) {
                    CharacterPose.STANDING -> Pair(0.55f, 0.85f)
                    CharacterPose.SITTING -> Pair(0.55f, 0.88f)
                    CharacterPose.LYING -> Pair(0.12f, 0.62f)
                }
                DwellingTier.GYM -> Pair(0.50f, 0.70f)
            }
        } else {
            when (tier) {
                DwellingTier.TARP -> when (pose) {
                    CharacterPose.STANDING -> Pair(0.50f, 0.87f)
                    CharacterPose.SITTING -> Pair(0.50f, 0.87f)
                    CharacterPose.LYING -> Pair(0.12f, 0.82f)
                }
                DwellingTier.TENT -> when (pose) {
                    CharacterPose.STANDING -> Pair(0.50f, 0.87f)
                    CharacterPose.SITTING -> Pair(0.50f, 0.87f)
                    CharacterPose.LYING -> Pair(0.15f, 0.77f)
                }
                DwellingTier.SHACK -> when (pose) {
                    CharacterPose.STANDING -> Pair(0.50f, 0.87f)
                    CharacterPose.SITTING -> Pair(0.50f, 0.87f)
                    CharacterPose.LYING -> Pair(0.18f, 0.69f)
                }
                DwellingTier.COTTAGE -> when (pose) {
                    CharacterPose.STANDING -> Pair(0.50f, 0.87f)
                    CharacterPose.SITTING -> Pair(0.50f, 0.87f)
                    CharacterPose.LYING -> Pair(0.18f, 0.68f)
                }
                DwellingTier.CASTLE -> when (pose) {
                    CharacterPose.STANDING -> Pair(0.50f, 0.87f)
                    CharacterPose.SITTING -> Pair(0.50f, 0.87f)
                    CharacterPose.LYING -> Pair(0.18f, 0.68f)
                }
                DwellingTier.GYM -> Pair(0.50f, 0.70f)
            }
        }
    }
}
