package com.hkfyg.camp.beacon

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.ViewGroup
import android.widget.*
import com.hkfyg.camp.BaseActivity
import com.hkfyg.camp.R
import com.hkfyg.camp.utils.DataStore
import org.altbeacon.beacon.*
import java.text.SimpleDateFormat
import java.util.*
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.service.RangedBeacon


class BeaconTestingActivity: BaseActivity()/*, BeaconConsumer*/{
    /*private var rootView: ViewGroup? = null
    private var listView: ListView? = null
    private var button: Button? = null

    private var deviceList: ArrayList<String> = arrayListOf()
    private var adapter: ArrayAdapter<String>? = null

    private var scanning: Boolean = false

    private var bluetoothAdapter: BluetoothAdapter? = null

    //reactive beacon
    //private var reactiveBeacons: ReactiveBeacons? = null
    //private var subscription: Disposable? = null

    //estimote
    //private val estimoteCloudCredentials = EstimoteCloudCredentials(Constants.estimoteAppId, Constants.estimoteAppToken)
    //private var proximityObserver: ProximityObserver? = null
    //private var venusZone: ProximityZone? = null
    //private var observationHandler: ProximityObserver.Handler? = null

    //android beacon library
    private var beaconManager: BeaconManager? = null
    private val region: Region = Region("", null, null, null)
    //private val region: Region = Region("", Identifier.parse("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null)

    private var lastDetectedBeaconId: String? = null
    private var detectedBeaconsMap: MutableMap<String, Double> = mutableMapOf()

    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val PERMISSION_REQUEST_CODE: Int = 0
    private val BLUETOOTH_ENABLE_REQUEST_CODE: Int = 1
    private val LIBRARY_TYPE: Int = 1   // 0: estimote, 1: android beacon library

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beacon_testing)

        this.rootView = findViewById<ViewGroup>(R.id.rootView)
        this.listView = findViewById<ListView>(R.id.listView)
        this.button = findViewById<Button>(R.id.button)

        this.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, this.deviceList)
        this.listView?.adapter = this.adapter

        this.button?.setOnClickListener{
            this.buttonClicked()
        }

        try{
            this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        } catch (exception: Exception){
            Log.d("BeaconTestingActivity", "getDefaultAdapter error: ${exception.message}")
        }

        //this.reactiveBeacons = ReactiveBeacons(this)
    }

    override fun onDestroy() {
        when(LIBRARY_TYPE){
            0 -> this.observationHandler?.stop()
            1 -> {
                this.beaconManager?.stopRangingBeaconsInRegion(region)
                this.beaconManager?.unbind(this)
            }
        }
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == BLUETOOTH_ENABLE_REQUEST_CODE){
            when(resultCode){
                Activity.RESULT_OK -> {
                    this.buttonClicked()
                } else -> {
                    Toast.makeText(this, "Bluetooth must be enabled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, this.getLocalizedStringById(R.string.permission_granted), Toast.LENGTH_SHORT).show()
                this.buttonClicked()
            }
        }
    }

    override fun onBeaconServiceConnect() {
        Log.d("BeaconTestingActivity", "onBeaconServiceConnect")
        this.beaconManager?.removeAllRangeNotifiers()
        this.beaconManager?.addRangeNotifier(object: RangeNotifier {
            override fun didRangeBeaconsInRegion(p0: MutableCollection<Beacon>?, p1: Region?) {
                Log.d("BeaconTestingActivity", "didRangeBeaconsInRegion, size: ${p0?.size}")
                runOnUiThread {
                    p0?.let {
                        var uuid: String? = null

                        for(beacon in it.iterator()){
                            uuid = "${beacon.id1?.toUuid()?.toString()};${beacon.id2?.toString()};${beacon.id3?.toString()}"
                            if(!uuid.isNullOrEmpty()) {
                                this@BeaconTestingActivity.detectedBeaconsMap.put(uuid, beacon.distance)
                            }
                        }

                        if(this@BeaconTestingActivity.detectedBeaconsMap.isNotEmpty()) {
                            val sortedMap = this@BeaconTestingActivity.detectedBeaconsMap.toList().sortedBy { (_, value) -> value }.toMap()
                            Log.d("BeaconTestingActivity", "sortedList")
                            for (item in sortedMap) {
                                Log.d("BeaconTestingActivity", "${item.key}, ${item.value}")
                            }
                            Log.d("BeaconTestingActivity", "\n\n\n")

                            val firstItem = sortedMap.entries.first()
                            if (this@BeaconTestingActivity.lastDetectedBeaconId == null || !firstItem.key.equals(this@BeaconTestingActivity.lastDetectedBeaconId)) {
                                this@BeaconTestingActivity.deviceList.add("${firstItem.key}, ${firstItem.value}")
                                this@BeaconTestingActivity.lastDetectedBeaconId = firstItem.key
                            }
                        }
                    }
                    this@BeaconTestingActivity.adapter?.notifyDataSetChanged()
                    this@BeaconTestingActivity.listView?.smoothScrollToPosition(this@BeaconTestingActivity.deviceList.size-1)
                }
            }
        })
    }

    private fun initProximityObserver(){
        if(this.proximityObserver == null) {
            this.proximityObserver = ProximityObserverBuilder(this.applicationContext, this.estimoteCloudCredentials)
                //.withBalancedPowerMode()
                .withLowLatencyPowerMode()
                .onError {
                    Log.d("BeaconTestingActivity", "proximityObserver error: ${it}")
                }
                .build()
        }

        if(this.venusZone == null) {
            this.venusZone = ProximityZoneBuilder()
                .forTag("testing")
                .inFarRange()
                .onEnter {
                    Log.d("BeaconTestingActivity", "onEnter, ${it.deviceId}")
                    runOnUiThread {
                        this.deviceList.add("${this.simpleDateFormat.format(Date())}, enter, ${it.deviceId}")
                        this.adapter?.notifyDataSetChanged()
                    }
                }
                .onExit {
                    Log.d("BeaconTestingActivity", "onExit, ${it.deviceId}")
                    runOnUiThread {
                        this.deviceList.add("${this.simpleDateFormat.format(Date())}, exit, ${it.deviceId}")
                        this.adapter?.notifyDataSetChanged()
                    }
                }
                .onContextChange {
                    Log.d("BeaconTestingActivity", "onContextChange")
                    for (context in it.iterator()) {
                        if (this.lastDetectedBeaconId == null || !this.lastDetectedBeaconId!!.equals(context.deviceId)) {
                            this.lastDetectedBeaconId = context.deviceId
                            runOnUiThread {
                                this.deviceList.add("${this.simpleDateFormat.format(Date())}, contextChange, ${context.deviceId}")
                                this.adapter?.notifyDataSetChanged()
                            }
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
        }
    }

    private fun initBeaconManager(){
        if(this.beaconManager == null) {
            this.beaconManager = BeaconManager.getInstanceForApplication(this)
            //this.beaconManager?.setForegroundScanPeriod(100)
            //this.beaconManager?.updateScanPeriods()
            RangedBeacon.setSampleExpirationMilliseconds(1000)
            //add estimote beacon layout
            this.beaconManager?.getBeaconParsers()?.add(BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"))
            this.beaconManager?.bind(this)
        }
    }

    private fun createBeaconRecord(beacon: com.hkfyg.camp.model.Beacon){
        DataStore.createBeaconRecord(beacon.id!!, { response ->
            Log.d("BeaconTestingActivity", "beaconRecord create success")
            Toast.makeText(this, "create beaconRecord success, id: ${beacon.uuid}", Toast.LENGTH_SHORT).show()
        }, { sessionExpired, error ->
            Log.d("BeaconTestingActivity", "beaconRecord create failed")
            Toast.makeText(this, "create beaconRecord failed, id: ${beacon.uuid}", Toast.LENGTH_SHORT).show()
        })
    }

    private fun buttonClicked(){
        if(this.scanning){
            //this.subscription?.dispose()
            //this.observationHandler?.stop()
            when(LIBRARY_TYPE) {
                0 -> this.observationHandler?.stop()
                1 -> {
                    this.beaconManager?.stopRangingBeaconsInRegion(region)
                }
            }
            this.scanning = false
            this.button?.text = "Start"
        } else {
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                this.requestPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
            } else if(this.bluetoothAdapter != null && !this.bluetoothAdapter!!.isEnabled) {
                this.startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BLUETOOTH_ENABLE_REQUEST_CODE)
            } else if(this.bluetoothAdapter != null){
                //this.initProximityObserver()
                when(LIBRARY_TYPE){
                    0 -> {
                        this.initProximityObserver()
                        this.observationHandler = this.proximityObserver?.startObserving(this.venusZone!!)
                    }
                    1 -> {
                        this.initBeaconManager()
                        this.beaconManager?.startRangingBeaconsInRegion(region)
                    }
                }
                this.deviceList.clear()
                this.adapter?.notifyDataSetChanged()

                this.scanning = true
                this.button?.text = "Stop"
            } else {
                Toast.makeText(this, "Cannot get default bluetooth adapter", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestPermission(permission: String){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, permission)){
            this.showAlertDialog("Require Location Permission", "Location permission is required", {
                ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSION_REQUEST_CODE)
            })
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSION_REQUEST_CODE)
        }
    }*/
}