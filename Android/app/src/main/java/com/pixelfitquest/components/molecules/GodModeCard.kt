package com.pixelfitquest.components.molecules

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelfitquest.ui.theme.ImperialGold
import com.pixelfitquest.ui.theme.LocalSpacing
import com.pixelfitquest.ui.theme.SilverSlate
import com.pixelfitquest.ui.theme.SilverSteel
import com.pixelfitquest.ui.theme.SlateBorder
import com.pixelfitquest.ui.theme.SlateBorderSubtle
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.SlateGroove
import com.pixelfitquest.ui.theme.determination

/**
 * Debug-only settings card that toggles God Mode.
 * When God Mode is active, all customization items are shown as unlocked.
 *
 * Only rendered in debug builds — gate it with [BuildConfig.DEBUG] at the call site.
 */
@Composable
fun GodModeCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = spacing.scale(56))
            .clip(RoundedCornerShape(spacing.cornerSm))
            .background(SlateDeep.copy(alpha = 0.94f))
            .border(
                BorderStroke(1.5.dp, if (enabled) ImperialGold.copy(alpha = 0.7f) else SlateBorder),
                RoundedCornerShape(spacing.cornerSm)
            )
            .clickable { onToggle(!enabled) }
            .padding(horizontal = spacing.sm, vertical = spacing.xs)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Recessed icon well
            Box(
                modifier = Modifier
                    .size(spacing.scale(36))
                    .clip(RoundedCornerShape(spacing.cornerXs))
                    .background(if (enabled) ImperialGold.copy(alpha = 0.12f) else SlateGroove)
                    .border(
                        1.dp,
                        if (enabled) ImperialGold.copy(alpha = 0.6f) else SlateBorder,
                        RoundedCornerShape(spacing.cornerXs)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = "God Mode",
                    tint = if (enabled) ImperialGold else SilverSlate,
                    modifier = Modifier.size(spacing.scale(18))
                )
            }

            Spacer(modifier = Modifier.width(spacing.sm))

            // Title & Subtitle
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "⚡ God Mode",
                    color = if (enabled) ImperialGold else SilverSteel,
                    fontFamily = determination,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    lineHeight = 16.sp
                )
                Text(
                    text = if (enabled) "All items unlocked (debug)" else "Unlock all items for testing",
                    color = SilverSlate,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(spacing.xs))

            // Pill badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(spacing.cornerXs))
                    .background(if (enabled) ImperialGold.copy(alpha = 0.18f) else SlateGroove)
                    .border(
                        1.dp,
                        if (enabled) ImperialGold.copy(alpha = 0.7f) else SlateBorderSubtle,
                        RoundedCornerShape(spacing.cornerXs)
                    )
                    .padding(horizontal = spacing.sm, vertical = spacing.scale(4)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (enabled) "ON" else "OFF",
                    color = if (enabled) ImperialGold else SilverSlate,
                    fontFamily = determination,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}
