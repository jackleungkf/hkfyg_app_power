package com.hkfyg.camp.task.cube

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.hkfyg.camp.R
import com.hkfyg.camp.task.TaskResultActivity
import com.hkfyg.camp.task.timer.BaseTimerActivity
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.EnumUtils
import com.hkfyg.camp.widget.CubeItemView

class CubeTimerActivity: BaseTimerActivity(){
    private var firstItemView: CubeItemView? = null
    private var secondItemView: CubeItemView? = null
    private var completeButton: Button? = null
    private var giveUpButtonContainer: ViewGroup? = null
    private var giveUpButton: Button? = null

    private var subTaskPosition: Int? = null
    private var currentRecordId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cube_timer)

        this.initialize()
        this.firstItemView = findViewById<CubeItemView>(R.id.firstItemView)
        this.secondItemView = findViewById<CubeItemView>(R.id.secondItemView)
        this.completeButton = findViewById<Button>(R.id.completeButton)
        this.giveUpButtonContainer = findViewById<ViewGroup>(R.id.giveUpButtonContainer)
        this.giveUpButton = findViewById<Button>(R.id.giveUpButton)

        this.completeButton?.text = this.getLocalizedStringById(R.string.start)
        this.completeButton?.setOnClickListener{
            this@CubeTimerActivity.completeButtonClicked()
        }

        this.giveUpButton?.setOnClickListener{
            this@CubeTimerActivity.giveUpButtonClicked()
        }

        this.subTaskPosition = this.intent?.getIntExtra("subTaskPosition", -1)

        this.setCurrentSubTask()
    }

    override fun setCurrentSubTask() {
        if(this.hasNextSubTask()){
            this.currentPosition += 1
        }

        this.subTaskPosition?.let{
            this.currentSubTask = this.task?.subtasks?.get(it)
        }

        this.setValues()
    }

    override fun hasNextSubTask(): Boolean {
        return this.currentPosition < DataStore.cubeCombinationList.list.size - 1
    }

    override fun setValues(){
        this.navBarView?.titleTextView?.text = this.currentSubTask?.name

        val combination = DataStore.cubeCombinationList.list[this.currentPosition]
        this.firstItemView?.textView?.text = combination.firstColor?.name
        combination.firstColor?.code?.let {
            this.firstItemView?.imageView?.setColorFilter(Color.parseColor(it))
            this.firstItemView?.textView?.setTextColor(Color.parseColor(this.getInvertedColorString(this.getRGBArray(it))))
            this.firstItemView?.textView?.alpha = 0.8f
        }

        this.secondItemView?.textView?.text = combination.secondColor?.name
        combination.secondColor?.code?.let{
            this.secondItemView?.imageView?.setColorFilter(Color.parseColor(it))
            this.secondItemView?.textView?.setTextColor(Color.parseColor(this.getInvertedColorString(this.getRGBArray(it))))
            this.secondItemView?.textView?.alpha = 0.8f
        }

        if(!this.started) {
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

        // show result activity
        //val intent = Intent(this, CubeTaskResultActivity::class.java)
        val intent = Intent(this, TaskResultActivity::class.java)
        intent.putExtra("isUpdate", true)
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
            this.showAlertDialog(this.getLocalizedStringById(R.string.start), this.getLocalizedStringById(R.string.confirm_start_message), {
                this.showLoadingView(this.rootView)
                val nextCombinationId: Int? = when(this.hasNextSubTask()){
                    true -> DataStore.cubeCombinationList.list[this.currentPosition].id
                    false -> null
                }

                DataStore.createCubeTaskRecord(nextCombinationId, { nextRecordId ->
                    this.currentRecordId = nextRecordId
                    this.hideLoadingView(this.rootView)
                    this.scheduleTimer()
                    this.started = true
                    this.completeButton?.text = this.getLocalizedStringById(R.string.complete)
                    this.completeButton?.background = this.resources.getDrawable(R.drawable.background_capsule_dark_green)
                    this.giveUpButtonContainer?.visibility = View.VISIBLE
                }, { sessionExpired, error ->
                    this.hideLoadingView(this.rootView)
                    if(sessionExpired){
                        this.logout(this, sessionExpired)
                    } else {
                        this.showErrorToast(this, error, EnumUtils.DataType.CUBETASKRECORDCREATE)
                    }
                })

                /*DataStore.createCubeTaskRecord({ record ->
                    this.hideLoadingView(this.rootView)
                    this.scheduleTimer()
                    this.started = true
                    this.navBarView?.backButton?.visibility = View.INVISIBLE
                    this.completeButton?.text = this.getLocalizedStringById(R.string.complete)
                    this.completeButton?.background = this.resources.getDrawable(R.drawable.background_capsule_dark_green)
                    this.giveUpButtonContainer?.visibility = View.VISIBLE
                }, { sessionExpired, error ->
                    this.hideLoadingView(this.rootView)
                    if (sessionExpired) {
                        this.logout(this, sessionExpired)
                    } else {
                        this.showErrorToast(this, error)
                    }
                })*/
            })
        } else {
            this.showAlertDialog(this.getLocalizedStringById(R.string.complete), this.getLocalizedStringById(R.string.confirm_complete_message), {
                //timerEndCallback()
                this.endCubeRecord(true)
            })
        }
    }

    private fun giveUpButtonClicked(){
        if(this.loading || this.currentSubTask == null){
            return
        }

        this.showAlertDialog(this.getLocalizedStringById(R.string.give_up), this.getLocalizedStringById(R.string.confirm_give_up_message), {
            //this.timerEndCallback()
            this.endCubeRecord(false)
        })
    }

    private fun endCubeRecord(success: Boolean){
        if(this.loading){
            return
        }

        if(this.currentPosition >= 0){
            this.currentRecordId?.let{
                val nextCombinationId: Int? = when(this.hasNextSubTask()){
                    true -> DataStore.cubeCombinationList.list[this.currentPosition+1].id
                    false -> null
                }

                this.showLoadingView(this.rootView)
                DataStore.endCubeRecord(it, success, nextCombinationId, { nextRecordId ->
                    this.currentRecordId = nextRecordId
                    this.hideLoadingView(this.rootView)
                    if(nextCombinationId != null){
                        this.setCurrentSubTask()
                    } else {
                        this.timerEndCallback()
                    }
                }, { sessionExpired, error ->
                    this.hideLoadingView(this.rootView)
                    if(sessionExpired){
                        this.logout(this, sessionExpired)
                    } else {
                        this.showErrorToast(this, error, EnumUtils.DataType.CUBERECORDEMD)
                    }
                })
            }
        }
    }

    private fun getRGBArray(colorString: String): IntArray{
        val intArray: IntArray = IntArray(3)
        val string = colorString.replace("#", "")

        for(i in 0 until 3){
            intArray[i] = Integer.parseInt(string.substring(i*2, i*2 + 2), 16)
        }
        return intArray
    }

    private fun getInvertedColorString(colorArray: IntArray): String{
        //val invertColorArray: IntArray = IntArray(colorArray.size)
        var colorString: String = "#"
        for(i in 0 until colorArray.size){
            //invertColorArray[i] = Math.abs(255 - colorArray[i])
            colorString += String.format("%02x", Math.abs(255 - colorArray[i])).toUpperCase()
        }
        return colorString
    }
}