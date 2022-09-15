package com.hkfyg.camp.task.magiccircle

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.hkfyg.camp.R
import com.hkfyg.camp.task.timer.BaseTimerActivity
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.EnumUtils

class MagicCircleTimerActivity: BaseTimerActivity(){
    private var completeButton: Button? = null
    private var giveUpButtonContainer: ViewGroup? = null
    private var giveUpButton: Button? = null

    private var subTaskPosition: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        this.initialize()
        this.completeButton = findViewById<Button>(R.id.completeButton)
        this.giveUpButtonContainer = findViewById<ViewGroup>(R.id.giveUpButtonContainer)
        this.giveUpButton = findViewById<Button>(R.id.giveUpButton)

        this.giveUpButton?.text = this.getLocalizedStringById(R.string.give_up)
        this.giveUpButton?.background = this.resources.getDrawable(R.drawable.background_capsule_dark_green)

        this.completeButton?.text = this.getLocalizedStringById(R.string.start)
        this.completeButton?.setOnClickListener{
            this@MagicCircleTimerActivity.completeButtonClicked()
        }

        this.giveUpButton?.setOnClickListener{
            this@MagicCircleTimerActivity.giveUpButtonClicked()
        }

        this.subTaskPosition = this.intent?.getIntExtra("subTaskPosition", -1)

        this.setCurrentSubTask()
    }

    override fun setCurrentSubTask() {
        this.subTaskPosition?.let{
            this.currentSubTask = this.task?.subtasks?.get(it)
        }

        this.setValues()
    }

    override fun hasNextSubTask(): Boolean {
        return false
    }

    override fun setValues(){
        this.navBarView?.titleTextView?.text = this.currentSubTask?.name

        this.imageView?.setImageResource(R.drawable.magiccircle1)

        if(!this.started){
            this.setTimerValue()
        }

        super.setValues()
    }

    override fun setTimerValue(){
        this.currentSubTask?.timeLimit?.let{
            this.timerValue = it
        }
    }

    override fun timerEndCallback(){
        super.timerEndCallback()

        if(this.loading){
            return
        }

        if(DataStore.user?.taskRecords?.magicCircleTaskRecord?.endTime == null) {
            this.endMagicCircleTaskRecord()
        } else {
            this.showResultActivity()
        }
    }

    private fun completeButtonClicked(){
        if(this.loading || this.currentSubTask == null){
            return
        }

        if(!this.started){
            this.showAlertDialog(this.getLocalizedStringById(R.string.start), this.getLocalizedStringById(R.string.confirm_start_message), {
                this.createMagicCircleTaskRecord()
            })
        } else {
            this.showAlertDialog(this.getLocalizedStringById(R.string.complete), this.getLocalizedStringById(R.string.confirm_complete_message), {
                this.endMagicCircleTaskRecord()
            })
        }
    }

    private fun giveUpButtonClicked(){
        if(this.loading || this.currentSubTask == null){
            return
        }

        this.showAlertDialog(this.getLocalizedStringById(R.string.give_up), this.getLocalizedStringById(R.string.confirm_give_up_message), {
            this.endMagicCircleTaskRecord()
        })
    }

    private fun createMagicCircleTaskRecord(){
        this.showLoadingView(this.rootView)
        DataStore.createMagicCircleTaskRecord({ _ ->
            this.hideLoadingView(this.rootView)
            this.scheduleTimer()
            this.started = true
            this.navBarView?.backButton?.visibility = View.INVISIBLE
            this.giveUpButtonContainer?.visibility = View.VISIBLE
            this.completeButton?.text = this.getLocalizedStringById(R.string.complete)
        }, { sessionExpired, error ->
            this.hideLoadingView(this.rootView)
            if(sessionExpired){
                this.logout(this, sessionExpired)
            } else {
                this.showErrorToast(this, error, EnumUtils.DataType.MAGICCIRCLETASKRECORDCREATE)
            }
        })
    }

    private fun endMagicCircleTaskRecord(){
        this.currentSubTask?.timeLimit?.let {
            this.showLoadingView(this.rootView)
            DataStore.endMagicCircleTaskRecord(it, { _ ->
                this.hideLoadingView(this.rootView)
                this.timerEndCallback()
            }, { sessionExpired, error ->
                this.hideLoadingView(this.rootView)
                if (sessionExpired) {
                    this.logout(this, sessionExpired)
                } else {
                    this.showErrorToast(this, error, EnumUtils.DataType.MAGICCIRCLETASKRECORDEND)
                }
            })
        }
    }

    private fun showResultActivity(){
        val intent = Intent(this, MagicCircleResultActivity::class.java)
        intent.putExtra("taskPosition", this.taskPosition)
        intent.putExtra("subTaskPosition", this.subTaskPosition)
        intent.putExtra("update", true)
        this.startActivity(intent)
        this.setResult(android.app.Activity.RESULT_OK)
        this.finish()
    }
}