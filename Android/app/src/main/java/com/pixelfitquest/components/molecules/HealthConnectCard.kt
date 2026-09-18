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
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.Icon
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
import com.pixelfitquest.health.HealthConnectStatus
import com.pixelfitquest.ui.theme.LocalSpacing

@Composable
fun HealthConnectCard(
    status: HealthConnectStatus,
    permissionsGranted: Boolean,
    onClick: () -> Unit,
) {
    val statusText = when (status) {
        HealthConnectStatus.AVAILABLE -> if (permissionsGranted) {
            stringResource(R.string.health_connect_status_connected)
        } else {
            stringResource(R.string.health_connect_status_not_granted)
        }
        HealthConnectStatus.UPDATE_REQUIRED ->
            stringResource(R.string.health_connect_status_update)
        HealthConnectStatus.UNAVAILABLE ->
            stringResource(R.string.health_connect_status_unavailable)
    }
    val clickable = status != HealthConnectStatus.UNAVAILABLE
    val spacing = LocalSpacing.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(spacing.cardHeight)
            .padding(start = spacing.xl, end = spacing.xl, bottom = spacing.xs)
            .clickable(enabled = clickable, onClick = onClick)
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
                    text = stringResource(R.string.health_connect_title),
                    color = Color.White
                )
                Text(
                    text = statusText,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp
                )
            }
            Icon(
                imageVector = Icons.Outlined.Favorite,
                contentDescription = stringResource(R.string.health_connect_title),
                tint = Color.White
            )
        }
    }
}
