package com.hkfyg.camp.task

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hkfyg.camp.R
import com.hkfyg.camp.adapter.SubTaskRecyclerViewAdapter
import com.hkfyg.camp.model.Location
import com.hkfyg.camp.model.Task
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.widget.NavBarView

class SubTaskFragment: Fragment(){
    private var rootView: ViewGroup? = null
    private var loadingView: ViewGroup? = null
    private var navBarView: NavBarView? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: Any? = null

    private var task: Task? = null
    private var subTask: Task.SubTask? = null

    var listener: SubTaskFragmentListener? = null

    companion object {
        fun newInstance(taskPosition: Int, subTaskPosition: Int?): SubTaskFragment{
            val fragment = SubTaskFragment()
            val bundle = Bundle()
            bundle.putInt("taskPosition", taskPosition)
            if(subTaskPosition != null) {
                bundle.putInt("subTaskPosition", subTaskPosition)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if(this.rootView == null){
            this.rootView = inflater.inflate(R.layout.fragment_subtask, container, false) as ViewGroup
            this.loadingView = this.rootView?.findViewById<ViewGroup>(R.id.loadingView)
            this.navBarView = this.rootView?.findViewById<NavBarView>(R.id.navBarView)
            this.recyclerView = this.rootView?.findViewById<RecyclerView>(R.id.recyclerView)

            this.navBarView?.titleTextView?.text = ""

            this.navBarView?.backButton?.setOnClickListener{
                this.listener?.subTaskBackButtonClicked(this@SubTaskFragment)
            }

            val taskPosition = this.arguments?.getInt("taskPosition", -1)
            val subTaskPosition = this.arguments?.getInt("subTaskPosition", -1)
            if(taskPosition != null && taskPosition >= 0 && taskPosition < DataStore.taskList.list.size && this.activity != null){
                this.task = DataStore.taskList.list[taskPosition]

                if(subTaskPosition != null && subTaskPosition >= 0) {
                    this.subTask = this.task?.subtasks?.get(subTaskPosition)
                    this.navBarView?.titleTextView?.text = this.subTask?.name

                    if(this.subTask!!.locations != null) {
                        this.adapter = SubTaskRecyclerViewAdapter<Location>(this.activity!!, taskPosition, subTaskPosition, this.subTask!!.locations!!, this.listener)
                        this.recyclerView?.layoutManager = LinearLayoutManager(this.activity!!)
                        this.recyclerView?.adapter = this.adapter as SubTaskRecyclerViewAdapter<Location>
                    }
                } else {
                    this.navBarView?.titleTextView?.text = this.task!!.name

                    if(this.task!!.subtasks != null) {
                        this.adapter = SubTaskRecyclerViewAdapter<Task.SubTask>(this.activity!!, taskPosition, null, this.task!!.subtasks!!, this.listener)
                        this.recyclerView?.layoutManager = LinearLayoutManager(this.activity!!)
                        this.recyclerView?.adapter = this.adapter as SubTaskRecyclerViewAdapter<Task.SubTask>
                    }
                }
            }
        }
        return this.rootView
    }

    fun showLoadingView(value: Boolean){
        if(value) {
            this.loadingView?.visibility = View.VISIBLE
        } else {
            this.loadingView?.visibility = View.GONE
        }
    }

    interface SubTaskFragmentListener{
        fun subTaskBackButtonClicked(fragment: SubTaskFragment)
        fun subTaskItemClicked(taskPosition: Int, subTaskPosition: Int, locationPosition: Int?)
    }
}