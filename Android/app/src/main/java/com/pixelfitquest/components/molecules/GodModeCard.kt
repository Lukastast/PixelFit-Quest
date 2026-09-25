package com.pixelfitquest.components.molecules

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pixelfitquest.R
import com.pixelfitquest.ui.theme.LocalSpacing

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
    val activeColor = Color(0xFFFFD700)   // Gold when on
    val inactiveColor = Color.White.copy(alpha = 0.55f)
    val labelColor = if (enabled) activeColor else inactiveColor

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(spacing.cardHeight * 1.2f)
            .clickable { onToggle(!enabled) }
    ) {
        Image(
            painter = painterResource(id = R.drawable.info_background_higher),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.md, vertical = spacing.sm),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "⚡ God Mode",
                    color = labelColor,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (enabled) "All items unlocked (debug only)" else "Unlock all items for testing",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                )
            }
            Text(
                text = if (enabled) "ON" else "OFF",
                color = labelColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = spacing.xs),
            )
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = "God Mode",
                tint = labelColor,
            )
        }
    }
}
