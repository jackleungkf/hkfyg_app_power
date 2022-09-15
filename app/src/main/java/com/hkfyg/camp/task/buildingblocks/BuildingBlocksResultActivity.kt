package com.hkfyg.camp.task.buildingblocks

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.hkfyg.camp.BaseActivity
import com.hkfyg.camp.R
import com.hkfyg.camp.model.Task
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.EnumUtils
import com.hkfyg.camp.widget.InputView
import com.hkfyg.camp.widget.NavBarView

class BuildingBlocksResultActivity: BaseActivity(){
    private var rootView: ViewGroup? = null
    private var navBarView: NavBarView? = null
    private var imageView: ImageView? = null
    private var usedItemNumberInputView: InputView? = null
    private var modelBuiltNumberInputView: InputView? = null
    private var similarityInputView: InputView? = null
    private var completeButton: Button? = null

    private var taskPosition: Int = -1
    private var subTaskPosition: Int = -1
    private var subTask: Task.SubTask? = null
    private var update: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_building_blocks_result)

        this.rootView = findViewById<ViewGroup>(R.id.rootView)
        this.navBarView = findViewById<NavBarView>(R.id.navBarView)
        this.imageView = findViewById<ImageView>(R.id.imageView)
        this.usedItemNumberInputView = findViewById<InputView>(R.id.usedItemNumberInputView)
        this.modelBuiltNumberInputView = findViewById<InputView>(R.id.modelbuiltNumberInputView)
        this.similarityInputView = findViewById<InputView>(R.id.similarityInputView)
        this.completeButton = findViewById(R.id.completeButton)

        this.navBarView?.backButton?.setOnClickListener{
            this.finish()
        }

        this.modelBuiltNumberInputView?.editText?.isEnabled = false
        this.similarityInputView?.editText?.isEnabled = false

        this.taskPosition = this.intent.getIntExtra("taskPosition", -1)
        this.subTaskPosition = this.intent.getIntExtra("subTaskPosition", -1)

        if(this.taskPosition >= 0 && this.subTaskPosition >= 0){
            this.subTask = DataStore.taskList.list.get(this.taskPosition)?.subtasks?.get(this.subTaskPosition)
        }

        this.completeButton?.setOnClickListener{
            this@BuildingBlocksResultActivity.completeButtonClicked()
        }

        this.update = this.intent.getBooleanExtra("update", false)

        this.setValues()
        this.updateDisplayLanguage()
        this.updateLayouts()
    }

    override fun onBackPressed() {
        if(this.update == null){
            this.update = this.intent.getBooleanExtra("update", false)
        }

        if(!this.update!!){
            super.onBackPressed()
        }
    }

    private fun updateDisplayLanguage(){
        this.usedItemNumberInputView?.textView?.text = this.getLocalizedStringById(R.string.number_of_items_used)
        this.modelBuiltNumberInputView?.textView?.text = this.getLocalizedStringById(R.string.number_of_models_built)
        this.similarityInputView?.textView?.text = this.getLocalizedStringById(R.string.similarity)
        this.completeButton?.text = this.getLocalizedStringById(R.string.complete)
    }

    private fun setValues(){
        this.subTask?.let{
            this.navBarView?.titleTextView?.text = this.subTask?.name
        }

        DataStore.user?.taskRecords?.buildingBlockTasksRecord?.let{
            val usedItemNumber = when(it.usedItemNumber){
                null -> 0
                else -> it.usedItemNumber!!
            }

            var modelBuiltNumber: Int = 0
            var similarity: Double = 0.0

            if(it.records != null) {
                modelBuiltNumber = it.records!!.size
                for (record in it.records!!) {
                    record.similarity?.let {
                        similarity += it
                    }
                }
            }

            if(modelBuiltNumber > 0){
                similarity = similarity * 100 / modelBuiltNumber.toDouble()
            }

            if(this.update == null || !this.update!!){
                this.usedItemNumberInputView?.editText?.setText(usedItemNumber.toString())
            }
            this.modelBuiltNumberInputView?.editText?.setText(modelBuiltNumber.toString())
            if(similarity == Math.floor(similarity)) {
                this.similarityInputView?.editText?.setText(String.format("%d%%", similarity.toInt()))
            } else {
                this.similarityInputView?.editText?.setText(String.format("%.2f%%", similarity))
            }
        }
    }

    private fun updateLayouts(){
        if(this.update != null && this.update!!){
            this.completeButton?.visibility = View.VISIBLE
            this.navBarView?.backButton?.visibility = View.INVISIBLE
            this.usedItemNumberInputView?.editText?.inputType = InputType.TYPE_CLASS_NUMBER

            this.usedItemNumberInputView?.editText?.isEnabled = true
        } else {
            this.completeButton?.visibility = View.GONE
            this.navBarView?.backButton?.visibility = View.VISIBLE

            this.usedItemNumberInputView?.editText?.isEnabled = false
        }
    }

    private fun completeButtonClicked(){
        this.dismissKeyboard(this)

        val usedItemNumber = this.usedItemNumberInputView?.editText?.text?.toString()?.toIntOrNull()

        if(usedItemNumber == null){
            Toast.makeText(this, String.format(this.getLocalizedStringById(R.string.please_input), this.getLocalizedStringById(R.string.number_of_items_used)), Toast.LENGTH_SHORT).show()
            return
        }

        this.showLoadingView(this.rootView)
        DataStore.updateBuildingBlocksTaskRecord(usedItemNumber, { _ ->
            this.hideLoadingView(this.rootView)
            this.update = false
            this.updateLayouts()
        }, { sessionExpired, error ->
            this.hideLoadingView(this.rootView)
            this.showErrorToast(this, error, EnumUtils.DataType.BUILDINGBLOCKSTASKRECORDUPDATE)
        })
    }
}