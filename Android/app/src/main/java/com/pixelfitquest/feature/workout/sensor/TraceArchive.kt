package com.pixelfitquest.feature.workout.sensor

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * One raw IMU recording, before interpolation. Sensor timestamps are elapsed
 * nanoseconds. Accuracy is the sensor accuracy reported with the sample.
 */
data class TraceSensorInfo(
    val stream: String,
    val type: Int,
    val name: String,
    val vendor: String,
)

data class TraceLabel(
    /** Ground-truth rep count, if the lifter filled it in. Null means unlabeled. */
    val trueRepCount: Int?,
    val failedRepIndices: List<Int>,
    val notes: String,
)

data class TraceMetadata(
    val deviceModel: String,
    val apiLevel: Int,
    val sensors: List<TraceSensorInfo>,
    val exerciseId: String,
    val weightKg: Float?,
    val mountSide: String,
    val label: TraceLabel,
)

data class AccuracyEvent(
    val tNanos: Long,
    val sensorType: Int,
    val accuracy: Int,
)

data class TimedGyroUncal(
    val tNanos: Long,
    val x: Float,
    val y: Float,
    val z: Float,
    val biasX: Float,
    val biasY: Float,
    val biasZ: Float,
    val accuracy: Int,
)

data class RawTrace(
    val metadata: TraceMetadata,
    val accel: List<TimedVec3>,
    val gyro: List<TimedVec3>,
    val gyroUncalibrated: List<TimedGyroUncal>,
    val rotationVector: List<TimedQuat>,
    val gameRotationVector: List<TimedQuat>,
    val accuracy: List<AccuracyEvent>,
)

/**
 * Zip of the raw streams plus metadata. Pure JVM so unit tests can round-trip
 * it without Android. One file is kept under the app files directory.
 */
object TraceArchive {
    const val DIR_NAME = "trace_exports"
    const val FILE_NAME = "imu-trace.zip"

    private const val META = "metadata.txt"
    private const val ACCEL = "accel.csv"
    private const val GYRO = "gyro.csv"
    private const val GYRO_UNCAL = "gyro_uncalibrated.csv"
    private const val ROTATION = "rotation_vector.csv"
    private const val GAME_ROTATION = "game_rotation_vector.csv"
    private const val ACCURACY = "accuracy.csv"

    fun write(file: File, trace: RawTrace) {
        file.parentFile?.mkdirs()
        ZipOutputStream(file.outputStream().buffered()).use { zip ->
            put(zip, META, metadataText(trace.metadata))
            put(zip, ACCEL, vecCsv(trace.accel))
            put(zip, GYRO, vecCsv(trace.gyro))
            put(zip, GYRO_UNCAL, gyroUncalCsv(trace.gyroUncalibrated))
            put(zip, ROTATION, quatCsv(trace.rotationVector))
            put(zip, GAME_ROTATION, quatCsv(trace.gameRotationVector))
            put(zip, ACCURACY, accuracyCsv(trace.accuracy))
        }
    }

    /** Overwrites the single trace zip in [filesDir]/trace_exports. */
    fun writeSingle(filesDir: File, trace: RawTrace): File {
        val dir = File(filesDir, DIR_NAME)
        dir.mkdirs()
        dir.listFiles()?.forEach { child ->
            if (child.isFile && child.name != FILE_NAME) child.delete()
        }
        val out = File(dir, FILE_NAME)
        if (out.exists()) out.delete()
        write(out, trace)
        return out
    }

    fun read(file: File): RawTrace {
        ZipFile(file).use { zip ->
            val meta = readMeta(text(zip, META))
            return RawTrace(
                metadata = meta,
                accel = parseVec(text(zip, ACCEL)),
                gyro = parseVec(text(zip, GYRO)),
                gyroUncalibrated = parseGyroUncal(text(zip, GYRO_UNCAL)),
                rotationVector = parseQuat(text(zip, ROTATION)),
                gameRotationVector = parseQuat(text(zip, GAME_ROTATION)),
                accuracy = parseAccuracy(text(zip, ACCURACY)),
            )
        }
    }

    private fun put(zip: ZipOutputStream, name: String, body: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(body.toByteArray(StandardCharsets.UTF_8))
        zip.closeEntry()
    }

    private fun text(zip: ZipFile, name: String): String {
        val entry = zip.getEntry(name) ?: error("missing $name")
        return zip.getInputStream(entry).use { input ->
            BufferedReader(InputStreamReader(input, StandardCharsets.UTF_8)).readText()
        }
    }

    internal fun metadataText(meta: TraceMetadata): String = buildString {
        line("deviceModel", meta.deviceModel)
        line("apiLevel", meta.apiLevel.toString())
        line("exerciseId", meta.exerciseId)
        line("weightKg", meta.weightKg?.let { java.lang.Float.toString(it) } ?: "")
        line("mountSide", meta.mountSide)
        line("trueRepCount", meta.label.trueRepCount?.toString() ?: "")
        line("failedRepIndices", meta.label.failedRepIndices.joinToString(","))
        line("notes", meta.label.notes)
        meta.sensors.forEachIndexed { i, sensor ->
            line("sensor.$i.stream", sensor.stream)
            line("sensor.$i.type", sensor.type.toString())
            line("sensor.$i.name", sensor.name)
            line("sensor.$i.vendor", sensor.vendor)
        }
    }

