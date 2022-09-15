package com.hkfyg.camp.task.express

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
//import com.estimote.proximity_sdk.api.EstimoteCloudCredentials
//import com.estimote.proximity_sdk.api.ProximityObserver
//import com.estimote.proximity_sdk.api.ProximityObserverBuilder
//import com.estimote.proximity_sdk.api.ProximityZoneBuilder
import com.hkfyg.camp.R
import com.hkfyg.camp.model.Beacon
import com.hkfyg.camp.task.timer.BaseTimerActivity
import com.hkfyg.camp.utils.Constants
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.EnumUtils
import com.hkfyg.camp.utils.Utils
import org.altbeacon.beacon.*
import org.altbeacon.beacon.service.RangedBeacon
import java.util.*

class ExpressUserTimerActivity: BaseTimerActivity(), BeaconConsumer {
    private var teamTextView: TextView? = null
    private var nameTextView: TextView? = null
    private var teamImageView: ImageView? = null

    private var subTaskPosition: Int? = null
    private var ended: Boolean = false

    private var teamImageList: ArrayList<Int> = arrayListOf(R.drawable.team_a_2, R.drawable.team_b_2, R.drawable.team_c_2, R.drawable.team_d_2)
    private var teamTextColorList: ArrayList<Int> = arrayListOf(R.color.colorAccent, R.color.colorBlue, R.color.colorGreen, R.color.colorDarkOrange)

    //estimote
    //private val estimoteCloudCredentials = EstimoteCloudCredentials(Constants.estimoteAppId, Constants.estimoteAppToken)
    //private var proximityHandler: ProximityObserver.Handler? = null

    //android beacon library
    private var beaconManager: BeaconManager? = null
    //private val region: Region = Region("", null, null, null)
    private val region: Region = Region("", Identifier.parse("b9407f30-f5f8-466e-aff9-25556b57fe6d"), null, null)
    private val beaconLayoutList: List<String> = listOf(
        "m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"     //estimote beacon layout
    )

    private val detectedBeaconMap: MutableMap<String, Double> = mutableMapOf()
    private var lastDetectedBeaconId: String? = null

    private var bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val PERMISSION_REQUEST_CODE: Int = 0
    private val BLUETOOTH_ENABLE_REQUEST_CODE: Int = 1

    private val LIBRARY_TYPE: Int = 1   //0: estimote, 1: android beacon library

