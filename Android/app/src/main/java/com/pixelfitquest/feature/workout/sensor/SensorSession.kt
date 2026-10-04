package com.pixelfitquest.feature.workout.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import kotlin.math.sqrt

/**
 * Record-only phone IMU. Sampling still prefers SENSOR_DELAY_FASTEST, then ≤200 Hz,
 * then GAME delay. Analysis keeps using the magnetometer rotation vector when that
 * sensor exists; game rotation is recorded beside it and is not fed to the analyzer yet.
 * Linear acceleration is not registered — nothing in analysis reads it.
 */
class SensorSession(
    private val sensorManager: SensorManager,
    private val onAccelTick: (count: Int, firstNanos: Long, lastNanos: Long) -> Unit = { _, _, _ -> },
    private val onMissingAccelerometer: () -> Unit = {},
) : SensorEventListener {

    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val gyroscopeUncal: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED)
    private val rotationVector: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val gameRotationVector: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)

    private val lock = Any()
    private val accel = ArrayList<TimedVec3>(4096)
    private val gyro = ArrayList<TimedVec3>(4096)
    private val gyroUncal = ArrayList<TimedGyroUncal>(4096)
    private val rotation = ArrayList<TimedQuat>(4096)
    private val gameRotation = ArrayList<TimedQuat>(4096)
    private val accuracyEvents = ArrayList<AccuracyEvent>(64)
    private val sensorInfos = ArrayList<TraceSensorInfo>(6)

    @Volatile private var registered = false

    /** Sampling period actually used after [register], or null if not registered. */
    @Volatile var activeSamplingPeriodUs: Int? = null
        private set

    val hasAccelerometer: Boolean get() = accelerometer != null

    fun clear() {
        synchronized(lock) {
            accel.clear()
            gyro.clear()
            gyroUncal.clear()
            rotation.clear()
            gameRotation.clear()
            accuracyEvents.clear()
        }
    }

    fun register() {
        if (registered) return
        val accelSensor = accelerometer
        if (accelSensor == null) {
            onMissingAccelerometer()
            return
        }
        // FASTEST is 0 µs. API 31+ throws SecurityException without HIGH_SAMPLING_RATE_SENSORS
        // (declared in the manifest). 5000 µs (200 Hz) is the fastest rate allowed without it.
        val rates = intArrayOf(
            SensorManager.SENSOR_DELAY_FASTEST,
            MAX_RATE_WITHOUT_HIGH_SAMPLING_US,
            SensorManager.SENSOR_DELAY_GAME,
        )
        for (rate in rates) {
            try {
                registerAll(accelSensor, rate)
                registered = true
                activeSamplingPeriodUs = rate
                synchronized(lock) {
                    sensorInfos.clear()
                    sensorInfos += describe("accel", accelSensor)
                    gyroscope?.let { sensorInfos += describe("gyro", it) }
                    gyroscopeUncal?.let { sensorInfos += describe("gyro_uncalibrated", it) }
                    rotationVector?.let { sensorInfos += describe("rotation_vector", it) }
                    gameRotationVector?.let { sensorInfos += describe("game_rotation_vector", it) }
                }
                return
            } catch (_: SecurityException) {
                sensorManager.unregisterListener(this)
            }
        }
        onMissingAccelerometer()
    }

    private fun registerAll(accelSensor: Sensor, samplingPeriodUs: Int) {
        sensorManager.registerListener(this, accelSensor, samplingPeriodUs)
        gyroscope?.let { sensorManager.registerListener(this, it, samplingPeriodUs) }
        gyroscopeUncal?.let { sensorManager.registerListener(this, it, samplingPeriodUs) }
        rotationVector?.let { sensorManager.registerListener(this, it, samplingPeriodUs) }
        gameRotationVector?.let { sensorManager.registerListener(this, it, samplingPeriodUs) }
    }

    fun unregister() {
        if (!registered) return
        sensorManager.unregisterListener(this)
        registered = false
        activeSamplingPeriodUs = null
    }

    /**
     * Analyzer input. Magnetometer rotation vector wins when it produced samples,
     * matching the previous single-stream preference. Game rotation is attached
     * on the side and is not what [com.pixelfitquest.feature.workout.analysis.Attitude] reads.
     * Calibrated gyro wins over the uncalibrated stream. Bias fields are kept only
     * on the raw export until the analyzer is switched.
     */
    fun snapshotInterpolated(): List<ImuSample> {
        val trace = synchronized(lock) {
            val gyroForAnalysis = if (gyro.isNotEmpty()) {
                gyro.toList()
            } else {
                gyroUncal.map { TimedVec3(it.tNanos, it.x, it.y, it.z, it.accuracy) }
            }
            val rotationForAnalysis = if (rotation.isNotEmpty()) rotation.toList() else gameRotation.toList()
            ImuTrace(
                accel = accel.toList(),
                gyro = gyroForAnalysis,
                rotation = rotationForAnalysis,
                linearAccel = emptyList(),
                gameRotation = gameRotation.toList(),
                gyroUncalibrated = gyroUncal.toList(),
                accuracy = accuracyEvents.toList(),
            )
        }
        return ImuInterpolate.ontoAccel(trace)
    }

    /** Raw, un-interpolated streams for the trace zip. */
    fun snapshotRaw(): RawSensorDump = synchronized(lock) {
        RawSensorDump(
            accel = accel.toList(),
            gyro = gyro.toList(),
            gyroUncalibrated = gyroUncal.toList(),
            rotationVector = rotation.toList(),
            gameRotationVector = gameRotation.toList(),
            accuracy = accuracyEvents.toList(),
            sensors = sensorInfos.toList(),
        )
    }

    override fun onSensorChanged(event: SensorEvent) {
        val t = event.timestamp
        val accuracy = event.accuracy
        when (event.sensor.type) {
            Sensor.TYPE_GYROSCOPE -> {
                synchronized(lock) {
                    gyro += TimedVec3(t, event.values[0], event.values[1], event.values[2], accuracy)
                }
            }
            Sensor.TYPE_GYROSCOPE_UNCALIBRATED -> {
                val biasX = event.values.getOrElse(3) { 0f }
                val biasY = event.values.getOrElse(4) { 0f }
                val biasZ = event.values.getOrElse(5) { 0f }
                synchronized(lock) {
                    gyroUncal += TimedGyroUncal(
                        tNanos = t,
                        x = event.values[0],
                        y = event.values[1],
                        z = event.values[2],
                        biasX = biasX,
                        biasY = biasY,
                        biasZ = biasZ,
                        accuracy = accuracy,
                    )
                }
            }
            Sensor.TYPE_ROTATION_VECTOR -> {
                synchronized(lock) { rotation += quatFromEvent(event, t, accuracy) }
            }
            Sensor.TYPE_GAME_ROTATION_VECTOR -> {
                synchronized(lock) { gameRotation += quatFromEvent(event, t, accuracy) }
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val count: Int
                val first: Long
                val last: Long
                synchronized(lock) {
                    accel += TimedVec3(t, event.values[0], event.values[1], event.values[2], accuracy)
                    count = accel.size
                    first = accel.first().tNanos
                    last = t
                }
                onAccelTick(count, first, last)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        val type = sensor?.type ?: return
        val event = AccuracyEvent(
            tNanos = SystemClock.elapsedRealtimeNanos(),
            sensorType = type,
            accuracy = accuracy,
        )
        synchronized(lock) { accuracyEvents += event }
    }

    private fun quatFromEvent(event: SensorEvent, t: Long, accuracy: Int): TimedQuat {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val w = if (event.values.size > 3) {
            event.values[3]
        } else {
            val mag2 = x * x + y * y + z * z
            if (mag2 <= 1f) sqrt(1f - mag2) else 0f
        }
        return TimedQuat(t, x, y, z, w, accuracy)
    }

    private fun describe(stream: String, sensor: Sensor) = TraceSensorInfo(
        stream = stream,
        type = sensor.type,
        name = sensor.name,
        vendor = sensor.vendor,
    )

    companion object {
        /** Fastest sampling period allowed on API 31+ without HIGH_SAMPLING_RATE_SENSORS. */
        const val MAX_RATE_WITHOUT_HIGH_SAMPLING_US = 5_000
    }
}

/** Un-interpolated recording. Metadata (device, exercise, label) is added at export. */
data class RawSensorDump(
    val accel: List<TimedVec3>,
    val gyro: List<TimedVec3>,
    val gyroUncalibrated: List<TimedGyroUncal>,
    val rotationVector: List<TimedQuat>,
    val gameRotationVector: List<TimedQuat>,
    val accuracy: List<AccuracyEvent>,
    val sensors: List<TraceSensorInfo>,
)
