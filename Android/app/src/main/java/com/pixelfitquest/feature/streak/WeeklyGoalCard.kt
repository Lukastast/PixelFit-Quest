package com.pixelfitquest.feature.streak

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelfitquest.R
import com.pixelfitquest.feature.streak.model.MAX_WEEKLY_TARGET
import com.pixelfitquest.feature.streak.model.MIN_WEEKLY_TARGET
import com.pixelfitquest.ui.theme.LocalSpacing
import com.pixelfitquest.ui.theme.SilverSlate
import com.pixelfitquest.ui.theme.SilverSteel
import com.pixelfitquest.ui.theme.SlateBorder
import com.pixelfitquest.ui.theme.SlateBorderSubtle
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.SlateGroove
import com.pixelfitquest.ui.theme.TorchAmber
import com.pixelfitquest.ui.theme.determination

@Composable
fun WeeklyGoalCard(
    targetSessions: Int,
    onTargetChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val canDecrease = targetSessions > MIN_WEEKLY_TARGET
    val canIncrease = targetSessions < MAX_WEEKLY_TARGET

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = spacing.scale(56))
            .clip(RoundedCornerShape(spacing.cornerSm))
            .background(SlateDeep.copy(alpha = 0.94f))
            .border(BorderStroke(1.5.dp, SlateBorder), RoundedCornerShape(spacing.cornerSm))
            .padding(horizontal = spacing.sm, vertical = spacing.xs)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Recessed streak flame icon well
            Box(
                modifier = Modifier
                    .size(spacing.scale(36))
                    .clip(RoundedCornerShape(spacing.cornerXs))
                    .background(SlateGroove)
                    .border(1.dp, SlateBorder, RoundedCornerShape(spacing.cornerXs)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.streak),
                    contentDescription = "Streak flame",
                    modifier = Modifier.size(spacing.scale(20))
                )
            }

            Spacer(modifier = Modifier.width(spacing.sm))

            // Title & Subtitle
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.weekly_streak_target_label),
                    color = SilverSteel,
                    fontFamily = determination,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    lineHeight = 16.sp
                )
                Text(
                    text = stringResource(R.string.weekly_streak_target_hint),
                    color = SilverSlate,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(spacing.xs))

            // Stepper controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.xxs)
            ) {
                // Minus button
                Box(
                    modifier = Modifier
                        .size(spacing.scale(28))
                        .clip(RoundedCornerShape(spacing.cornerXs))
                        .background(SlateGroove)
                        .border(
                            BorderStroke(
                                1.dp,
                                if (canDecrease) SlateBorder else SlateBorderSubtle.copy(alpha = 0.4f)
                            ),
                            RoundedCornerShape(spacing.cornerXs)
                        )
                        .clickable(enabled = canDecrease) {
                            onTargetChange((targetSessions - 1).coerceAtLeast(MIN_WEEKLY_TARGET))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = stringResource(R.string.weekly_streak_decrease_goal),
                        tint = if (canDecrease) SilverSteel else SilverSlate.copy(alpha = 0.35f),
                        modifier = Modifier.size(spacing.scale(16))
                    )
                }

                // Target days text
                Text(
                    text = "$targetSessions d",
                    fontFamily = determination,
                    color = TorchAmber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(min = spacing.scale(38))
                )

                // Plus button
                Box(
                    modifier = Modifier
                        .size(spacing.scale(28))
                        .clip(RoundedCornerShape(spacing.cornerXs))
                        .background(SlateGroove)
                        .border(
                            BorderStroke(
                                1.dp,
                                if (canIncrease) SlateBorder else SlateBorderSubtle.copy(alpha = 0.4f)
                            ),
                            RoundedCornerShape(spacing.cornerXs)
                        )
                        .clickable(enabled = canIncrease) {
                            onTargetChange((targetSessions + 1).coerceAtMost(MAX_WEEKLY_TARGET))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.weekly_streak_increase_goal),
                        tint = if (canIncrease) SilverSteel else SilverSlate.copy(alpha = 0.35f),
                        modifier = Modifier.size(spacing.scale(16))
                    )
                }
            }
        }
    }
}
