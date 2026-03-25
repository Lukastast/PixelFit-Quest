package com.pixelfitquest.components.molecules

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import com.pixelfitquest.components.atoms.PixelArtButton

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DisplayNameCard(displayName: String, onUpdateDisplayNameClick: (String) -> Unit) {
    var showDisplayNameDialog by remember { mutableStateOf(false) }
    var newDisplayName by remember { mutableStateOf(displayName) }

    val cardTitle = displayName.ifBlank { stringResource(R.string.profile_name) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(32.dp, 0.dp, 32.dp, 8.dp)
            .clickable {
                newDisplayName = displayName
                showDisplayNameDialog = true
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
                Icons.Filled.Edit,
                contentDescription = "Edit",
                tint = Color.White
            )
        }
    }

    if (showDisplayNameDialog) {
        Dialog(
            onDismissRequest = { showDisplayNameDialog = false }
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
                        .fillMaxWidth(0.95f)
                        .heightIn(max = 300.dp)
                ) {
                    Text(stringResource(R.string.profile_name), color = Color.White)

                    Box(
                        modifier = Modifier
                            .width(280.dp)
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.inputfield),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                        TextField(
                            singleLine = true,
                            value = newDisplayName,
                            onValueChange = { newDisplayName = it },
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.96f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                errorContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                errorIndicatorColor = Color.Transparent
                            )
                        )
                    }

                    Row {
                        PixelArtButton(
                            onClick = { showDisplayNameDialog = false },
                            imageRes = R.drawable.button_unclicked,
                            pressedRes = R.drawable.button_clicked,
                            modifier = Modifier.height(50.dp).width(130.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.cancel),
                                color = Color.Black
                            )
                        }
                        PixelArtButton(
                            onClick = {
                                onUpdateDisplayNameClick(newDisplayName)
                                showDisplayNameDialog = false
                            },
                            imageRes = R.drawable.button_unclicked,
                            pressedRes = R.drawable.button_clicked,
                            modifier = Modifier.height(50.dp).width(130.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.update),
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
