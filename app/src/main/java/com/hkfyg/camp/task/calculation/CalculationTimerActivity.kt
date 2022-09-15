package com.hkfyg.camp.task.calculation

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.hkfyg.camp.R
import com.hkfyg.camp.model.taskrecords.CalculationTaskRecord
import com.hkfyg.camp.shahbaz_momi.Value
import com.hkfyg.camp.task.TaskResultActivity
import com.hkfyg.camp.task.timer.BaseTimerActivity
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.EnumUtils
import com.hkfyg.camp.widget.InputView
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent

class CalculationTimerActivity: BaseTimerActivity(){
    private var inputViewContainer: ViewGroup? = null
    private var numbersTextView: TextView? = null
    private var inputView: InputView? = null
    private var buttonsContainer: ViewGroup? = null
    private var completeButton: Button? = null
    private var giveUpButtonContainer: ViewGroup? = null
    private var giveUpButton: Button? = null

    private var subTaskPosition: Int? = null
    private var currentRecordId: Int? = null

    private var currentCalculationItem: CalculationTaskRecord.CalculationItem? = null
    private var requiresValidation: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculation_timer)

        this.initialize()
        this.inputViewContainer = findViewById<ViewGroup>(R.id.inputViewContainer)
        this.numbersTextView = findViewById<TextView>(R.id.numbersTextView)
        this.inputView = findViewById<InputView>(R.id.inputView)
        this.buttonsContainer = findViewById<ViewGroup>(R.id.buttonsContainer)
        this.completeButton = findViewById<Button>(R.id.completeButton)
        this.giveUpButtonContainer = findViewById<ViewGroup>(R.id.giveUpButtonContainer)
        this.giveUpButton = findViewById<Button>(R.id.giveUpButton)

        //this.inputView?.editText?.inputType = InputType.TYPE_CLASS_TEXT.or(InputType.TYPE_TEXT_VARIATION_PHONETIC)
        this.inputView?.editText?.inputType = InputType.TYPE_CLASS_PHONE

        this.completeButton?.text = this.getLocalizedStringById(R.string.start)
        this.completeButton?.setOnClickListener{
            this@CalculationTimerActivity.completeButtonClicked()
        }

        this.giveUpButton?.setOnClickListener{
            this@CalculationTimerActivity.giveUpButtonClicked()
        }

        KeyboardVisibilityEvent.setEventListener(this, { isOpen ->
            if(isOpen){
                this.buttonsContainer?.visibility = View.GONE
            } else {
                Handler().postDelayed({
                    this.buttonsContainer?.visibility = View.VISIBLE
                }, 500)
            }
        })

        this.inputView?.textView?.text = this.getLocalizedStringById(R.string.answer)

        this.subTaskPosition = this.intent?.getIntExtra("subTaskPosition", -1)

        this.setCurrentSubTask()
    }

    override fun setCurrentSubTask() {
        if(this.hasNextSubTask()){
            this.currentPosition += 1
        }

        if(this.subTaskPosition != null && this.subTaskPosition!! >= 0 && this.task?.subtasks != null && this.task!!.subtasks!!.size > this.subTaskPosition!!){
            this.currentSubTask = this.task?.subtasks?.get(this.subTaskPosition!!)
        }

        this.setValues()
    }

    override fun hasNextSubTask(): Boolean {
        return this.currentPosition < DataStore.calculationItemList.list.size - 1
    }

    override fun setValues(){
        this.currentCalculationItem = DataStore.calculationItemList.list.get(this.currentPosition)
        this.navBarView?.titleTextView?.text = currentCalculationItem?.name

        if(this.currentCalculationItem?.numbers != null && currentCalculationItem!!.numbers!!.size > 0) {
            this.requiresValidation = true
            this.imageView?.visibility = View.GONE
            this.numbersTextView?.text = this.currentCalculationItem?.name
            this.inputViewContainer?.visibility = View.VISIBLE

            if(this.started){
                this.inputView?.visibility = View.VISIBLE
            } else {
                this.inputView?.visibility = View.GONE
            }
        } else {
            this.requiresValidation = false
            this.imageView?.visibility = View.VISIBLE
            this.imageView?.setImageResource(R.drawable.calculation1)
            this.inputViewContainer?.visibility = View.GONE
        }

        if(!this.started) {
            this.setTimerValue()
        }

        super.setValues()
    }

    override fun setTimerValue(){
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

    private fun validateExpression(expression: String): Float?{
        try{
            return Value(expression).resolve()
        } catch(exception: Exception){
            return null
        }
    }

    private fun validateNumberSet(list: ArrayList<CalculationTaskRecord.CalculationNumber>?, string: String): Boolean{
        if(list == null){
            return false
        }

        val numberSet: MutableSet<Int> = mutableSetOf()
        for(n in list){
            n.number?.let {
                numberSet.add(it)
            }
        }

        val inputNumberSet: MutableSet<Int> = mutableSetOf()

        var number: String = ""
        for(i in 0 until string.length){
            val character = string[i]
            if(character.isDigit()){
                number += character
            } else {
                number.toIntOrNull()?.let {
                    if (inputNumberSet.contains(it)) {
                        return false
                    }
                    inputNumberSet.add(it)
                    number = ""
                }
            }
        }

        number.toIntOrNull()?.let{
            inputNumberSet.add(it)
        }

        if(numberSet.size != inputNumberSet.size){
            return false
        } else {
            for(n in numberSet){
                if(!inputNumberSet.contains(n)){
                    return false
                }
            }
        }

        return true
    }

    private fun validateAnswer(value: Float, target: Int?): Boolean{
        return value.toInt() == target
    }

    private fun completeButtonClicked(){
        if(this.loading || this.currentSubTask == null){
            return
        }

        if(!this.started){
            val message = this.getLocalizedStringById(R.string.confirm_start_message)
            this.showAlertDialog(this.getLocalizedStringById(R.string.start), message, {
                DataStore.calculationItemList.list.get(this.currentPosition).id?.let{
                    this.showLoadingView(this.rootView)
                    DataStore.createCalculationTaskRecord(it, { nextRecord ->
                        this.currentRecordId = nextRecord
                        this.hideLoadingView(this.rootView)
                        this.scheduleTimer()
                        this.started = true
                        this.navBarView?.backButton?.visibility = View.INVISIBLE
                        this.completeButton?.text = this.getLocalizedStringById(R.string.complete)
                        this.completeButton?.background = this.resources.getDrawable(R.drawable.background_capsule_dark_green)
                        this.giveUpButtonContainer?.visibility = View.VISIBLE
                        if(this.requiresValidation){
                            this.inputView?.visibility = View.VISIBLE
                        }
                    }, { sessionExpired, error ->
                        this.hideLoadingView(this.rootView)
                        if(sessionExpired){
                            this.logout(this, sessionExpired)
                        } else {
                            this.showErrorToast(this, error, EnumUtils.DataType.CALCULATIONTASKRECORDCREATE)
                        }
                    })
                }
            })
        } else {
            this.showAlertDialog(this.getLocalizedStringById(R.string.complete), this.getLocalizedStringById(R.string.confirm_complete_message), {
                if(this.requiresValidation){
                    var expression = this.inputView?.editText?.text?.toString()

                    if(expression.isNullOrEmpty()){
                        Toast.makeText(this, this.getLocalizedStringById(R.string.please_enter_expression), Toast.LENGTH_SHORT).show()
                        return@showAlertDialog
                    }

                    expression = expression?.replace("x", "*")
                    expression = expression?.replace("X", "*")
                    expression = expression?.replace("[", "(")
                    expression = expression?.replace("]", ")")
                    expression = expression?.replace("{", "(")
                    expression = expression?.replace("}", ")")

                    this.currentCalculationItem?.let {
                        val value = this.validateExpression(expression!!)

                        if(value == null){
                            Toast.makeText(this, this.getLocalizedStringById(R.string.invalid_expression), Toast.LENGTH_SHORT).show()
                            return@showAlertDialog
                        }

                        if (!this.validateNumberSet(it.numbers, expression)) {
                            Toast.makeText(this, this.getLocalizedStringById(R.string.invalid_number_set), Toast.LENGTH_SHORT).show()
                            return@showAlertDialog
                        }

                        if(!this.validateAnswer(value, it.numbersTarget)){
                            Toast.makeText(this, this.getLocalizedStringById(R.string.incorrect_answer), Toast.LENGTH_SHORT).show()
                            return@showAlertDialog
                        }

                        if(this.requiresValidation){
                            this.dismissKeyboard(this)
                        }

                        this.endCalculationRecord(true)
                    }
                } else {
                    this.endCalculationRecord(true)
                }
            })
        }
    }

    private fun giveUpButtonClicked(){
        if(this.loading || this.currentSubTask == null){
            return
        }

        if(this.requiresValidation){
            this.dismissKeyboard(this)
        }

        this.showAlertDialog(this.getLocalizedStringById(R.string.give_up), this.getLocalizedStringById(R.string.confirm_give_up_message), {
            this.endCalculationRecord(false)
        })
    }

    private fun endCalculationRecord(success: Boolean){
        if(this.loading){
            return
        }

        this.currentRecordId?.let{
            var nextItemId: Int? = null
            if(this.hasNextSubTask()){
                nextItemId = DataStore.calculationItemList.list.get(this.currentPosition + 1).id
            }

            this.showLoadingView(this.rootView)
            DataStore.endCalculationRecord(it, success, nextItemId, { nextRecordId ->
                this.currentRecordId = nextRecordId
                this.hideLoadingView(this.rootView)
                this.inputView?.editText?.setText("")
                if(nextItemId != null && this.timerValue > 0){
                    this.setCurrentSubTask()
                } else {
                    this.timerEndCallback()
                }
            }, { sessionExpired, error ->
                this.hideLoadingView(this.rootView)
                if(sessionExpired){
                    this.logout(this, sessionExpired)
                } else {
                    this.showErrorToast(this, error, EnumUtils.DataType.CALCULATIONRECORDEND)
                }
            })
        }
    }
}