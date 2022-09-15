package com.hkfyg.camp.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater

import android.view.ViewGroup
import android.widget.TextView
import com.hkfyg.camp.R
import com.hkfyg.camp.model.local.SelectionItem
import com.hkfyg.camp.task.SubTaskFragment

class SubTaskRecyclerViewAdapter<T: SelectionItem>: RecyclerView.Adapter<SubTaskRecyclerViewAdapter.ViewHolder>{
    private var context: Context? = null
    private var taskPosition: Int = -1
    private var subTaskPosition: Int? = null
    private var list: ArrayList<T> = ArrayList<T>()
    private var listener: SubTaskFragment.SubTaskFragmentListener? = null
    private var subTaskRecyclerViewAdapterListener: SubTaskRecyclerViewAdapterListener? = null

    private var selectedPosition: Int? = null

    constructor(context: Context, taskPosition: Int, subTaskPosition: Int?, list: ArrayList<T>, listener: SubTaskFragment.SubTaskFragmentListener?){
        this.context = context
        this.taskPosition = taskPosition
        this.subTaskPosition = subTaskPosition
        this.list = list
        this.listener = listener
    }

    constructor(context: Context, taskPosition: Int, list: ArrayList<T>, subTaskRecyclerViewAdapterListener: SubTaskRecyclerViewAdapterListener?){
        this.context = context
        this.taskPosition = taskPosition
        this.list = list
        this.subTaskRecyclerViewAdapterListener = subTaskRecyclerViewAdapterListener
    }

    class ViewHolder(val rootView: ViewGroup): RecyclerView.ViewHolder(rootView){
        var titleTextView: TextView? = null

        init{
            this.titleTextView = this.rootView.findViewById<TextView>(R.id.titleTextView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rootView = LayoutInflater.from(parent.context).inflate(R.layout.cell_subtask, parent, false) as ViewGroup
        return ViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(position == this.selectedPosition){
            holder.titleTextView?.background = this.context?.resources?.getDrawable(R.drawable.background_accent_fill)
        } else {
            holder.titleTextView?.background = this.context?.resources?.getDrawable(R.drawable.background_green_fill)
        }

        val item = this.list[position]
        holder.titleTextView?.text = item.getDisplayName()

        holder.rootView.setOnClickListener{
            if(this.subTaskPosition == null) {
                if(position == this.selectedPosition) {
                    if(this.listener != null) {
                        this.listener?.subTaskItemClicked(this.taskPosition, position, null)
                    } else {
                        this.subTaskRecyclerViewAdapterListener?.subTaskItemClicked(position)
                    }
                } else {
                    this.setSelectedPosition(position)
                }
            } else {
                if(position == this.selectedPosition) {
                    this.listener?.subTaskItemClicked(this.taskPosition, this.subTaskPosition!!, position)
                } else {
                    this.setSelectedPosition(position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return this.list.size
    }

    private fun setSelectedPosition(position: Int){
        val previousSelectedPosition = this.selectedPosition
        this.selectedPosition = position
        if(previousSelectedPosition != null){
            this.notifyItemChanged(previousSelectedPosition)
        }
        this.notifyItemChanged(position)
    }

    interface SubTaskRecyclerViewAdapterListener{
        fun subTaskItemClicked(position: Int)
    }
}