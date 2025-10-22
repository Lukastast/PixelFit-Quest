package com.pixelfitquest.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.pixelfitquest.Helpers.TypewriterText
import com.pixelfitquest.R
import com.pixelfitquest.viewmodel.IntroViewModel
import kotlinx.coroutines.delay

@Composable
fun IntroScreen(
    navController: NavController,
    viewModel: IntroViewModel = hiltViewModel()
) {
    val delayAdvanceMs = 1000L // 1-second pause before auto-advance
    val pauseAfterTextMs = 500L // 0.5-second pause after text finishes
    val transitionDurationMs = 500L // Fade duration
    var currentSlide by remember { mutableStateOf(0) }
    var textFinished by remember { mutableStateOf(false) } // Track when text is done
    var showSlide by remember { mutableStateOf(true) } // Track fade state for slide
    val slides = listOf(
        Slide(R.drawable.slide1, "In a shadowed world, OBESITY grips 1 in 8. HEROES weaken, lost to distractions. Knowledge and gyms abound."),
        Slide(R.drawable.slide2, "Warriors can't track moves or stay motivated. Apps promise help, but none forge ADVENTURE. No quests. No glory."),
        Slide(R.drawable.slide3, "Welcome, brave hero! Behold PixelFit Quest! Level up through workouts. Your quest begins. Ready to fight?")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = showSlide,
            enter = fadeIn(animationSpec = tween(durationMillis = transitionDurationMs.toInt())),
            exit = fadeOut(animationSpec = tween(durationMillis = transitionDurationMs.toInt()))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        if (currentSlide < slides.size - 1) currentSlide++
                        else viewModel.onIntroComplete(navController)
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(150.dp))  // Space between image and text


                Image(
                    painter = painterResource(id = slides[currentSlide].imageRes),
                    contentDescription = slides[currentSlide].text,
                    modifier = Modifier
                        .size(300.dp, 200.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(16.dp))  // Space between image and text

                // Typewriter text below image
                TypewriterText(
                    text = slides[currentSlide].text,
                    delayMs = 100L,  // Slower typing for effect
                    onComplete = {
                        textFinished = true  // Mark text as finished
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // Handle auto-advance with delay after text finishes, including fade transition
        LaunchedEffect(currentSlide, slides.size, textFinished) {
            if (currentSlide < slides.size - 1 && textFinished) {
                delay(pauseAfterTextMs)  // Pause after text completes
                showSlide = false  // Fade out current slide
                delay(transitionDurationMs)  // Wait for fade out
                currentSlide++  // Advance slide
                showSlide = true  // Fade in next slide
                textFinished = false  // Reset for next slide
                delay(delayAdvanceMs)  // Original advance delay after transition
            } else if (currentSlide == slides.size - 1 && textFinished) {
                delay(pauseAfterTextMs)  // Pause after last slide text
                showSlide = false  // Fade out last slide
                delay(transitionDurationMs)  // Wait for fade out
                viewModel.onIntroComplete(navController)
                textFinished = false  // Reset
            }
        }
    }
}

data class Slide(
    val imageRes: Int,
    val text: String
)