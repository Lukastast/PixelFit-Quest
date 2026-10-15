package com.pixelfitquest.feature.workout.sensor

import com.pixelfitquest.BuildConfig
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

enum class TraceSplit { TUNE, HOLDOUT, UNSET }
enum class StartType { RACK, FLOOR, HANG }
enum class PhoneTop { TOWARD_PLATES, TOWARD_CENTER, UNKNOWN }
enum class StillAtStart { YES, NO, UNSURE }

/**
 * Null [trueRepCount] means unlabeled (the harness skips it).
 * [failedRepNumbers] are 1-based and count every attempt: 5 good reps
 * then a failed 6th is trueRepCount 5 and failedRepNumbers [6].
 * [sleeve] is the lifter's view, facing the bar.
 */
data class TraceLabel(
    val trueRepCount: Int? = null,
    val failedRepNumbers: List<Int> = emptyList(),
    val startType: StartType? = null,
    val sleeve: MountSide = MountSide.UNKNOWN,
    val phoneTop: PhoneTop = PhoneTop.UNKNOWN,
    val stillAtStart: StillAtStart = StillAtStart.UNSURE,
    val notes: String = "",
)

data class MeasuredStream(val medianDtUs: Long = 0L, val count: Int = 0)

/** Schema v2 header. A missing schemaVersion on read means v1. */
data class TraceMetadata(
    val schemaVersion: Int = TraceArchive.SCHEMA_V2,
    val recordingId: String = "",
    val recordedAt: String = "",
    val sessionDate: String = "",
    val split: TraceSplit = TraceSplit.UNSET,
    val appVersion: String = "",
    val appVersionCode: Int = 0,
    val buildType: String = "debug",
    val analysisVersion: Int = 0,
    val appCount: Int? = null,
    val requestedSamplingPeriodUs: Int? = null,
    val measured: Map<String, MeasuredStream> = emptyMap(),
    val deviceModel: String = "",
    val apiLevel: Int = 0,
    val exerciseId: String = "",
    val weightKg: Float? = null,
    val sensors: List<TraceSensorInfo> = emptyList(),
    val label: TraceLabel = TraceLabel(),
)

data class RawTrace(
    val metadata: TraceMetadata = TraceMetadata(),
    val accel: List<TimedVec3> = emptyList(),
    val gyro: List<TimedVec3> = emptyList(),
    val gyroUncalibrated: List<TimedGyroUncal> = emptyList(),
    val rotationVector: List<TimedQuat> = emptyList(),
    val gameRotationVector: List<TimedQuat> = emptyList(),
    val accuracy: List<AccuracyEvent> = emptyList(),
)

/**
 * Debug builds record. Default is [BuildConfig.DEBUG]. Tests set [enabled] false.
 * Writers also check [BuildConfig.DEBUG] directly, so release builds never write
 * (minify is off, so the code stays in the APK but is unreachable).
 */
object TraceRecording {
    var enabled: Boolean = BuildConfig.DEBUG
}

/**
 * Schema v2 zip under [DIR_NAME]: `trace_<yyyyMMdd-HHmmss>_<exerciseId>_<id8>.zip`.
 * [writeRetained] writes a `.partial`, renames it, then keeps the newest
 * [TraceRetention.MAX_TRACES] files and at most [TraceRetention.MAX_BYTES].
 * Oldest go first. Release builds do not write.
 *
 * Replay scoring, per exercise: missed = max(0, true − counted) and extra =
 * max(0, counted − true), separately, plus whether each failedRepNumbers entry
 * was flagged and kept out of the total. Tuning and held-out never share a
 * recordingId or a sessionDate. HOLDOUT is never opened while picking thresholds.
 *
 * #182 is not merged. Its wipe should call [deleteAll] ([File.delete] leaves a
 * non-empty directory). Slice 1c share must use this same debug gate.
 */
object TraceArchive {
    const val SCHEMA_V2 = 2
    const val DIR_NAME = "trace_exports"

