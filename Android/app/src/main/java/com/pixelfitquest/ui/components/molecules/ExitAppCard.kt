package com.pixelfitquest.ui.components.molecules

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.pixelfitquest.R
import com.pixelfitquest.ui.components.atoms.PixelArtButton

@Composable
fun ExitAppCard(onSignOutClick: () -> Unit) {
    var showExitAppDialog by remember { mutableStateOf(false) }

    val cardTitle = stringResource(R.string.sign_out)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(32.dp, 0.dp, 32.dp, 8.dp)
            .clickable {
                showExitAppDialog = true
            }
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
                    text = cardTitle,
                    color = Color.White
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Sign Out",
                tint = Color.White
            )
        }
    }

    if (showExitAppDialog) {
        Dialog(
            onDismissRequest = { showExitAppDialog = false }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.questloginboard),
                    contentDescription = "Dialog Background",
                    modifier = Modifier.height(250.dp).width(500.dp),
                    contentScale = ContentScale.FillBounds
                )
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .heightIn(max = 300.dp)
                ) {
                    Text(stringResource(R.string.sign_out_title), color = Color.White)
                    Text(stringResource(R.string.sign_out_description), color = Color.White)
                    Row {
                        PixelArtButton(
                            onClick = { showExitAppDialog = false },
                            imageRes = R.drawable.button_unclicked,
                            pressedRes = R.drawable.button_clicked,
                            modifier = Modifier.height(50.dp).width(130.dp)
                        ) {
                            Text(stringResource(R.string.cancel), color = Color.Black)
                        }
                        PixelArtButton(
                            onClick = {
                                onSignOutClick()
                                showExitAppDialog = false
                            },
                            imageRes = R.drawable.button_unclicked,
                            pressedRes = R.drawable.button_clicked,
                            modifier = Modifier.height(50.dp).width(130.dp)
                        ) {
                            Text(stringResource(R.string.sign_out), color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}
