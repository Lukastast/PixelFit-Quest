package com.pixelfitquest.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun PixelArtButton(
    onClick: () -> Unit,
    imageRes: Int,  // Normal state PNG
    pressedRes: Int = imageRes,  // Optional pressed PNG
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = { }  // Optional content like Text
) {
    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100L)  // Brief delay for feedback
            isPressed = false
        }
    }

    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,  // Disable ripple/shadow
                role = Role.Button
            ) {
                isPressed = true
                onClick()
            }
            .clip(RoundedCornerShape(4.dp))  // Optional rounding
    ) {
        Image(
            painter = painterResource(id = if (isPressed) pressedRes else imageRes),
            contentDescription = "Button",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        // Optional content (e.g., text on button)
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}