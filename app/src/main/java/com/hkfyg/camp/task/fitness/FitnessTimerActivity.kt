package com.hkfyg.camp.task.fitness

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import com.hkfyg.camp.R
import com.hkfyg.camp.model.taskrecords.FitnessTaskRecord
import com.hkfyg.camp.qr.QRCodeScanFragment
import com.hkfyg.camp.task.TaskResultActivity
import com.hkfyg.camp.task.timer.BaseTimerActivity
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.EnumUtils
import com.hkfyg.camp.utils.Utils
import com.hkfyg.camp.widget.InputView
import com.hkfyg.camp.widget.MapView
import java.util.*

class FitnessTimerActivity: BaseTimerActivity(), QRCodeScanFragment.QRCodeScanFragmentListener{
    private var completeButton: Button? = null
    private var inputView: InputView? = null
    private var scrollView: ScrollView? = null
    private var descriptionTextView: TextView? = null
    private var qrCodeScanFragmentContainer: FrameLayout? = null
    /*private var mapView: ViewGroup? = null
    private var mapTextView: TextView? = null
    private var mapLoadingImageView: LoadingImageView? = null
    private var qrCodeLocationTextView: TextView? = null
    private var qrCodeLoadingImageView: LoadingImageView? = null*/
    private var mapView: MapView? = null

    private var subTaskPositionList: ArrayList<Int>? = null
    private var locationId: Int? = null
    private var locationUniqueId: String? = null
    private var locationName: String? = null
    private var locationMapImageUrl: String? = null
    private var locationQRCodeImageUrl: String? = null

    private var currentRecordId: Int? = null

    private var locationQRCodeList: ArrayList<Int> = arrayListOf<Int>()

    private var isBreak: Boolean = false
    private var updated: Boolean = false

    private var qrCodeScanFragment: QRCodeScanFragment? = null

    private val BREAK_TIME: Int = 2 * 60 //in seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        this.initialize()
        this.completeButton = findViewById<Button>(R.id.completeButton)
        this.inputView = findViewById<InputView>(R.id.inputView)
        this.scrollView = findViewById<ScrollView>(R.id.scrollView)
        this.descriptionTextView = findViewById<TextView>(R.id.descriptionTextView)
        this.completeButton = findViewById<Button>(R.id.completeButton)
        this.qrCodeScanFragmentContainer = findViewById<FrameLayout>(R.id.qr_code_scan_fragment_container)
        /*this.mapView = findViewById<ViewGroup>(R.id.mapView)
        this.mapTextView = findViewById<TextView>(R.id.mapTextView)
        this.mapLoadingImageView = findViewById<LoadingImageView>(R.id.mapLoadingImageView)
        this.qrCodeLocationTextView = findViewById<TextView>(R.id.qrCodeLocationTextView)
        this.qrCodeLoadingImageView = findViewById<LoadingImageView>(R.id.qrCodeLoadingImageView)*/
        this.mapView = findViewById<MapView>(R.id.mapView)

        this.navBarView?.rightButton?.visibility = View.VISIBLE
        this.navBarView?.rightButton?.setOnClickListener{
            this@FitnessTimerActivity.toggleMapView()
        }

        this.reminderTextView?.visibility = View.GONE

        this.imageView?.visibility = View.GONE

        this.inputView?.textView?.text = String.format(this.getLocalizedStringById(R.string.please_input), this.getLocalizedStringById(R.string.value))
        this.inputView?.editText?.inputType = InputType.TYPE_CLASS_NUMBER

        this.completeButton?.text = this.getLocalizedStringById(R.string.start)
        this.completeButton?.setOnClickListener{
            this@FitnessTimerActivity.completeButtonClicked()
        }

        this.subTaskPositionList = this.intent?.getIntegerArrayListExtra("subTaskPositionList")
        this.locationId = this.intent?.getIntExtra("locationId", -1)
        this.locationUniqueId = this.intent?.getStringExtra("locationUniqueId")
        this.locationName = this.intent?.getStringExtra("locationName")
        this.locationMapImageUrl = this.intent.getStringExtra("locationMapImageUrl")
        this.locationQRCodeImageUrl = this.intent.getStringExtra("locationQRCodeImageUrl")