    private const val SCHEMA_V1 = 1
    private val STAMP = Regex("""(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})""")
    private val UNSAFE = Regex("[^A-Za-z0-9._-]")

    fun write(file: File, trace: RawTrace): Boolean {
        if (!BuildConfig.DEBUG) return false
        return writeDebug(file, trace)
    }

    fun writeRetained(
        filesDir: File,
        trace: RawTrace,
        maxTraces: Int = TraceRetention.MAX_TRACES,
        maxBytes: Long = TraceRetention.MAX_BYTES,
    ): File? {
        if (!BuildConfig.DEBUG) return null
        return writeRetainedDebug(filesDir, trace, maxTraces, maxBytes)
    }

    fun read(file: File): RawTrace = ZipFile(file).use { zip ->
        fun text(name: String): String {
            val entry = zip.getEntry(name) ?: return ""
            return zip.getInputStream(entry).bufferedReader().use { it.readText() }
        }
        RawTrace(
            metadata = metadataOf(text("metadata.txt")),
            accel = vec(text("accel.csv")),
            gyro = vec(text("gyro.csv")),
            gyroUncalibrated = gyroUncal(text("gyro_uncalibrated.csv")),
            rotationVector = quat(text("rotation_vector.csv")),
            gameRotationVector = quat(text("game_rotation_vector.csv")),
            accuracy = accuracy(text("accuracy.csv")),
        )
    }

    /** Newest complete zip first. */
    fun list(filesDir: File): List<File> {
        val dir = File(filesDir, DIR_NAME)
        if (!dir.isDirectory) return emptyList()
        return zips(dir).sortedWith(compareByDescending<File> { it.lastModified() }.thenByDescending { it.name })
    }

    fun deleteAll(filesDir: File) {
        File(filesDir, DIR_NAME).deleteRecursively()
    }

    internal fun measure(trace: RawTrace): Map<String, MeasuredStream> = linkedMapOf(
        "accel" to rate(trace.accel.map { it.tNanos }),
        "gyro" to rate(trace.gyro.map { it.tNanos }),
        "gyro_uncalibrated" to rate(trace.gyroUncalibrated.map { it.tNanos }),
        "rotation_vector" to rate(trace.rotationVector.map { it.tNanos }),
        "game_rotation_vector" to rate(trace.gameRotationVector.map { it.tNanos }),
    )

    internal fun prepare(trace: RawTrace) = trace.copy(
        metadata = trace.metadata.copy(schemaVersion = SCHEMA_V2, buildType = "debug", measured = measure(trace)),
    )

    internal fun applyRetention(dir: File, maxTraces: Int, maxBytes: Long) {
        val files = zips(dir)
        val doomed = TraceRetention.toDelete(
            files.map { TraceRetention.Entry(it.name, it.lastModified(), it.length()) },
            maxTraces,
            maxBytes,
        )
        val byName = files.associateBy { it.name }
        doomed.forEach { byName[it.name]?.delete() }
    }

    internal fun medianDtUs(times: List<Long>): Long? {
        if (times.size < 2) return null
        val deltas = LongArray(times.size - 1) { i -> times[i + 1] - times[i] }
        deltas.sort()
        val mid = deltas.size / 2
        val medianNs = if (deltas.size % 2 == 1) deltas[mid] else (deltas[mid - 1] + deltas[mid]) / 2
        return medianNs / 1_000L
    }

    private fun writeDebug(file: File, trace: RawTrace): Boolean {
        if (!TraceRecording.enabled) return false
        val parent = file.parentFile
        if (parent != null && !parent.isDirectory && !parent.mkdirs()) return false
        writeZip(file, trace)
        return file.isFile
    }

