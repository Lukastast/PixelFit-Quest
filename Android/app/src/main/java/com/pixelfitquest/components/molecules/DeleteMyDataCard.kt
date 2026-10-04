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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelfitquest.R
import com.pixelfitquest.ui.theme.HeartRuby
import com.pixelfitquest.ui.theme.LocalSpacing
import com.pixelfitquest.ui.theme.SlateDeep
import com.pixelfitquest.ui.theme.determination

@Composable
fun DeleteMyDataCard(
    signedIn: Boolean,
    onDeleteMyData: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }
    val spacing = LocalSpacing.current
    val cancelFocus = remember { FocusRequester() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = spacing.scale(56))
            .clip(RoundedCornerShape(spacing.cornerSm))
            .background(SlateDeep.copy(alpha = 0.94f))
            .border(BorderStroke(1.5.dp, HeartRuby.copy(alpha = 0.45f)), RoundedCornerShape(spacing.cornerSm))
            .clickable { showDialog = true }
            .padding(horizontal = spacing.sm, vertical = spacing.xs)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(spacing.scale(36))
                    .clip(RoundedCornerShape(spacing.cornerXs))
                    .background(HeartRuby.copy(alpha = 0.12f))
                    .border(1.dp, HeartRuby.copy(alpha = 0.4f), RoundedCornerShape(spacing.cornerXs)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteForever,
                    contentDescription = null,
                    tint = HeartRuby,
                    modifier = Modifier.size(spacing.scale(18))
                )
            }
            Spacer(modifier = Modifier.width(spacing.sm))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.delete_my_data),
                    color = HeartRuby,
                    fontFamily = determination,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    lineHeight = 16.sp
                )
                Text(
                    text = stringResource(R.string.delete_my_data_subtitle),
                    color = HeartRuby.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(spacing.xs))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = HeartRuby.copy(alpha = 0.5f),
                modifier = Modifier.size(spacing.scale(12))
            )
        }
    }

    if (showDialog) {
        val body = buildString {
            append(stringResource(R.string.delete_my_data_body_local))
            append(" ")
            append(
                stringResource(
                    if (signedIn) {
                        R.string.delete_my_data_body_account
                    } else {
                        R.string.delete_my_data_body_no_account
                    }
                )
            )
        }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.delete_my_data_title)) },
            text = { Text(body) },
            // Cancel is the confirm slot so it is the default action.
            confirmButton = {
                TextButton(
                    onClick = { showDialog = false },
                    modifier = Modifier.focusRequester(cancelFocus),
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        onDeleteMyData()
                    }
                ) {
                    Text(stringResource(R.string.delete_my_data))
                }
            },
        )
        LaunchedEffect(Unit) {
            cancelFocus.requestFocus()
        }
    }
}
