package com.hkfyg.camp.task

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import com.hkfyg.camp.BaseActivity
import com.hkfyg.camp.R
import com.hkfyg.camp.adapter.TaskRecyclerViewAdapter
import com.hkfyg.camp.utils.Constants
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.EnumUtils

class TaskFragment: Fragment(){
    private var rootView: ViewGroup? = null
    private var versionNameTextView: TextView? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: TaskRecyclerViewAdapter? = null
    var itemClickedListener: TaskRecyclerViewAdapter.TaskRecyclerViewAdapterListener? = null

    companion object {
        fun newInstance(): TaskFragment{
            return TaskFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if(this.rootView == null) {
            this.rootView = inflater.inflate(R.layout.fragment_task, container, false) as? ViewGroup
            this.versionNameTextView = this.rootView?.findViewById<TextView>(R.id.versionNameTextView)
            this.recyclerView = this.rootView?.findViewById<RecyclerView>(R.id.recyclerView)
            this.swipeRefreshLayout = this.rootView?.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)

            this.context?.let {
                Constants.getVersionName(it)?.let {
                    this.versionNameTextView?.text = ("v${it}")
                }
            }

            this.swipeRefreshLayout?.setOnRefreshListener {
                val activity = activity as? BaseActivity
                if (activity != null && activity.loading) {
                    this.swipeRefreshLayout?.isRefreshing = false
                    return@setOnRefreshListener
                }

                DataStore.user?.taskRecords = null
                this.getTaskList()
            }

            this.getTaskList()
        }

        return this.rootView
    }

    private fun getTaskList(){
        val activity = this.activity as? BaseActivity
        activity?.showLoadingView(this.rootView)
        when(this.swipeRefreshLayout?.isRefreshing){
            false -> this.swipeRefreshLayout?.isEnabled = false
            else -> {}
        }

        DataStore.getTaskList({ results ->
            activity?.hideLoadingView(this.rootView)
            this.swipeRefreshLayout?.isRefreshing = false
            this.swipeRefreshLayout?.isEnabled = true

            if(activity != null) {
                this.adapter = TaskRecyclerViewAdapter(activity, results, activity, this.itemClickedListener)
                this.recyclerView?.adapter = this.adapter
                this.recyclerView?.layoutManager = GridLayoutManager(activity, 2)
            }
        }, { sessionExpired, error ->
            activity?.hideLoadingView(this.rootView)
            this.swipeRefreshLayout?.isRefreshing = false
            this.swipeRefreshLayout?.isEnabled = true

            if(sessionExpired){
                activity?.logout(activity, sessionExpired)
            } else {
                activity?.showErrorToast(activity, error, EnumUtils.DataType.TASK)
            }
        })
    }
}

