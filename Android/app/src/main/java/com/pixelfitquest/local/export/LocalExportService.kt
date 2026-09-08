package com.pixelfitquest.local.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.pixelfitquest.local.LocalPixelFitStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalExportService @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val store: LocalPixelFitStore,
) {
    suspend fun buildJson(): String = LocalExportFormatter.toJson(store.snapshotForExport())

    suspend fun buildCsv(): String = LocalExportFormatter.toCsv(store.snapshotForExport())

    suspend fun shareJson(activityContext: Context) {
        share(
            activityContext = activityContext,
            fileName = "pixelfit-export.json",
            contents = buildJson(),
            mimeType = "application/json",
            chooserTitle = "Export workout log (JSON)",
        )
    }

    suspend fun shareCsv(activityContext: Context) {
        share(
            activityContext = activityContext,
            fileName = "pixelfit-export.csv",
            contents = buildCsv(),
            mimeType = "text/csv",
            chooserTitle = "Export workout log (CSV)",
        )
    }

    private fun share(
        activityContext: Context,
        fileName: String,
        contents: String,
        mimeType: String,
        chooserTitle: String,
    ) {
        val dir = File(appContext.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, fileName)
        file.writeText(contents)
        val uri = FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.export",
            file,
        )
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(send, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        activityContext.startActivity(chooser)
    }
}
