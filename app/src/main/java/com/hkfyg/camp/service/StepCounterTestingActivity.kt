package com.hkfyg.camp.service

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.TextView
import com.hkfyg.camp.BaseActivity
import com.hkfyg.camp.R

class StepCounterTestingActivity: BaseActivity(){
    private var textView: TextView? = null
    private var toggleButton: Button? = null

    private var serviceIntent: Intent? = null
    private var service: StepCounterService? = null

    private val connection = object: ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as StepCounterService.StepCounterServiceBinder
            this@StepCounterTestingActivity.service = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            this@StepCounterTestingActivity.service = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_step_counter_testing)

        this.textView = findViewById<TextView>(R.id.textView)
        this.toggleButton = findViewById<Button>(R.id.toggleButton)

        this.toggleButton?.text = this.getLocalizedStringById(R.string.start)

        this.serviceIntent = Intent(this.applicationContext, StepCounterService::class.java)

        this.toggleButton?.setOnClickListener{
            if(!this.isServiceRunning(StepCounterService::class.java)){
                this.bindService(this.serviceIntent, this.connection, Context.BIND_AUTO_CREATE)
                this.startService(this.serviceIntent)

                this.toggleButton?.text = this.getLocalizedStringById(R.string.complete)
            } else {
                val stepCount = this.service?.getStepCount()
                this.stopService(this.serviceIntent)
                this.unbindService(this.connection)

                this.toggleButton?.text = this.getLocalizedStringById(R.string.start)
                this.textView?.text = stepCount.toString()
            }
        }
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean{
        val activityManager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        for(service in activityManager.getRunningServices(Integer.MAX_VALUE)){
            if(serviceClass.name == service.service.className) {
                return true
            }
        }

        return false
    }
}