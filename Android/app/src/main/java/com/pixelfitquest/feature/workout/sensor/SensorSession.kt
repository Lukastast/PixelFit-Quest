package com.pixelfitquest.feature.workout.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Record-only IMU session. Merges accel (required) with last gyro / rotation
 * vector. Does not analyze samples.
 */
class SensorSession(
    private val sensorManager: SensorManager,
    private val onSample: (ImuSample) -> Unit,
    private val onMissingAccelerometer: () -> Unit = {},
) : SensorEventListener {

    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val rotationVector: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    @Volatile private var lastGx: Float? = null
    @Volatile private var lastGy: Float? = null
    @Volatile private var lastGz: Float? = null
    @Volatile private var lastQx: Float? = null
    @Volatile private var lastQy: Float? = null
    @Volatile private var lastQz: Float? = null
    @Volatile private var lastQw: Float? = null

    val hasAccelerometer: Boolean get() = accelerometer != null

    fun register() {
        val accel = accelerometer
        if (accel == null) {
            onMissingAccelerometer()
            return
        }
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME)
        gyroscope?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        rotationVector?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    fun unregister() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_GYROSCOPE -> {
                lastGx = event.values[0]
                lastGy = event.values[1]
                lastGz = event.values[2]
            }
            Sensor.TYPE_ROTATION_VECTOR -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val w = if (event.values.size > 3) {
                    event.values[3]
                } else {
                    val mag2 = x * x + y * y + z * z
                    if (mag2 <= 1f) sqrt(1f - mag2) else 0f
                }
                lastQx = x
                lastQy = y
                lastQz = z
                lastQw = w
            }
            Sensor.TYPE_ACCELEROMETER -> {
                onSample(
                    ImuSample(
                        tNanos = event.timestamp,
                        ax = event.values[0],
                        ay = event.values[1],
                        az = event.values[2],
                        gx = lastGx,
                        gy = lastGy,
                        gz = lastGz,
                        qx = lastQx,
                        qy = lastQy,
                        qz = lastQz,
                        qw = lastQw,
                    )
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
