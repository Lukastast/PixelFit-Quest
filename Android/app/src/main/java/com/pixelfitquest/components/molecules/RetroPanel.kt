package com.pixelfitquest.components.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelfitquest.ui.theme.PlatinumWhite
import com.pixelfitquest.ui.theme.SilverSlate
import com.pixelfitquest.ui.theme.SlateBorder
import com.pixelfitquest.ui.theme.SlateBorderSubtle
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.SlateGroove
import com.pixelfitquest.ui.theme.determination

/**
 * Shared retro panel composable used on Home screen and in the workout screen overhaul (W2).
 * Renders a midnight-slate elevated container with a crisp pixel-art border.
 */
@Composable
fun RetroPanel(
    modifier: Modifier = Modifier,
    backgroundColor: Color = SlateDeep.copy(alpha = 0.94f),
    borderColor: Color = SlateBorder,
    borderWidth: Dp = 2.dp,
    shape: Shape = RoundedCornerShape(8.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .background(color = backgroundColor, shape = shape)
            .border(width = borderWidth, color = borderColor, shape = shape)
            .clip(shape)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            content = content
        )
    }
}

/**
 * Shared retro pill composable for HUD badges (streak, level, mount chip).
 * When clickable it reserves a 48dp touch target and shows the theme ripple on tap.
 */
@Composable
fun RetroPill(
    modifier: Modifier = Modifier,
    backgroundColor: Color = SlateDeep.copy(alpha = 0.94f),
    borderColor: Color = SlateBorder,
    borderWidth: Dp = 2.dp,
    shape: Shape = RoundedCornerShape(20.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .then(if (onClick != null) Modifier.heightIn(min = 48.dp) else Modifier)
            .background(color = backgroundColor, shape = shape)
            .border(width = borderWidth, color = borderColor, shape = shape)
            .clip(shape)
            .then(
                if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick)
                else Modifier
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

/**
 * Recessed stat card used inside RetroPanel for weekly counters (Workouts, Sets, etc.).
 */
@Composable
fun RetroStatBox(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = SlateGroove.copy(alpha = 0.85f),
    borderColor: Color = SlateBorderSubtle,
) {
    val shape = RoundedCornerShape(6.dp)
    Column(
        modifier = modifier
            .background(color = backgroundColor, shape = shape)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .clip(shape)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = value,
            fontFamily = determination,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PlatinumWhite,
            lineHeight = 20.sp,
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = SilverSlate,
            lineHeight = 12.sp,
        )
    }
}
