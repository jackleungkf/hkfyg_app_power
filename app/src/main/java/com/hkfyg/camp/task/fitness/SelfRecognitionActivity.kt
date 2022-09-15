package com.hkfyg.camp.task.fitness

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.hkfyg.camp.BaseActivity
import com.hkfyg.camp.R
import com.hkfyg.camp.adapter.SelectionListAdapter
import com.hkfyg.camp.model.local.Gender
import com.hkfyg.camp.model.taskrecords.SelfRecognitionTaskRecord
import com.hkfyg.camp.model.Task
import com.hkfyg.camp.network.CallServer
import com.hkfyg.camp.task.TaskResultActivity
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.EnumUtils
import com.hkfyg.camp.widget.InputView
import com.hkfyg.camp.widget.NavBarView
import org.json.JSONObject

class SelfRecognitionActivity: BaseActivity(){
    private var rootView: ViewGroup? = null
    private var navBarView: NavBarView? = null
    private var instructionTextView: TextView? = null
    private var bodyFatRatioInputView: InputView? = null
    private var orginFatInputView: InputView? = null
    private var muscleInputView: InputView? = null
    private var metabolismRateInputView: InputView? = null
    private var bodyAgeInputView: InputView? = null
    private var bodyShapeInputView: InputView? = null
    private var bmiInputView: InputView? = null
    private var genderInputView: InputView? = null
    private var submitButton: Button? = null

    private var selectedGenderIndex: Int = -1

    private var taskPosition: Int = -1
    private var subTaskPosition: Int = -1

    private var genderList: ArrayList<Gender> = arrayListOf()

    private var subTask: Task.SubTask? = null

