package com.pixelfitquest.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pixelfitquest.R

// Custom FontFamily for Jacquard 24
/*val jacquard24 = FontFamily(
    Font(R.font.jacquard24regular, FontWeight.Normal) // References the TTF file (rename to jacquard24_regular.ttf if needed)
)*/

val determination = FontFamily(
    Font(R.font.determination, FontWeight.Normal)
)
val typography = Typography(



    bodyMedium = TextStyle(
        fontFamily = determination,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),

    bodyLarge = TextStyle(
        fontFamily = determination,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),

    displayLarge = TextStyle(
        //fontFamily = jacquard24,
        fontFamily = determination,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    titleLarge = TextStyle(
        //fontFamily = jacquard24,
        fontFamily = determination,
        fontWeight = FontWeight.Normal,
        fontSize = 64.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        //fontFamily = jacquard24,
        fontFamily = determination,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)