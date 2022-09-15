package com.hkfyg.camp.service

import android.annotation.TargetApi
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.Toast

class StepCounterService: Service(), SensorEventListener{
    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var startValue: Float? = null
    private var endValue: Float? = null

    override fun onBind(intent: Intent?): IBinder {
        return StepCounterServiceBinder()
    }

    inner class StepCounterServiceBinder: Binder(){
        fun getService(): StepCounterService{
            return this@StepCounterService
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, "step counter started", Toast.LENGTH_SHORT).show()

        this.sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        this.stepSensor = this.sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if(this.stepSensor == null){
            Toast.makeText(this, "No Step Senser", Toast.LENGTH_SHORT).show()
        } else {
            this.sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_FASTEST)
        }

        return Service.START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        this.sensorManager?.unregisterListener(this)
    }

    fun getStepCount(): Float?{
        if(this.startValue != null && this.endValue != null){
            return (this.endValue!! - this.startValue!!)
        } else if(this.startValue != null){
            return 0f
        }
        return null
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(this.startValue == null){
            this.startValue = event?.values?.get(0)
        } else {
            this.endValue = event?.values?.get(0)
        }
    }
}