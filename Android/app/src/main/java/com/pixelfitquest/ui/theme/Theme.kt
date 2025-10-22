package com.pixelfitquest.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = QuestBlue,      // Health / vitality
    secondary = FireOrange,    // Energy / progress
    tertiary = RewardGold,     // Rewards / coins
    background = DarkStone,
    surface = VitalGreen,
    onPrimary = LightGray,
    onSecondary = white,
    onTertiary = DarkStone

)

private val LightColorScheme = lightColorScheme(
    primary = LightQuestBlue,
    secondary = LightOrange,
    tertiary = SoftGold,
    background = LightStone,
    surface = LightGreen,
    onPrimary = DarkText,
    onSecondary = white,
    onTertiary = DarkText
)

@Composable
fun PixelFitQuestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