    private var update: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_self_recognition)

        this.rootView = findViewById<ViewGroup>(R.id.rootView)
        this.navBarView = findViewById<NavBarView>(R.id.navBarView)
        this.instructionTextView = findViewById<TextView>(R.id.instructionTextView)
        this.bodyFatRatioInputView = findViewById<InputView>(R.id.bodyFatRatioInputView)
        this.orginFatInputView = findViewById<InputView>(R.id.orginFatInputView)
        this.muscleInputView = findViewById<InputView>(R.id.muscleInputView)
        this.metabolismRateInputView = findViewById<InputView>(R.id.metabolismRateInputView)
        this.bodyAgeInputView = findViewById<InputView>(R.id.bodyAgeInputView)
        this.bodyShapeInputView = findViewById<InputView>(R.id.bodyShapeInputView)
        this.genderInputView = findViewById<InputView>(R.id.genderInputView)
        this.bmiInputView = findViewById<InputView>(R.id.bmiInputView)
        this.submitButton = findViewById<Button>(R.id.submitButton)

        this.navBarView?.backButton?.setOnClickListener{
            this@SelfRecognitionActivity.finish()
        }

        this.bodyFatRatioInputView?.editText?.inputType = InputType.TYPE_CLASS_NUMBER.or(InputType.TYPE_NUMBER_FLAG_DECIMAL)
        this.orginFatInputView?.editText?.inputType = InputType.TYPE_CLASS_NUMBER.or(InputType.TYPE_NUMBER_FLAG_DECIMAL)
        this.muscleInputView?.editText?.inputType = InputType.TYPE_CLASS_NUMBER.or(InputType.TYPE_NUMBER_FLAG_DECIMAL)
        this.metabolismRateInputView?.editText?.inputType = InputType.TYPE_CLASS_NUMBER.or(InputType.TYPE_NUMBER_FLAG_DECIMAL)
        this.bodyAgeInputView?.editText?.inputType = InputType.TYPE_CLASS_NUMBER.or(InputType.TYPE_NUMBER_FLAG_DECIMAL)
        this.bmiInputView?.editText?.inputType = InputType.TYPE_CLASS_NUMBER.or(InputType.TYPE_NUMBER_FLAG_DECIMAL)

        this.genderInputView?.editText?.visibility = View.GONE
        this.genderInputView?.button?.visibility = View.VISIBLE
        this.genderInputView?.button?.setOnClickListener{
            this.dismissKeyboard(this)
            this@SelfRecognitionActivity.genderButtonClicked()
        }

        this.submitButton?.setOnClickListener{
            this@SelfRecognitionActivity.submitButtonClicked()
        }

        this.taskPosition = this.intent.getIntExtra("taskPosition", -1)
        this.subTaskPosition = this.intent.getIntExtra("subTaskPosition", -1)

        this.update = this.intent.getBooleanExtra("update", false)

        if(this.update!!){
            this.navBarView?.backButton?.visibility = View.INVISIBLE
        }

        if(this.taskPosition >= 0 && this.subTaskPosition >= 0){
            this.subTask = DataStore.taskList.list[taskPosition].subtasks?.get(subTaskPosition)
        }

        this.updateDisplayLanguage()
    }

    override fun onBackPressed() {
        if(this.update == null){
            this.update = this.intent.getBooleanExtra("update", false)
        }

        if(!this.update!!) {
            super.onBackPressed()
        }
    }

    private fun updateDisplayLanguage(){
        if(this.subTask != null){
            this.navBarView?.titleTextView?.text = this.subTask?.name
        }

        this.instructionTextView?.text = this.getLocalizedStringById(R.string.self_recognition_instruction)
        this.bodyFatRatioInputView?.textView?.text = this.getLocalizedStringById(R.string.body_fat_ratio)
        this.orginFatInputView?.textView?.text = this.getLocalizedStringById(R.string.orgin_fat)
        this.muscleInputView?.textView?.text = this.getLocalizedStringById(R.string.muscle)
        this.metabolismRateInputView?.textView?.text = this.getLocalizedStringById(R.string.metabolism_rate)
        this.bodyAgeInputView?.textView?.text = this.getLocalizedStringById(R.string.body_age)
        this.bodyShapeInputView?.textView?.text = this.getLocalizedStringById(R.string.body_shape)
        this.bmiInputView?.textView?.text = this.getLocalizedStringById(R.string.bmi)
        this.genderInputView?.textView?.text = this.getLocalizedStringById(R.string.gender)

        this.genderList.clear()
        this.genderList = arrayListOf(
                Gender("F", this.getLocalizedStringById(R.string.female)),
                Gender("M", this.getLocalizedStringById(R.string.male))
        )

        this.submitButton?.text = this.getLocalizedStringById(R.string.complete)

        this.setValues()
    }

    private fun setValues(){
        val record = DataStore.user?.taskRecords?.selfRecognitionTaskRecord
        if(record != null){
            this.bodyFatRatioInputView?.editText?.setText(record.bodyFatRatio.toString())
            this.orginFatInputView?.editText?.setText(record.orginFat.toString())
            this.muscleInputView?.editText?.setText(record.muscle.toString())
            this.metabolismRateInputView?.editText?.setText(record.metabolism_rate.toString())
            this.bodyAgeInputView?.editText?.setText(record.bodyAge.toString())
            this.bodyShapeInputView?.editText?.setText(record.bodyShape.toString())
            this.bmiInputView?.editText?.setText(record.bmi.toString())
            when(record.gender){
                "F" -> this.selectedGenderIndex = 0
                "M" -> this.selectedGenderIndex = 1
                else -> {}
            }
            if(this.selectedGenderIndex >= 0) {
                this.genderInputView?.button?.text = this.genderList[this.selectedGenderIndex].name
            }
        }
    }

    private fun genderButtonClicked(){
        val builder = AlertDialog.Builder(this@SelfRecognitionActivity)
        builder.setTitle(R.string.select)
        builder.setAdapter(SelectionListAdapter(this@SelfRecognitionActivity, android.R.layout.simple_list_item_1, android.R.id.text1, this.genderList), { dialog, which ->
            this.selectedGenderIndex = which
            this.genderInputView?.button?.text = this.genderList[which].name
        })
        builder.create().show()
    }

    private fun submitButtonClicked(){
        this.dismissKeyboard(this)

        if(this.loading){
            return
        }

        val bodyFatRatioString = this.bodyFatRatioInputView?.editText?.text.toString()
        val orginFatString = this.orginFatInputView?.editText?.text?.toString()
        val muscleString = this.muscleInputView?.editText?.text?.toString()
        val metabolismRateString = this.metabolismRateInputView?.editText?.text?.toString()
        val bodyAgeString = this.bodyAgeInputView?.editText?.text?.toString()
        val bodyShapeString = this.bodyShapeInputView?.editText?.text?.toString()
        val bmiString = this.bmiInputView?.editText?.text.toString()

        var bodyFatRatio: Double? = null
        var orginFat: Double? = null
        var muscle: Double? = null
        var metabolismRate: Double? = null
        var bodyAge: Double? = null
        var bmi: Double? = null
        var gender: String? = null

        if(bodyFatRatioString.isNullOrEmpty()){
            Toast.makeText(this, String.format(this.getLocalizedStringById(R.string.please_input), this.getLocalizedStringById(R.string.body_fat_ratio).toLowerCase()), Toast.LENGTH_SHORT).show()
            return
        } else {
            bodyFatRatio = bodyFatRatioString!!.toDoubleOrNull()
        }

        if(orginFatString.isNullOrEmpty()){
            Toast.makeText(this, String.format(this.getLocalizedStringById(R.string.please_input), this.getLocalizedStringById(R.string.orgin_fat).toLowerCase()), Toast.LENGTH_SHORT).show()
            return
        } else {
            orginFat = orginFatString!!.toDoubleOrNull()
        }

        if(muscleString.isNullOrEmpty()){
            Toast.makeText(this, String.format(this.getLocalizedStringById(R.string.please_input), this.getLocalizedStringById(R.string.muscle).toLowerCase()), Toast.LENGTH_SHORT).show()
            return
        } else {
            muscle = muscleString!!.toDoubleOrNull()
        }

        if(metabolismRateString.isNullOrEmpty()){
            Toast.makeText(this, String.format(this.getLocalizedStringById(R.string.please_input), this.getLocalizedStringById(R.string.metabolism_rate).toLowerCase()), Toast.LENGTH_SHORT).show()
            return
        } else {
            metabolismRate = metabolismRateString!!.toDoubleOrNull()
        }

        if(bodyAgeString.isNullOrEmpty()){
            Toast.makeText(this, String.format(this.getLocalizedStringById(R.string.please_input), this.getLocalizedStringById(R.string.body_age).toLowerCase()), Toast.LENGTH_SHORT).show()
            return
        } else {
            bodyAge = bodyAgeString!!.toDoubleOrNull()
        }

        if(bodyShapeString.isNullOrEmpty()){
            Toast.makeText(this, String.format(this.getLocalizedStringById(R.string.please_input), this.getLocalizedStringById(R.string.body_shape).toLowerCase()), Toast.LENGTH_SHORT).show()
            return
        }

        if(bmiString.isNullOrEmpty()){
            Toast.makeText(this, String.format(this.getLocalizedStringById(R.string.please_input), this.getLocalizedStringById(R.string.bmi).toLowerCase()), Toast.LENGTH_SHORT).show()
            return
        } else {
            bmi = bmiString.toDoubleOrNull()
        }

        if(this.selectedGenderIndex < 0){
            Toast.makeText(this, String.format(this.getLocalizedStringById(R.string.please_select), this.getLocalizedStringById(R.string.gender).toLowerCase()), Toast.LENGTH_SHORT).show()
            return
        } else {
            gender = this.genderList[this.selectedGenderIndex].value
        }

        this.showLoadingView(this.rootView)
        val params = JSONObject()
        params.put("body_fat_ratio", bodyFatRatio)
        params.put("orgin_fat", orginFat)
        params.put("muscle", muscle)
        params.put("metabolism_rate", metabolismRate)
        params.put("body_age", bodyAge)
        params.put("body_shape", bodyShapeString)
        params.put("bmi", bmi)
        params.put("gender", gender)

        if(DataStore.user?.taskRecords?.selfRecognitionTaskRecord != null) {
            val id = DataStore.user!!.taskRecords!!.selfRecognitionTaskRecord!!.id
            CallServer.put("campaign/"+DataStore.campaignId+"/selfrecognitiontaskRecord/"+id+"/", params, SelfRecognitionTaskRecord::class.java, { response ->
                DataStore.user?.taskRecords?.selfRecognitionTaskRecord = response
                this.hideLoadingView(this.rootView)
                val intent = Intent()
                intent.putExtra("taskPosition", this.taskPosition)
                intent.putExtra("subTaskPosition", this.subTaskPosition)
                setResult(android.app.Activity.RESULT_OK, intent)
                this.finish()
            }, { sessionExpired, error ->
                this.hideLoadingView(this.rootView)
                if(sessionExpired){
                    this@SelfRecognitionActivity.logout(this, sessionExpired)
                } else {
                    this@SelfRecognitionActivity.showErrorToast(this, error, EnumUtils.DataType.SELFRECOGNITIONTASKRECORDUPDATE)
                }
            })
        } else {
            CallServer.post("campaign/"+DataStore.campaignId+"/selfrecognitiontaskrecord/", params, SelfRecognitionTaskRecord::class.java, { response ->
                DataStore.user?.taskRecords?.selfRecognitionTaskRecord = response
                //val intent = Intent()
                //intent.putExtra("taskPosition", this.taskPosition)
                //intent.putExtra("subTaskPosition", this.subTaskPosition)
                //setResult(android.app.Activity.RESULT_OK, intent)

                this.hideLoadingView(this.rootView)
                val intent = Intent(this, TaskResultActivity::class.java)
                intent.putExtra("taskPosition", this.taskPosition)
                this.startActivity(intent)
                setResult(android.app.Activity.RESULT_OK, intent)
                this.finish()
            }, { sessionExpired, error ->
                this.hideLoadingView(this.rootView)
                if(sessionExpired){
                    this@SelfRecognitionActivity.logout(this, sessionExpired)
                } else {
                    this@SelfRecognitionActivity.showErrorToast(this, error, EnumUtils.DataType.SELFRECOGNITIONTASKRECORDUPDATE)
                }
            })
        }
    }
}