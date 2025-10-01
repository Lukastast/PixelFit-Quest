package com.PixelFitQuest.Helpers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.pixelfitquest.ui.theme.typography
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TypewriterText(
    text: String,
    delayMs: Long = 100L,
    onComplete: () -> Unit = {}
){
    var displayedText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            displayedText = ""
            text.forEach { char ->
                displayedText = displayedText + char
                delay(delayMs)
            }
            onComplete()
        }
    }

    Text(
        text = displayedText,
        color = Color.White,
        style = typography.labelLarge,
        modifier = Modifier
            .fillMaxSize()
            /*.background(Color.Black) add background color if needed */
            .clickable {
            scope.cancel()
            displayedText = text
            onComplete()
        }

    )
}