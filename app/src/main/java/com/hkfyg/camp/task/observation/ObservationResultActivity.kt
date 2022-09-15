package com.hkfyg.camp.task.observation

import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import com.hkfyg.camp.BaseActivity
import com.hkfyg.camp.R
import com.hkfyg.camp.model.Task
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.Utils
import com.hkfyg.camp.widget.InputView
import com.hkfyg.camp.widget.NavBarView

class ObservationResultActivity: BaseActivity(){
    private var rootView: ViewGroup? = null
    private var navBarView: NavBarView? = null
    private var imageView: ImageView? = null
    private var imageNumberInputView: InputView? = null
    private var timeInputView: InputView? = null

    private var taskPosition: Int = -1
    private var subTaskPosition: Int = -1
    private var subTask: Task.SubTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_observation_result)

        this.rootView = findViewById<ViewGroup>(R.id.rootView)
        this.navBarView = findViewById<NavBarView>(R.id.navBarView)
        this.imageView = findViewById<ImageView>(R.id.imageView)
        this.imageNumberInputView = findViewById<InputView>(R.id.imageNumberInputView)
        this.timeInputView = findViewById<InputView>(R.id.timeInputView)

        this.navBarView?.backButton?.setOnClickListener{
            this.finish()
        }

        this.imageNumberInputView?.editText?.isEnabled = false
        this.timeInputView?.editText?.isEnabled = false

        this.taskPosition = this.intent.getIntExtra("taskPosition", -1)
        this.subTaskPosition = this.intent.getIntExtra("subTaskPosition", -1)

        if(this.taskPosition >=0 && this.subTaskPosition >= 0){
            this.subTask = DataStore.taskList.list.get(this.taskPosition)?.subtasks?.get(this.subTaskPosition)
        }

        this.setValues()
        this.updateDisplayLanguage()
    }

    private fun setValues(){
        this.navBarView?.titleTextView?.text = this.subTask?.name
        val record = DataStore.user?.taskRecords?.observationTaskRecord
        if(record?.createdTime != null && record.endTime != null) {
            val imageNumber = record.records?.size
            val timeLimitInSeconds = when(this.subTask?.timeLimit){
                null -> 0
                else -> this.subTask!!.timeLimit!!
            }
            val timeLeftString = Utils.getTimeLeftString(record.createdTime!!, record.endTime!!, "yyyy-MM-dd'T'HH:mm:ss'Z'", timeLimitInSeconds)
            this.imageNumberInputView?.editText?.setText(String.format(this.getLocalizedStringById(R.string.number_of_image), imageNumber))
            this.timeInputView?.editText?.setText(timeLeftString)
        }
    }

    private fun updateDisplayLanguage(){
        this.imageNumberInputView?.textView?.text = this.getLocalizedStringById(R.string.number_of_image_found)
        this.timeInputView?.textView?.text = this.getLocalizedStringById(R.string.finish_time_left)
    }
}