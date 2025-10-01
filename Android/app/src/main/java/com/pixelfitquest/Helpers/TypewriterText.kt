package com.PixelFitQuest.Helpers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.pixelfitquest.ui.theme.typography
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TypewriterText(
    text: String,
    delayMs: Long = 100L,
    onComplete: () -> Unit = {}
) {
    var displayedText by remember { mutableStateOf("") }
    var job by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(Unit) {
        job?.cancel() // Cancel any existing job
        val stringBuilder = StringBuilder()
        job = launch {
            stringBuilder.clear()
            displayedText = ""
            text.forEach { char ->
                stringBuilder.append(char)
                displayedText = stringBuilder.toString()
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
            .clickable {
                job?.cancel() // Cancel only the typewriter job
                displayedText = text
                onComplete()
            }
    )
}