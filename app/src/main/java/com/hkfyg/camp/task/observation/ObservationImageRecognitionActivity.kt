package com.hkfyg.camp.task.observation

import android.content.*
import android.hardware.Camera
import android.os.Bundle
import android.view.*
import com.hkfyg.camp.R
import com.hkfyg.camp.task.BaseImageRecognitionActivity
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.EnumUtils
import com.hkfyg.camp.utils.Utils
import com.hkfyg.camp.widget.MapView

class ObservationImageRecognitionActivity: BaseImageRecognitionActivity(){
    private var mapView: MapView? = null

    private var locationId: Int? = null
    private var locationName: String? = null
    private var locationUniqueId: String? = null
    private var locationMapImageUrl: String? = null

    //private var locationQRCodeList: ArrayList<Int> = arrayListOf<Int>()

    /*private var pendingToStart: Boolean = false
    private val noConnectionHandler = Handler()
    private val noConnectionRunnable = object: Runnable{
        override fun run(){
            this@ObservationImageRecognitionActivity.promptEnableMobileData()
        }
    }
    private var connectionStatusReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            this@ObservationImageRecognitionActivity.connectionStatusChanged()
        }
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_recognition)

        this.totalImageCount = DataStore.observationImageList.list.size

        DataStore.user?.taskRecords?.observationTaskRecord?.let{
            this.currentImageIndex = it.records?.takeIf { it.size > 0 }?.let{
                /*val lastOrder = it.get(it.size-1).image?.order?.let{
                    it
                } ?: -1*/
                var lastOrder = -1
                for(i in 0 until DataStore.observationImageList.list.size){
                    if(DataStore.observationImageList.list.get(i).id == it.get(it.size-1).image?.id){
                        lastOrder = i
                        break
                    }
                }
                lastOrder
            } ?: -1
        }

        this.initialize()
        this.mapView = findViewById<MapView>(R.id.mapView)

        this.locationId = this.intent?.getIntExtra("locationId", -1)
        this.locationName = this.intent?.getStringExtra("locationName")
        this.locationUniqueId = this.intent?.getStringExtra("locationUniqueId")
        this.locationMapImageUrl = this.intent?.getStringExtra("locationMapImageUrl")

        this.navBarView?.rightButton?.visibility = View.VISIBLE
        this.navBarView?.rightButton?.setOnClickListener{
            this@ObservationImageRecognitionActivity.toggleMapView()
        }

        this.mapView?.setParameters(this, this.locationId, this.locationName, EnumUtils.InstructionImageTaskType.OBSERVATION.ordinal)

        this.pictureCallback = Camera.PictureCallback{ data, camera ->
            this.camera?.startPreview()

            this@ObservationImageRecognitionActivity.processImage(data)?.let{
                val file = it
                val image = DataStore.observationImageList.list.get(this.currentImageIndex)
                image.uniqueId?.let {
                    this@ObservationImageRecognitionActivity.predictImage(file, it, false, { _ ->
                        DataStore.addObservationRecord(image.id, { response ->
                            this.hideLoadingView(this.rootView)
                            this.updateCurrentImage()
                        }, { sessionExpired, error ->
                            this.hideLoadingView(this.rootView)
                            this.showErrorToast(this, error, EnumUtils.DataType.IMAGEPREDICTION)
                        })
                    })
                }
            }
        }

        this.captureButton?.setOnClickListener{
            this@ObservationImageRecognitionActivity.captureButtonClicked(this.pictureCallback!!)
        }

        this.giveUpButton?.setOnClickListener {
            if (this.loading) {
                return@setOnClickListener
            }

            if (!this.started) {
                this.showAlertDialog(this.getLocalizedStringById(R.string.start), this.getLocalizedStringById(R.string.confirm_start_message_with_map), {
                    this.startTask()
                    /*when(this.getConnectionType()){
                        EnumUtils.CONNECTIONTYPE.WIFI -> {
                            this.pendingToStart = true
                            val wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                            wifiManager.isWifiEnabled = false
                        }
                        EnumUtils.CONNECTIONTYPE.MOBILEDATA -> {
                            this.startTask()
                        } else -> {
                            this.pendingToStart = true
                            this.promptEnableMobileData()
                        }
                    }*/
                })
            } else {
                val message = String.format(this.getLocalizedStringById(R.string.confirm_give_up_message_observe), DataStore.observationImageList.list.size - this.currentImageIndex - 1)
                this.showAlertDialog(this.getLocalizedStringById(R.string.give_up), message, {
                    this.updateCurrentImage()
                })
            }
        }

        /*val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        this.registerReceiver(this.connectionStatusReceiver, intentFilter)*/
    }

    override fun onDestroy() {
        //this.unregisterReceiver(this.connectionStatusReceiver)
        super.onDestroy()
    }

    override fun updateCurrentImage(){
        if(this.currentImageIndex < DataStore.observationImageList.list.size - 1){
            this.currentImageIndex += 1
            this.currentImageUrl = DataStore.observationImageList.list[this.currentImageIndex].thumbnail
            super.updateCurrentImage()
        } else {
            this.timerEndCallback()
        }
    }

    override fun setTimerValue() {
        //super.setTimerValue()
        this.subTask?.timeLimit?.let{
            val timeLimit = it
            this.timerValue = DataStore.user?.taskRecords?.observationTaskRecord?.createdTime?.let{
                Utils.getTimeLeft(it, "yyyy-MM-dd'T'HH:mm:ss'Z'", timeLimit)?.takeIf { it > 0 }?.let{
                    it.toInt()
                } ?: 0
            } ?: timeLimit
        }

        if(!this.started && DataStore.user?.taskRecords?.observationTaskRecord != null){
            this.setStarted()
        }
    }

    override fun timerEndCallback(){
        super.timerEndCallback()

        if(this.loading){
            return
        }

        this.showLoadingView(this.rootView)
        val timeLimit = when(this.subTask?.timeLimit){
            null -> 0
            else -> this.subTask!!.timeLimit!!
        }

        DataStore.updateObservationTaskRecordEndTime(timeLimit, { response ->
            this.hideLoadingView(this.rootView)
            val intent = Intent(this@ObservationImageRecognitionActivity, ObservationResultActivity::class.java)
            intent.putExtra("taskPosition", this.taskPosition)
            intent.putExtra("subTaskPosition", this.subTaskPosition)
            startActivity(intent)

            this.finishActivity()
        }, { sessionExpired, error ->
            this.hideLoadingView(this.rootView)
            this.showErrorToast(this, error, EnumUtils.DataType.OBSERVATIONTASKRECORDEND)
        })
    }

    /*override  fun promptEnableMobileData(){
        this.hideLoadingView(this.rootView)
        this.showAlertDialog("No Network Connection", "Enable mobile data in settings", {
            this.showLoadingView(this.rootView)
            val intent = Intent()
            intent.component = ComponentName("com.android.settings", "com.android.settings.Settings\$DataUsageSummaryActivity")
            startActivity(intent)
        })
    }

    private fun connectionStatusChanged(){
        if(this.started || this.pendingToStart) {
            when (this.getConnectionType()) {
                EnumUtils.CONNECTIONTYPE.MOBILEDATA -> {
                    this.hideLoadingView(this.rootView)
                    this.pendingToStart = false
                    this.noConnectionHandler.removeCallbacks(this.noConnectionRunnable)

                    if(!this.started){
                        this.startTask()
                    } else {
                        DataStore.getTaskRecords(true, { response ->
                            //nothing to do
                        }, { sessionExpired, error ->
                            if(sessionExpired){
                                this.logout(this, sessionExpired)
                            } else {
                                this.promptEnableMobileData()
                            }
                        })
                    }
                }
                EnumUtils.CONNECTIONTYPE.NONE -> {
                    this.showLoadingView(this.rootView)
                    this.noConnectionHandler.postDelayed(this.noConnectionRunnable, 5000)
                }
                else -> {
                }
            }
        }
    }*/

    private fun startTask(){
        if (this.locationId != null && this.locationId!! > 0) {
            this.showLoadingView(this.rootView)
            DataStore.createObservationTaskRecord(this.locationId!!, { response ->
                this.hideLoadingView(this.rootView)
                this.setStarted()
            }, { sessionExpired, error ->
                this.hideLoadingView(this.rootView)
                if (sessionExpired) {
                    this.logout(this, sessionExpired)
                } else {
                    this.showErrorToast(this, error, EnumUtils.DataType.OBSERVATIONTASKRECORDCREATE)
                }
            })
        }
    }

    private fun setStarted(){
        this.started = true
        this.scheduleTimer()
        this.navBarView?.backButton?.visibility = View.INVISIBLE
        this.giveUpButton?.text = this.getLocalizedStringById(R.string.give_up)
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
}