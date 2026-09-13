package com.pixelfitquest.feature.levels.cosmetics

import androidx.compose.ui.layout.ContentScale
import com.pixelfitquest.R
import com.pixelfitquest.feature.levels.model.CosmeticCatalog
import com.pixelfitquest.feature.levels.model.CosmeticDefinition
import com.pixelfitquest.feature.levels.model.CosmeticKind

data class HomeThemeSpec(
    val drawableRes: Int,
    val contentScale: ContentScale,
)

object HomeThemeVisuals {
    fun specFor(themeId: String): HomeThemeSpec = when (themeId) {
        CosmeticCatalog.HOME_GYM -> HomeThemeSpec(
            drawableRes = R.drawable.gym_background,
            contentScale = ContentScale.Crop,
        )
        CosmeticCatalog.HOME_EMBER -> HomeThemeSpec(
            drawableRes = R.drawable.home_theme_ember,
            contentScale = ContentScale.Crop,
        )
        CosmeticCatalog.HOME_DUSK -> HomeThemeSpec(
            drawableRes = R.drawable.home_theme_dusk_gym,
            contentScale = ContentScale.Crop,
        )
        CosmeticCatalog.HOME_NIGHT -> HomeThemeSpec(
            drawableRes = R.drawable.home_theme_night,
            contentScale = ContentScale.Crop,
        )
        CosmeticCatalog.HOME_LEGEND -> HomeThemeSpec(
            drawableRes = R.drawable.home_theme_legend,
            contentScale = ContentScale.Crop,
        )
        else -> HomeThemeSpec(
            drawableRes = R.drawable.logsigninbackground,
            contentScale = ContentScale.Crop,
        )
    }

    fun previewRes(definition: CosmeticDefinition): Int = when (definition.kind) {
        CosmeticKind.HOME_THEME -> specFor(definition.id).drawableRes
        CosmeticKind.CHARACTER_SKIN -> when (definition.id) {
            CosmeticCatalog.SKIN_FITNESS -> R.drawable.fitness_character_male_idle
            CosmeticCatalog.SKIN_SHADOW -> R.drawable.locked_male_character_idle
            else -> R.drawable.character_male_idle
        }
        CosmeticKind.TITLE -> when (definition.id) {
            CosmeticCatalog.TITLE_ADVENTURER -> R.drawable.achievement_silver_workout_10_times
            CosmeticCatalog.TITLE_CHAMPION,
            CosmeticCatalog.TITLE_LEGEND,
            -> R.drawable.achievement_gold_workout_50_times
            else -> R.drawable.achievement_bronze_workout_1_time
        }
    }

    fun xpBarDrawable(progressIndex: Int): Int = when (progressIndex) {
        1 -> R.drawable.xp_20_percent
        2 -> R.drawable.xp_40_percent
        3 -> R.drawable.xp_60_percent
        4 -> R.drawable.xp_80_percent
        5 -> R.drawable.xp_100_percent
        else -> R.drawable.xp_0_percent
    }
}
