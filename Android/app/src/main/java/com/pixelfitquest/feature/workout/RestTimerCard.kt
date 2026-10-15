package com.pixelfitquest.feature.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.pixelfitquest.R
import com.pixelfitquest.ui.theme.LocalSpacing
import com.pixelfitquest.ui.theme.determination
import java.util.Locale

@Composable
fun RestTimerCard(
    remainingMs: Long,
    paused: Boolean,
    autostart: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStopAutostart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val totalSeconds = ((remainingMs + 999L) / 1000L).toInt()
    val label = String.format(Locale.US, "%d:%02d", totalSeconds / 60, totalSeconds % 60)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.78f), RoundedCornerShape(spacing.cornerMd))
            .padding(spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        Text(
            text = label,
            color = Color.White,
            fontFamily = determination,
            fontWeight = FontWeight.Bold,
            fontSize = 42.sp,
        )
        Text(
            text = stringResource(
                if (autostart) R.string.rest_timer_autostart_on else R.string.rest_timer_autostart_off,
            ),
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
            RestChip(
                text = stringResource(if (paused) R.string.rest_timer_resume else R.string.rest_timer_pause),
                onClick = if (paused) onResume else onPause,
            )
            if (autostart) {
                RestChip(
                    text = stringResource(R.string.rest_timer_stop_autostart),
                    onClick = onStopAutostart,
                )
            }
        }
    }
}

@Composable
private fun RestChip(text: String, onClick: () -> Unit) {
    val spacing = LocalSpacing.current
    Text(
        text = text,
        color = Color.White,
        fontFamily = determination,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        modifier = Modifier
            .background(Color(0xFF1565C0), RoundedCornerShape(spacing.cornerSm))
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.sm, vertical = spacing.xs),
    )
}
