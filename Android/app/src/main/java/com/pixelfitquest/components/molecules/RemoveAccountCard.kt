package com.pixelfitquest.components.molecules

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.window.Dialog
import com.pixelfitquest.R
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.ui.theme.LocalSpacing

@Composable
fun RemoveAccountCard(
    onRemoveAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showRemoveAccDialog by remember { mutableStateOf(false) }

    val cardTitle = stringResource(R.string.delete_account)
    val spacing = LocalSpacing.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(spacing.cardHeight)
            .clickable {
                showRemoveAccDialog = true
            }
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
                modifier = Modifier
                    .widthIn(max = spacing.scale(420))
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.questloginboard),
                    contentDescription = "Dialog Background",
                    modifier = Modifier.fillMaxWidth(0.95f).height(spacing.dialogHeight),
                    contentScale = ContentScale.FillBounds
                )
                Column(
                    modifier = Modifier
                        .padding(spacing.xl)
                        .fillMaxWidth(0.95f)
                        .heightIn(max = spacing.scale(300))
                ) {
                    Text(stringResource(R.string.delete_account_title), color = Color.White)
                    Text(stringResource(R.string.delete_account_description), color = Color.White)
                    Row {
                        PixelArtButton(
                            onClick = { showRemoveAccDialog = false },
                            imageRes = R.drawable.button_unclicked,
                            pressedRes = R.drawable.button_clicked,
                            modifier = Modifier.height(spacing.scale(50)).width(spacing.buttonWidthSm)
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
                            modifier = Modifier.height(spacing.scale(50)).width(spacing.buttonWidthSm)
                        ) {
                            Text(stringResource(R.string.delete_account), color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}
