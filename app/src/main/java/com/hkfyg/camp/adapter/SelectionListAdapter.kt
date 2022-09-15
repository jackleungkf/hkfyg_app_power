package com.hkfyg.camp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.hkfyg.camp.model.local.SelectionItem

class SelectionListAdapter<T: SelectionItem>: ArrayAdapter<T> {
    var resources: Int = 0
    var cellId: Int = 0
    var list: ArrayList<T> = ArrayList<T>()
    var reuseView: View? = null

    constructor(context: Context, resources: Int, cellId: Int, list: ArrayList<T>): super(context, resources, list){
        this.resources = resources
        this.cellId = cellId
        this.list = list
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View?{
        if(convertView == null){
            this.reuseView = (this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(this.resources, null)
        } else {
            reuseView = convertView
        }

        val textView = this.reuseView?.findViewById<TextView>(cellId)
        textView?.text = this.list[position].getDisplayName()

        return this.reuseView
    }

    override fun getCount(): Int {
        return this.list.size
    }
}