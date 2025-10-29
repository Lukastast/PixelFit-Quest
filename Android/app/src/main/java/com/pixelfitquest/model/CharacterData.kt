package com.pixelfitquest.model

import androidx.compose.ui.graphics.Color

data class CharacterData(
    val gender: String = "male"
) {
    constructor() : this(gender = "male")
}
