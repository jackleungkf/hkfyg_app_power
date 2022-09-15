package com.hkfyg.camp.adapter

import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hkfyg.camp.R
import com.hkfyg.camp.widget.LoadingImageView

class MapViewRecyclerViewAdapter: RecyclerView.Adapter<MapViewRecyclerViewAdapter.ViewHolder>{
    var list: ArrayList<Int> = ArrayList<Int>()

    constructor(list: ArrayList<Int>): super(){
        this.list = list
    }

    class ViewHolder(val rootView: ViewGroup): RecyclerView.ViewHolder(rootView){
        var loadingImageView: LoadingImageView? = null

        init{
            this.loadingImageView = this.rootView.findViewById<LoadingImageView>(R.id.rootView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val rootView = LayoutInflater.from(parent.context).inflate(R.layout.cell_loading_image_view, parent, false) as ViewGroup
        return ViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int){
        //holder.loadingImageView?.loadImage(this.list[position])
        holder.loadingImageView?.loadImageRes(this.list[position])
    }

    override fun getItemCount(): Int {
        return this.list.size
    }
}