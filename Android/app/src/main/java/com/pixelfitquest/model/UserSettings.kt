package com.pixelfitquest.model

data class UserSettings(
    val height: Int = 178,
    val armLength: Float? = null,
    val musicVolume: Int = 50  // NEW: 0-100 for volume percentage, default 50%
)