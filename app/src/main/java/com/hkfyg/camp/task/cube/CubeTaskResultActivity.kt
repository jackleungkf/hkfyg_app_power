package com.hkfyg.camp.task.cube

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.hkfyg.camp.BaseActivity
import com.hkfyg.camp.R
import com.hkfyg.camp.model.Task
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.EnumUtils
import com.hkfyg.camp.widget.InputView
import com.hkfyg.camp.widget.NavBarView
import kotlinx.android.synthetic.main.activity_timer.*

class CubeTaskResultActivity: BaseActivity(){
    private var rootView: ViewGroup? = null
    private var nabBarView: NavBarView? = null
    private var inputView: InputView? = null
    private var completeButton: Button? = null

    private var subTask: Task.SubTask? = null

    private var isUpdate: Boolean? = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cube_task_result_input)

        this.rootView = this.findViewById<ViewGroup>(R.id.rootView)
        this.nabBarView = this.findViewById<NavBarView>(R.id.navBarView)
        this.inputView = this.findViewById<InputView>(R.id.inputView)
        this.completeButton = this.findViewById<Button>(R.id.completeButton)

        this.navBarView?.backButton?.setOnClickListener{
            this.finish()
        }

        this.completeButton?.setOnClickListener{
            this.completeButtonClicked()
        }

        this.inputView?.editText?.inputType = InputType.TYPE_CLASS_NUMBER.or(InputType.TYPE_NUMBER_FLAG_DECIMAL)

        val taskPosition = this.intent?.getIntExtra("taskPosition", -1)
        val subTaskPosition = this.intent?.getIntExtra("subTaskPosition", -1)
        this.isUpdate = this.intent?.getBooleanExtra("isUpdate", false)

        if(taskPosition != null && taskPosition >= 0 && subTaskPosition != null && subTaskPosition >= 0){
            this.subTask = DataStore.taskList.list[taskPosition].subtasks?.get(subTaskPosition)
        }

        this.updateDisplayLanguage()
        this.setValues()
    }

    override fun onBackPressed() {
        val isUpdate = this.intent?.getBooleanExtra("isUpdate", false)
        when(isUpdate){
            false -> super.onBackPressed()
            else -> {}
        }
    }

    private fun updateDisplayLanguage(){
        this.inputView?.textView?.text = this.getLocalizedStringById(R.string.completed_cube_count)
    }

    private fun setValues(){
        this.navBarView?.titleTextView?.text = this.subTask?.name
        when(this.isUpdate){
            true -> {
                this.nabBarView?.backButton?.visibility = View.INVISIBLE
                this.inputView?.editText?.isEnabled = true
            }
            else -> {
                this.nabBarView?.backButton?.visibility = View.VISIBLE
                this.inputView?.editText?.isEnabled = false
                // TO-DO: set values
                this.completeButton?.visibility = View.GONE
            }
        }
    }

    private fun completeButtonClicked(){
        this.dismissKeyboard(this)

        if(this.loading){
            return
        }

        val countString = this.inputView?.editText?.text?.toString()
        var count: Double? = null
        try {
            count = countString?.toDoubleOrNull()
        } catch (e: Exception){}

        if(count == null) {
            Toast.makeText(this, String.format(this.getLocalizedStringById(R.string.please_input), this.getLocalizedStringById(R.string.completed_cube_count)), Toast.LENGTH_SHORT).show()
            return
        } else {
            DataStore.updateCubeTaskRecord(count, { response ->
                this.nabBarView?.backButton?.visibility = View.VISIBLE
                this.inputView?.editText?.isEnabled = false
                this.completeButton?.visibility = View.GONE
            }, { sessionExpired, error ->
                if(sessionExpired){
                    this.logout(this, sessionExpired)
                } else {
                    this.showErrorToast(this, error, EnumUtils.DataType.CUBETASKRECORDUPDATE)
                }
            })
        }
    }
}