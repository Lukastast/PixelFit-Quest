package com.pixelfitquest.components.molecules

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
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
import com.pixelfitquest.firebase.model.User

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
                        .fillMaxWidth(0.95f)
                        .heightIn(max = 300.dp)
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
}
