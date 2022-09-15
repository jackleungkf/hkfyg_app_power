package com.hkfyg.camp.task.magiccircle

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.hkfyg.camp.BaseActivity
import com.hkfyg.camp.R
import com.hkfyg.camp.adapter.RadioItemSelectionRecyclerViewAdapter
import com.hkfyg.camp.model.Task
import com.hkfyg.camp.model.taskrecords.MagicCircleTaskRecord
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.EnumUtils
import com.hkfyg.camp.utils.Utils
import com.hkfyg.camp.widget.InputView
import com.hkfyg.camp.widget.NavBarView
import kotlinx.android.synthetic.main.activity_login.*

class MagicCircleResultActivity: BaseActivity(), RadioItemSelectionRecyclerViewAdapter.RadioItemSelectionRecyclerAdapterListener{
    private var rootView: ViewGroup? = null
    private var navBarView: NavBarView? = null
    private var completeTimeInputView: InputView? = null
    private var completedItemTextView: TextView? = null
    private var recyclerView: RecyclerView? = null
    private var completeButton: Button? = null

    private var adapter: RadioItemSelectionRecyclerViewAdapter<MagicCircleTaskRecord.MagicCircleRecord>? = null

    private var subTask: Task.SubTask? = null
    private var update: Boolean? = null
    private var recordList: ArrayList<MagicCircleTaskRecord.MagicCircleRecord> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_magic_circle_task_result)

        this.rootView = findViewById<ViewGroup>(R.id.rootView)
        this.navBarView = findViewById<NavBarView>(R.id.navBarView)
        this.completeTimeInputView = findViewById<InputView>(R.id.completeTimeInputView)
        this.completedItemTextView = findViewById<TextView>(R.id.completedItemTextView)
        this.recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        this.completeButton = findViewById<Button>(R.id.completeButton)

        this.navBarView?.backButton?.setOnClickListener{
            this.onBackPressed()
        }

        this.completeTimeInputView?.editText?.isEnabled = false

        this.update = this.intent.getBooleanExtra("update", false)

        this.completeButton?.setOnClickListener{
            this@MagicCircleResultActivity.completeButtonClicked()
        }

        this.updateDisplayLanguage()
        this.setValues()
        this.setLayout(this.update!!)
    }

    override fun onBackPressed() {
        if(this.update == null) {
            this.update = this.intent.getBooleanExtra("update", false)
        }

        if(!this.update!!) {
            super.onBackPressed()
        }
    }

    private fun updateDisplayLanguage(){
        this.completeButton?.text = this.getLocalizedStringById(R.string.complete)
        this.completeTimeInputView?.textView?.text = this.getLocalizedStringById(R.string.finish_magic_circle_time_left)
        this.completedItemTextView?.text = this.getLocalizedStringById(R.string.completed_magic_circle_numbers)
    }

    private fun setValues(){
        val taskPosition = this.intent.getIntExtra("taskPosition", -1)
        val subTaskPosition = this.intent.getIntExtra("subTaskPosition", -1)

        if(taskPosition >= 0 && subTaskPosition >= 0){
            this.subTask = DataStore.taskList.list.get(taskPosition).subtasks?.get(subTaskPosition)
            this.subTask?.let {
                this.navBarView?.titleTextView?.text = it.name
            }
        }

        val timeLimit = when(this.subTask?.timeLimit){
            null -> 2100
            else -> this.subTask!!.timeLimit!!
        }

        DataStore.user?.taskRecords?.magicCircleTaskRecord?.let{
            //if(it.createdTime != null && it.endTime != null) {
            //    this.completeTimeInputView?.editText?.setText(Utils.getTimeLeftString(it.createdTime!!, it.endTime!!, "YYYY-MM-DD'T'HH:mm:ss'Z'", timeLimit * 60))
            //}

            if(it.createdTime != null && it.endTime != null) {
                this.completeTimeInputView?.editText?.setText(Utils.getTimeLeftString(it.createdTime!!, it.endTime!!, "YYYY-MM-DD'T'HH:mm:ss'Z'", timeLimit))
            } else {
                this.completeTimeInputView?.editText?.setText("00:00:00")
            }
        }

        if(this.update!!){
            this.navBarView?.backButton?.visibility = View.INVISIBLE
            val action = {
                for (item in DataStore.magicCircleItemList.list) {
                    this.recordList.add(MagicCircleTaskRecord.MagicCircleRecord(item))
                }
                this.setAdapter()
            }

            if(DataStore.magicCircleItemList.list.size <= 0){
                this.showLoadingView(this.rootView)
                DataStore.getMagicCircleItemList({ _ ->
                    this.hideLoadingView(this.rootView)
                    action()
                }, { sessionExpired, error ->
                    this.hideLoadingView(this.rootView)
                    if(sessionExpired){
                        this.logout(this, sessionExpired)
                    } else {
                        this.showErrorToast(this, error, EnumUtils.DataType.MAGICCIRCLEITEMLIST)
                    }
                })
            } else {
                action()
            }
        } else {
            this.adapter?.setRadioButtonsEnabled(false)
            DataStore.user?.taskRecords?.magicCircleTaskRecord?.records?.let {
                this.recordList = it
                this.setAdapter()
            }
        }

        this.setLayout(this.update!!)
    }

    private fun setLayout(update: Boolean){
        if(update){
            this.navBarView?.backButton?.visibility = View.INVISIBLE
            this.completeButton?.visibility = View.VISIBLE
        } else {
            this.navBarView?.backButton?.visibility = View.VISIBLE
            this.completeButton?.visibility = View.GONE
        }

        this.adapter?.setRadioButtonsEnabled(update)
    }

    private fun setAdapter(){
        this.adapter = RadioItemSelectionRecyclerViewAdapter(this, this.recordList, this)
        this.recyclerView?.layoutManager = GridLayoutManager(this, 2)
        this.recyclerView?.adapter = adapter
    }

    private fun completeButtonClicked(){
        if(this.loading){
            return
        }

        this.showLoadingView(this.rootView)
        DataStore.updateMagicCircleTaskRecord(this.recordList, { _ ->
            this.hideLoadingView(this.rootView)
            this.update = false
            this.setLayout(this.update!!)
        }, { sessionExpired, error ->
            this.hideLoadingView(this.rootView)
            if (sessionExpired) {
                this.logout(this, sessionExpired)
            } else {
                this.showErrorToast(this, error, EnumUtils.DataType.MAGICCIRCLETASKRECORDUPDATE)
            }
        })
    }

    override fun radioButtonClicked(position: Int, checked: Boolean) {
        this.recordList.get(position).splitSuccess = checked
        this.recordList.get(position).restoreSuccess = checked
    }
}