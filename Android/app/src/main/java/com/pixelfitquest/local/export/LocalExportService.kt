package com.pixelfitquest.local.export

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.pixelfitquest.R
import com.pixelfitquest.local.LocalPixelFitStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalExportService @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val store: LocalPixelFitStore,
) {
    suspend fun buildJson(): String = LocalExportFormatter.toJson(store.snapshotForExport())

    suspend fun buildCsv(): String = LocalExportFormatter.toCsv(store.snapshotForExport())

    suspend fun counts(): ExportCounts {
        val snapshot = store.snapshotForExport()
        return ExportCounts(
            workoutCount = snapshot.workouts.size,
            setCount = snapshot.workouts.sumOf { workout ->
                workout.exercises.sumOf { it.sets.size }
            },
        )
    }

    /**
     * Builds cache files for the selected format(s) from the Room-backed snapshot.
     * Uses the existing FileProvider authority `${applicationId}.export` and
     * cache-path `exports/` — do not register a second provider.
     */
    suspend fun prepareExports(format: ExportFormat): List<PreparedExport> {
        val snapshot = store.snapshotForExport()
        val stamp = fileStamp(Date())
        val dir = File(appContext.cacheDir, CACHE_DIR).apply { mkdirs() }
        val prepared = mutableListOf<PreparedExport>()
        val formats = format.expanded()
        if (ExportFormat.JSON in formats) {
            val name = "pixelfit-export-$stamp.json"
            val bytes = LocalExportFormatter.toJson(snapshot).toByteArray(Charsets.UTF_8)
            val file = File(dir, name).apply { writeBytes(bytes) }
            prepared += PreparedExport(name, MIME_JSON, bytes, file)
        }
        if (ExportFormat.CSV in formats) {
            val name = "pixelfit-export-$stamp.csv"
            val bytes = LocalExportFormatter.toCsv(snapshot).toByteArray(Charsets.UTF_8)
            val file = File(dir, name).apply { writeBytes(bytes) }
            prepared += PreparedExport(name, MIME_CSV, bytes, file)
        }
        return prepared
    }

    fun sharePrepared(activityContext: Context, files: List<PreparedExport>) {
        if (files.isEmpty()) return
        val authority = "${appContext.packageName}.export"
        val uris = ArrayList<Uri>(files.size)
        for (prepared in files) {
            val file = prepared.cacheFile ?: continue
            if (!file.exists()) file.writeBytes(prepared.bytes)
            uris += FileProvider.getUriForFile(appContext, authority, file)
        }
        if (uris.isEmpty()) return

        val send = if (uris.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                type = files.first().mimeType
                putExtra(Intent.EXTRA_STREAM, uris.first())
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    appContext.getString(R.string.export_share_subject),
                )
                attachExportUris(uris)
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "*/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    appContext.getString(R.string.export_share_subject),
                )
                attachExportUris(uris)
            }
        }

        val chooser = Intent.createChooser(
            send,
            appContext.getString(R.string.export_share_title),
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        activityContext.startActivity(chooser)
    }

    fun saveToUri(uri: Uri, prepared: PreparedExport) {
        appContext.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(prepared.bytes)
        } ?: error("Could not open output stream")
    }

    /** Convenience wrappers kept for simple one-shot share callers. */
    suspend fun shareJson(activityContext: Context) {
        val files = prepareExports(ExportFormat.JSON)
        sharePrepared(activityContext, files)
    }

    suspend fun shareCsv(activityContext: Context) {
        val files = prepareExports(ExportFormat.CSV)
        sharePrepared(activityContext, files)
    }

    private fun Intent.attachExportUris(uris: List<Uri>) {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val first = uris.firstOrNull() ?: return
        val clip = ClipData.newRawUri("export", first)
        for (index in 1 until uris.size) {
            clip.addItem(ClipData.Item(uris[index]))
        }
        clipData = clip
    }

    companion object {
        const val CACHE_DIR = "exports"
        const val MIME_JSON = "application/json"
        const val MIME_CSV = "text/csv"

        fun fileStamp(date: Date): String {
            val format = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            return format.format(date)
        }
    }
}