    private fun writeRetainedDebug(filesDir: File, trace: RawTrace, maxTraces: Int, maxBytes: Long): File? {
        if (!TraceRecording.enabled) return null
        val dir = File(filesDir, DIR_NAME)
        if (!dir.isDirectory && !dir.mkdirs()) return null
        dir.listFiles()?.forEach { if (it.name.endsWith(".partial")) it.delete() }
        val dest = uniqueDest(dir, fileName(trace.metadata))
        val partial = File(dir, dest.name + ".partial")
        try {
            writeZip(partial, trace)
            if (!partial.renameTo(dest)) {
                partial.delete()
                return null
            }
        } catch (e: Exception) {
            partial.delete()
            dest.delete()
            return null
        }
        var newest = 0L
        dir.listFiles()?.forEach { if (it != dest && it.lastModified() > newest) newest = it.lastModified() }
        // +1s so a coarse filesystem clock still orders this file last.
        dest.setLastModified(maxOf(System.currentTimeMillis(), newest + 1_000L))
        applyRetention(dir, maxTraces, maxBytes)
        return dest.takeIf { it.isFile }
    }

    private fun fileName(meta: TraceMetadata): String {
        val match = STAMP.find(meta.recordedAt)
        val stamp = if (match == null) "00000000-000000" else {
            val (y, mo, d, h, min, s) = match.destructured
            "$y$mo$d-$h$min$s"
        }
        val exercise = meta.exerciseId.replace(UNSAFE, "_").take(40).ifEmpty { "exercise" }
        val id8 = meta.recordingId.take(8).replace(UNSAFE, "_").ifEmpty { "00000000" }
        return "trace_${stamp}_${exercise}_$id8.zip"
    }

    private fun uniqueDest(dir: File, name: String): File {
        var candidate = File(dir, name)
        val base = name.removeSuffix(".zip")
        var n = 2
        while (candidate.exists()) candidate = File(dir, "${base}_${n++}.zip")
        return candidate
    }

