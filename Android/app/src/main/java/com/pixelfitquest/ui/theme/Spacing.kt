package com.pixelfitquest.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

/**
 * Material 3 window-width classes. Compact covers phones in portrait;
 * Medium/Expanded cover large unfolded / tablet widths.
 */
enum class PixelFitWidthClass {
    Compact,
    Medium,
    Expanded;

    companion object {
        fun fromWidthDp(widthDp: Int): PixelFitWidthClass = when {
            widthDp < COMPACT_MAX_DP -> Compact
            widthDp < MEDIUM_MAX_DP -> Medium
            else -> Expanded
        }
    }
}

/** 360dp-wide compact phone — the original hardcoded layout target. */
const val PIXELFIT_DESIGN_WIDTH_DP = 360

const val COMPACT_MAX_DP = 600
const val MEDIUM_MAX_DP = 840

/**
 * Scale factor for spacing given the current window.
 *
 * Uses the **shortest** side so a landscape workout on a phone does not
 * jump to tablet spacing (width class is Expanded, but it is still a phone).
 * Compact phones interpolate around [PIXELFIT_DESIGN_WIDTH_DP].
 */
fun computeSpacingScale(widthDp: Int, heightDp: Int): Float {
    val shortest = min(widthDp, heightDp).coerceAtLeast(1)
    return when {
        shortest < COMPACT_MAX_DP ->
            (shortest.toFloat() / PIXELFIT_DESIGN_WIDTH_DP).coerceIn(0.88f, 1.18f)
        shortest < MEDIUM_MAX_DP -> 1.28f
        else -> 1.45f
    }
}

/**
 * Shared 8dp-based spacing tokens, already multiplied by the window scale.
 * At 360dp shortest-side the values match the previous hardcoded layout.
 */
data class PixelFitSpacing(
    val widthClass: PixelFitWidthClass,
    val scale: Float,
    val xxxs: Dp,
    val xxs: Dp,
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
    val xl: Dp,
    val xxl: Dp,
    val xxxl: Dp,
    val screen: Dp,
    val formGutter: Dp,
    val cardHeight: Dp,
    val barSm: Dp,
    val barMd: Dp,
    val barLg: Dp,
    val workoutRow: Dp,
    val missions: Dp,
    val inputHeight: Dp,
    val buttonHeight: Dp,
    val buttonWidthSm: Dp,
    val navBar: Dp,
    val navIcon: Dp,
    val dialogHeight: Dp,
    val cornerXs: Dp,
    val cornerSm: Dp,
    val cornerMd: Dp,
) {
    fun scale(dp: Int): Dp = (dp * scale).dp

    fun scale(dp: Float): Dp = (dp * scale).dp

    companion object {
        fun fromWindow(widthDp: Int, heightDp: Int): PixelFitSpacing {
            val scale = computeSpacingScale(widthDp, heightDp)
            fun s(value: Int): Dp = (value * scale).dp
            return PixelFitSpacing(
                widthClass = PixelFitWidthClass.fromWidthDp(widthDp),
                scale = scale,
                xxxs = s(2),
                xxs = s(4),
                xs = s(8),
                sm = s(12),
                md = s(16),
                lg = s(24),
                xl = s(32),
                xxl = s(48),
                xxxl = s(64),
                screen = s(16),
                formGutter = s(70),
                cardHeight = s(80),
                barSm = s(60),
                barMd = s(80),
                barLg = s(100),
                workoutRow = s(140),
                missions = s(250),
                inputHeight = s(60),
                buttonHeight = s(60),
                buttonWidthSm = s(130),
                navBar = s(80),
                navIcon = s(72),
                dialogHeight = s(250),
                cornerXs = s(4),
                cornerSm = s(8),
                cornerMd = s(12),
            )
        }
    }
}

val LocalSpacing = compositionLocalOf {
    PixelFitSpacing.fromWindow(PIXELFIT_DESIGN_WIDTH_DP, 800)
}

@Composable
fun rememberPixelFitSpacing(): PixelFitSpacing {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp
    val heightDp = configuration.screenHeightDp
    return remember(widthDp, heightDp) {
        PixelFitSpacing.fromWindow(widthDp, heightDp)
    }
}

val MaterialTheme.spacing: PixelFitSpacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current

/** Scale a one-off dp value against the current window spacing scale. */
@Composable
@ReadOnlyComposable
fun Dp.scaled(): Dp = this * LocalSpacing.current.scale
