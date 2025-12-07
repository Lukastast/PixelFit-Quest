package com.pixelfitquest.model.enums

import androidx.compose.ui.graphics.Color

enum class WorkoutFeedback(val text: String, val color: Color, val scale: Float = 1.5f) {
    PERFECT("Perfect!", Color.Blue, 1.6f),

    EXCELLENT("Excellent!", Color.Green, 1.4f),
    GREAT("Great!", Color.Yellow, 1.2f),
    GOOD("Good!", color = Color(0xFFFFAB00), 1.0f),
    MISS("Miss!", Color.Red, 1.0f)
}