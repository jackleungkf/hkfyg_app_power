package com.hkfyg.camp.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.hkfyg.camp.R
import com.hkfyg.camp.model.local.SelectionItem
import com.hkfyg.camp.widget.TogglableRadioButton

class RadioItemSelectionRecyclerViewAdapter<T: SelectionItem.RadioItem>: RecyclerView.Adapter<RadioItemSelectionRecyclerViewAdapter.ViewHolder>{
    private var context: Context? = null
    private var list: ArrayList<T> = ArrayList<T>()
    private var listener: RadioItemSelectionRecyclerAdapterListener? = null

    private var radioButtonsEnabled: Boolean = true

    constructor(context: Context, list: ArrayList<T>, listener: RadioItemSelectionRecyclerAdapterListener?){
        this.context = context
        this.list = list
        this.listener = listener
    }

    class ViewHolder(val rootView: ViewGroup): RecyclerView.ViewHolder(rootView){
        var radioButton: TogglableRadioButton? = null
        var textView: TextView? = null

        init{
            this.radioButton = this.rootView.findViewById<TogglableRadioButton>(R.id.radioButton)
            this.textView = this.rootView.findViewById<TextView>(R.id.textView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rootView = LayoutInflater.from(parent.context).inflate(R.layout.cell_radio_item, parent, false) as ViewGroup
        return ViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = this.list[position]
        holder.textView?.text = item.getDisplayName()
        //holder.radioButton?.togglable = true
        holder.radioButton?.isChecked = item.isChecked()
        holder.radioButton?.togglable = this.radioButtonsEnabled
        Log.d("radioItemAdapter", "position: ${position}, checked: ${item.isChecked()}")
        holder.radioButton?.setOnClickListener{
            this@RadioItemSelectionRecyclerViewAdapter.listener?.radioButtonClicked(position, holder.radioButton!!.isChecked())
        }
    }

    override fun getItemCount(): Int{
        return this.list.size
    }

    fun setRadioButtonsEnabled(value: Boolean){
        this.radioButtonsEnabled = value
        this.notifyDataSetChanged()
    }

    interface RadioItemSelectionRecyclerAdapterListener{
        fun radioButtonClicked(position: Int, checked: Boolean)
    }
}