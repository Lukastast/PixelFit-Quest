package com.pixelfitquest.model

data class UserData(
    val height: Int = 178,
    val armLength: Float? = null,
    val musicVolume: Int = 50,
    val level: Int = 1,
    val coins: Int = 0,
    val exp: Int = 0,
    val streak: Int = 0
)