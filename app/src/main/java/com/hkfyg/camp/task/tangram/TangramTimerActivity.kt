package com.hkfyg.camp.task.tangram

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.hkfyg.camp.R
import com.hkfyg.camp.task.TaskResultActivity
import com.hkfyg.camp.task.timer.BaseTimerActivity
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.EnumUtils

class TangramTimerActivity: BaseTimerActivity(){
    private var completeButton: Button? = null
    private var giveUpButtonContainer: ViewGroup? = null
    private var giveUpButton: Button? = null

    private var subTaskPosition: Int? = null
    private var currentRecordId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        this.initialize()
        this.completeButton = findViewById<Button>(R.id.completeButton)
        this.giveUpButtonContainer = findViewById<ViewGroup>(R.id.giveUpButtonContainer)
        this.giveUpButton = findViewById<Button>(R.id.giveUpButton)

        this.completeButton?.text = this.getLocalizedStringById(R.string.start)
        this.completeButton?.setOnClickListener{
            this@TangramTimerActivity.completeButtonClicked()
        }

        this.giveUpButton?.setOnClickListener{
            this@TangramTimerActivity.giveUpButtonClicked()
        }

        this.subTaskPosition = this.intent?.getIntExtra("subTaskPosition", -1)

        this.setCurrentSubTask()
    }

    override fun setCurrentSubTask() {
        if(this.hasNextSubTask()){
            this.currentPosition += 1
        }

        if(this.subTaskPosition != null && this.subTaskPosition!! >= 0 && this.task?.subtasks != null && this.task?.subtasks!!.size > this.subTaskPosition!!) {
            this.currentSubTask = this.task?.subtasks?.get(this.subTaskPosition!!)
        }

        this.setValues()
    }

    override fun hasNextSubTask(): Boolean {
        return this.currentPosition < DataStore.tangramItemList.list.size - 1
    }

    override fun setValues(){
        this.navBarView?.titleTextView?.text = DataStore.tangramItemList.list.get(this.currentPosition).name

        this.imageView?.setImageResource(R.drawable.tangram1)

        if(!this.started){
            this.setTimerValue()
        }

        super.setValues()
    }

    override fun setTimerValue() {
        this.currentSubTask?.timeLimit?.let{
            this.timerValue = it
        }
    }

    override fun timerEndCallback() {
        super.timerEndCallback()

        if(this.loading){
            return
        }

        // show result
        val intent = Intent(this, TaskResultActivity::class.java)
        intent.putExtra("taskPosition", this.taskPosition)
        intent.putExtra("subTaskPosition", this.subTaskPosition)
        this.startActivity(intent)
        this.setResult(android.app.Activity.RESULT_OK)
        this.finish()
    }

    private fun completeButtonClicked(){
        if(this.loading || this.currentSubTask == null){
            return
        }

        if(!this.started){
            val message = this.getLocalizedStringById(R.string.confirm_start_message)
            this.showAlertDialog(this.getLocalizedStringById(R.string.start), message, {
                DataStore.tangramItemList.list.get(this.currentPosition).id?.let{
                    this.showLoadingView(this.rootView)
                    DataStore.createTangramTaskRecord(it, { nextRecordId ->
                        this.currentRecordId = nextRecordId
                        this.hideLoadingView(this.rootView)
                        this.scheduleTimer()
                        this.started = true
                        this.navBarView?.backButton?.visibility = View.INVISIBLE
                        this.completeButton?.text = this.getLocalizedStringById(R.string.complete)
                        this.completeButton?.background = this.resources?.getDrawable(R.drawable.background_capsule_dark_green)
                        this.giveUpButtonContainer?.visibility = View.VISIBLE
                    }, { sessionExpired, error ->
                        this.hideLoadingView(this.rootView)
                        if(sessionExpired){
                            this.logout(this, sessionExpired)
                        } else {
                            this.showErrorToast(this, error, EnumUtils.DataType.TANGRAMTASKRECORDCREATE)
                        }
                    })
                }
            })
        } else {
            this.showAlertDialog(this.getLocalizedStringById(R.string.complete), this.getLocalizedStringById(R.string.confirm_complete_message), {
                this.endTangramRecord(true)
            })
        }
    }

    private fun giveUpButtonClicked(){
        if(this.loading || this.currentSubTask == null){
            return
        }

        this.showAlertDialog(this.getLocalizedStringById(R.string.give_up), this.getLocalizedStringById(R.string.confirm_give_up_message), {
            this.endTangramRecord(false)
        })
    }

    private fun endTangramRecord(success: Boolean){
        if(this.loading){
            return
        }

        if(this.currentPosition >= 0) {
            this.currentRecordId?.let {
                var nextItemId: Int? = null
                if(this.hasNextSubTask()){
                    nextItemId = DataStore.tangramItemList.list.get(this.currentPosition + 1).id
                }

                this.showLoadingView(this.rootView)
                DataStore.endTangramRecord(it, success, nextItemId, { nextRecordId ->
                    this.currentRecordId = nextRecordId
                    this.hideLoadingView(this.rootView)
                    if (nextItemId != null && this.timerValue > 0) {
                        this.setCurrentSubTask()
                    } else {
                        this.timerEndCallback()
                    }
                }, { sessionExpired, error ->
                    this.hideLoadingView(this.rootView)
                    if (sessionExpired) {
                        this.logout(this, sessionExpired)
                    } else {
                        this.showErrorToast(this, error, EnumUtils.DataType.TANGRAMRECORDEND)
                    }
                })
            }
        }
    }
}