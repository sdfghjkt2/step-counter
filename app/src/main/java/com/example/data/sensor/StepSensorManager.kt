package com.example.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class StepSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepDetectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private val _stepEvents = MutableSharedFlow<Int>(replay = 0)
    val stepEvents: SharedFlow<Int> = _stepEvents.asSharedFlow()

    val isSensorAvailable: Boolean = stepCounterSensor != null || stepDetectorSensor != null

    private var initialStepCount = -1f

    fun startListening() {
        stepCounterSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        } ?: stepDetectorSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                val totalStepsSinceBoot = it.values[0]
                if (initialStepCount < 0) {
                    initialStepCount = totalStepsSinceBoot
                } else {
                    val delta = (totalStepsSinceBoot - initialStepCount).toInt()
                    if (delta > 0) {
                        initialStepCount = totalStepsSinceBoot
                        _stepEvents.tryEmit(delta)
                    }
                }
            } else if (it.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                if (it.values[0] == 1.0f) {
                    _stepEvents.tryEmit(1)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
