package com.pixelfitquest.feature.streak

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.pixelfitquest.R
import com.pixelfitquest.feature.streak.model.MAX_WEEKLY_TARGET
import com.pixelfitquest.feature.streak.model.MIN_WEEKLY_TARGET
import com.pixelfitquest.ui.theme.LocalSpacing

@Composable
fun WeeklyGoalCard(
    targetSessions: Int,
    onTargetChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(spacing.cardHeight)
    ) {
        Image(
            painter = painterResource(id = R.drawable.info_background_higher),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.md, vertical = spacing.sm)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.weekly_streak_target_label),
                    color = Color.White
                )
                Text(
                    text = stringResource(R.string.weekly_streak_target_hint),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                IconButton(
                    onClick = {
                        onTargetChange((targetSessions - 1).coerceAtLeast(MIN_WEEKLY_TARGET))
                    },
                    modifier = Modifier.size(spacing.scale(24))
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = stringResource(R.string.weekly_streak_decrease_goal),
                        tint = Color.White
                    )
                }
                Text(
                    text = "$targetSessions",
                    color = Color.White
                )
                IconButton(
                    onClick = {
                        onTargetChange((targetSessions + 1).coerceAtMost(MAX_WEEKLY_TARGET))
                    },
                    modifier = Modifier.size(spacing.scale(24))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.weekly_streak_increase_goal),
                        tint = Color.White
                    )
                }
            }
        }
    }
}
