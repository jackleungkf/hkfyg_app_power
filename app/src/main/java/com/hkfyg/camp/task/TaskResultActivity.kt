package com.hkfyg.camp.task

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import com.hkfyg.camp.BaseActivity
import com.hkfyg.camp.R
import com.hkfyg.camp.adapter.TaskResultRecyclerViewAdapter
import com.hkfyg.camp.model.Task
import com.hkfyg.camp.model.local.TaskResult
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.EnumUtils
import com.hkfyg.camp.widget.NavBarView

class TaskResultActivity: BaseActivity(){
    private var rootView: ViewGroup? = null
    private var navBarView: NavBarView? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: TaskResultRecyclerViewAdapter? = null

    private var taskPosition: Int? = -1
    private var subTaskPosition: Int? = -1

    private var task: Task? = null
    private var subTask: Task.SubTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_result)

        this.rootView = findViewById<ViewGroup>(R.id.rootView)
        this.navBarView = findViewById<NavBarView>(R.id.navBarView)
        this.recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        this.navBarView?.backButton?.setOnClickListener{
            this.finish()
        }

        this.taskPosition = this.intent.getIntExtra("taskPosition", -1)
        this.subTaskPosition = this.intent.getIntExtra("subTaskPosition", -1)

        if(this.taskPosition != null && this.taskPosition!! >= 0){
            if(this.subTaskPosition == null || this.subTaskPosition!! < 0) {
                this.task = DataStore.taskList.list[this.taskPosition!!]
                this.navBarView?.titleTextView?.text = String.format(this.getLocalizedStringById(R.string.task_result), this.task?.name)

                when (this.task?.uniqueId) {
                    EnumUtils.TaskUniqueId.FITNESS.uniqueId -> {
                        DataStore.user?.taskRecords?.fitnessTaskRecord?.location?.let {
                            this.showLoadingView(this.rootView)
                            DataStore.getFitnessItemList(it, { _ ->
                                this.hideLoadingView(this.rootView)
                                val list = DataStore.user!!.taskRecords!!.fitnessTaskRecord!!.getTaskResultList(this)
                                this.setAdapter(list)
                            }, { sessionExpired, error ->
                                this.hideLoadingView(this.rootView)
                                if(sessionExpired){
                                    this.logout(this, sessionExpired)
                                } else {
                                    this.showErrorToast(this, error, EnumUtils.DataType.TYPINGSCRIPTLIST)
                                }
                            })
                        }
                    }
                }
            } else {
                this.subTask = DataStore.taskList.list[this.taskPosition!!].subtasks?.get(this.subTaskPosition!!)
                this.navBarView?.titleTextView?.text = String.format(this.getLocalizedStringById(R.string.task_result), this.subTask?.name)

                when(this.subTask?.uniqueId){
                    EnumUtils.SubTaskUniqueId.TYPING.uniqueId -> {
                        this.showLoadingView(this.rootView)
                        DataStore.getTypingScriptList({ _ ->
                            this.hideLoadingView(this.rootView)
                            DataStore.user?.taskRecords?.typingTaskRecord?.let{
                                this.setAdapter(it.getTaskResultList(this))
                            }
                        }, { sessionExpired, error ->
                            this.hideLoadingView(this.rootView)
                            if(sessionExpired){
                                this.logout(this, sessionExpired)
                            } else {
                                this.showErrorToast(this, error, EnumUtils.DataType.TYPINGSCRIPTLIST)
                            }
                        })
                    }
                    EnumUtils.SubTaskUniqueId.BALANCE.uniqueId -> {
                        this.showLoadingView(this.rootView)
                        DataStore.getBalanceItemList({ _ ->
                            this.hideLoadingView(this.rootView)
                            DataStore.user?.taskRecords?.balanceTaskRecord?.let{
                                this.setAdapter(it.getTaskResultList(this))
                            }
                        }, { sessionExpired, error ->
                            this.hideLoadingView(this.rootView)
                            if(sessionExpired){
                                this.logout(this, sessionExpired)
                            } else {
                                this.showErrorToast(this, error, EnumUtils.DataType.BALANCEITEMLIST)
                            }
                        })
                    }
                    EnumUtils.SubTaskUniqueId.TANGRAM.uniqueId -> {
                        this.showLoadingView(this.rootView)
                        DataStore.getTangramItemList({ _ ->
                            this.hideLoadingView(this.rootView)
                            DataStore.user?.taskRecords?.tangramTaskRecord?.let {
                                this.setAdapter(it.getTaskResultList(this))
                            }
                        }, { sessionExpired, error ->
                            this.hideLoadingView(this.rootView)
                            if(sessionExpired){
                                this.logout(this, sessionExpired)
                            } else {
                                this.showErrorToast(this, error, EnumUtils.DataType.TANGRAMITEMLIST)
                            }
                        })
                    }
                    EnumUtils.SubTaskUniqueId.CALCULATION.uniqueId -> {
                        this.showLoadingView(this.rootView)
                        DataStore.getCalculationItemList({ _ ->
                            this.hideLoadingView(this.rootView)
                            DataStore.user?.taskRecords?.calculationTaskRecord?.let{
                                this.setAdapter(it.getTaskResultList((this)))
                            }
                        }, { sessionExpired, error ->
                            this.hideLoadingView(this.rootView)
                            if(sessionExpired){
                                this.logout(this, sessionExpired)
                            } else {
                                this.showErrorToast(this, error, EnumUtils.DataType.CALCULATIONITEMLIST)
                            }
                        })
                    } EnumUtils.SubTaskUniqueId.CUBE.uniqueId -> {
                        this.showLoadingView(this.rootView)
                        DataStore.getCubeCombinationList({ _ ->
                            this.hideLoadingView(this.rootView)
                            DataStore.user?.taskRecords?.cubeTaskRecord?.let{
                                this.setAdapter(it.getTaskResultList(this))
                            }
                        }, { sessionExpired, error ->
                            this.hideLoadingView(this.rootView)
                            if(sessionExpired){
                                this.logout(this, sessionExpired)
                            } else {
                                this.showErrorToast(this, error, EnumUtils.DataType.CUBEITEMLIST)
                            }
                        })
                    } else -> {
                    }
                }
            }
        }
    }

    private fun setAdapter(list: ArrayList<TaskResult>) {
        this.adapter = TaskResultRecyclerViewAdapter(this, list)
        this.recyclerView?.adapter = this.adapter
        this.recyclerView?.layoutManager = LinearLayoutManager(this)
    }
}