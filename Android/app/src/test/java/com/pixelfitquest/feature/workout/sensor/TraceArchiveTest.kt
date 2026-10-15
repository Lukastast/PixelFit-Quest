package com.pixelfitquest.feature.workout.sensor

import com.pixelfitquest.BuildConfig
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.math.sin

class TraceArchiveTest {
    @get:Rule val folder = TemporaryFolder()

    @Before fun enableRecording() {
        assertTrue(BuildConfig.DEBUG)
        TraceRecording.enabled = true
    }

    @After fun restoreRecordingFlag() {
        TraceRecording.enabled = BuildConfig.DEBUG
    }

    @Test fun v2RoundTrip_allLabelFields() {
        val trace = RawTrace(
            metadata = TraceMetadata(
                recordingId = "12345678-aaaa-bbbb-cccc-ddddeeeeffff",
                recordedAt = "2026-10-12T18:03:11+02:00",
                sessionDate = "2026-10-12",
                split = TraceSplit.HOLDOUT,
                appVersion = "1.2.3",
                appVersionCode = 9,
                buildType = "debug",
                analysisVersion = 3,
                appCount = 5,
                requestedSamplingPeriodUs = 0,
                deviceModel = "Pixel 8",
                apiLevel = 34,
                exerciseId = "bench-press",
                weightKg = null,
                sensors = listOf(
                    TraceSensorInfo("accel", 1, "BMI260 Accelerometer", "Bosch", 2500, 1_000_000, 0.01f, 78.4f),
                    TraceSensorInfo("gyro", 4, "BMI260 Gyroscope", "Bosch", 2500, 1_000_000, 0.001f, 34.9f),
                    TraceSensorInfo("rotation_vector", 11, "Rotation Vector", "AOSP", 0, 0, 0f, 1f),
                ),
                label = TraceLabel(
                    trueRepCount = 5,
                    failedRepNumbers = emptyList(),
                    startType = StartType.FLOOR,
                    sleeve = MountSide.RIGHT,
                    phoneTop = PhoneTop.TOWARD_CENTER,
                    stillAtStart = StillAtStart.NO,
                    notes = "a=b,c\næøå",
                ),
            ),
            accel = listOf(TimedVec3(1_000L, 0.1f, -9.81f, 0.25f, 3)),
            gyro = listOf(TimedVec3(1_000L, 0.01f, -0.02f, 0.03f, 3)),
            gyroUncalibrated = listOf(TimedGyroUncal(1_000L, 0.05f, -0.02f, 0.03f, 0.04f, 0f, -0.001f, 2)),
            rotationVector = listOf(TimedQuat(1_000L, 0f, 0f, 0f, 1f, 3)),
            gameRotationVector = listOf(TimedQuat(2_000L, 0.1f, -0.2f, 0.3f, 0.9f, 1)),
            accuracy = listOf(AccuracyEvent(500L, 1, 3)),
        )
        val file = folder.newFile("round.zip")
        assertTrue(TraceArchive.write(file, trace))
        assertEquals(TraceArchive.prepare(trace), TraceArchive.read(file))
        val text = zipText(file, "metadata.txt")
        assertTrue(text.contains("mountSide="))
        assertTrue(text.contains("label.failedRepNumbers="))
        assertTrue(text.contains("sensor.2.maxRange="))
        assertFalse(text.contains("sensor.3."))
        assertFalse(text.contains("failedRepIndices"))
        assertTrue(text.contains("schemaVersion="))
        assertTrue(text.contains("measured.accel.medianDtUs="))
    }

    @Test fun readsV1Zip() {
        val file = folder.newFile("v1.zip")
        writeV1(file)
        val read = TraceArchive.read(file)
        assertEquals(1, read.metadata.schemaVersion)
        assertEquals(TraceSplit.UNSET, read.metadata.split)
        assertEquals("", read.metadata.recordingId)
        assertEquals(MountSide.LEFT, read.metadata.label.sleeve)
        assertEquals(5, read.metadata.label.trueRepCount)
        assertEquals(listOf(2, 6), read.metadata.label.failedRepNumbers)
        assertEquals("paused", read.metadata.label.notes)
        assertNull(read.metadata.label.startType)
        assertEquals(60f, read.metadata.weightKg)
        assertEquals(0, read.metadata.sensors.single().minDelayUs)
        assertEquals(1_000L, read.accel.single().tNanos)
        assertEquals(0.01f, read.gyroUncalibrated.single().biasX)
        assertTrue(read.metadata.measured.isEmpty())
    }

