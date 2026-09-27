package com.pixelfitquest.local.export.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.pixelfitquest.R
import com.pixelfitquest.components.atoms.PixelArtButton
import com.pixelfitquest.components.molecules.SettingsActionCard
import com.pixelfitquest.helpers.SnackbarManager
import com.pixelfitquest.local.export.ExportFormat
import com.pixelfitquest.local.export.LocalExportEvent
import com.pixelfitquest.local.export.LocalExportViewModel
import com.pixelfitquest.local.export.PreparedExport
import com.pixelfitquest.ui.theme.ImperialGold
import com.pixelfitquest.ui.theme.LocalSpacing
import com.pixelfitquest.ui.theme.SilverSlate
import com.pixelfitquest.ui.theme.SilverSteel
import com.pixelfitquest.ui.theme.SlateBorder
import com.pixelfitquest.ui.theme.SlateBorderSubtle
import com.pixelfitquest.ui.theme.SlateGroove
import com.pixelfitquest.ui.theme.TorchAmber
import com.pixelfitquest.ui.theme.determination

/**
 * Settings entry: one card opens a dialog with JSON / CSV / both,
 * Share (existing FileProvider) and Save (SAF CreateDocument).
 * Data is Room-backed via [com.pixelfitquest.local.export.LocalExportService].
 */
@Composable
fun LocalExportCard(
    viewModel: LocalExportViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val spacing = LocalSpacing.current
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
        modifier = modifier,
    ) {
        viewModel.refreshCounts()
        showDialog = true
    }

    if (showDialog) {
        Dialog(
            onDismissRequest = { showDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = spacing.scale(420))
                    .fillMaxWidth(0.92f)
                    .padding(horizontal = spacing.sm),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.questloginboard),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.FillBounds
                )
                Column(
                    modifier = Modifier
                        .padding(horizontal = spacing.xl, vertical = spacing.xl)
                        .heightIn(max = spacing.scale(380))
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    Text(
                        text = stringResource(R.string.export_dialog_title).uppercase(),
                        fontFamily = determination,
                        color = ImperialGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.export_dialog_subtitle),
                        color = SilverSlate,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )

                    // Recessed counts badge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(spacing.cornerXs))
                            .background(SlateGroove)
                            .border(1.dp, SlateBorder, RoundedCornerShape(spacing.cornerXs))
                            .padding(horizontal = spacing.sm, vertical = spacing.xxs),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(
                                R.string.export_counts,
                                state.workoutCount,
                                state.setCount,
                            ),
                            fontFamily = determination,
                            color = ImperialGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.xxs))

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
                        Text(
                            text = status,
                            fontFamily = determination,
                            color = TorchAmber,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.xxs))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PixelArtButton(
                            onClick = { viewModel.share(selectedFormat) },
                            imageRes = R.drawable.button_unclicked,
                            pressedRes = R.drawable.button_clicked,
                            modifier = Modifier
                                .height(spacing.scale(44))
                                .width(spacing.buttonWidthSm)
                        ) {
                            Text(
                                text = stringResource(R.string.export_action_share),
                                fontFamily = determination,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 11.sp
                            )
                        }
                        PixelArtButton(
                            onClick = { viewModel.save(selectedFormat) },
                            imageRes = R.drawable.button_unclicked,
                            pressedRes = R.drawable.button_clicked,
                            modifier = Modifier
                                .height(spacing.scale(44))
                                .width(spacing.buttonWidthSm)
                        ) {
                            Text(
                                text = stringResource(R.string.export_action_save),
                                fontFamily = determination,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 11.sp
                            )
                        }
                    }

                    PixelArtButton(
                        onClick = { showDialog = false },
                        imageRes = R.drawable.button_unclicked,
                        pressedRes = R.drawable.button_clicked,
                        modifier = Modifier
                            .height(spacing.scale(44))
                            .width(spacing.buttonWidthSm)
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                            fontFamily = determination,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 11.sp
                        )
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
    val spacing = LocalSpacing.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.cornerXs))
            .background(if (selected) SlateGroove else Color.Transparent)
            .border(
                1.dp,
                if (selected) ImperialGold else SlateBorderSubtle,
                RoundedCornerShape(spacing.cornerXs)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.sm, vertical = spacing.xs)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.xs)
        ) {
            Text(
                text = if (selected) "●" else "○",
                color = if (selected) ImperialGold else SilverSlate,
                fontSize = 12.sp
            )
            Text(
                text = label,
                fontFamily = determination,
                color = if (selected) ImperialGold else SilverSteel,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
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
