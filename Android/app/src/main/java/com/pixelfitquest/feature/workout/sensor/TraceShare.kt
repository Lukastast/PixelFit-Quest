package com.pixelfitquest.feature.workout.sensor

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/** Share the single trace zip through the existing export FileProvider. */
object TraceShare {
    fun share(context: Context, file: File) {
        val authority = "${context.packageName}.export"
        val uri = FileProvider.getUriForFile(context, authority, file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "PixelFit IMU trace")
            clipData = ClipData.newRawUri("imu-trace", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Share IMU trace"))
    }
}
