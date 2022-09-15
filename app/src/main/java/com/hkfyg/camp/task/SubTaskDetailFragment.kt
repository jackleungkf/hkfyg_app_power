package com.hkfyg.camp.task

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.hkfyg.camp.BaseActivity
import com.hkfyg.camp.R
import com.hkfyg.camp.campaign.TabActivity
import com.hkfyg.camp.model.Task
import com.hkfyg.camp.model.TaskRecords
import com.hkfyg.camp.network.CallServer
import com.hkfyg.camp.task.fitness.FitnessTaskDestinationSelectionActivity
import com.hkfyg.camp.task.fitness.SelfRecognitionActivity
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.EnumUtils
import com.hkfyg.camp.widget.NavBarView

class SubTaskDetailFragment: Fragment(){
    private var rootView: ViewGroup? = null
    private var navBarView: NavBarView? = null
    private var imageView: ImageView? = null
    private var objectiveTextView: TextView? = null
    private var objectiveDetailTextView: TextView? = null
    private var procedureTextView: TextView? = null
    private var procedureDetailTextView: TextView? = null
    private var startButton: Button? = null

    private var taskPosition: Int? = -1
    private var subTaskPosition: Int? = -1
    private var task: Task? = null
    private var subTask: Task.SubTask? = null

    var listener: SubTaskDetailFragmentListener? = null

    companion object {
        fun newInstance(taskPosition: Int, subTaskPosition: Int, location: Int?): SubTaskDetailFragment{
            val subTaskDetailFragment = SubTaskDetailFragment()
            val arguments = Bundle()
            arguments.putInt("taskPosition", taskPosition)
            arguments.putInt("subTaskPosition", subTaskPosition)
            if(location != null) {
                arguments.putInt("location", location)
            }
            subTaskDetailFragment.arguments = arguments
            return subTaskDetailFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): ViewGroup? {
        if(this.rootView == null){
            this.rootView = inflater.inflate(R.layout.fragment_subtask_detail, container, false) as ViewGroup
            this.navBarView = this.rootView?.findViewById<NavBarView>(R.id.navBarView)
            this.imageView = this.rootView?.findViewById<ImageView>(R.id.imageView)
            this.objectiveTextView = this.rootView?.findViewById<TextView>(R.id.objectiveTextView)
            this.objectiveDetailTextView = this.rootView?.findViewById<TextView>(R.id.objectiveDetailTextView)
            this.procedureTextView = this.rootView?.findViewById<TextView>(R.id.procedureTextView)
            this.procedureDetailTextView = this.rootView?.findViewById<TextView>(R.id.procedureDetailTextView)
            this.startButton = this.rootView?.findViewById<Button>(R.id.startButton)

            this.navBarView?.backButton?.setOnClickListener{
                this.listener?.subTaskDetailBackButtonClicked(this@SubTaskDetailFragment)
            }

            this.taskPosition = this.arguments?.getInt("taskPosition", -1)
            this.subTaskPosition= this.arguments?.getInt("subTaskPosition", -1)

            if(this.taskPosition != null && this.taskPosition!! >= 0 && this.subTaskPosition != null && this.subTaskPosition!! >= 0) {
                this.subTask = DataStore.taskList.list[this.taskPosition!!].subtasks?.get(this.subTaskPosition!!)
            } else if(this.taskPosition != null && this.taskPosition!! >= 0){
                this.task = DataStore.taskList.list[this.taskPosition!!]
            }

            this.updateDisplayLanguage()
            this.setValues()
        }
        return this.rootView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == TabActivity.SELF_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK){
            /*val taskPosition = data?.getIntExtra("taskPosition", -1)
            val subTaskPosition = data?.getIntExtra("subTaskPosition", -1)
            if(taskPosition!! >= 0 && subTaskPosition!! >= 0 && taskPosition < DataStore.taskList.list.size){
                this.taskPosition  = taskPosition
                this.taskPosition = subTaskPosition
                this.task = DataStore.taskList.list[this.taskPosition!!]
                this.subTask = null
                this.setValues()
            }*/
            this.listener?.subTaskDetailBackButtonClicked(this)
        } else if(requestCode == TabActivity.FITNESS_LOCATION_REQUEST_CODE && resultCode == RESULT_OK){
            this.listener?.subTaskDetailBackButtonClicked(this)

            /*val pair = DataStore.getSubTaskByUniqueId(this.taskPosition, EnumUtils.SubTaskUniqueId.SELF_RECOGNITION.uniqueId)
            if(pair != null){
                this.task = null
                this.subTaskPosition = pair.first
                this.subTask = pair.second
                this.setValues()
            } else {
                this.listener?.subTaskDetailBackButtonClicked(this)
            }*/
        }
    }

