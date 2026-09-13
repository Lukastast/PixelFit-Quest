package com.pixelfitquest.feature.levels

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.pixelfitquest.feature.levels.cosmetics.HomeThemeVisuals

/** Drop-in home background. HomeScreen should place this as the first child of its root Box. */
@Composable
fun HomeThemeBackdrop(
    themeId: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale? = null,
) {
    val spec = HomeThemeVisuals.specFor(themeId)
    Image(
        painter = painterResource(id = spec.drawableRes),
        contentDescription = null,
        modifier = modifier.fillMaxSize(),
        contentScale = contentScale ?: spec.contentScale,
    )
}
