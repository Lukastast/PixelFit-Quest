package com.PixelFitQuest.Helpers

import android.media.MediaPlayer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.pixelfitquest.ui.theme.typography
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.pixelfitquest.R


@Composable
fun TypewriterText(
    text: String,
    delayMs: Long = 100L,
    onComplete: () -> Unit = {},
    modifier: Modifier,
    style: TextStyle,
    textAlign: TextAlign,
    color: Color
) {
    var displayedText by remember { mutableStateOf("") }
    var job by remember { mutableStateOf<Job?>(null) }
    val context = LocalContext.current
    val mediaPlayer = MediaPlayer.create(context, R.raw.typewriter_blip)

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

    LaunchedEffect(text) {
        job?.cancel() // Cancel any existing job
        val stringBuilder = StringBuilder()
        job = launch {
            stringBuilder.clear()
            displayedText = ""
            text.forEach { char ->
                stringBuilder.append(char)
                displayedText = stringBuilder.toString()
                mediaPlayer.seekTo(0)
                mediaPlayer.start()
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