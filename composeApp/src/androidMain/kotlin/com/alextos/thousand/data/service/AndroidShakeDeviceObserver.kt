package com.alextos.thousand.data.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.alextos.thousand.domain.service.ShakeDeviceObserver
import com.alextos.thousand.domain.service.ShakeDeviceObserverDelegate
import kotlin.math.sqrt

class AndroidShakeDeviceObserver(
    context: Context,
) : ShakeDeviceObserver, SensorEventListener {
    override var delegate: ShakeDeviceObserverDelegate? = null

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var lastShakeAtMillis: Long = 0L
    private var isStarted: Boolean = false

    fun start() {
        if (isStarted) return
        accelerometer ?: return

        isStarted = sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_UI,
        )
    }

    fun stop() {
        if (!isStarted) return

        sensorManager.unregisterListener(this)
        isStarted = false
    }

    override fun shake() {
        delegate?.deviceDidShake()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val gravity = calculateGravity(event.values)
        if (gravity < SHAKE_THRESHOLD_GRAVITY) return

        val now = System.currentTimeMillis()
        if (now - lastShakeAtMillis < SHAKE_COOLDOWN_MILLIS) return

        lastShakeAtMillis = now
        shake()
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int,
    ) = Unit

    private fun calculateGravity(values: FloatArray): Float {
        val x = values.getOrNull(0) ?: 0f
        val y = values.getOrNull(1) ?: 0f
        val z = values.getOrNull(2) ?: 0f
        val acceleration = sqrt(x * x + y * y + z * z)
        return acceleration / SensorManager.GRAVITY_EARTH
    }

    private companion object {
        const val SHAKE_THRESHOLD_GRAVITY = 2.7f
        const val SHAKE_COOLDOWN_MILLIS = 700L
    }
}
