package com.pixelfitquest.feature.intro

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.pixelfitquest.helpers.TypewriterText
import com.pixelfitquest.R
import com.pixelfitquest.feature.intro.model.Slide
import com.pixelfitquest.feature.intro.IntroViewModel
import com.pixelfitquest.ui.theme.spacing
import kotlinx.coroutines.delay

@Composable
fun IntroScreen(
    navController: NavController,
    viewModel: IntroViewModel = hiltViewModel()
) {
    val delayAdvanceMs = 1000L
    val pauseAfterTextMs = 500L
    val transitionDurationMs = 500L
    var currentSlide by remember { mutableStateOf(0) }
    var textFinished by remember { mutableStateOf(false) }
    var showSlide by remember { mutableStateOf(true) }
    val slides = listOf(
        Slide(
            R.drawable.slide1,
            R.string.intro_slide_1
        ),
        Slide(R.drawable.slide2, R.string.intro_slide_2),
        Slide(R.drawable.slide3, R.string.intro_slide_3)
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
            val spacing = MaterialTheme.spacing
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .clickable {
                        if (currentSlide < slides.size - 1) currentSlide++
                        else viewModel.onIntroComplete(navController)
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.4f))

                Image(
                    painter = painterResource(id = slides[currentSlide].imageRes),
                    contentDescription = stringResource(slides[currentSlide].textRes),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(1.5f),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(spacing.md))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = spacing.lg, vertical = spacing.sm),
                    contentAlignment = Alignment.TopCenter
                ) {
                    TypewriterText(
                        text = stringResource(slides[currentSlide].textRes),
                        delayMs = 100L,
                        onComplete = {
                            textFinished = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.weight(0.25f))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(MaterialTheme.spacing.md),
            contentAlignment = Alignment.BottomEnd
        ) {
            IconButton(
                onClick = { viewModel.onIntroComplete(navController) }
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = stringResource(R.string.skip_intro),
                    tint = Color.White,
                    modifier = Modifier.size(MaterialTheme.spacing.xxl)
                )
            }
        }


        LaunchedEffect(currentSlide, slides.size, textFinished) {
            if (currentSlide < slides.size - 1 && textFinished) {
                delay(pauseAfterTextMs)
                showSlide = false
                delay(transitionDurationMs)
                currentSlide++
                showSlide = true
                textFinished = false
                delay(delayAdvanceMs)
            } else if (currentSlide == slides.size - 1 && textFinished) {
                delay(pauseAfterTextMs)
                showSlide = false
                delay(transitionDurationMs)
                viewModel.onIntroComplete(navController)
                textFinished = false
            }
        }
    }
}
