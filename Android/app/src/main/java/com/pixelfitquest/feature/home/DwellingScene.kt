package com.pixelfitquest.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.pixelfitquest.components.atoms.DwellingCharacterSprite
import com.pixelfitquest.feature.home.model.CharacterPose
import com.pixelfitquest.feature.home.model.DwellingTier
import com.pixelfitquest.feature.home.model.DwellingVisuals

@Composable
fun DwellingScene(
    tier: DwellingTier,
    pose: CharacterPose,
    gender: String,
    variant: String,
    isLandscape: Boolean,
    onPoseCycle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgRes = if (isLandscape) {
        DwellingVisuals.landscapeBgRes(tier)
    } else {
        DwellingVisuals.portraitBgRes(tier)
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Layer 1: Dwelling background
        Image(
            painter = painterResource(id = bgRes),
            contentDescription = tier.displayName,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        // Layer 2: Interactive Character in dwelling
        val (anchorX, anchorY) = DwellingVisuals.characterAnchor(tier, pose, isLandscape)
        val interactionSource = remember { MutableInteractionSource() }

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = BiasAlignment(anchorX * 2f - 1f, anchorY * 2f - 1f)
        ) {
            DwellingCharacterSprite(
                pose = pose,
                gender = gender,
                variant = variant,
                isAnimating = true,
                modifier = Modifier
                    .fillMaxWidth(if (isLandscape) 0.28f else 0.44f)
                    .aspectRatio(1f)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                    ) {
                        onPoseCycle()
                    }
            )
        }

        // Layer 3: Furniture overlay (if any)
        DwellingVisuals.furnitureRes(tier, isLandscape)?.let { furnRes ->
            Image(
                painter = painterResource(id = furnRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
