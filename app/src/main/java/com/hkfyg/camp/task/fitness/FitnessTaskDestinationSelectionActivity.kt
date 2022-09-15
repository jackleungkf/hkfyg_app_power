package com.hkfyg.camp.task.fitness

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import com.hkfyg.camp.BaseActivity
import com.hkfyg.camp.R
import com.hkfyg.camp.adapter.SubTaskRecyclerViewAdapter
import com.hkfyg.camp.campaign.TabActivity
import com.hkfyg.camp.model.Location
import com.hkfyg.camp.model.Task
import com.hkfyg.camp.task.SubTaskFragment
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.EnumUtils
import com.hkfyg.camp.widget.NavBarView

class FitnessTaskDestinationSelectionActivity: BaseActivity(), SubTaskRecyclerViewAdapter.SubTaskRecyclerViewAdapterListener{
    private var rootView: ViewGroup? = null
    private var navBarView: NavBarView? = null
    private var imageView: ImageView? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: SubTaskRecyclerViewAdapter<Location>? = null

    private var taskPosition: Int = -1
    private var task: Task? = null
    private var selectedLocationPosition: Int? = null

    private var wifiManager: WifiManager? = null
    private var startTaskPending: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_destination_selection)
        this.rootView = findViewById<ViewGroup>(R.id.rootView)
        this.navBarView = findViewById<NavBarView>(R.id.navBarView)
        this.imageView = findViewById<ImageView>(R.id.imageView)
        this.recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        this.navBarView?.backButton?.setOnClickListener{
            this.finish()
        }

        this.taskPosition = this.intent.getIntExtra("taskPosition", -1)

        if(this.taskPosition >= 0){
            this.task = DataStore.taskList.list[this.taskPosition]
            this.navBarView?.titleTextView?.text = this.task!!.name

            if(this.task?.locations != null) {
                this.adapter = SubTaskRecyclerViewAdapter(this, this.taskPosition, this.task!!.locations!!, this)
                this.recyclerView?.adapter = this.adapter
                this.recyclerView?.layoutManager = LinearLayoutManager(this)
            }
        }

        this.wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK && requestCode == TabActivity.TIMER_ACTIVITY_REQUEST_CODE){
            this.setResult(android.app.Activity.RESULT_OK)
            this.finish()
        }
    }

    private fun startTask(){
        this.selectedLocationPosition?.let {
            val intent = Intent(this@FitnessTaskDestinationSelectionActivity, FitnessTimerActivity::class.java)
            intent.putExtra("taskPosition", this.taskPosition)
            intent.putExtra("locationId", this.task!!.locations!![it].id)
            intent.putExtra("locationUniqueId", this.task!!.locations!![it].uniqueId)
            intent.putExtra("locationName", this.task!!.locations!![it].name)
            intent.putExtra("locationMapImageUrl", this.task!!.locations!![it].mapImageThumbnail)
            intent.putExtra("locationQRCodeImageUrl", this.task!!.locations!![it].qrCodeImageThumbnail)

            this.startActivityForResult(intent, TabActivity.TIMER_ACTIVITY_REQUEST_CODE)
        }
    }

    override fun subTaskItemClicked(position: Int) {
        val locationId = this.task!!.locations!![position].id
        if(locationId != null){
            /*val subTaskPositionList = arrayListOf<Int>()
            for(i in 1 until this.task!!.subtasks!!.size){
                subTaskPositionList.add(i)
            }
            val intent = Intent(this@FitnessTaskDestinationSelectionActivity, FitnessTimerActivity::class.java)
            intent.putExtra("taskPosition", this.taskPosition)
            intent.putExtra("subTaskPositionList", subTaskPositionList)
            intent.putExtra("locationId", this.task!!.locations!![position].id)
            intent.putExtra("locationUniqueId", this.task!!.locations!![position].uniqueId)
            intent.putExtra("locationName", this.task!!.locations!![position].name)
            intent.putExtra("locationMapImageUrl", this.task!!.locations!![position].mapImageThumbnail)
            intent.putExtra("locationQRCodeImageUrl", this.task!!.locations!![position].qrCodeImageThumbnail)

            //this.startActivity(intent)
            //this.setResult(android.app.Activity.RESULT_OK)
            //this.finish()

            this.startActivityForResult(intent, TabActivity.TIMER_ACTIVITY_REQUEST_CODE)*/

            this.showLoadingView(this.rootView)
            DataStore.getFitnessItemList(locationId, { results ->
                /*this.hideLoadingView(this.rootView)
                val intent = Intent(this@FitnessTaskDestinationSelectionActivity, FitnessTimerActivity::class.java)
                intent.putExtra("taskPosition", this.taskPosition)
                intent.putExtra("locationId", this.task!!.locations!![position].id)
                intent.putExtra("locationUniqueId", this.task!!.locations!![position].uniqueId)
                intent.putExtra("locationName", this.task!!.locations!![position].name)
                intent.putExtra("locationMapImageUrl", this.task!!.locations!![position].mapImageThumbnail)
                intent.putExtra("locationQRCodeImageUrl", this.task!!.locations!![position].qrCodeImageThumbnail)

                this.startActivityForResult(intent, TabActivity.TIMER_ACTIVITY_REQUEST_CODE)*/

                this.selectedLocationPosition = position
                when(this.getConnectionType()){
                    EnumUtils.CONNECTIONTYPE.MOBILEDATA -> {
                        this.wifiManager?.isWifiEnabled = true
                        this.startTask()
                    }
                    EnumUtils.CONNECTIONTYPE.WIFI -> {
                        this.showLoadingView(this.rootView)
                        this.registerConnectionStatusReceiver(this.connectionStatusReceiver)
                        this.wifiManager?.isWifiEnabled = false
                    }
                    EnumUtils.CONNECTIONTYPE.NONE -> {
                        this.promptEnableMobileData()
                    }
                }
            }, { sessionExpired, error ->
                this.hideLoadingView(this.rootView)
                if(sessionExpired){
                    this.logout(this, sessionExpired)
                } else {
                    this.showErrorToast(this, error, EnumUtils.DataType.FITNESSITEMLIST)
                }
            })
        }
    }

    override fun connectionStatusChanged(){
        when (this.getConnectionType()) {
            EnumUtils.CONNECTIONTYPE.MOBILEDATA -> {
                this.noConnectionHandler.removeCallbacks(this.noConnectionRunnable)
                this.wifiManager?.isWifiEnabled = true
                this.startTaskPending = true
            }
            EnumUtils.CONNECTIONTYPE.WIFI -> {
                if(this.startTaskPending) {
                    this.hideLoadingView(this.rootView)
                    this.startTaskPending = false
                    this.unregisterReceiver(this.connectionStatusReceiver)
                    this.startTask()
                }
            }
            EnumUtils.CONNECTIONTYPE.NONE -> {
                this.showLoadingView(this.rootView)
                this.noConnectionHandler.removeCallbacks(this.noConnectionRunnable)
                this.noConnectionHandler.postDelayed(this.noConnectionRunnable, 5000)
            }
        }
    }

    override fun promptEnableMobileData(){
        this.hideLoadingView(this.rootView)
        this.showAlertDialog("No Network Connection", "Enable mobile data in settings", {
            this.showLoadingView(this.rootView)
            val intent = Intent()
            intent.component = ComponentName("com.android.settings", "com.android.settings.Settings\$DataUsageSummaryActivity")
            startActivity(intent)
        })
    }
}