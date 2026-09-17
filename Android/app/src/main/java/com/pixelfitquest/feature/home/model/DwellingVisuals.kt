package com.pixelfitquest.feature.home.model

import com.pixelfitquest.R

object DwellingVisuals {
    fun portraitBgRes(tier: DwellingTier): Int = when (tier) {
        DwellingTier.TARP -> R.drawable.dwelling_tarp_portrait
        DwellingTier.TENT -> R.drawable.dwelling_tent_portrait
        DwellingTier.SHACK -> R.drawable.dwelling_shack_portrait
        DwellingTier.COTTAGE -> R.drawable.dwelling_cottage_portrait
        DwellingTier.CASTLE -> R.drawable.dwelling_castle_portrait
    }

    fun landscapeBgRes(tier: DwellingTier): Int = when (tier) {
        DwellingTier.TARP -> R.drawable.dwelling_tarp_landscape
        DwellingTier.TENT -> R.drawable.dwelling_tent_landscape
        DwellingTier.SHACK -> R.drawable.dwelling_shack_landscape
        DwellingTier.COTTAGE -> R.drawable.dwelling_cottage_landscape
        DwellingTier.CASTLE -> R.drawable.dwelling_castle_landscape
    }

    fun furnitureRes(tier: DwellingTier, isLandscape: Boolean): Int? = when (tier) {
        DwellingTier.TARP -> null
        DwellingTier.TENT -> R.drawable.dwelling_tent_furniture
        DwellingTier.SHACK -> R.drawable.dwelling_shack_furniture
        DwellingTier.COTTAGE -> R.drawable.dwelling_cottage_furniture
        DwellingTier.CASTLE -> R.drawable.dwelling_castle_furniture
    }

    /**
     * Character anchor (x, y) normalized to [0..1] range within the dwelling container.
     */
    fun characterAnchor(
        tier: DwellingTier,
        pose: CharacterPose,
        isLandscape: Boolean,
    ): Pair<Float, Float> {
        return if (isLandscape) {
            when (pose) {
                CharacterPose.STANDING -> Pair(0.52f, 0.58f)
                CharacterPose.SITTING -> Pair(0.48f, 0.62f)
                CharacterPose.LYING -> Pair(0.55f, 0.64f)
            }
        } else {
            when (pose) {
                CharacterPose.STANDING -> Pair(0.50f, 0.60f)
                CharacterPose.SITTING -> Pair(0.46f, 0.63f)
                CharacterPose.LYING -> Pair(0.52f, 0.65f)
            }
        }
    }
}
