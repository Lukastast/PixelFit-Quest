package com.pixelfitquest.local.export.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.pixelfitquest.R
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.components.molecules.SettingsActionCard
import com.pixelfitquest.helpers.SnackbarManager
import com.pixelfitquest.local.export.ExportFormat
import com.pixelfitquest.local.export.LocalExportEvent
import com.pixelfitquest.local.export.LocalExportViewModel
import com.pixelfitquest.local.export.PreparedExport

/**
 * Settings entry: one card opens a dialog with JSON / CSV / both,
 * Share (existing FileProvider) and Save (SAF CreateDocument).
 * Data is Room-backed via [com.pixelfitquest.local.export.LocalExportService].
 */
@Composable
fun LocalExportCard(
    viewModel: LocalExportViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedFormat by remember { mutableStateOf(ExportFormat.JSON) }
    val saveSession = remember { SaveSession() }

    val saveDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        val remaining = writeNextSave(context, uri, saveSession.queue)
        saveSession.queue = remaining
        remaining.firstOrNull()?.let { saveSession.launch(it.fileName) }
    }
    saveSession.launch = { fileName -> saveDocument.launch(fileName) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LocalExportEvent.Shared -> {
                    SnackbarManager.showMessage(
                        context.getString(R.string.export_share_ready, event.fileCount)
                    )
                }
                is LocalExportEvent.Save -> {
                    val first = event.files.firstOrNull() ?: return@collect
                    saveSession.queue = event.files
                    saveDocument.launch(first.fileName)
                }
            }
        }
    }

    SettingsActionCard(
        title = stringResource(R.string.export_card_title),
        subtitle = stringResource(R.string.export_card_subtitle),
        icon = Icons.Filled.Share,
    ) {
        viewModel.refreshCounts()
        showDialog = true
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.questloginboard),
                    contentDescription = null,
                    modifier = Modifier
                        .height(360.dp)
                        .width(500.dp),
                    contentScale = ContentScale.FillBounds
                )
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(stringResource(R.string.export_dialog_title), color = Color.White)
                    Text(stringResource(R.string.export_dialog_subtitle), color = Color.White)
                    Text(
                        text = stringResource(
                            R.string.export_counts,
                            state.workoutCount,
                            state.setCount,
                        ),
                        color = Color.White
                    )
                    FormatOption(
                        label = stringResource(R.string.export_format_json),
                        selected = selectedFormat == ExportFormat.JSON,
                        onClick = { selectedFormat = ExportFormat.JSON }
                    )
                    FormatOption(
                        label = stringResource(R.string.export_format_csv),
                        selected = selectedFormat == ExportFormat.CSV,
                        onClick = { selectedFormat = ExportFormat.CSV }
                    )
                    FormatOption(
                        label = stringResource(R.string.export_format_both),
                        selected = selectedFormat == ExportFormat.BOTH,
                        onClick = { selectedFormat = ExportFormat.BOTH }
                    )
                    val status = when {
                        state.inProgress -> stringResource(R.string.export_preparing)
                        state.message != null -> state.message ?: ""
                        else -> ""
                    }
                    if (status.isNotEmpty()) {
                        Text(status, color = Color.White)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PixelArtButton(
                            onClick = { viewModel.share(selectedFormat) },
                            imageRes = R.drawable.button_unclicked,
                            pressedRes = R.drawable.button_clicked,
                            modifier = Modifier
                                .height(50.dp)
                                .width(130.dp)
                        ) {
                            Text(stringResource(R.string.export_action_share), color = Color.Black)
                        }
                        PixelArtButton(
                            onClick = { viewModel.save(selectedFormat) },
                            imageRes = R.drawable.button_unclicked,
                            pressedRes = R.drawable.button_clicked,
                            modifier = Modifier
                                .height(50.dp)
                                .width(130.dp)
                        ) {
                            Text(stringResource(R.string.export_action_save), color = Color.Black)
                        }
                    }
                    PixelArtButton(
                        onClick = { showDialog = false },
                        imageRes = R.drawable.button_unclicked,
                        pressedRes = R.drawable.button_clicked,
                        modifier = Modifier
                            .height(50.dp)
                            .width(130.dp)
                    ) {
                        Text(stringResource(R.string.cancel), color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun FormatOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = if (selected) "> $label" else "  $label",
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp)
    )
}

private class SaveSession {
    var queue: List<PreparedExport> = emptyList()
    var launch: (String) -> Unit = {}
}

internal fun writeNextSave(
    context: Context,
    uri: Uri?,
    queue: List<PreparedExport>,
): List<PreparedExport> {
    val current = queue.firstOrNull()
    if (uri == null || current == null) return emptyList()
    return try {
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(current.bytes)
        }
        val remaining = queue.drop(1)
        if (remaining.isEmpty()) {
            SnackbarManager.showMessage(context.getString(R.string.export_saved))
        }
        remaining
    } catch (e: Exception) {
        SnackbarManager.showMessage(
            e.message ?: context.getString(R.string.export_failed)
        )
        emptyList()
    }
}