    private fun writeZip(file: File, trace: RawTrace) {
        val stored = prepare(trace)
        ZipOutputStream(file.outputStream().buffered()).use { zip ->
            fun put(name: String, body: String) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(body.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
            put("metadata.txt", metaText(stored.metadata))
            put("accel.csv", csv("t_ns,x,y,z,accuracy", stored.accel.map { csvRow(it.tNanos, it.x, it.y, it.z, it.accuracy) }))
            put("gyro.csv", csv("t_ns,x,y,z,accuracy", stored.gyro.map { csvRow(it.tNanos, it.x, it.y, it.z, it.accuracy) }))
            put(
                "gyro_uncalibrated.csv",
                csv(
                    "t_ns,x,y,z,bias_x,bias_y,bias_z,accuracy",
                    stored.gyroUncalibrated.map { csvRow(it.tNanos, it.x, it.y, it.z, it.biasX, it.biasY, it.biasZ, it.accuracy) },
                ),
            )
            put("rotation_vector.csv", csv("t_ns,x,y,z,w,accuracy", stored.rotationVector.map {
                csvRow(it.tNanos, it.x, it.y, it.z, it.w, it.accuracy)
            }))
            put("game_rotation_vector.csv", csv("t_ns,x,y,z,w,accuracy", stored.gameRotationVector.map {
                csvRow(it.tNanos, it.x, it.y, it.z, it.w, it.accuracy)
            }))
            put("accuracy.csv", csv("t_ns,sensor_type,accuracy", stored.accuracy.map { csvRow(it.tNanos, it.sensorType, it.accuracy) }))
        }
    }

    private fun metaText(meta: TraceMetadata): String = buildString {
        fun add(key: String, value: String) {
            append(key).append('=').append(URLEncoder.encode(value, Charsets.UTF_8.name())).append('\n')
        }
        add("schemaVersion", meta.schemaVersion.toString())
        add("recordingId", meta.recordingId)
        add("recordedAt", meta.recordedAt)
        add("sessionDate", meta.sessionDate)
        add("split", meta.split.name)
        add("appVersion", meta.appVersion)
        add("appVersionCode", meta.appVersionCode.toString())
        add("buildType", meta.buildType)
        add("analysisVersion", meta.analysisVersion.toString())
        add("appCount", meta.appCount?.toString().orEmpty())
        add("requestedSamplingPeriodUs", meta.requestedSamplingPeriodUs?.toString().orEmpty())
        meta.measured.forEach { (stream, rate) ->
            add("measured.$stream.medianDtUs", rate.medianDtUs.toString())
            add("measured.$stream.count", rate.count.toString())
        }
        add("deviceModel", meta.deviceModel)
        add("apiLevel", meta.apiLevel.toString())
        add("exerciseId", meta.exerciseId)
        add("weightKg", meta.weightKg?.let { java.lang.Float.toString(it) }.orEmpty())
        meta.sensors.forEachIndexed { i, sensor ->
            add("sensor.$i.stream", sensor.stream)
            add("sensor.$i.type", sensor.type.toString())
            add("sensor.$i.name", sensor.name)
            add("sensor.$i.vendor", sensor.vendor)
            add("sensor.$i.minDelayUs", sensor.minDelayUs.toString())
            add("sensor.$i.maxDelayUs", sensor.maxDelayUs.toString())
            add("sensor.$i.resolution", java.lang.Float.toString(sensor.resolution))
            add("sensor.$i.maxRange", java.lang.Float.toString(sensor.maxRange))
        }
        val label = meta.label
        add("label.trueRepCount", label.trueRepCount?.toString().orEmpty())
        add("label.failedRepNumbers", label.failedRepNumbers.joinToString(","))
        add("label.startType", label.startType?.name.orEmpty())
        add("label.sleeve", label.sleeve.name)
        add("label.phoneTop", label.phoneTop.name)
        add("label.stillAtStart", label.stillAtStart.name)
        add("label.notes", label.notes)
        add("mountSide", label.sleeve.name)
    }

    private fun metadataOf(text: String): TraceMetadata {
        val map = LinkedHashMap<String, String>()
        for (raw in text.lineSequence()) {
            val eq = raw.indexOf('=')
            if (eq <= 0) continue
            map[raw.substring(0, eq)] = URLDecoder.decode(raw.substring(eq + 1), Charsets.UTF_8.name())
        }
        fun opt(key: String) = map[key].orEmpty()
        val period = map["requestedSamplingPeriodUs"]
        return TraceMetadata(
            schemaVersion = opt("schemaVersion").toIntOrNull() ?: SCHEMA_V1,
            recordingId = opt("recordingId"),
            recordedAt = opt("recordedAt"),
            sessionDate = opt("sessionDate"),
            split = enumOrNull<TraceSplit>(map["split"]) ?: TraceSplit.UNSET,
            appVersion = opt("appVersion"),
            appVersionCode = opt("appVersionCode").toIntOrNull() ?: 0,
            buildType = opt("buildType"),
            analysisVersion = opt("analysisVersion").toIntOrNull() ?: 0,
            appCount = map["appCount"]?.toIntOrNull(),
            requestedSamplingPeriodUs = if (period.isNullOrEmpty()) null else period.toIntOrNull(),
            measured = measuredOf(map),
            deviceModel = opt("deviceModel"),
            apiLevel = opt("apiLevel").toIntOrNull() ?: 0,
            exerciseId = opt("exerciseId"),
            weightKg = map["weightKg"]?.toFloatOrNull(),
            sensors = sensorsOf(map),
            label = labelOf(map),
        )
    }

    private fun labelOf(map: Map<String, String>) = TraceLabel(
        trueRepCount = (map["label.trueRepCount"] ?: map["trueRepCount"])?.toIntOrNull(),
        failedRepNumbers = (map["label.failedRepNumbers"] ?: map["failedRepIndices"]).orEmpty()
            .split(',').mapNotNull { it.trim().toIntOrNull() },
        startType = enumOrNull<StartType>(map["label.startType"]),
        sleeve = enumOrNull<MountSide>(map["label.sleeve"] ?: map["mountSide"]) ?: MountSide.UNKNOWN,
        phoneTop = enumOrNull<PhoneTop>(map["label.phoneTop"]) ?: PhoneTop.UNKNOWN,
        stillAtStart = enumOrNull<StillAtStart>(map["label.stillAtStart"]) ?: StillAtStart.UNSURE,
        notes = map["label.notes"] ?: map["notes"].orEmpty(),
    )

    private fun sensorsOf(map: Map<String, String>): List<TraceSensorInfo> {
        val sensors = ArrayList<TraceSensorInfo>()
        var i = 0
        while (map.containsKey("sensor.$i.stream")) {
            fun field(name: String) = map["sensor.$i.$name"].orEmpty()
            sensors += TraceSensorInfo(
                stream = field("stream"),
                type = field("type").toIntOrNull() ?: 0,
                name = field("name"),
                vendor = field("vendor"),
                minDelayUs = field("minDelayUs").toIntOrNull() ?: 0,
                maxDelayUs = field("maxDelayUs").toIntOrNull() ?: 0,
                resolution = field("resolution").toFloatOrNull() ?: 0f,
                maxRange = field("maxRange").toFloatOrNull() ?: 0f,
            )
            i++
        }
        return sensors
    }

    private fun measuredOf(map: Map<String, String>): Map<String, MeasuredStream> {
        val measured = LinkedHashMap<String, MeasuredStream>()
        for ((key, value) in map) {
            if (!key.startsWith("measured.")) continue
            val rest = key.removePrefix("measured.")
            val dot = rest.indexOf('.')
            if (dot <= 0) continue
            val stream = rest.substring(0, dot)
            val prev = measured[stream] ?: MeasuredStream()
            measured[stream] = when (rest.substring(dot + 1)) {
                "medianDtUs" -> prev.copy(medianDtUs = value.toLongOrNull() ?: 0L)
                "count" -> prev.copy(count = value.toIntOrNull() ?: 0)
                else -> prev
            }
        }
        return measured
    }

    private inline fun <reified T : Enum<T>> enumOrNull(raw: String?) =
        if (raw.isNullOrEmpty()) null else enumValues<T>().firstOrNull { it.name == raw }

    private fun csv(header: String, rows: List<String>) = buildString {
        append(header).append('\n')
        rows.forEach { append(it).append('\n') }
    }

    private fun csvRow(vararg cells: Any) = cells.joinToString(",") {
        if (it is Float) java.lang.Float.toString(it) else it.toString()
    }

    private fun body(text: String) = text.lineSequence().drop(1).filter { it.isNotBlank() }.map { it.split(',') }

    private fun vec(text: String) = body(text).map { c ->
        TimedVec3(c[0].toLong(), c[1].toFloat(), c[2].toFloat(), c[3].toFloat(), c[4].toInt())
    }.toList()

    private fun quat(text: String) = body(text).map { c ->
        TimedQuat(c[0].toLong(), c[1].toFloat(), c[2].toFloat(), c[3].toFloat(), c[4].toFloat(), c[5].toInt())
    }.toList()

    private fun gyroUncal(text: String) = body(text).map { c ->
        TimedGyroUncal(
            c[0].toLong(), c[1].toFloat(), c[2].toFloat(), c[3].toFloat(),
            c[4].toFloat(), c[5].toFloat(), c[6].toFloat(), c[7].toInt(),
        )
    }.toList()

    private fun accuracy(text: String) = body(text).map { c ->
        AccuracyEvent(c[0].toLong(), c[1].toInt(), c[2].toInt())
    }.toList()

    private fun zips(dir: File) = dir.listFiles()?.filter { it.isFile && it.name.endsWith(".zip") }.orEmpty()

    private fun rate(times: List<Long>) = MeasuredStream(medianDtUs(times) ?: 0L, times.size)
}
