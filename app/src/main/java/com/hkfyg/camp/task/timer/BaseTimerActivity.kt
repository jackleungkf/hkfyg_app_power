package com.hkfyg.camp.task.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import com.hkfyg.camp.BaseActivity
import com.hkfyg.camp.R
import com.hkfyg.camp.model.Task
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.EnumUtils
import com.hkfyg.camp.utils.Utils
import com.hkfyg.camp.widget.NavBarView
import java.util.*

abstract class BaseTimerActivity: BaseActivity(){
    var rootView: ViewGroup? = null
    var navBarView: NavBarView? = null
    var reminderTextView: TextView? = null
    var imageView: ImageView? = null
    var gifImageView: ImageView? = null
    var timerTextView: TextView? = null

    var taskPosition: Int? = null

    var task: Task? = null
    var currentPosition: Int = -1
    var currentSubTask: Task.SubTask? = null

    var timerValue: Int = 0
    var countDown: Boolean = true

    var timer: Timer? = null

    var started: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag").apply {
                if(this.isHeld()) {
                    this.release()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag").apply {
                acquire(60000)
            }
        }
    }

    override fun onBackPressed() {
        if(!this.started){
            super.onBackPressed()
        }
    }

    open fun initialize(){
        this.rootView = findViewById<ViewGroup>(R.id.rootView)
        this.navBarView = findViewById<NavBarView>(R.id.navBarView)
        this.reminderTextView = findViewById<TextView>(R.id.reminderTextView)
        this.imageView = findViewById<ImageView>(R.id.imageView)
        this.gifImageView = findViewById<ImageView>(R.id.gifImageView)
        this.timerTextView = findViewById<TextView>(R.id.timerTextView)

        this.navBarView?.backButton?.setOnClickListener{
            this.finish()
        }

        this.reminderTextView?.text = this.getLocalizedStringById(R.string.complete_remind)
///////////////////////////////////////////////////////////////////////////
//////////   update: 2021/10/28 count down setting
//        this.taskPosition = this.intent?.getIntExtra("taskPosition", -1)

//        if(this.taskPosition != null && this.taskPosition!! >= 0){
//            this.task = DataStore.taskList.list[this.taskPosition!!]
//        }
////////////////////////////////////////////////////////////////////////////
    }

    abstract fun setCurrentSubTask()

    abstract fun hasNextSubTask(): Boolean

    open fun setValues(){
        if(this.timer == null) {
            if (this.timerValue == 0) {
                this.countDown = false
            } else {
                this.countDown = true
                this.setTimerText()
            }
        }
        //this.scheduleTimer()
    }

    abstract fun setTimerValue()

    fun cancelTimer(){
        this.timer?.cancel()
        this.timer = null
    }

    fun scheduleTimer(){
        this.cancelTimer()

        this.timer = Timer()
        this.timer?.schedule(object: TimerTask(){
            override fun run() {
                if(this@BaseTimerActivity.countDown){
                    this@BaseTimerActivity.timerValue -= 1
                } else {
                    this@BaseTimerActivity.timerValue += 1
                }

                this@BaseTimerActivity.runOnUiThread{
                    if(!this@BaseTimerActivity.countDown || this@BaseTimerActivity.timerValue >= 0) {
                        this@BaseTimerActivity.setTimerText()
                    } else {
                        this@BaseTimerActivity.timerEndCallback()
                        /////////////////////////////////////////////////
                        //  1.  user == staff, update and logout
                        //  2.  user == player , logout only.
                        /////////////////////////////////////////////////
                        if(DataStore.user?.isStaff==true){
                            this@BaseTimerActivity.showAlertDialogWithoutCancellation(this@BaseTimerActivity.getLocalizedStringById(R.string.logout), this@BaseTimerActivity.getLocalizedStringById(R.string.confirm_eventComplete_message),this@BaseTimerActivity.getLocalizedStringById(R.string.logout)) {
                                this@BaseTimerActivity.showLoadingView(this@BaseTimerActivity.rootView)
                                DataStore.updatePowerTaskRecord({
                                    this@BaseTimerActivity.hideLoadingView(this@BaseTimerActivity.rootView)
                                    //this.completeButton?.visibility = View.INVISIBLE
                                    this@BaseTimerActivity.logout(this@BaseTimerActivity, false)
                                }, { sessionExpired, error ->
                                    this@BaseTimerActivity.hideLoadingView(this@BaseTimerActivity.rootView)
                                    if (sessionExpired) {
                                        this@BaseTimerActivity.logout(this@BaseTimerActivity, sessionExpired)
                                    } else {
                                        this@BaseTimerActivity.showErrorToast(this@BaseTimerActivity, sessionExpired, com.hkfyg.camp.utils.EnumUtils.DataType.EXPRESSTASKRECORDUPDATE)
                                    }
                                })
                            }
                        } else {
                            this@BaseTimerActivity.showAlertDialogWithoutCancellation(this@BaseTimerActivity.getLocalizedStringById(R.string.logout), this@BaseTimerActivity.getLocalizedStringById(R.string.confirm_eventComplete_message),this@BaseTimerActivity.getLocalizedStringById(R.string.logout)){
                                this@BaseTimerActivity.logout(this@BaseTimerActivity, false)
                            }
                        }

                    }
                }
            }
        }, 1000, 1000)
    }

    fun setTimerText(){
        this.timerTextView?.text = Utils.getTimeString(this.timerValue.toLong())
    }

    open fun timerEndCallback(){
        this.cancelTimer()
        this.dismissAlertDialog()
    }
}