    @Test fun twoExportsBothSurvive() {
        val root = folder.newFolder()
        val at = "2026-10-12T18:03:11+02:00"
        val first = TraceArchive.writeRetained(root, minimal("aaaaaaaa-1111-1111-1111-111111111111", at))
        val second = TraceArchive.writeRetained(root, minimal("bbbbbbbb-2222-2222-2222-222222222222", at))
        assertNotEquals(first!!.name, second!!.name)
        assertTrue(first.name.contains("20261012-180311_curl_aaaaaaaa"))
        assertTrue(second.name.contains("20261012-180311_curl_bbbbbbbb"))
        assertEquals(listOf(second.name, first.name), TraceArchive.list(root).map { it.name })
        assertNotEquals(TraceArchive.read(first).metadata.recordingId, TraceArchive.read(second).metadata.recordingId)
    }

    @Test fun keepsNewest40() {
        assertEquals(40, TraceRetention.MAX_TRACES)
        val root = folder.newFolder()
        val first = TraceArchive.writeRetained(root, minimal(UUID.randomUUID().toString()))
        var last: File? = null
        repeat(40) { last = TraceArchive.writeRetained(root, minimal(UUID.randomUUID().toString())) }
        val listed = TraceArchive.list(root)
        assertEquals(40, listed.size)
        assertEquals(last!!.name, listed.first().name)
        assertFalse(first!!.exists())
    }

    @Test fun byteCapDropsOldest() {
        assertEquals(200L * 1024 * 1024, TraceRetention.MAX_BYTES)
        val entries = listOf(
            TraceRetention.Entry("old", 1, 100),
            TraceRetention.Entry("mid", 2, 100),
            TraceRetention.Entry("new", 3, 100),
        )
        assertEquals(listOf("old"), TraceRetention.toDelete(entries, maxBytes = 250).map { it.name })
        val root = folder.newFolder()
        val first = TraceArchive.writeRetained(root, minimal(UUID.randomUUID().toString()), maxBytes = Long.MAX_VALUE)!!
        val second = TraceArchive.writeRetained(root, minimal(UUID.randomUUID().toString()), maxBytes = Long.MAX_VALUE)!!
        val third = TraceArchive.writeRetained(root, minimal(UUID.randomUUID().toString()), maxBytes = Long.MAX_VALUE)!!
        TraceArchive.applyRetention(File(root, TraceArchive.DIR_NAME), TraceRetention.MAX_TRACES, second.length() + third.length())
        assertFalse(first.exists())
        assertTrue(second.exists() && third.exists())
    }

    @Test fun measuredRateFromTimestamps() {
        val accel = (0 until 21).map { TimedVec3(it * 5_000_000L, 0f, 0f, 0f, 3) }
        assertEquals(5_000L, TraceArchive.medianDtUs(accel.map { it.tNanos }))
        val file = folder.newFile("rate.zip")
        assertTrue(TraceArchive.write(file, minimal("rate-id").copy(accel = accel)))
        val measured = TraceArchive.read(file).metadata.measured.getValue("accel")
        assertEquals(5_000L, measured.medianDtUs)
        assertEquals(21, measured.count)
    }

    @Test fun deleteAllRemovesDirectory() {
        val root = folder.newFolder()
        assertTrue(TraceArchive.writeRetained(root, minimal(UUID.randomUUID().toString()))!!.isFile)
        TraceArchive.deleteAll(root)
        assertFalse(File(root, TraceArchive.DIR_NAME).exists())
    }

