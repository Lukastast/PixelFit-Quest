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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelfitquest.R
import com.pixelfitquest.health.HealthConnectStatus

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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(32.dp, 0.dp, 32.dp, 8.dp)
            .clickable(enabled = clickable, onClick = onClick)
    ) {
        Image(
            painter = painterResource(id = R.drawable.info_background_higher),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
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
