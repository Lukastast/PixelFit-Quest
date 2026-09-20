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
    private val accel = ArrayList<TimedVec3>(4096)
    private val gyro = ArrayList<TimedVec3>(4096)
    private val rotation = ArrayList<TimedQuat>(4096)
    private val linear = ArrayList<TimedVec3>(2048)

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
        val t = event.timestamp
        when (event.sensor.type) {
            Sensor.TYPE_GYROSCOPE, Sensor.TYPE_GYROSCOPE_UNCALIBRATED -> {
                synchronized(lock) {
                    gyro += TimedVec3(t, event.values[0], event.values[1], event.values[2])
                }
            }
            Sensor.TYPE_ROTATION_VECTOR, Sensor.TYPE_GAME_ROTATION_VECTOR -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val w = if (event.values.size > 3) {
                    event.values[3]
                } else {
                    val mag2 = x * x + y * y + z * z
                    if (mag2 <= 1f) sqrt(1f - mag2) else 0f
                }
                synchronized(lock) {
                    rotation += TimedQuat(t, x, y, z, w)
                }
            }
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                synchronized(lock) {
                    linear += TimedVec3(t, event.values[0], event.values[1], event.values[2])
                }
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val count: Int
                val first: Long
                val last: Long
                synchronized(lock) {
                    accel += TimedVec3(t, event.values[0], event.values[1], event.values[2])
                    count = accel.size
                    first = accel.first().tNanos
                    last = t
                }
                onAccelTick(count, first, last)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    companion object {
        /** Fastest sampling period allowed on API 31+ without HIGH_SAMPLING_RATE_SENSORS. */
        const val MAX_RATE_WITHOUT_HIGH_SAMPLING_US = 5_000
    }
}