    @Test fun disabledWritesNothingAndCreatesNoDirectory() {
        TraceRecording.enabled = false
        val root = folder.newFolder()
        assertNull(TraceArchive.writeRetained(root, minimal("off")))
        assertFalse(File(root, TraceArchive.DIR_NAME).exists())
        assertTrue(TraceArchive.list(root).isEmpty())
        val direct = File(root, "nested/trace.zip")
        assertFalse(TraceArchive.write(direct, minimal("off")))
        assertFalse(direct.exists())
        assertFalse(File(root, "nested").exists())
    }

    @Test fun synthetic60sAt400Hz_zipSize() {
        val n = 60 * 400
        val dt = 2_500_000L
        fun w(i: Int, phase: Float) = sin(i * 0.013 + phase).toFloat()
        val trace = minimal("400hz-60s").copy(
            accel = List(n) { i -> TimedVec3(i * dt, w(i, 0f), -9.81f + w(i, 1f), w(i, 2f), 3) },
            gyro = List(n) { i -> TimedVec3(i * dt, w(i, 0.4f), w(i, 0.8f), w(i, 1.2f), 3) },
            gyroUncalibrated = List(n) { i ->
                TimedGyroUncal(i * dt, w(i, 0.2f), w(i, 0.5f), w(i, 0.9f), 0.01f, -0.02f, 0.003f, 3)
            },
            rotationVector = List(n) { i -> TimedQuat(i * dt, w(i, 0.1f), w(i, 0.3f), w(i, 0.6f), 0.9f, 3) },
            gameRotationVector = List(n) { i -> TimedQuat(i * dt, w(i, 1.1f), w(i, 1.4f), w(i, 1.7f), 0.8f, 2) },
            accuracy = List(60) { i -> AccuracyEvent(i * 1_000_000_000L, 1, 3) },
        )
        val file = folder.newFile("sixty.zip")
        assertTrue(TraceArchive.write(file, trace))
        val accel = TraceArchive.read(file).metadata.measured.getValue("accel")
        assertEquals(2_500L, accel.medianDtUs)
        assertEquals(n, accel.count)
        System.err.println("synthetic 60s 400Hz zip bytes=${file.length()}")
        assertTrue("zip bytes=${file.length()}", file.length() in 50_000L..8_000_000L)
    }

    private fun minimal(id: String, at: String = "2026-10-15T12:00:00+02:00") = RawTrace(
        metadata = TraceMetadata(recordingId = id, recordedAt = at, sessionDate = "2026-10-15", exerciseId = "curl"),
    )

    private fun zipText(file: File, name: String) = ZipFile(file).use { zip ->
        zip.getInputStream(zip.getEntry(name)).bufferedReader().use { it.readText() }
    }

    /** Exact 4b95587 layout: no schemaVersion, failedRepIndices, mountSide. */
    private fun writeV1(file: File) {
        val meta = listOf(
            "deviceModel" to "Pixel+8",
            "apiLevel" to "34",
            "exerciseId" to "bench-press",
            "weightKg" to "60.0",
            "mountSide" to "LEFT",
            "trueRepCount" to "5",
            "failedRepIndices" to "2,6",
            "notes" to "paused",
            "sensor.0.stream" to "accel",
            "sensor.0.type" to "1",
            "sensor.0.name" to "acc",
            "sensor.0.vendor" to "Bosch",
        ).joinToString("") { (key, value) -> "$key=$value\n" }
        ZipOutputStream(file.outputStream()).use { zip ->
            fun put(name: String, body: String) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(body.toByteArray())
                zip.closeEntry()
            }
            put("metadata.txt", meta)
            put("accel.csv", "t_ns,x,y,z,accuracy\n1000,0.5,-9.8,0.25,3\n")
            put("gyro.csv", "t_ns,x,y,z,accuracy\n")
            put("gyro_uncalibrated.csv", "t_ns,x,y,z,bias_x,bias_y,bias_z,accuracy\n1000,0.1,0.2,0.3,0.01,0.02,-0.03,2\n")
            put("rotation_vector.csv", "t_ns,x,y,z,w,accuracy\n")
            put("game_rotation_vector.csv", "t_ns,x,y,z,w,accuracy\n")
            put("accuracy.csv", "t_ns,sensor_type,accuracy\n")
        }
    }
}
