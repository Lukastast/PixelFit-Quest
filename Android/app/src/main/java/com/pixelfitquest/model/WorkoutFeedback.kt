package com.pixelfitquest.model

import androidx.compose.ui.graphics.Color

enum class WorkoutFeedback(val text: String, val color: Color, val scale: Float = 1.5f) {
    PERFECT("Perfect!", Color.Green, 1.8f),
    GREAT("Great!", Color.Yellow, 1.4f),
    GOOD("Good!", color = Color(0xFFFFAB00), 1.2f),
    MISS("Miss!", Color.Red, 1.0f)
}