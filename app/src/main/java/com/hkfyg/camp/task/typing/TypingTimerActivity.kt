package com.hkfyg.camp.task.typing

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.hkfyg.camp.R
import com.hkfyg.camp.task.TaskResultActivity
import com.hkfyg.camp.task.timer.BaseTimerActivity
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.EnumUtils
import com.hkfyg.camp.widget.InputView

class TypingTimerActivity: BaseTimerActivity(){
    private var completeButton: Button? = null
    private var textView: TextView? = null
    private var resultInputViewContainer: ViewGroup? = null
    private var resultInputView: InputView? = null

    private var subTaskPosition: Int = -1
    private var currentRecordId: Int? = null

    private var updated: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_typing_timer)

        this.initialize()
        this.completeButton = findViewById<Button>(R.id.completeButton)
        this.textView = findViewById<TextView>(R.id.textView)
        this.resultInputViewContainer = findViewById<ViewGroup>(R.id.resultInputViewContainer)
        this.resultInputView = findViewById<InputView>(R.id.resultInputView)

        this.resultInputView?.textView?.text = this.getLocalizedStringById(R.string.correctly_typed_count)
        this.resultInputView?.editText?.inputType = InputType.TYPE_CLASS_NUMBER

        this.completeButton?.text = this.getLocalizedStringById(R.string.start)

        this.completeButton?.setOnClickListener{
            this@TypingTimerActivity.completeButtonClicked()
        }

        this.subTaskPosition = this.intent.getIntExtra("subTaskPosition", -1)

        this.setCurrentSubTask()
    }

    override fun setCurrentSubTask(){
        if(this.hasNextSubTask()){
            this.currentPosition += 1
        }

        if(this.subTaskPosition >= 0 && this.task?.subtasks != null && this.task!!.subtasks!!.size > this.subTaskPosition){
            this.currentSubTask = this.task?.subtasks?.get(this.subTaskPosition!!)
        }

        this.setValues()
    }

    override fun hasNextSubTask(): Boolean {
        return this.currentPosition < DataStore.typingScriptList.list.size - 1
    }

    override fun setValues(){
        var taskName = ""

        DataStore.typingScriptList.list.get(this.currentPosition).let {
            when (it.language) {
                "en" -> taskName = this.getLocalizedStringById(R.string.english_typing)
                "zh" -> taskName = this.getLocalizedStringById(R.string.chinese_typing)
                else -> {}
            }
            textView?.text = it.text
        }
        this.navBarView?.titleTextView?.text = String.format(this.getLocalizedStringById(R.string.test_format), taskName)

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

    override fun timerEndCallback(){
        super.timerEndCallback()

        if(this.loading){
            return
        }

        if(!this.updated){
            this.timerTextView?.setTextColor(this.resources.getColor(R.color.colorAccent))
            this.showResultInputView()
        } else {
            val intent = Intent(this, TaskResultActivity::class.java)
            intent.putExtra("taskPosition", this.taskPosition)
            intent.putExtra("subTaskPosition", this.subTaskPosition)
            this.startActivity(intent)
            this.setResult(Activity.RESULT_OK)
            this.finish()
        }
    }

    private fun completeButtonClicked(){
        if(this.loading){
            return
        }

        if(!this.started) {
            this.showAlertDialog(this.getLocalizedStringById(R.string.start), this.getLocalizedStringById(R.string.confirm_start_message), {
                if (this.hasNextSubTask()) {
                    DataStore.typingScriptList.list.get(this.currentPosition).id?.let {
                        this.showLoadingView(this.rootView)
                        DataStore.createTypingTaskRecord(it, { nextRecordId ->
                            this.hideLoadingView(this.rootView)
                            this.currentRecordId = nextRecordId
                            this.started = true
                            this.scheduleTimer()
                            this.navBarView?.backButton?.visibility = View.INVISIBLE
                            this.completeButton?.text = this.getLocalizedStringById(R.string.complete)
                        }, { sessionExpired, error ->
                            this.hideLoadingView(this.rootView)
                            if (sessionExpired) {
                                this.logout(this, sessionExpired)
                            } else {
                                this.showErrorToast(this, error, EnumUtils.DataType.TYPINGSCRIPTLIST)
                            }
                        })
                    }
                }
            })
        } else if(!this.updated && this.resultInputViewContainer?.visibility == View.GONE){
            this.showAlertDialog(this.getLocalizedStringById(R.string.complete), this.getLocalizedStringById(R.string.confirm_complete_message), {
                this.showResultInputView()
            })
        } else if(!this.updated && this.resultInputViewContainer?.visibility == View.VISIBLE) {
            this.dismissKeyboard(this)

            val correctCount = this.resultInputView?.editText?.text?.toString()?.toIntOrNull()

            if(correctCount == null){
                Toast.makeText(this, String.format(this.getLocalizedStringById(R.string.please_input), this.getLocalizedStringById(R.string.correctly_typed_count)), Toast.LENGTH_SHORT).show()
                return
            }

            this.currentRecordId?.let {
                val nextScriptId = when(this.hasNextSubTask()){
                    false -> null
                    else -> DataStore.typingScriptList.list.get(this.currentPosition + 1).id
                }

                this.showLoadingView(this.rootView)
                DataStore.endTypingTaskRecord(it, correctCount, nextScriptId, { nextRecordId ->
                    this.hideLoadingView(this.rootView)
                    this.currentRecordId = nextRecordId

                    if(!this.hasNextSubTask() || this.timerValue <= 0){
                        this.updated = true
                        this.timerEndCallback()
                    } else {
                        this.setCurrentSubTask()
                        this.hideResultInputView()
                    }
                }, { sessionExpired, error ->
                    this.hideLoadingView(this.rootView)
                    this.showErrorToast(this, error, EnumUtils.DataType.TYPINGTASKRECORDEND)
                })
            }
        } else if(this.updated){
            if(this.timerValue <= 0){
                this.timerEndCallback()
            } else {
                this.hideResultInputView()
            }
        }
    }

    private fun showResultInputView(){
        this.resultInputView?.editText?.setText("")

        this.textView?.visibility = View.GONE
        this.resultInputViewContainer?.visibility = View.VISIBLE
    }

    private fun hideResultInputView(){
        this.textView?.visibility = View.VISIBLE
        this.resultInputViewContainer?.visibility = View.GONE
    }
}