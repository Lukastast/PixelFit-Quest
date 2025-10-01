package com.PixelFitQuest.Helpers

import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
    modifier: Modifier = Modifier,
    style: TextStyle = typography.labelLarge,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = Color.White
) {
    var displayedText by remember { mutableStateOf("") }
    var job by remember { mutableStateOf<Job?>(null) }
    val context = LocalContext.current

    val soundPool = remember {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .build()
        SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()
    }

    var soundId by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        soundId = soundPool.load(context, R.raw.typewriter_blip, 1)
    }

    DisposableEffect(Unit) {
        onDispose {
            soundPool.release()
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
                if (soundId != 0) {
                    soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
                }
                delay(delayMs)
            }
            onComplete()
        }
    }

    Text(
        text = displayedText,
        color = color,
        style = style,
        textAlign = textAlign,
        modifier = modifier
            .clickable {
                job?.cancel() // Cancel only the typewriter job
                displayedText = text
                onComplete()
            }
    )
}