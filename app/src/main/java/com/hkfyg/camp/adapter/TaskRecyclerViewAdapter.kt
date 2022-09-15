package com.hkfyg.camp.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import com.hkfyg.camp.BaseActivity
import com.hkfyg.camp.R
import com.hkfyg.camp.model.Task
import com.hkfyg.camp.utils.EnumUtils

class TaskRecyclerViewAdapter: RecyclerView.Adapter<TaskRecyclerViewAdapter.ViewHolder>{
    var context: Context? = null
    var baseActivity: BaseActivity? = null
    var list: ArrayList<Task> = ArrayList<Task>()
    var itemClickedListener: TaskRecyclerViewAdapterListener? = null

    constructor(context: Context, list: ArrayList<Task>, baseActivity: BaseActivity, itemClickedListener: TaskRecyclerViewAdapterListener?): super(){
        this.context = context
        this.baseActivity = baseActivity
        this.list = list
        this.itemClickedListener = itemClickedListener
    }

    class ViewHolder(val rootView: ViewGroup): RecyclerView.ViewHolder(rootView){
        var imageButton: ImageButton? = null

        init{
            this.imageButton = this.rootView.findViewById<ImageButton>(R.id.imageButton)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rootView = LayoutInflater.from(parent.context).inflate(R.layout.cell_task, parent, false) as ViewGroup
        return ViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = this.list[position]
        when(task.uniqueId){
            EnumUtils.TaskUniqueId.FITNESS.uniqueId -> holder.imageButton?.setImageResource(R.drawable.task_fitness)
            EnumUtils.TaskUniqueId.TECHNIQUES.uniqueId -> holder.imageButton?.setImageResource(R.drawable.task_techniques)
            EnumUtils.TaskUniqueId.BRAIN.uniqueId -> holder.imageButton?.setImageResource(R.drawable.task_brain)
            EnumUtils.TaskUniqueId.POWER.uniqueId -> holder.imageButton?.setImageResource(R.drawable.task_overcome)
            EnumUtils.TaskUniqueId.EXPRESS.uniqueId -> holder.imageButton?.setImageResource(R.drawable.task_express)
        }

        holder.imageButton?.setOnClickListener{
            Log.d("TaskRecyclerViewAdapter", "itemClicked: " + position)
            this.itemClickedListener?.taskItemClicked(position)
        }
    }

    override fun getItemCount(): Int {
        return this.list.size
    }

    interface TaskRecyclerViewAdapterListener{
        fun taskItemClicked(position: Int)
    }
}