    private fun updateDisplayLanguage(){
        val activity = this.activity as? BaseActivity
        this.objectiveTextView?.text = activity?.getLocalizedStringById(R.string.task_objective)
        this.procedureDetailTextView?.text = activity?.getLocalizedStringById(R.string.task_procedures)
    }

    private fun setValues(){
        if(this.subTask != null){
            this.navBarView?.titleTextView?.text = this.subTask?.name
            this.objectiveDetailTextView?.text = this.subTask?.objective
            this.procedureDetailTextView?.text = this.subTask?.procedure
            /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                this.procedureDetailTextView?.text = Html.fromHtml(this.subTask?.procedure, Html.FROM_HTML_MODE_COMPACT)
            } else {
                this.procedureDetailTextView?.text = Html.fromHtml(this.subTask?.procedure)
            }*/

            when(this.subTask!!.uniqueId){
                EnumUtils.SubTaskUniqueId.SELF_RECOGNITION.uniqueId -> this.imageView?.setImageResource(R.drawable.subtask_selfrecognition)
                EnumUtils.SubTaskUniqueId.OBSERVATION.uniqueId -> this.imageView?.setImageResource(R.drawable.subtask_observation)
                EnumUtils.SubTaskUniqueId.BUILDING_BLOCKS.uniqueId-> this.imageView?.setImageResource(R.drawable.subtask_building)
                EnumUtils.SubTaskUniqueId.TYPING.uniqueId -> this.imageView?.setImageResource(R.drawable.subtask_typing)
                EnumUtils.SubTaskUniqueId.BALANCE.uniqueId -> this.imageView?.setImageResource(R.drawable.subtask_balance)
                EnumUtils.SubTaskUniqueId.MAGIC_CIRCLE.uniqueId -> this.imageView?.setImageResource(R.drawable.subtask_magic_circle)
                EnumUtils.SubTaskUniqueId.TANGRAM.uniqueId -> this.imageView?.setImageResource(R.drawable.subtask_tangram)
                EnumUtils.SubTaskUniqueId.CALCULATION.uniqueId -> this.imageView?.setImageResource(R.drawable.subtask_calculation)
                EnumUtils.SubTaskUniqueId.CUBE.uniqueId -> this.imageView?.setImageResource(R.drawable.subtask_cube)
                EnumUtils.SubTaskUniqueId.POWER.uniqueId -> this.imageView?.setImageResource(R.drawable.subtask_power)
                EnumUtils.SubTaskUniqueId.EXPRESS.uniqueId -> this.imageView?.setImageResource(R.drawable.subtask_express)
                else -> {}
            }

            (this.imageView?.layoutParams as? LinearLayout.LayoutParams)?.let{
                val layoutParams = it
                this.subTask?.uniqueId?.let {
                    if (it.equals(EnumUtils.SubTaskUniqueId.POWER.uniqueId) || it.equals(EnumUtils.SubTaskUniqueId.EXPRESS.uniqueId)) {
                        layoutParams.gravity = Gravity.CENTER_HORIZONTAL
                    } else {
                        layoutParams.gravity = Gravity.END
                    }
                }
                this.imageView?.layoutParams = layoutParams
            }

            this.startButton?.setOnClickListener {
                this@SubTaskDetailFragment.startButtonClicked()
            }
        } else if(this.task != null){
            this.navBarView?.titleTextView?.text = this.task?.name
            this.objectiveDetailTextView?.text = this.task?.objective
            this.procedureDetailTextView?.text = this.task?.procedure
            this.imageView?.setImageResource(R.drawable.subtask_fitness)

            this.startButton?.setOnClickListener{
                this@SubTaskDetailFragment.startButtonClicked()
            }
        }
    }

    private fun startButtonClicked(){
        if(this.task != null){
            val intent = Intent(this.activity, FitnessTaskDestinationSelectionActivity::class.java)
            intent.putExtra("taskPosition", this.taskPosition)
            this.activity?.startActivityForResult(intent, TabActivity.FITNESS_LOCATION_REQUEST_CODE)
        } else if(this.subTask != null) {
            val action = {
                when(this.subTask?.uniqueId){
                    EnumUtils.SubTaskUniqueId.SELF_RECOGNITION.uniqueId -> {
                        val intent = Intent(this.activity, SelfRecognitionActivity::class.java)
                        intent.putExtra("taskPosition", this.taskPosition!!)
                        intent.putExtra("subTaskPosition", this.subTaskPosition!!)
                        intent.putExtra("update", true)
                        this.activity?.startActivityForResult(intent, TabActivity.SELF_RECOGNITION_REQUEST_CODE)
                    }
                    EnumUtils.SubTaskUniqueId.OBSERVATION.uniqueId -> {
                        this@SubTaskDetailFragment.listener?.subTaskStartButtonClicked(this@SubTaskDetailFragment.taskPosition!!, this@SubTaskDetailFragment.subTaskPosition!!)
                    }
                    EnumUtils.SubTaskUniqueId.BUILDING_BLOCKS.uniqueId -> {
                        this@SubTaskDetailFragment.listener?.subTaskStartButtonClicked(this@SubTaskDetailFragment.taskPosition!!, this@SubTaskDetailFragment.subTaskPosition!!)
                    }
                    EnumUtils.SubTaskUniqueId.TYPING.uniqueId -> {
                        this@SubTaskDetailFragment.listener?.subTaskStartButtonClicked(this@SubTaskDetailFragment.taskPosition!!, this@SubTaskDetailFragment.subTaskPosition!!)
                    }
                    EnumUtils.SubTaskUniqueId.BALANCE.uniqueId -> {
                        this@SubTaskDetailFragment.listener?.subTaskStartButtonClicked(this@SubTaskDetailFragment.taskPosition!!, this@SubTaskDetailFragment.subTaskPosition!!)
                    }
                    EnumUtils.SubTaskUniqueId.MAGIC_CIRCLE.uniqueId -> {
                        this@SubTaskDetailFragment.listener?.subTaskStartButtonClicked(this@SubTaskDetailFragment.taskPosition!!, this@SubTaskDetailFragment.subTaskPosition!!)
                    }
                    EnumUtils.SubTaskUniqueId.TANGRAM.uniqueId -> {
                        this@SubTaskDetailFragment.listener?.subTaskStartButtonClicked(this@SubTaskDetailFragment.taskPosition!!, this@SubTaskDetailFragment.subTaskPosition!!)
                    }
                    EnumUtils.SubTaskUniqueId.CALCULATION.uniqueId -> {
                        this@SubTaskDetailFragment.listener?.subTaskStartButtonClicked(this@SubTaskDetailFragment.taskPosition!!, this@SubTaskDetailFragment.subTaskPosition!!)
                    }
                    EnumUtils.SubTaskUniqueId.CUBE.uniqueId -> {
                        this@SubTaskDetailFragment.listener?.subTaskStartButtonClicked(this@SubTaskDetailFragment.taskPosition!!, this@SubTaskDetailFragment.subTaskPosition!!)
                    }
                    EnumUtils.SubTaskUniqueId.POWER.uniqueId -> {
                        this@SubTaskDetailFragment.listener?.subTaskStartButtonClicked(this@SubTaskDetailFragment.taskPosition!!, this@SubTaskDetailFragment.subTaskPosition!!)
                    }
                    EnumUtils.SubTaskUniqueId.EXPRESS.uniqueId -> {
                        this@SubTaskDetailFragment.listener?.subTaskStartButtonClicked(this@SubTaskDetailFragment.taskPosition!!, this@SubTaskDetailFragment.subTaskPosition!!)
                    }
                    else -> {}
                }
            }

            if (DataStore.user?.taskRecords == null) {
                CallServer.get("campaign/" + DataStore.campaignId + "/taskrecords/", null, TaskRecords::class.java, { response ->
                    DataStore.user?.taskRecords = response
                    action()
                }, { sessionExpired, error ->
                    if (sessionExpired) {
                        (this.activity as? BaseActivity)?.logout(this.activity as BaseActivity, sessionExpired)
                    } else {
                        (this.activity as? BaseActivity)?.showErrorToast(this.activity as BaseActivity, error, EnumUtils.DataType.TASKRECORDS)
                    }
                })
            } else {
                action()
            }
        }
    }

    interface SubTaskDetailFragmentListener{
        fun subTaskDetailBackButtonClicked(fragment: Fragment)
        fun subTaskStartButtonClicked(taskPosition: Int, subTaskPosition: Int)
    }
}