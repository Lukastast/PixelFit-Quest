package com.pixelfitquest.local.export

import java.io.File

enum class ExportFormat {
    JSON,
    CSV,
    BOTH,
    ;

    fun expanded(): Set<ExportFormat> = when (this) {
        BOTH -> setOf(JSON, CSV)
        else -> setOf(this)
    }
}

data class PreparedExport(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
    val cacheFile: File? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PreparedExport) return false
        return fileName == other.fileName &&
            mimeType == other.mimeType &&
            bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}

data class ExportCounts(
    val workoutCount: Int = 0,
    val setCount: Int = 0,
)
