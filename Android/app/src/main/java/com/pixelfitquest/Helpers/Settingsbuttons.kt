package com.pixelfitquest.Helpers

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.pixelfitquest.R
import com.pixelfitquest.model.User
import com.pixelfitquest.ui.components.PixelArtButton

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DisplayNameCard(displayName: String, onUpdateDisplayNameClick: (String) -> Unit) {
    var showDisplayNameDialog by remember { mutableStateOf(false) }
    var newDisplayName by remember { mutableStateOf(displayName) }

    val cardTitle = displayName.ifBlank { stringResource(R.string.profile_name) }

    // Custom card with background image for display name
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
        // Background image
        Image(
            painter = painterResource(id = R.drawable.info_background_higher),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // Content on top
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
                    TextField(
                        value = newDisplayName,
                        onValueChange = { newDisplayName = it }
                    )
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AccountCenterCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onCardClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) { Text(title) }
            Icon(icon, contentDescription = "Icon")
        }
    }
}

fun Modifier.card(): Modifier {
    return this.padding(16.dp, 0.dp, 16.dp, 8.dp)
}

@Composable
fun ExitAppCard(onSignOutClick: () -> Unit) {
    var showExitAppDialog by remember { mutableStateOf(false) }

    val cardTitle = stringResource(R.string.sign_out)

    // Custom card with background image for exit app
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(32.dp, 0.dp, 32.dp, 8.dp)
            .clickable {
                showExitAppDialog = true
            }
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.info_background_higher),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // Content on top
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
                Icons.Filled.ExitToApp,
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
                        .heightIn(max = 300.dp)  // Shorter (max 300dp)
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

@Composable
fun GoogleLinkCard(
    user: User?,
    onLinkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLinkDialog by remember { mutableStateOf(false) }

    AccountCenterCard(
        stringResource(R.string.link_google_account),
        Icons.Filled.Link,
        modifier.card()
    ) {
        showLinkDialog = true
    }

    if (showLinkDialog) {
        Dialog(
            onDismissRequest = { showLinkDialog = false }
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
                        .fillMaxWidth(0.95f)  // Wider
                        .heightIn(max = 300.dp)  // Shorter
                ) {
                    Text(stringResource(R.string.link_google_title), color = Color.White)
                    Text(stringResource(R.string.link_google_description), color = Color.White)
                    Row {
                        Button(onClick = { showLinkDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                        Button(onClick = {
                            onLinkClick()
                            showLinkDialog = false
                        }) {
                            Text(stringResource(R.string.link_google))
                        }
                    }
                }
            }
        }
    }
    // TODO: Add Google linking logic
}

@Composable
fun RemoveAccountCard(onRemoveAccountClick: () -> Unit) {
    var showRemoveAccDialog by remember { mutableStateOf(false) }

    val cardTitle = stringResource(R.string.delete_account)

    // Custom card with background image for remove account
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(32.dp, 0.dp, 32.dp, 8.dp)
            .clickable {
                showRemoveAccDialog = true
            }
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.info_background_higher),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // Content on top
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
                Icons.Filled.Delete,
                contentDescription = "Delete",
                tint = Color.White
            )
        }
    }

    if (showRemoveAccDialog) {
        Dialog(
            onDismissRequest = { showRemoveAccDialog = false }
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
                        .fillMaxWidth(0.95f)  // Wider
                        .heightIn(max = 300.dp)  // Shorter
                ) {
                    Text(stringResource(R.string.delete_account_title), color = Color.White)
                    Text(stringResource(R.string.delete_account_description), color = Color.White)
                    Row {
                        PixelArtButton(
                            onClick = { showRemoveAccDialog = false },
                            imageRes = R.drawable.button_unclicked,
                            pressedRes = R.drawable.button_clicked,
                            modifier = Modifier.height(50.dp).width(130.dp)
                        ) {
                            Text(stringResource(R.string.cancel), color = Color.Black)
                        }
                        PixelArtButton(
                            onClick = {
                                onRemoveAccountClick()
                                showRemoveAccDialog = false
                            },
                            imageRes = R.drawable.button_unclicked,
                            pressedRes = R.drawable.button_clicked,
                            modifier = Modifier.height(50.dp).width(130.dp)
                        ) {
                            Text(stringResource(R.string.delete_account), color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

// NEW: VolumeCard composable (matches style of other cards: Box with background, 32.dp padding, white text, +/- icons)
@Composable
fun VolumeCard(
    musicVolume: Int,
    onVolumeChange: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(32.dp, 0.dp, 32.dp, 8.dp)
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.info_background_higher),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // Content on top (title, percentage, +/- buttons)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Volume",
                    color = Color.White
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        val newVolume = (musicVolume - 10).coerceAtLeast(0)
                        onVolumeChange(newVolume)
                    },
                    modifier = Modifier.size(24.dp)  // Smaller icons to fit Row
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease Volume",
                        tint = Color.White
                    )
                }
                Text(
                    text = "${musicVolume}%",
                    color = Color.White
                )
                IconButton(
                    onClick = {
                        val newVolume = (musicVolume + 10).coerceAtMost(100)
                        onVolumeChange(newVolume)
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase Volume",
                        tint = Color.White
                    )
                }
            }
        }
    }
}