    private val receiver: BroadcastReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)){
                BluetoothAdapter.STATE_ON -> {
                    this@ExpressUserTimerActivity.startBeaconDetection()
                }
                BluetoothAdapter.STATE_OFF -> {
                    this@ExpressUserTimerActivity.showAlertDialog("", this@ExpressUserTimerActivity.getLocalizedStringById(R.string.bluetooth_required), {
                        this@ExpressUserTimerActivity.enableBluetooth()
                    })
                }
                BluetoothAdapter.STATE_TURNING_ON -> {
                }
                BluetoothAdapter.STATE_TURNING_OFF -> {
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_user)

        this.initialize()

        this.navBarView?.backButton?.setOnClickListener{
            this.onBackPressed()
        }

        this.timerTextView?.text = ""

        this.subTaskPosition = this.intent.getIntExtra("subTaskPosition", -1)

        this.setCurrentSubTask()

        val intentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        this.registerReceiver(this.receiver, intentFilter)
    }

    override fun onDestroy() {
        this.unregisterReceiver(this.receiver)
        this.beaconManager?.unbind(this)
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == BLUETOOTH_ENABLE_REQUEST_CODE){
            when(resultCode){
                Activity.RESULT_OK -> {
                    this.startBeaconDetection()
                } else -> {
                    Toast.makeText(this, this.getLocalizedStringById(R.string.bluetooth_required), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == PERMISSION_REQUEST_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, this.getLocalizedStringById(R.string.permission_granted), Toast.LENGTH_SHORT).show()
                this.startBeaconDetection()
            }
        }
    }

    override fun onBackPressed() {
        if (this.started && !this.ended) {
            return
        } else {
            this.stopBeaconDetection()
            this.finish()
        }
    }

    override fun initialize() {
        super.initialize()

        this.teamTextView = findViewById<TextView>(R.id.teamTextView)
        this.nameTextView = findViewById<TextView>(R.id.nameTextView)
        this.teamImageView = findViewById<ImageView>(R.id.teamImageView)
    }

    override fun setCurrentSubTask() {
        if(this.subTaskPosition != null && this.subTaskPosition!! >= 0 && this.task?.subtasks != null && this.task?.subtasks!!.size > this.subTaskPosition!!){
            this.currentSubTask = this.task?.subtasks?.get(this.subTaskPosition!!)
        }

        this.setValues()
    }

    override fun hasNextSubTask(): Boolean {
        return false
    }

    override fun setValues() {
        this.navBarView?.titleTextView?.text = getString(R.string.express_test_title)

        this.setTimerValue()

        val position = when(DataStore.user?.team?.name?.toUpperCase()){
            "B" -> 1
            "C" -> 2
            "D" -> 3
            else -> 0
        }
        val textColor = this.resources.getColor(this.teamTextColorList.get(position))

        this.teamImageView?.setImageResource(this.teamImageList.get(position))
        this.teamTextView?.setTextColor(textColor)
        this.nameTextView?.setTextColor(textColor)

        this.teamTextView?.text = this.getLocalizedStringById(R.string.team).capitalize()
        this.nameTextView?.text = DataStore.user?.team?.name

        super.setValues()
    }

    override fun setTimerValue() {
        if(DataStore.user?.taskRecords?.expressTaskRecord == null){
            this.currentSubTask?.timeLimit?.let{
                this.timerValue = it
            }
        } else if(DataStore.user?.taskRecords?.expressTaskRecord?.endTime != null){
            this.timerEndCallback()
        } else if(DataStore.user?.taskRecords?.expressTaskRecord != null){
            this.currentSubTask?.timeLimit?.let{
                Utils.getTimeLeft(DataStore.user?.taskRecords?.expressTaskRecord?.createdTime!!, "yyyy-MM-dd'T'HH:mm:ss'Z'", it)?.let{
                    this.timerValue = it.toInt()
                    this.setStarted()
                    if(it > 0){
                        this.timerValue = it.toInt()
                    } else {
                        this.timerValue = 0
                        this.timerTextView?.text = "00:00:00"
                        this.timerEndCallback()
                    }
                }
            }
        }
    }

    override fun timerEndCallback() {
        super.timerEndCallback()
        this.setEnded()
    }

    override fun onBeaconServiceConnect(){
        Log.d("ExpressUserTimer", "onBeaconServiceConnect")
        this.beaconManager?.removeAllRangeNotifiers()
        this.beaconManager?.addRangeNotifier(object: RangeNotifier {
            override fun didRangeBeaconsInRegion(p0: MutableCollection<org.altbeacon.beacon.Beacon>?, p1: Region?) {
                p0?.let {
                    if (it.isNotEmpty()) {
                        var uuid: String? = null

                        for (beacon in it.iterator()) {
                            uuid = "${beacon.id1?.toUuid()?.toString()};${beacon.id2?.toString()};${beacon.id3?.toString()}"
                            if (!uuid.isNullOrEmpty()) {
                                this@ExpressUserTimerActivity.detectedBeaconMap.put(uuid, beacon.distance)
                            }
                        }

                        if (this@ExpressUserTimerActivity.detectedBeaconMap.isNotEmpty()) {
                            val sortedMap = this@ExpressUserTimerActivity.detectedBeaconMap.toList().sortedBy { (_, value) -> value }.toMap()
                            Log.d("ExpressUserTimer", "sortedList")
                            for (item in sortedMap) {
                                Log.d("ExpressUserTimer", "${item.key}, ${item.value}")
                            }

                            val firstItem = sortedMap.entries.first()
                            if (this@ExpressUserTimerActivity.lastDetectedBeaconId == null || !firstItem.key.equals(this@ExpressUserTimerActivity.lastDetectedBeaconId)) {
                                this@ExpressUserTimerActivity.lastDetectedBeaconId = firstItem.key
                                DataStore.beaconList.list.find {
                                    when (it.uuid) {
                                        null -> false
                                        else -> it.uuid!!.equals(firstItem.key)
                                    }
                                }?.let {
                                    this@ExpressUserTimerActivity.createBeaconRecord(it)
                                }
                            }
                        }
                    }
                }
            }
        })
        this.beaconManager?.startRangingBeaconsInRegion(region)
    }

    private fun setStarted(){
        this.started = true
        this.scheduleTimer()
        this.navBarView?.backButton?.visibility = View.INVISIBLE
        this.setResult(RESULT_OK)
        this.startBeaconDetection()
    }

    private fun setEnded(){
        this.stopBeaconDetection()
        this.ended = true
        this.timerTextView?.setTextColor(this.resources.getColor(R.color.colorAccent))
        this.navBarView?.backButton?.visibility = View.VISIBLE
    }

    private fun requestPermission(permission: String){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, permission)){
            this.showAlertDialog("Require Location Permission", "Location permission is required", {
                ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSION_REQUEST_CODE)
            })
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSION_REQUEST_CODE)
        }
    }

    private fun enableBluetooth(){
        this.showLoadingView(this.rootView)
        this.startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BLUETOOTH_ENABLE_REQUEST_CODE)
    }

    private fun startBeaconDetection(){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        } else if(!this.bluetoothAdapter.isEnabled){
            this.enableBluetooth()
        } else {
            if(LIBRARY_TYPE == 0) {
                /*val proximityObserver = ProximityObserverBuilder(this.applicationContext, this.estimoteCloudCredentials)
                        //.withBalancedPowerMode()
                        .withLowLatencyPowerMode()
                        .onError {
                            Log.d("ExpressUserTimer", "proximityObserver error: ${it}")
                        }
                        .build()

                val venueZone = ProximityZoneBuilder()
                        .forTag("testing")
                        .inFarRange()
                        .onEnter {
                            Log.d("ExpressUserTimer", "onEnter, ${it.deviceId}")
                        }
                        .onExit {
                            Log.d("ExpressUserTimer", "onExit, ${it.deviceId}")
                        }
                        .onContextChange {
                            Log.d("BeaconTestingActivity", "onContextChange")
                            for (context in it.iterator()) {
                                if (this.lastDetectedBeaconId == null || !this.lastDetectedBeaconId!!.equals(context.deviceId)) {
                                    this.lastDetectedBeaconId = context.deviceId
                                    DataStore.beaconList.list.find {
                                        when (it.uuid) {
                                            null -> false
                                            else -> it.uuid!!.equals(context.deviceId)
                                        }
                                    }?.let {
                                        this.createBeaconRecord(it)
                                    }
                                }
                            }
                        }
                        .build()

                this.proximityHandler = proximityObserver.startObserving(venueZone)
                //this.hideLoadingView(this.rootView)*/
            } else if(LIBRARY_TYPE == 1){
                if(this.beaconManager == null) {
                    this.beaconManager = BeaconManager.getInstanceForApplication(this)
                }
                //this.beaconManager?.setForegroundScanPeriod(100)
                //this.beaconManager?.updateScanPeriods()
                RangedBeacon.setSampleExpirationMilliseconds(1000)
                for(layout in this.beaconLayoutList) {
                    this.beaconManager?.getBeaconParsers()?.add(BeaconParser().setBeaconLayout(layout))
                }
                this.beaconManager?.bind(this)
            }
            this.hideLoadingView(this.rootView)
        }
    }

    private fun stopBeaconDetection(){
        when(LIBRARY_TYPE){
            //0 -> this.proximityHandler?.stop()
            1 -> {
                this.beaconManager?.stopRangingBeaconsInRegion(region)
            }
        }
    }

    private fun createBeaconRecord(beacon: Beacon){
        DataStore.createBeaconRecord(beacon.id!!, { _ ->
        }, { sessionExpired, error ->
            if(sessionExpired){
                this.stopBeaconDetection()
                this.logout(this, sessionExpired)
            } else {
                this.showErrorToast(this, error, EnumUtils.DataType.BEACONRECORDCREATE)
            }
        })
    }
}