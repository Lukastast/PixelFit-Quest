package com.pixelfitquest.feature.workout.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Record-only phone IMU. Buffers raw timestamped streams at SENSOR_DELAY_FASTEST
 * and interpolates onto the accel timeline at snapshot. Does not analyze.
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
        val rate = SensorManager.SENSOR_DELAY_FASTEST
        sensorManager.registerListener(this, accelSensor, rate)
        gyroscope?.let { sensorManager.registerListener(this, it, rate) }
        rotationVector?.let { sensorManager.registerListener(this, it, rate) }
        linearAccel?.let { sensorManager.registerListener(this, it, rate) }
        registered = true
    }

    fun unregister() {
        if (!registered) return
        sensorManager.unregisterListener(this)
        registered = false
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
}
