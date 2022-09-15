package com.hkfyg.camp.campaign

import android.app.Activity
import android.content.*
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.widget.Toast
import com.hkfyg.camp.BaseActivity
import com.hkfyg.camp.R
import com.hkfyg.camp.adapter.TabViewPagerAdapter
import com.hkfyg.camp.adapter.TaskRecyclerViewAdapter
import com.hkfyg.camp.model.Location
import com.hkfyg.camp.qr.QRCodeScanFragment
import com.hkfyg.camp.result.ResultFragment
import com.hkfyg.camp.task.*
import com.hkfyg.camp.task.balance.BalanceTimerActivity
import com.hkfyg.camp.task.buildingblocks.BuildingBlocksImageRecognitionActivity
import com.hkfyg.camp.task.buildingblocks.BuildingBlocksResultActivity
import com.hkfyg.camp.task.calculation.CalculationTimerActivity
import com.hkfyg.camp.task.cube.CubeTimerActivity
import com.hkfyg.camp.task.express.ExpressTimerActivity
import com.hkfyg.camp.task.express.ExpressUserTimerActivity
import com.hkfyg.camp.task.fitness.FitnessTimerActivity
import com.hkfyg.camp.task.magiccircle.MagicCircleResultActivity
import com.hkfyg.camp.task.magiccircle.MagicCircleTimerActivity
import com.hkfyg.camp.task.observation.ObservationResultActivity
import com.hkfyg.camp.task.observation.ObservationImageRecognitionActivity
import com.hkfyg.camp.task.power.PowerTimerActivity
import com.hkfyg.camp.task.tangram.TangramTimerActivity
import com.hkfyg.camp.task.typing.TypingTimerActivity
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.EnumUtils
import com.hkfyg.camp.utils.Utils
import kotlinx.android.synthetic.main.activity_timer.*
import java.util.*

