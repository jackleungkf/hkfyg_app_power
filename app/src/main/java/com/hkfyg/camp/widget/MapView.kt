package com.hkfyg.camp.widget

import android.content.Context
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.hkfyg.camp.BaseActivity
import com.hkfyg.camp.R
import com.hkfyg.camp.adapter.InstructionImageRecyclerViewAdapter
import com.hkfyg.camp.model.InstructionImage
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.EnumUtils

class MapView: LinearLayout {
    var rootView: ViewGroup? = null
    var loadingView: ViewGroup? = null
    var swipeRefreshLayout: SwipeRefreshLayout? = null
    var recyclerView: RecyclerView? = null
    var adapter: InstructionImageRecyclerViewAdapter? = null

    var firstAppear = true

    private var baseActivity: BaseActivity? = null
    private var locationId: Int? = null
    private var locationName: String? = null
    private var taskType: Int? = null

    private var loading = false

    constructor(context: Context): super(context){
        this.initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs){
        this.initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr){
        this.initialize(context)
    }

    fun setParameters(baseActivity: BaseActivity, locationId: Int?, locationName: String?, taskType: Int){
        this.baseActivity = baseActivity
        this.locationId = locationId
        this.locationName = locationName
        this.taskType = taskType

        this.locationName?.let {
            this.recyclerView?.layoutManager = LinearLayoutManager(this.context)
            this.adapter = InstructionImageRecyclerViewAdapter(this.baseActivity!!, this.locationName!!, arrayListOf())
            this.recyclerView?.adapter = this.adapter
        }
    }

    fun refresh(){
        if(this.loading){
            return
        }

        this.loading = true

        if(this.baseActivity != null && this.locationId != null && this.locationName != null){
            val successCallback = { result: ArrayList<InstructionImage> ->
                this.loading = false
                this.swipeRefreshLayout?.isRefreshing = false
                this.loadingView?.visibility = View.GONE

                this.setAdapter(result)
            }

            val failureCallback = { sessionExpired: Boolean, error: Any? ->
                this.loading = false
                this.swipeRefreshLayout?.isRefreshing = false
                this.loadingView?.visibility = View.GONE

                if(sessionExpired){
                    this.baseActivity!!.logout(this.baseActivity!!, sessionExpired)
                } else {
                    this.baseActivity!!.showErrorToast(this.baseActivity!!, error, EnumUtils.DataType.INSTRUCTIONIMAGELIST)
                }
            }

            when(this.taskType){
                EnumUtils.InstructionImageTaskType.FITNESS.ordinal -> DataStore.getFitnessInstructionImageList(this.locationId!!, successCallback, failureCallback)
                else -> DataStore.getObservationInstructionImageList(this.locationId!!, successCallback, failureCallback)
            }
        } else {
            this.swipeRefreshLayout?.isRefreshing = false
        }
    }

    private fun initialize(context: Context){
        LayoutInflater.from(context).inflate(R.layout.view_mapview, this, true)
        this.rootView = findViewById<ViewGroup>(R.id.rootView)
        this.loadingView = findViewById<ViewGroup>(R.id.loadingView)
        this.swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        this.recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        this.swipeRefreshLayout?.setOnRefreshListener{
            this.refresh()
        }
    }

    private fun setAdapter(list: ArrayList<InstructionImage>){
        this.adapter?.setList(list)
        this.adapter?.notifyDataSetChanged()
    }
}