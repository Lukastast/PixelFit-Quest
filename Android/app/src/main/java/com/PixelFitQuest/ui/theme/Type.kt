package com.PixelFitQuest.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.PixelFitQuest.R // Adjust if your R is in a different package

// Custom FontFamily for Jacquard 24
val jacquard24 = FontFamily(
    Font(R.font.jacquard24regular, FontWeight.Normal) // References the TTF file (rename to jacquard24_regular.ttf if needed)
)

// Updated Typography with Jacquard 24
val typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default, // Keep default for body text
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // Add custom styles using Jacquard 24, e.g., for headings or labels
    displayLarge = TextStyle(
        fontFamily = jacquard24,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    titleLarge = TextStyle(
        fontFamily = jacquard24,
        fontWeight = FontWeight.Normal,
        fontSize = 64.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = jacquard24,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
    // Uncomment and customize other styles as needed
)