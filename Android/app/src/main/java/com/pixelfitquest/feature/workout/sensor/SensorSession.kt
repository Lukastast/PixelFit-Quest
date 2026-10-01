package com.pixelfitquest.feature.workout.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Record-only phone IMU. Prefers SENSOR_DELAY_FASTEST (requires HIGH_SAMPLING_RATE_SENSORS
 * on API 31+), falls back to ≤200 Hz, then GAME delay. Interpolates onto the accel
 * timeline at snapshot. Does not analyze.
 *
 * Hot path stores samples in structure-of-arrays buffers (no per-event TimedVec3/Quat).
 * Rates, registered sensors, and snapshot→SetAnalyzer sample sequences are unchanged.
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
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED)
    private val rotationVector: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
    private val linearAccel: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    private val lock = Any()
    private val accel = TimedVec3Buffer()
    private val gyro = TimedVec3Buffer()
    private val rotation = TimedQuatBuffer()
    private val linear = TimedVec3Buffer(initialCapacity = 2048)

    @Volatile private var registered = false

    /** Sampling period actually used after [register], or null if not registered. */
    @Volatile var activeSamplingPeriodUs: Int? = null
        private set

    val hasAccelerometer: Boolean get() = accelerometer != null

    fun clear() {
        synchronized(lock) {
            accel.clear()
            gyro.clear()
            rotation.clear()
            linear.clear()
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
                return
            } catch (_: SecurityException) {
                sensorManager.unregisterListener(this)
            }
        }
        // All rates rejected — surface the same UX path as a missing accelerometer.
        onMissingAccelerometer()
    }

    private fun registerAll(accelSensor: Sensor, samplingPeriodUs: Int) {
        sensorManager.registerListener(this, accelSensor, samplingPeriodUs)
        gyroscope?.let { sensorManager.registerListener(this, it, samplingPeriodUs) }
        rotationVector?.let { sensorManager.registerListener(this, it, samplingPeriodUs) }
        linearAccel?.let { sensorManager.registerListener(this, it, samplingPeriodUs) }
    }

    fun unregister() {
        if (!registered) return
        sensorManager.unregisterListener(this)
        registered = false
        activeSamplingPeriodUs = null
    }

    fun snapshotInterpolated(): List<ImuSample> {
        val trace = synchronized(lock) {
            ImuTrace(
                accel = accel.toList(),
                gyro = gyro.toList(),
                rotation = rotation.toList(),
                linearAccel = linear.toList(),
            )
        }
        return ImuInterpolate.ontoAccel(trace)
    }

    override fun onSensorChanged(event: SensorEvent) {
        // Copy event fields once — SensorEvent.values is reused by the platform.
        val t = event.timestamp
        val type = event.sensor.type
        val values = event.values
        when (type) {
            Sensor.TYPE_GYROSCOPE, Sensor.TYPE_GYROSCOPE_UNCALIBRATED -> {
                val x = values[0]
                val y = values[1]
                val z = values[2]
                synchronized(lock) {
                    gyro.add(t, x, y, z)
                }
            }
            Sensor.TYPE_ROTATION_VECTOR, Sensor.TYPE_GAME_ROTATION_VECTOR -> {
                val x = values[0]
                val y = values[1]
                val z = values[2]
                val w = if (values.size > 3) {
                    values[3]
                } else {
                    val mag2 = x * x + y * y + z * z
                    if (mag2 <= 1f) sqrt(1f - mag2) else 0f
                }
                synchronized(lock) {
                    rotation.add(t, x, y, z, w)
                }
            }
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                val x = values[0]
                val y = values[1]
                val z = values[2]
                synchronized(lock) {
                    linear.add(t, x, y, z)
                }
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val x = values[0]
                val y = values[1]
                val z = values[2]
                val count: Int
                val first: Long
                synchronized(lock) {
                    accel.add(t, x, y, z)
                    count = accel.size
                    first = accel.firstTNanos()
                }
                onAccelTick(count, first, t)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    companion object {
        /** Fastest sampling period allowed on API 31+ without HIGH_SAMPLING_RATE_SENSORS. */
        const val MAX_RATE_WITHOUT_HIGH_SAMPLING_US = 5_000
    }
}