        this.mapView?.setParameters(this, this.locationId, this.locationName, EnumUtils.InstructionImageTaskType.FITNESS.ordinal)

        this.setCurrentSubTask()

        DataStore.user?.taskRecords?.fitnessTaskRecord?.let{
            it.records?.takeIf{ it.size > 0}?.let {
                var index: Int = 0
                val lastRecord = it.get(it.size-1)
                for (i in 0 until DataStore.fitnessItemList.list.size) {
                    val item = DataStore.fitnessItemList.list.get(i)
                    if (item.id != null && item.id?.equals(lastRecord.item?.id) == true) {
                        index = i
                        this.currentRecordId = lastRecord.id
                        if (item.timeLimit != null && lastRecord.createdTime != null) {
                            if (item.timeLimit!! <= 0) {
                                this.countDown = false
                                this.timerValue = Utils.getDifferenceSinceTime(lastRecord.createdTime!!, "yyyy-MM-dd'T'HH:mm:ss'Z'")?.toInt() ?: 0
                                this.setTimerText()
                            } else {
                                this.countDown = true
                                //this.timerValue = Utils.getTimeLeft(lastRecord.createdTime!!, "yyyy-MM-dd'T'HH:mm:ss'Z'", item.timeLimit!!)?.toInt()?.takeIf { it > 0 } ?: 0
                                this.timerValue = item.timeLimit!!
                                this.setTimerText()
                                this.locationUniqueId = null
                            }
                        }
                        break
                    }
                }
                this.currentPosition = index
                this.timer = Timer()
                this.setValues()
                this.setStarted()
            }
        }
    }

    override fun setCurrentSubTask(){
        /*if(this.subTaskPositionList != null && this.subTaskPositionList!!.size > 0){
            this.currentPosition += 1
        }

        if(this.task?.subtasks != null && this.currentPosition >= 0 && this.subTaskPositionList!![this.currentPosition] >= 0 && this.subTaskPositionList!![this.currentPosition] < this.task!!.subtasks!!.size) {
            this.currentSubTask = this.task?.subtasks?.get(this.subTaskPositionList!!.get(this.currentPosition))
        }*/

        if(this.hasNextSubTask()){
            this.currentPosition += 1
        }

        this.setValues()
    }

    override fun hasNextSubTask(): Boolean{
        //return (this.subTaskPositionList != null && this.currentPosition < this.subTaskPositionList!!.size - 1)
        return this.currentPosition < DataStore.fitnessItemList.list.size - 1
    }

    override fun setValues(){
        var item: FitnessTaskRecord.FitnessItem? = null
        if(this.currentPosition >= 0 && this.currentPosition < DataStore.fitnessItemList.list.size){
            item = DataStore.fitnessItemList.list.get(this.currentPosition)
        }

        if(!this.isBreak) {
            //this.navBarView?.titleTextView?.text = this.currentSubTask?.name
            if(this.currentPosition >= 0 && this.currentPosition < DataStore.fitnessItemList.list.size){
                this.navBarView?.titleTextView?.text = item?.name
            }
            //this.imageView?.visibility = View.VISIBLE
            this.gifImageView?.visibility = View.VISIBLE
            this.inputView?.visibility = View.GONE
            this.scrollView?.visibility = View.GONE
            this.completeButton?.visibility = View.INVISIBLE
            this.updated = false
        } else {
            this.navBarView?.titleTextView?.text = this.getLocalizedStringById(R.string.break_time)
            //this.imageView?.visibility = View.GONE
            this.gifImageView?.visibility = View.GONE
            this.inputView?.editText?.setText("")
            this.inputView?.visibility = View.VISIBLE
            this.completeButton?.visibility = View.VISIBLE

            //this.updated = false
            this.completeButton?.text = this.getLocalizedStringById(R.string.complete)
        }

        if((!this.started && DataStore.user?.taskRecords?.fitnessTaskRecord == null) || this.isBreak){
            this.completeButton?.visibility = View.VISIBLE
        } else {
            this.completeButton?.visibility = View.INVISIBLE
        }

        this.inputView?.editText?.isEnabled = true

        when(item?.type){
            EnumUtils.SubTaskUniqueId.CARDIORESPIRATORY_TEST.uniqueId -> {
                //this.imageView?.setImageResource(R.drawable.running1)
                this.gifImageView?.setImageResource(R.drawable.running2)
                if(this.locationUniqueId != null && (!this.isBreak || !this.started)){
                    this.completeButton?.visibility = View.VISIBLE
                }  else if(!this.isBreak && this.locationUniqueId == null){
                    this.completeButton?.visibility = View.INVISIBLE
                } else if(this.isBreak && this.currentPosition <= 0){
                    this.inputView?.visibility = View.GONE
                    this.completeButton?.visibility = View.INVISIBLE
                    this.showNextTaskDescription()
                }
            }
            EnumUtils.SubTaskUniqueId.SITUP_TEST.uniqueId -> this.gifImageView?.setImageResource(R.drawable.sit_up2)
            EnumUtils.SubTaskUniqueId.PUSHUP_TEST.uniqueId -> this.gifImageView?.setImageResource(R.drawable.push_up2)
            EnumUtils.SubTaskUniqueId.BURPEE_TEST.uniqueId -> this.gifImageView?.setImageResource(R.drawable.burpee2)
            else -> {}
        }

        if(this.timer == null) {
            this.setTimerValue()
        }

        super.setValues()
    }

    override fun setTimerValue(){
        /*if (!this.isBreak && this.currentSubTask?.timeLimit != null) {
            this.timerValue = this.currentSubTask!!.timeLimit!!
        } else if (this.isBreak) {
            this.timerValue = BREAK_TIME
        }*/
        if (!this.isBreak && this.currentPosition >= 0 && this.currentPosition < DataStore.fitnessItemList.list.size) {
            DataStore.fitnessItemList.list.get(this.currentPosition).timeLimit?.let {
                this.timerValue = it
            }
        } else if (this.isBreak) {
            this.timerValue = BREAK_TIME
        }
    }

    override fun timerEndCallback(){
        super.timerEndCallback()

        if(this.loading){
            return
        } else if(this.isBreak && !this.updated){
            this.completeButtonClicked()
        } else {
            if (this.mapView?.visibility == View.VISIBLE) {
                this.toggleMapView()
            }

            if (this.currentPosition >= 0) {
                if (this.hasNextSubTask() || (this.currentPosition == DataStore.fitnessItemList.list.size - 1 && !this.isBreak)) {
                    this.isBreak = !this.isBreak

                    if (this.isBreak) {
                        this.completeButton?.visibility = View.INVISIBLE
                        this.setValues()
                        this.scheduleTimer()
                    } else {
                        this.setCurrentSubTask()
                        this.scheduleTimer()
                    }
                } else {
                    val intent = Intent(this, TaskResultActivity::class.java)
                    intent.putExtra("taskPosition", this.taskPosition)
                    this.startActivity(intent)
                    this.finish()

                    /*DataStore.getSubTaskByUniqueId(this.taskPosition, EnumUtils.SubTaskUniqueId.SELF_RECOGNITION.uniqueId)?.let{
                        val intent = Intent(this, SelfRecognitionActivity::class.java)
                        intent.putExtra("taskPosition", this.taskPosition)
                        intent.putExtra("subTaskPosition", it.first)
                        intent.putExtra("update", true)
                        this.startActivity(intent)
                        this.finish()
                    }*/
                }
            }
        }
    }

    private fun setStarted(){
        this.scheduleTimer()
        this.started = true
        this.navBarView?.backButton?.visibility = View.INVISIBLE
        this.completeButton?.text = this.getLocalizedStringById(R.string.complete)
        this.setResult(android.app.Activity.RESULT_OK)
    }

    private fun showNextTaskDescription(){
        if(this.hasNextSubTask()){
            val nextItem = DataStore.fitnessItemList.list.get(this.currentPosition+1)
            this.inputView?.visibility = View.GONE
            this.descriptionTextView?.text = nextItem.description
            this.scrollView?.visibility = View.VISIBLE
        }
        this.completeButton?.visibility = View.VISIBLE
        this.completeButton?.text = this.getLocalizedStringById(R.string.next)
    }

    private fun completeButtonClicked(){
        if(this.loading || this.currentPosition < 0){
            return
        }

        if(!this.started){
            val message = String.format(this.getLocalizedStringById(R.string.cardiorespiratory_test_start_message), this.locationName)
            this.showAlertDialog(this.getLocalizedStringById(R.string.start), message, {
                if(this.locationId != null) {
                    DataStore.fitnessItemList.list.get(this.currentPosition).id?.let {
                        this.showLoadingView(this.rootView)
                        DataStore.createFitnessTaskRecord(this.locationId!!, it, { nextRecordId ->
                            this.currentRecordId = nextRecordId
                            this.hideLoadingView(this.rootView)
                            this.setStarted()
                        }, { sessionExpired, error ->
                            this.hideLoadingView(this.rootView)
                            if (sessionExpired) {
                                this.logout(this, sessionExpired)
                            } else {
                                this.showErrorToast(this, error, EnumUtils.DataType.FITNESSTASKRECORDCREATE)
                            }
                        })
                    }
                }
            })
        } else if(this.locationUniqueId != null){
            this.showQRCodeScanView()
        } else if(this.task != null){
            if(!this.updated && this.scrollView?.visibility != View.VISIBLE) {
                this.dismissKeyboard(this)

                this.currentRecordId?.let {
                    var inputValue = this.inputView?.editText?.text?.toString()?.toIntOrNull()

                    if(this.timerValue <= 0 && inputValue == null){
                        inputValue = 0
                    }

                    var dataType = EnumUtils.DataType.FITNESSSITUPCOUNTUPDATE

                    val item = DataStore.fitnessItemList.list.get(this.currentPosition)
                    when (item.type) {
                        EnumUtils.SubTaskUniqueId.CARDIORESPIRATORY_TEST.uniqueId -> {
                            if (inputValue == null) {
                                Toast.makeText(this, String.format(this.getLocalizedStringById(R.string.please_input), this.getLocalizedStringById(R.string.number_of_laps)), Toast.LENGTH_SHORT).show()
                                return
                            }
                            dataType = EnumUtils.DataType.FITNESSSITUPCOUNTUPDATE
                        }
                        EnumUtils.SubTaskUniqueId.SITUP_TEST.uniqueId -> {
                            if (inputValue == null) {
                                Toast.makeText(this, String.format(this.getLocalizedStringById(R.string.please_input), this.getLocalizedStringById(R.string.sit_up_count)), Toast.LENGTH_SHORT).show()
                                return
                            }
                            dataType = EnumUtils.DataType.FITNESSSITUPCOUNTUPDATE
                        }
                        EnumUtils.SubTaskUniqueId.PUSHUP_TEST.uniqueId -> {
                            if (inputValue == null) {
                                Toast.makeText(this, String.format(this.getLocalizedStringById(R.string.please_input), this.getLocalizedStringById(R.string.push_up_count)), Toast.LENGTH_SHORT).show()
                                return
                            }
                            dataType = EnumUtils.DataType.FITNESSPUSHUPCOUNTUPDATE
                        }
                        EnumUtils.SubTaskUniqueId.BURPEE_TEST.uniqueId -> {
                            if (inputValue == null) {
                                Toast.makeText(this, String.format(this.getLocalizedStringById(R.string.please_input), this.getLocalizedStringById(R.string.burpee_count)), Toast.LENGTH_SHORT).show()
                                return
                            }
                            dataType = EnumUtils.DataType.FITNESSBURPEECOUNTUPDATE
                        }
                    }

                    var nextItemId: Int? = null
                    if(this.hasNextSubTask()){
                        nextItemId = DataStore.fitnessItemList.list.get(this.currentPosition + 1).id
                    }

                    this.showLoadingView(this.rootView)
                    DataStore.endFitnessTaskRecord(it, inputValue, nextItemId, { nextRecordId ->
                        this.currentRecordId = nextRecordId
                        this.hideLoadingView(this.rootView)
                        this.updated = true
                        this.inputView?.editText?.isEnabled = false
                        this.completeButton?.visibility = View.INVISIBLE

                        Toast.makeText(this, this.getLocalizedStringById(R.string.value_updated), Toast.LENGTH_SHORT).show()

                        this.showNextTaskDescription()

                        if (this.timerValue <= 0) {
                            this.timerEndCallback()
                        }

                    }, { sessionExpired, error ->
                        this.hideLoadingView(this.rootView)
                        if (sessionExpired) {
                            this.logout(this, sessionExpired)
                        } else {
                            this.showErrorToast(this, error, dataType)
                        }
                    })
                }
            } else if(this.isBreak){
                this.showAlertDialog(this.getLocalizedStringById(R.string.finish_break), this.getLocalizedStringById(R.string.confirm_finish_break), {
                    this.timerEndCallback()
                })
            }
        }
    }

    private fun showQRCodeScanView(){
        if(this.qrCodeScanFragment == null) {
            this.qrCodeScanFragment = QRCodeScanFragment.newInstance()
            this.qrCodeScanFragment?.listener = this
        } else {
            this.qrCodeScanFragment?.resumeCamera()
        }

        val transaction = this.supportFragmentManager.beginTransaction()
        transaction.replace(R.id.qr_code_scan_fragment_container, this.qrCodeScanFragment)
        transaction.commit()
        this.qrCodeScanFragmentContainer?.visibility = View.VISIBLE
        this.rootView?.bringChildToFront(this.qrCodeScanFragmentContainer)

        Toast.makeText(this, String.format(this.getLocalizedStringById(R.string.please_scan_qr_code), this.locationName), Toast.LENGTH_SHORT).show()
    }

    private fun hideQRCodeScanView(){
        val transaction = this.supportFragmentManager.beginTransaction()
        transaction.remove(this.supportFragmentManager.findFragmentById(R.id.qr_code_scan_fragment_container))
        transaction.commit()
        this.qrCodeScanFragmentContainer?.visibility = View.GONE
    }

    private fun toggleMapView(){
        if(this.mapView?.visibility == View.VISIBLE){
            this.mapView?.visibility = View.GONE
        } else {
            this.mapView?.visibility = View.VISIBLE
            this.rootView?.bringChildToFront(this.mapView)

            if(this.mapView != null && this.mapView!!.firstAppear){
                this.mapView?.firstAppear = false
                this.mapView?.refresh()
            }
            this.mapView?.adapter?.notifyDataSetChanged()
        }
    }

    override fun qrResultHandled(result: String) {
        if(this.loading){
            return
        }

        this.hideQRCodeScanView()

        if(result.equals(this.locationUniqueId)){
            this.currentRecordId?.let{
                val nextItemId = when(this.hasNextSubTask()){
                    true -> DataStore.fitnessItemList.list.get(this.currentPosition+1).id
                    else -> null
                }

                this.showLoadingView(this.rootView)
                DataStore.endFitnessTaskRecord(it, null, nextItemId, { nextRecordId ->
                    this.currentRecordId = nextRecordId
                    this.hideLoadingView(this.rootView)
                    this.locationUniqueId = null
                    this.updated = true
                    this.navBarView?.rightButton?.visibility = View.GONE
                    this.timerEndCallback()
                }, { sessionExpired, error ->
                    this.hideLoadingView(this.rootView)

                    if(sessionExpired){
                        this.cancelTimer()
                        this.logout(this, sessionExpired)
                    } else {
                        this.showErrorToast(this, error, EnumUtils.DataType.FITNESSARRIVALTIMEUPDATE)
                    }
                })
            }
        } else {
            Toast.makeText(this, this.getLocalizedStringById(R.string.invalid_qr_code), Toast.LENGTH_SHORT).show()
        }
    }
}