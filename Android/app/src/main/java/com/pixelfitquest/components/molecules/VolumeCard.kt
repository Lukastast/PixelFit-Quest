package com.pixelfitquest.components.molecules

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.pixelfitquest.R
import com.pixelfitquest.ui.theme.LocalSpacing

@Composable
fun VolumeCard(
    musicVolume: Int,
    onVolumeChange: (Int) -> Unit
) {
    val spacing = LocalSpacing.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(spacing.cardHeight)
            .padding(start = spacing.xl, end = spacing.xl, bottom = spacing.xs)
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.info_background_higher),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        // Content on top (title, percentage, +/- buttons)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.md, vertical = spacing.sm)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Volume",
                    color = Color.White
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                IconButton(
                    onClick = {
                        val newVolume = (musicVolume - 10).coerceAtLeast(0)
                        onVolumeChange(newVolume)
                    },
                    modifier = Modifier.size(spacing.lg)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease Volume",
                        tint = Color.White
                    )
                }
                Text(
                    text = "${musicVolume}%",
                    color = Color.White
                )
                IconButton(
                    onClick = {
                        val newVolume = (musicVolume + 10).coerceAtMost(100)
                        onVolumeChange(newVolume)
                    },
                    modifier = Modifier.size(spacing.lg)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase Volume",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
