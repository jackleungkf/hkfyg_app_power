package com.hkfyg.camp.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.hkfyg.camp.BaseActivity
import com.hkfyg.camp.R
import com.hkfyg.camp.model.local.TaskResult

class TaskResultRecyclerViewAdapter: RecyclerView.Adapter<TaskResultRecyclerViewAdapter.ViewHolder>{
    private var baseActivity: BaseActivity? = null
    private var list: ArrayList<TaskResult> = arrayListOf()

    constructor(baseActivity: BaseActivity, list: ArrayList<TaskResult>): super(){
        this.baseActivity = baseActivity
        this.list = list
    }

    open class ViewHolder(val rootView: ViewGroup): RecyclerView.ViewHolder(rootView){
        var taskNameTextView: TextView? = null
        var timeTextView: TextView? = null
        var timeValueTextView: TextView? = null

        init{
            this.taskNameTextView = this.rootView.findViewById<TextView>(R.id.taskNameTextView)
            this.timeTextView = this.rootView.findViewById<TextView>(R.id.timeTextView)
            this.timeValueTextView = this.rootView.findViewById<TextView>(R.id.timeValueTextView)
        }
    }

    class ImageViewHolder(rootView: ViewGroup): ViewHolder(rootView){
        var imageView: ImageView? = null

        init{
            this.imageView = this.rootView.findViewById<ImageView>(R.id.imageView)
        }
    }

    class NoImageViewHolder(rootView: ViewGroup): ViewHolder(rootView){
        var titleTextView: TextView? = null
        var valueTextView: TextView? = null

        init{
            this.titleTextView = this.rootView.findViewById(R.id.titleTextView)
            this.valueTextView = this.rootView.findViewById(R.id.valueTextView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskResultRecyclerViewAdapter.ViewHolder {
        if(viewType == 0){
            val rootView = LayoutInflater.from(parent.context).inflate(R.layout.cell_task_result, parent, false) as ViewGroup
            return TaskResultRecyclerViewAdapter.ImageViewHolder(rootView)
        } else {
            val rootView = LayoutInflater.from(parent.context).inflate(R.layout.cell_task_result_score, parent, false) as ViewGroup
            return TaskResultRecyclerViewAdapter.NoImageViewHolder(rootView)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = this.list[position]

        holder.taskNameTextView?.text = result.taskName
        holder.timeTextView?.text = baseActivity?.getLocalizedStringById(result.descriptionStringResId)
        holder.timeValueTextView?.text = result.valueString

        when(holder.itemViewType){
            0 -> {
                result.imageResId?.let {
                    (holder as? ImageViewHolder)?.imageView?.setImageResource(it)
                }
            }
            1 -> {
                val viewHolder = holder as? NoImageViewHolder
                viewHolder?.titleTextView?.text = baseActivity?.getLocalizedStringById(result.descriptionStringResId)
                viewHolder?.valueTextView?.text = result.valueString
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val result = this.list[position]
        if(result.imageResId != null){
            return 0
        } else {
            return 1
        }
    }

    override fun getItemCount(): Int {
        return this.list.size
    }
}