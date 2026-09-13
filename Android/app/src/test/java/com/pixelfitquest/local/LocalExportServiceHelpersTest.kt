package com.pixelfitquest.local

import com.pixelfitquest.local.export.ExportFormat
import com.pixelfitquest.local.export.LocalExportService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date
import java.util.TimeZone

class LocalExportServiceHelpersTest {
    @Test
    fun exportFormatBothExpandsToJsonAndCsv() {
        assertEquals(setOf(ExportFormat.JSON, ExportFormat.CSV), ExportFormat.BOTH.expanded())
        assertEquals(setOf(ExportFormat.JSON), ExportFormat.JSON.expanded())
        assertEquals(setOf(ExportFormat.CSV), ExportFormat.CSV.expanded())
    }

    @Test
    fun fileStampIsUtcCompact() {
        val date = Date(0L) // 1970-01-01T00:00:00Z
        val stamp = LocalExportService.fileStamp(date)
        assertEquals("19700101-000000", stamp)
        assertTrue(TimeZone.getTimeZone("UTC").rawOffset == 0)
    }
}