    private fun StringBuilder.line(key: String, value: String) {
        append(key)
        append('=')
        append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()))
        append('\n')
    }

    internal fun readMeta(text: String): TraceMetadata {
        val map = LinkedHashMap<String, String>()
        for (raw in text.lineSequence()) {
            if (raw.isBlank() || !raw.contains('=')) continue
            val key = raw.substringBefore('=')
            val value = URLDecoder.decode(raw.substringAfter('='), StandardCharsets.UTF_8.name())
            map[key] = value
        }
        val sensors = ArrayList<TraceSensorInfo>()
        var i = 0
        while (map.containsKey("sensor.$i.stream")) {
            sensors += TraceSensorInfo(
                stream = map.getValue("sensor.$i.stream"),
                type = map.getValue("sensor.$i.type").toInt(),
                name = map.getValue("sensor.$i.name"),
                vendor = map["sensor.$i.vendor"].orEmpty(),
            )
            i++
        }
        val weight = map["weightKg"].orEmpty().takeIf { it.isNotEmpty() }?.toFloat()
        val trueCount = map["trueRepCount"].orEmpty().takeIf { it.isNotEmpty() }?.toInt()
        val failed = map["failedRepIndices"].orEmpty()
            .split(',')
            .mapNotNull { part -> part.toIntOrNull() }
        return TraceMetadata(
            deviceModel = map["deviceModel"].orEmpty(),
            apiLevel = map["apiLevel"]?.toIntOrNull() ?: 0,
            sensors = sensors,
            exerciseId = map["exerciseId"].orEmpty(),
            weightKg = weight,
            mountSide = map["mountSide"].orEmpty().ifEmpty { MountSide.UNKNOWN.name },
            label = TraceLabel(
                trueRepCount = trueCount,
                failedRepIndices = failed,
                notes = map["notes"].orEmpty(),
            ),
        )
    }

    private fun vecCsv(rows: List<TimedVec3>): String = buildString {
        append("t_ns,x,y,z,accuracy\n")
        for (s in rows) {
            append(s.tNanos)
            append(',')
            append(java.lang.Float.toString(s.x))
            append(',')
            append(java.lang.Float.toString(s.y))
            append(',')
            append(java.lang.Float.toString(s.z))
            append(',')
            append(s.accuracy)
            append('\n')
        }
    }

    private fun quatCsv(rows: List<TimedQuat>): String = buildString {
        append("t_ns,x,y,z,w,accuracy\n")
        for (s in rows) {
            append(s.tNanos)
            append(',')
            append(java.lang.Float.toString(s.x))
            append(',')
            append(java.lang.Float.toString(s.y))
            append(',')
            append(java.lang.Float.toString(s.z))
            append(',')
            append(java.lang.Float.toString(s.w))
            append(',')
            append(s.accuracy)
            append('\n')
        }
    }

    private fun gyroUncalCsv(rows: List<TimedGyroUncal>): String = buildString {
        append("t_ns,x,y,z,bias_x,bias_y,bias_z,accuracy\n")
        for (s in rows) {
            append(s.tNanos)
            append(',')
            append(java.lang.Float.toString(s.x))
            append(',')
            append(java.lang.Float.toString(s.y))
            append(',')
            append(java.lang.Float.toString(s.z))
            append(',')
            append(java.lang.Float.toString(s.biasX))
            append(',')
            append(java.lang.Float.toString(s.biasY))
            append(',')
            append(java.lang.Float.toString(s.biasZ))
            append(',')
            append(s.accuracy)
            append('\n')
        }
    }

    private fun accuracyCsv(rows: List<AccuracyEvent>): String = buildString {
        append("t_ns,sensor_type,accuracy\n")
        for (s in rows) {
            append(s.tNanos)
            append(',')
            append(s.sensorType)
            append(',')
            append(s.accuracy)
            append('\n')
        }
    }

    private fun parseVec(text: String): List<TimedVec3> {
        val out = ArrayList<TimedVec3>()
        for (line in bodyLines(text)) {
            val c = line.split(',')
            if (c.size < 5) continue
            out += TimedVec3(c[0].toLong(), c[1].toFloat(), c[2].toFloat(), c[3].toFloat(), c[4].toInt())
        }
        return out
    }

    private fun parseQuat(text: String): List<TimedQuat> {
        val out = ArrayList<TimedQuat>()
        for (line in bodyLines(text)) {
            val c = line.split(',')
            if (c.size < 6) continue
            out += TimedQuat(
                c[0].toLong(),
                c[1].toFloat(),
                c[2].toFloat(),
                c[3].toFloat(),
                c[4].toFloat(),
                c[5].toInt(),
            )
        }
        return out
    }

    private fun parseGyroUncal(text: String): List<TimedGyroUncal> {
        val out = ArrayList<TimedGyroUncal>()
        for (line in bodyLines(text)) {
            val c = line.split(',')
            if (c.size < 8) continue
            out += TimedGyroUncal(
                tNanos = c[0].toLong(),
                x = c[1].toFloat(),
                y = c[2].toFloat(),
                z = c[3].toFloat(),
                biasX = c[4].toFloat(),
                biasY = c[5].toFloat(),
                biasZ = c[6].toFloat(),
                accuracy = c[7].toInt(),
            )
        }
        return out
    }

    private fun parseAccuracy(text: String): List<AccuracyEvent> {
        val out = ArrayList<AccuracyEvent>()
        for (line in bodyLines(text)) {
            val c = line.split(',')
            if (c.size < 3) continue
            out += AccuracyEvent(c[0].toLong(), c[1].toInt(), c[2].toInt())
        }
        return out
    }

    private fun bodyLines(text: String): Sequence<String> =
        text.lineSequence().drop(1).filter { it.isNotBlank() }
}