class TabActivity: BaseActivity(), TaskRecyclerViewAdapter.TaskRecyclerViewAdapterListener,
    SubTaskFragment.SubTaskFragmentListener, SubTaskDetailFragment.SubTaskDetailFragmentListener,
    QRCodeScanFragment.QRCodeScanFragmentListener{
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null
    private var viewPagerAdapter: TabViewPagerAdapter? = null

    private val fragmentStackMap: MutableMap<Int, ArrayList<Fragment>> = mutableMapOf()

    private var taskPosition: Int? = null
    private var proceedType: EnumUtils.ProceedType? = null

    private var selectedTaskPosition: Int? = null
    private var selectedSubTaskPosition: Int? = null
    private var selectedLocation: Location? = null

    private var startActivityPending: Boolean = false
    /*private var connectionStatusReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            this@TabActivity.connectionStatusChanged()
        }
    }
    private val noConnectionHandler = Handler()
    private val noConnectionRunnable = object: Runnable{
        override fun run(){
            this@TabActivity.promptEnableMobileData()
        }
    }*/
    private var wifiManager: WifiManager? = null

    companion object {
        var IMAGE_RECOGNITION_REQUEST_CODE = 0
        val SELF_RECOGNITION_REQUEST_CODE = 1
        val FITNESS_LOCATION_REQUEST_CODE = 2
        val TIMER_ACTIVITY_REQUEST_CODE = 3
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabs)

        this.viewPager = findViewById<ViewPager>(R.id.viewPager)
        this.tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        this.tabLayout?.addTab(this.tabLayout!!.newTab().setIcon(R.drawable.challenge_active))
        this.tabLayout?.addTab(this.tabLayout!!.newTab().setIcon(R.drawable.qr_code_inactive))
        this.tabLayout?.addTab(this.tabLayout!!.newTab().setIcon(R.drawable.result_inactive))

        val taskFragment = TaskFragment.newInstance()
        val qrCodeScanFragment = QRCodeScanFragment.newInstance(true)
        val resultFragment = ResultFragment.newInstance()

        taskFragment.itemClickedListener = this
        qrCodeScanFragment.listener = this

        this.fragmentStackMap.put(0, arrayListOf(taskFragment))
        this.fragmentStackMap.put(1, arrayListOf(qrCodeScanFragment))
        this.fragmentStackMap.put(2, arrayListOf(resultFragment))

        this.viewPagerAdapter = TabViewPagerAdapter(this.supportFragmentManager, this.fragmentStackMap)
        this.viewPager?.adapter = this.viewPagerAdapter
        this.viewPager?.addOnPageChangeListener(object: TabLayout.TabLayoutOnPageChangeListener(this.tabLayout!!){})
        this.tabLayout?.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if(tab != null){
                    this@TabActivity.viewPager?.setCurrentItem(tab.position)
                }

                when(tab?.position){
                    0 -> {
                        this@TabActivity.tabLayout?.getTabAt(0)?.setIcon(R.drawable.challenge_active)
                        this@TabActivity.tabLayout?.getTabAt(1)?.setIcon(R.drawable.qr_code_inactive)
                        this@TabActivity.tabLayout?.getTabAt(2)?.setIcon(R.drawable.result_inactive)
                    }
                    1 -> {
                        this@TabActivity.tabLayout?.getTabAt(0)?.setIcon(R.drawable.challenge_inactive)
                        this@TabActivity.tabLayout?.getTabAt(1)?.setIcon(R.drawable.qr_code_active)
                        this@TabActivity.tabLayout?.getTabAt(2)?.setIcon(R.drawable.result_inactive)

                        //(this@TabActivity.fragmentStackMap.get(1)?.get(0) as? QRCodeScanFragment)?.previewing = true
                        val fragment = this@TabActivity.fragmentStackMap.get(1)?.get(0) as? QRCodeScanFragment
                        fragment?.resumeCamera()
                    }
                    2 -> {
                        this@TabActivity.tabLayout?.getTabAt(0)?.setIcon(R.drawable.challenge_inactive)
                        this@TabActivity.tabLayout?.getTabAt(1)?.setIcon(R.drawable.qr_code_inactive)
                        this@TabActivity.tabLayout?.getTabAt(2)?.setIcon(R.drawable.result_active)
                    } else -> {}
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                if(tab?.position == 1) {
                    this@TabActivity.taskPosition = null

                    //(this@TabActivity.fragmentStackMap.get(1)?.get(0) as? QRCodeScanFragment)?.previewing = false
                    val fragment = this@TabActivity.fragmentStackMap.get(1)?.get(0) as? QRCodeScanFragment
                    fragment?.stopCamera()
                }
            }
        })

        this.updateDisplayLanguage()

        this.wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    override fun onResume() {
        super.onResume()
        (this.fragmentStackMap.get(1)?.get(0) as? QRCodeScanFragment)?.listener = this
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onAttachFragment(fragment: Fragment?) {
        super.onAttachFragment(fragment)

        if(this.loading){
            this.showLoadingView(this.rootView)
        } else {
            this.hideLoadingView(this.rootView)
        }
    }

    override fun onBackPressed() {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK && (requestCode == IMAGE_RECOGNITION_REQUEST_CODE || requestCode == TIMER_ACTIVITY_REQUEST_CODE)){
            val fragmentList: ArrayList<Fragment> = arrayListOf(this.fragmentStackMap.get(0)!![0])
            this.fragmentStackMap.put(0, fragmentList)
            this.viewPagerAdapter?.notifyDataSetChanged()
        } else {
            val fragmentList = this.fragmentStackMap.get(0)
            if(fragmentList != null && fragmentList.size > 0){
                fragmentList.get(fragmentList.size-1).onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun updateDisplayLanguage(){
        this.tabLayout?.getTabAt(0)?.setText(this.getLocalizedStringById(R.string.challenge).capitalize())
        this.tabLayout?.getTabAt(1)?.setText(this.getLocalizedStringById(R.string.scan_qr_code).capitalize())
        this.tabLayout?.getTabAt(2)?.setText(this.getLocalizedStringById(R.string.my_result).capitalize())
    }

    private fun startObservationImageRecognitionActivity(){
        this.selectedLocation?.id?.let {
            val intent = Intent(this@TabActivity, ObservationImageRecognitionActivity::class.java)
            intent.putExtra("taskPosition", this.selectedTaskPosition)
            intent.putExtra("subTaskPosition", this.selectedSubTaskPosition)
            intent.putExtra("locationId", it)
            intent.putExtra("locationName", this.selectedLocation?.name)
            intent.putExtra("locationUniqueId", this.selectedLocation?.uniqueId)
            intent.putExtra("locationMapImageUrl", this.selectedLocation?.mapImageThumbnail)
            startActivityForResult(intent, IMAGE_RECOGNITION_REQUEST_CODE)
        }
    }

    override fun connectionStatusChanged(){
        when (this.getConnectionType()) {
            EnumUtils.CONNECTIONTYPE.MOBILEDATA -> {
                this.noConnectionHandler.removeCallbacks(this.noConnectionRunnable)
                this.wifiManager?.isWifiEnabled = true
                this.startActivityPending = true
            }
            EnumUtils.CONNECTIONTYPE.WIFI -> {
                if(this.startActivityPending) {
                    this.fragmentStackMap.get(0)?.let {
                        (it.get(it.size - 1) as? SubTaskFragment)?.showLoadingView(false)
                    }
                    this.startActivityPending = false
                    this.unregisterReceiver(this.connectionStatusReceiver)
                    this.startObservationImageRecognitionActivity()
                }
            }
            EnumUtils.CONNECTIONTYPE.NONE -> {
                this.fragmentStackMap.get(0)?.let{
                    (it.get(it.size-1) as? SubTaskFragment)?.showLoadingView(true)
                }
                this.noConnectionHandler.removeCallbacks(this.noConnectionRunnable)
                this.noConnectionHandler.postDelayed(this.noConnectionRunnable, 5000)
            }
        }
    }

    override fun promptEnableMobileData(){
        this.fragmentStackMap.get(0)?.let{
            (it.get(it.size-1) as? SubTaskFragment)?.showLoadingView(false)
        }

        this.showAlertDialog("No Network Connection", "Enable mobile data in settings", {
            this.fragmentStackMap.get(0)?.let{
                (it.get(it.size-1) as? SubTaskFragment)?.showLoadingView(true)
            }
            val intent = Intent()
            intent.component = ComponentName("com.android.settings", "com.android.settings.Settings\$DataUsageSummaryActivity")
            startActivity(intent)
        })
    }

    override fun taskItemClicked(position: Int) {
        if(this.loading){
            return
        }

        val task = DataStore.taskList.list[position]
        if(!task.uniqueId.isNullOrEmpty() && task.uniqueId!!.equals(EnumUtils.TaskUniqueId.FITNESS.uniqueId)) {
            val action = {
                if (DataStore.user?.taskRecords?.fitnessTaskRecord != null) {
                    /*val intent = Intent(this, TaskResultActivity::class.java)
                    intent.putExtra("taskPosition", position)
                    startActivity(intent)*/
                    this.showTaskResult(EnumUtils.TaskUniqueId.FITNESS.uniqueId, position, {})
                } else if (DataStore.user?.taskRecords?.fitnessTaskRecord == null) {
                    //Scan QR Code before can proceed
                    this.taskPosition = position
                    this.proceedType = EnumUtils.ProceedType.SUBTASKDETAIL
                    this.tabLayout?.setScrollPosition(1, 0f, true)
                    this.viewPager?.setCurrentItem(1, true)

                    // Testing
                    //task.uniqueId?.let {
                    //    this.qrResultHandled(it)
                    //}
                } else {
                    //TO-DO: handle case of app quited or crashed during task being performed
                }
            }

            if (DataStore.user?.taskRecords == null) {
                this.showLoadingView(this.rootView)
                DataStore.getTaskRecords(false, { result ->
                    this.hideLoadingView(this.rootView)
                    action()
                }, { sessionExpired, error ->
                    this.hideLoadingView(this.rootView)
                    if (sessionExpired) {
                        this.logout(this, sessionExpired)
                    } else {
                        this.showErrorToast(this, error, EnumUtils.DataType.TASKRECORDS)
                    }
                })
            } else {
                action()
            }
        } else if(!task.uniqueId.isNullOrEmpty() && task.subtasks != null && task.subtasks!!.size == 1){
            val action = {
                if((task.uniqueId.equals(EnumUtils.TaskUniqueId.POWER.uniqueId)
                    && DataStore.user?.taskRecords?.powerTaskRecord != null)
                    || (task.uniqueId.equals(EnumUtils.TaskUniqueId.EXPRESS.uniqueId)
                    && DataStore.user?.taskRecords?.expressTaskRecord != null)){
                    this.subTaskStartButtonClicked(position, 0)
                } else if ((task.uniqueId.equals(EnumUtils.TaskUniqueId.POWER.uniqueId)
                    && DataStore.user?.taskRecords?.powerTaskRecord == null)
                    || (task.uniqueId.equals(EnumUtils.TaskUniqueId.EXPRESS.uniqueId)
                    && DataStore.user?.taskRecords?.expressTaskRecord == null)){

                    //Scan QR Code before can proceed
                    this.taskPosition = position
                    this.proceedType = EnumUtils.ProceedType.SUBTASKDETAIL
                    this.tabLayout?.setScrollPosition(1, 0f, true)
                    this.viewPager?.setCurrentItem(1, true)

                    // Testing
                    //task.uniqueId?.let {
                    //    this.qrResultHandled(it)
                    //}
                }
            }

            if (DataStore.user?.taskRecords == null) {
                this.showLoadingView(this.rootView)
                DataStore.getTaskRecords(false, { result ->
                    this.hideLoadingView(this.rootView)
                    action()
                }, { sessionExpired, error ->
                    this.hideLoadingView(this.rootView)
                    if (sessionExpired) {
                        this.logout(this, sessionExpired)
                    } else {
                        this.showErrorToast(this, error, EnumUtils.DataType.TASKRECORDS)
                    }
                })
            } else {
                action()
            }
        } else {
            val action = {
                this.showTaskResult(DataStore.getSubTaskRecordUniqueId(task), position, {
                    //Scan QR code before can proceed
                    this.taskPosition = position
                    this.proceedType = EnumUtils.ProceedType.SUBTASK
                    this.tabLayout?.setScrollPosition(1, 0f, true)
                    this.viewPager?.currentItem = 1
                    this.viewPagerAdapter?.notifyDataSetChanged()

                    // Testing
                    //task.uniqueId?.let {
                    //    this.qrResultHandled(it)
                    //}
                })
            }

            if (DataStore.user?.taskRecords == null) {
                this.showLoadingView(this.rootView)
                DataStore.getTaskRecords(false, { result ->
                    this.hideLoadingView(this.rootView)
                    action()
                }, { sessionExpired, error ->
                    this.hideLoadingView(this.rootView)
                    if (sessionExpired) {
                        this.logout(this, sessionExpired)
                    } else {
                        this.showErrorToast(this, error, EnumUtils.DataType.TASKRECORDS)
                    }
                })
            } else {
                action()
            }
        }
    }

    override fun subTaskBackButtonClicked(fragment: SubTaskFragment){
        this.fragmentStackMap.get(0)?.remove(fragment)
        this.viewPagerAdapter?.notifyDataSetChanged()
    }

    override fun subTaskStartButtonClicked(taskPosition: Int, subTaskPosition: Int) {
        if(this.loading){
            return
        }

        val subTask = DataStore.taskList.list[taskPosition].subtasks?.get(subTaskPosition)
        when (subTask?.uniqueId){
            EnumUtils.SubTaskUniqueId.OBSERVATION.uniqueId -> {
                this.subTaskItemClicked(taskPosition, subTaskPosition, -1)
            }
            EnumUtils.SubTaskUniqueId.BUILDING_BLOCKS.uniqueId -> {
                this.loading = true
                DataStore.getBuildingBlocksImageList({ _ ->
                    this.hideLoadingView(this.rootView)
                    val intent = Intent(this, BuildingBlocksImageRecognitionActivity::class.java)
                    intent.putExtra("taskPosition", taskPosition)
                    intent.putExtra("subTaskPosition", subTaskPosition)
                    this.startActivityForResult(intent, TIMER_ACTIVITY_REQUEST_CODE)
                }, { sessionExpired, error ->
                    this.hideLoadingView(this.rootView)
                    this.showErrorToast(this, error, EnumUtils.DataType.BUILDINGBLOCKSIMAGELIST)
                })
            }
            EnumUtils.SubTaskUniqueId.TYPING.uniqueId -> {
                this.loading = true
                DataStore.getTypingScriptList({ result ->
                    this.hideLoadingView(this.rootView)
                    val intent = Intent(this, TypingTimerActivity::class.java)
                    intent.putExtra("taskPosition", taskPosition)
                    intent.putExtra("subTaskPosition", subTaskPosition)
                    this.startActivityForResult(intent, TIMER_ACTIVITY_REQUEST_CODE)
                }, { sessionExpired, error ->
                    this.hideLoadingView(this.rootView)
                    this.showErrorToast(this, error, EnumUtils.DataType.TYPINGSCRIPTLIST)
                })
            }
            EnumUtils.SubTaskUniqueId.BALANCE.uniqueId -> {
                this.loading = true
                DataStore.getBalanceItemList({ result ->
                    this.hideLoadingView(this.rootView)
                    val intent = Intent(this, BalanceTimerActivity::class.java)
                    intent.putExtra("taskPosition", taskPosition)
                    intent.putExtra("subTaskPosition", subTaskPosition)
                    this.startActivityForResult(intent, TIMER_ACTIVITY_REQUEST_CODE)
                }, { sessionExpired, error ->
                    this.hideLoadingView(this.rootView)
                    this.showErrorToast(this, error, EnumUtils.DataType.BALANCEITEMLIST)
                })
            }
            EnumUtils.SubTaskUniqueId.MAGIC_CIRCLE.uniqueId -> {
                this.loading = true
                DataStore.getMagicCircleItemList({ result ->
                    this.hideLoadingView(this.rootView)
                    val intent = Intent(this, MagicCircleTimerActivity::class.java)
                    intent.putExtra("taskPosition", taskPosition)
                    intent.putExtra("subTaskPosition", subTaskPosition)
                    this.startActivityForResult(intent, TIMER_ACTIVITY_REQUEST_CODE)
                }, { sessionExpired, error ->
                    this.hideLoadingView(this.rootView)
                    this.showErrorToast(this, error, EnumUtils.DataType.MAGICCIRCLEITEMLIST)
                })
            }
            EnumUtils.SubTaskUniqueId.TANGRAM.uniqueId -> {
                this.loading = true
                DataStore.getTangramItemList({ result ->
                    this.hideLoadingView(this.rootView)
                    val intent = Intent(this, TangramTimerActivity::class.java)
                    intent.putExtra("taskPosition", taskPosition)
                    intent.putExtra("subTaskPosition", subTaskPosition)
                    this.startActivityForResult(intent, TIMER_ACTIVITY_REQUEST_CODE)
                }, { sessionExpired, error ->
                    this.hideLoadingView(this.rootView)
                    this.showErrorToast(this, error, EnumUtils.DataType.TANGRAMITEMLIST)
                })
            }
            EnumUtils.SubTaskUniqueId.CALCULATION.uniqueId -> {
                this.loading = true
                DataStore.getCalculationItemList({ result ->
                    this.hideLoadingView(this.rootView)
                    val intent = Intent(this, CalculationTimerActivity::class.java)
                    intent.putExtra("taskPosition", taskPosition)
                    intent.putExtra("subTaskPosition", subTaskPosition)
                    this.startActivityForResult(intent, TIMER_ACTIVITY_REQUEST_CODE)
                }, { sessionExpired, error ->
                    this.hideLoadingView(this.rootView)
                    this.showErrorToast(this, error, EnumUtils.DataType.CALCULATIONITEMLIST)
                })
            }
            EnumUtils.SubTaskUniqueId.CUBE.uniqueId -> {
                this.loading = true
                DataStore.getCubeCombinationList({ result ->
                    this.hideLoadingView(this.rootView)
                    val intent = Intent(this, CubeTimerActivity::class.java)
                    intent.putExtra("taskPosition", taskPosition)
                    intent.putExtra("subTaskPosition", subTaskPosition)
                    this.startActivityForResult(intent, TIMER_ACTIVITY_REQUEST_CODE)
                }, { sessionExpired, error ->
                    this.hideLoadingView(this.rootView)
                    this.showErrorToast(this, error, EnumUtils.DataType.CUBEITEMLIST)
                })
            } EnumUtils.SubTaskUniqueId.POWER.uniqueId -> {
                if(DataStore.user?.isStaff == true || DataStore.user?.taskRecords?.powerTaskRecord != null) {
                    this.loading = true
                    DataStore.getPowerQuestionList({ result ->
                        this.hideLoadingView(this.rootView)
                        val intent = Intent(this, PowerTimerActivity::class.java)
                        intent.putExtra("taskPosition", taskPosition)
                        intent.putExtra("subTaskPosition", subTaskPosition)
                        this.startActivityForResult(intent, TIMER_ACTIVITY_REQUEST_CODE)
                    }, { sessionExpired, error ->
                        this.hideLoadingView(this.rootView)
                        this.showErrorToast(this, error, EnumUtils.DataType.POWERQUESTIONLIST)
                    })
                } else {
                    this.loading = true
                    DataStore.getTaskRecords(true, { results ->
                        this.hideLoadingView(this.rootView)
                        if(results.powerTaskRecord != null){
                            this.subTaskStartButtonClicked(taskPosition, subTaskPosition)
                        } else {
                            Toast.makeText(this, this.getLocalizedStringById(R.string.permission_denied), Toast.LENGTH_SHORT).show()
                        }
                    }, { sessionExpired, error ->
                        this.hideLoadingView(this.rootView)
                        if(sessionExpired){
                            this.logout(this, sessionExpired)
                        } else {
                            this.showErrorToast(this, sessionExpired, EnumUtils.DataType.TASKRECORDS)
                        }
                    })
                }
            } EnumUtils.SubTaskUniqueId.EXPRESS.uniqueId -> {
                if(DataStore.user?.isStaff == true || DataStore.user?.taskRecords?.expressTaskRecord != null) {
                    this.loading = true
                    if(DataStore.user?.isStaff == true || DataStore.user?.taskRecords?.expressTaskRecord?.endTime != null) {
                        DataStore.getExpressQuestionList({ result ->
                            this.hideLoadingView(this.rootView)
                            val intent = Intent(this, ExpressTimerActivity::class.java)
                            intent.putExtra("taskPosition", taskPosition)
                            intent.putExtra("subTaskPosition", subTaskPosition)
                            this.startActivityForResult(intent, TIMER_ACTIVITY_REQUEST_CODE)
                        }, { sessionExpired, error ->
                            this.hideLoadingView(this.rootView)
                            this.showErrorToast(this, error, EnumUtils.DataType.EXPRESSQUESTIONLIST)
                        })
                    } else {
                        this.loading = true
                        DataStore.getBeaconList({ _ ->
                            this.hideLoadingView(this.rootView)
                            val intent = Intent(this, ExpressUserTimerActivity::class.java)
                            intent.putExtra("taskPosition", taskPosition)
                            intent.putExtra("subTaskPosition", subTaskPosition)
                            this.startActivityForResult(intent, TIMER_ACTIVITY_REQUEST_CODE)
                        }, { sessionExpired, error ->
                            this.hideLoadingView(this.rootView)
                            this.showErrorToast(this, error, EnumUtils.DataType.BEACONLIST)
                        })
                    }
                } else {
                    this.loading = true
                    DataStore.getTaskRecords(true, { results ->
                        this.hideLoadingView(this.rootView)
                        if(results.expressTaskRecord != null){
                            this.subTaskStartButtonClicked(taskPosition, subTaskPosition)
                        } else {
                            Toast.makeText(this, this.getLocalizedStringById(R.string.permission_denied), Toast.LENGTH_SHORT).show()
                        }
                    }, { sessionExpired, error ->
                        this.hideLoadingView(this.rootView)
                        if(sessionExpired){
                            this.logout(this, sessionExpired)
                        } else {
                            this.showErrorToast(this, sessionExpired, EnumUtils.DataType.TASKRECORDS)
                        }
                    })
                }
            } else -> {}
        }
    }

    override fun subTaskItemClicked(taskPosition: Int, subTaskPosition: Int, locationPosition: Int?) {
        if(this.loading){
            return
        }

        if(locationPosition != null){
            val subTask = DataStore.taskList.list[taskPosition].subtasks?.get(subTaskPosition)

            if(locationPosition >= 0 && subTask?.uniqueId != null && subTask.uniqueId!!.equals(EnumUtils.SubTaskUniqueId.OBSERVATION.uniqueId)){
                val location = DataStore.taskList.list[taskPosition].subtasks?.get(subTaskPosition)?.locations?.get(locationPosition)
                location?.id?.let{
                    this.showLoadingView(this.rootView)
                    this.fragmentStackMap.get(0)?.let{
                        (it.get(it.size-1) as? SubTaskFragment)?.showLoadingView(true)
                    }
                    DataStore.getObservationImageList(it, { results ->
                        this.hideLoadingView(this.rootView)

                        /*val intent = Intent(this@TabActivity, ObservationImageRecognitionActivity::class.java)
                        intent.putExtra("taskPosition", taskPosition)
                        intent.putExtra("subTaskPosition", subTaskPosition)
                        intent.putExtra("locationId", it)
                        intent.putExtra("locationName", location.name)
                        intent.putExtra("locationUniqueId", location.uniqueId)
                        intent.putExtra("locationMapImageUrl", location.mapImageThumbnail)
                        startActivityForResult(intent, IMAGE_RECOGNITION_REQUEST_CODE)*/

                        this.selectedTaskPosition = taskPosition
                        this.selectedSubTaskPosition = subTaskPosition
                        this.selectedLocation = location

                        when(this.getConnectionType()){
                            EnumUtils.CONNECTIONTYPE.MOBILEDATA -> {
                                this.wifiManager?.isWifiEnabled = true
                                this.startObservationImageRecognitionActivity()
                            }
                            EnumUtils.CONNECTIONTYPE.WIFI -> {
                                this.fragmentStackMap.get(0)?.let{
                                    (it.get(it.size-1) as? SubTaskFragment)?.showLoadingView(true)
                                }
                                this.registerConnectionStatusReceiver(this.connectionStatusReceiver)
                                this.wifiManager?.isWifiEnabled = false
                            } else -> {
                                this.promptEnableMobileData()
                            }
                        }
                    }, { sessionExpired, error ->
                        this.hideLoadingView(this.rootView)
                        this.fragmentStackMap.get(0)?.let{
                            (it.get(it.size-1) as? SubTaskFragment)?.showLoadingView(false)
                        }
                        if(sessionExpired){
                            this.logout(this, sessionExpired)
                        } else {
                            this.showErrorToast(this, error, EnumUtils.DataType.OBSERVATIONIMAGELIST)
                        }
                    })
                }
            } else {
                val subTaskFragment = SubTaskFragment.newInstance(taskPosition, subTaskPosition)
                subTaskFragment.listener = this
                this.fragmentStackMap.get(0)?.add(subTaskFragment)
                this.viewPagerAdapter?.notifyDataSetChanged()
            }
        } else {
            //val subTaskDetailFragment = SubTaskDetailFragment.newInstance(taskPosition, subTaskPosition, locationPosition)
            //subTaskDetailFragment.listener = this
            //this.fragmentStackMap.get(0)?.add(subTaskDetailFragment)
            //this.viewPagerAdapter?.notifyDataSetChanged()

            this.showSubTaskDetailFragment(taskPosition, subTaskPosition, locationPosition)
        }
    }

    override fun subTaskDetailBackButtonClicked(fragment: Fragment) {
        this.fragmentStackMap.get(0)?.remove(fragment)
        this.viewPagerAdapter?.notifyDataSetChanged()
    }

    override fun qrResultHandled(result: String){
        if(this.validateQrResult(result)){
            this.tabLayout?.setScrollPosition(0, 0f, true)
            this.viewPager?.currentItem = 0
            this.viewPagerAdapter?.notifyDataSetChanged()

            this.showLoadingView(this.rootView)
            DataStore.getTaskRecords(false, { records ->
                this.hideLoadingView(this.rootView)

                if(this.proceedType == EnumUtils.ProceedType.SUBTASKDETAIL
                    || (result.equals(EnumUtils.TaskUniqueId.FITNESS.uniqueId) && DataStore.user?.taskRecords?.fitnessTaskRecord == null)
                    || (result.equals(EnumUtils.TaskUniqueId.POWER.uniqueId) && DataStore.user?.taskRecords?.powerTaskRecord == null)
                    || (result.equals(EnumUtils.TaskUniqueId.EXPRESS.uniqueId) && DataStore.user?.taskRecords?.expressTaskRecord == null)
                ){
                    DataStore.getTaskByUniqueId(result)?.let {
                        /*if (it.first >= 0 && it.second.subtasks != null && it.second.subtasks!!.size > 0) {
                            if(records.fitnessTaskRecord != null) {
                                val intent = Intent(this, TaskResultActivity::class.java)
                                intent.putExtra("taskPosition", it.first)
                                startActivity(intent)
                            } else {
                                this.showSubTaskDetailFragment(it.first, 0, null)
                            }
                        }*/
                        if(result.equals(EnumUtils.TaskUniqueId.FITNESS.uniqueId)) {
                            this.showSubTaskDetailFragment(it.first, -1, null)
                        } else if(it.second.subtasks != null && it.second.subtasks!!.size > 0){
                            this.showSubTaskDetailFragment(it.first, 0, null)
                        }
                    }
                } else if (this.taskPosition != null) {
                    //val subTaskFragment = SubTaskFragment.newInstance(this.taskPosition!!, null)
                    //subTaskFragment.listener = this
                    //this.fragmentStackMap.get(0)?.add(subTaskFragment)
                    //this.viewPagerAdapter?.notifyDataSetChanged()
                    this.showTaskResult(DataStore.getSubTaskRecordUniqueId(DataStore.taskList.list.get(this.taskPosition!!)), this.taskPosition, {
                        this.showSubTaskFragment(this.taskPosition!!, null)
                    })
                } else {
                    DataStore.getTaskByUniqueId(result)?.let {
                        this@TabActivity.showTaskResult(DataStore.getSubTaskRecordUniqueId(it.second), it.first, {
                            val fragmentList = this.fragmentStackMap.get(0)
                            if (fragmentList != null && fragmentList.size > 1) {
                                this.fragmentStackMap.put(0, arrayListOf<Fragment>(fragmentList.get(0)))
                            }
                            this.showSubTaskFragment(it.first, null)
                        })
                    }
                }
            }, { sessionExpired, error ->
                this.hideLoadingView(this.rootView)

                if(sessionExpired){
                    this.logout(this, sessionExpired)
                } else {
                    this.showErrorToast(this, error, EnumUtils.DataType.TASKRECORDS)
                }
            })
        } else {
            Toast.makeText(this, this.getLocalizedStringById(R.string.invalid_qr_code), Toast.LENGTH_SHORT).show()
            this.tabLayout?.setScrollPosition(0, 0f, true)
            this.viewPager?.currentItem = 0
            this.viewPagerAdapter?.notifyDataSetChanged()
        }

        this.taskPosition = null
        this.proceedType = null
    }

    private fun validateQrResult(result: String): Boolean{
        if(this.taskPosition == null){
            return true
        } else if(this.taskPosition!! >= 0 && this.taskPosition!! < DataStore.taskList.list.size){
            val task = DataStore.taskList.list[this.taskPosition!!]
            return (task.uniqueId != null && task.uniqueId!!.equals(result))
        }
        return false
    }

    private fun showSubTaskFragment(taskPosition: Int, subTaskPosition: Int?){
        val subTaskFragment = SubTaskFragment.newInstance(taskPosition, subTaskPosition)
        subTaskFragment.listener = this
        this.fragmentStackMap.get(0)?.add(subTaskFragment)
        this.viewPagerAdapter?.notifyDataSetChanged()
    }

    private fun showSubTaskDetailFragment(taskPosition: Int, subTaskPosition: Int, locationPosition: Int?){
        val subTaskDetailFragment = SubTaskDetailFragment.newInstance(taskPosition, subTaskPosition, locationPosition)
        subTaskDetailFragment.listener = this
        this.fragmentStackMap.get(0)?.add(subTaskDetailFragment)
        this.viewPagerAdapter?.notifyDataSetChanged()
    }

    private fun showTaskResult(uniqueId: String?, position: Int?, action: () -> Unit){
        when (uniqueId) {
            EnumUtils.TaskUniqueId.FITNESS.uniqueId -> {
                //val intent = Intent(this, TaskResultActivity::class.java)
                //intent.putExtra("taskPosition", position)
                //startActivity(intent)

                DataStore.user?.taskRecords?.fitnessTaskRecord?.locationObject?.takeIf{ it.id != null }?.let {
                    val location = it
                    this.showLoadingView(this.rootView)
                    DataStore.getFitnessItemList(location.id ?: 0, { results ->
                        DataStore.getTaskByUniqueId(EnumUtils.TaskUniqueId.FITNESS.uniqueId)?.first?.let {
                            val showFitnessTimerActivity = {
                                val intent = Intent(this@TabActivity, FitnessTimerActivity::class.java)
                                intent.putExtra("taskPosition", it)
                                intent.putExtra("locationId", location.id)
                                intent.putExtra("locationUniqueId", location.uniqueId)
                                intent.putExtra("locationName", location.name)

                                this.startActivityForResult(intent, TabActivity.TIMER_ACTIVITY_REQUEST_CODE)
                                this.hideLoadingView(this.rootView)
                            }

                            val recordSize = DataStore.user?.taskRecords?.fitnessTaskRecord?.records?.size
                            if(DataStore.user?.taskRecords?.fitnessTaskRecord != null && recordSize != null && recordSize >= DataStore.fitnessItemList.list.size){
                                val lastRecord = DataStore.user?.taskRecords?.fitnessTaskRecord?.records?.get(recordSize-1)
                                if(lastRecord?.endTime != null){
                                    this.hideLoadingView(this.rootView)
                                    val intent = Intent(this, TaskResultActivity::class.java)
                                    intent.putExtra("taskPosition", it)
                                    startActivity(intent)
                                } else {
                                    this.hideLoadingView(this.rootView)
                                    showFitnessTimerActivity()
                                }
                            } else {
                                showFitnessTimerActivity()
                            }
                        } ?: this.hideLoadingView(this.rootView)
                    }, { sessionExpired, error ->
                        this.hideLoadingView(this.rootView)
                        this.showErrorToast(this, error, EnumUtils.DataType.FITNESSITEMLIST)
                    })
                }
            }
            EnumUtils.SubTaskUniqueId.OBSERVATION.uniqueId -> {
                DataStore.getSubTaskByUniqueId(position, EnumUtils.SubTaskUniqueId.OBSERVATION.uniqueId)?.first?.let {
                    if(DataStore.user?.taskRecords?.observationTaskRecord?.endTime != null) {
                        val intent = Intent(this, ObservationResultActivity::class.java)
                        intent.putExtra("taskPosition", position)
                        intent.putExtra("subTaskPosition", it)
                        startActivity(intent)
                    } else {
                        val subTaskPosition = it
                        DataStore.user?.taskRecords?.observationTaskRecord?.location?.let {
                            this.showLoadingView(this.rootView)
                            DataStore.getObservationImageList(it.id!!, { result ->
                                this.hideLoadingView(this.rootView)

                                this.selectedTaskPosition = position
                                this.selectedSubTaskPosition = subTaskPosition
                                this.selectedLocation = it
                                this.startObservationImageRecognitionActivity()
                            }, { sessionExpired, error ->
                                this.hideLoadingView(this.rootView)
                                if(sessionExpired){
                                    this.logout(this, sessionExpired)
                                } else {
                                    this.showErrorToast(this, error, EnumUtils.DataType.OBSERVATIONIMAGELIST)
                                }
                            })
                        }
                    }
                }
            }
            EnumUtils.SubTaskUniqueId.BUILDING_BLOCKS.uniqueId -> {
                DataStore.getSubTaskByUniqueId(position, EnumUtils.SubTaskUniqueId.BUILDING_BLOCKS.uniqueId)?.first?.let {
                    val intent = Intent(this, BuildingBlocksResultActivity::class.java)
                    intent.putExtra("taskPosition", position)
                    intent.putExtra("subTaskPosition", it)
                    startActivity(intent)
                }
            }
            EnumUtils.SubTaskUniqueId.TYPING.uniqueId -> {
                DataStore.getSubTaskByUniqueId(position, EnumUtils.SubTaskUniqueId.TYPING.uniqueId)?.first?.let {
                    val intent = Intent(this, TaskResultActivity::class.java)
                    intent.putExtra("taskPosition", position)
                    intent.putExtra("subTaskPosition", it)
                    startActivity(intent)
                }
            }
            EnumUtils.SubTaskUniqueId.BALANCE.uniqueId -> {
                DataStore.getSubTaskByUniqueId(position, EnumUtils.SubTaskUniqueId.BALANCE.uniqueId)?.first?.let{
                    val intent = Intent(this, TaskResultActivity::class.java)
                    intent.putExtra("taskPosition", position)
                    intent.putExtra("subTaskPosition", it)
                    startActivity(intent)
                }
            }
            EnumUtils.SubTaskUniqueId.MAGIC_CIRCLE.uniqueId -> {
                DataStore.getSubTaskByUniqueId(position, EnumUtils.SubTaskUniqueId.MAGIC_CIRCLE.uniqueId)?.first?.let{
                    val intent = Intent(this, MagicCircleResultActivity::class.java)
                    intent.putExtra("taskPosition", position)
                    intent.putExtra("subTaskPosition", it)
                    startActivity(intent)
                }
            }
            EnumUtils.SubTaskUniqueId.TANGRAM.uniqueId -> {
                DataStore.getSubTaskByUniqueId(position, EnumUtils.SubTaskUniqueId.TANGRAM.uniqueId)?.first?.let{
                    val intent = Intent(this, TaskResultActivity::class.java)
                    intent.putExtra("taskPosition", position)
                    intent.putExtra("subTaskPosition", it)
                    startActivity(intent)
                }
            }
            EnumUtils.SubTaskUniqueId.CALCULATION.uniqueId -> {
                DataStore.getSubTaskByUniqueId(position, EnumUtils.SubTaskUniqueId.CALCULATION.uniqueId)?.first?.let {
                    val intent = Intent(this, TaskResultActivity::class.java)
                    intent.putExtra("taskPosition", position)
                    intent.putExtra("subTaskPosition", it)
                    startActivity(intent)
                }
            }
            EnumUtils.SubTaskUniqueId.CUBE.uniqueId -> {
                DataStore.getSubTaskByUniqueId(position, EnumUtils.SubTaskUniqueId.CUBE.uniqueId)?.first?.let{
                    //val intent = Intent(this, CubeTaskResultActivity::class.java)
                    val intent = Intent(this, TaskResultActivity::class.java)
                    intent.putExtra("taskPosition", position)
                    intent.putExtra("subTaskPosition", it)
                    startActivity(intent)
                }
            }
            EnumUtils.SubTaskUniqueId.POWER.uniqueId, EnumUtils.SubTaskUniqueId.EXPRESS.uniqueId -> {
                position?.let {
                    this.subTaskStartButtonClicked(it, 0)
                }
            } null -> {
                action()
            } else -> {}
        }
    }
}