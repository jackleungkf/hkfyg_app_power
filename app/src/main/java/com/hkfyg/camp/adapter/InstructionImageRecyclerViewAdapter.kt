package com.hkfyg.camp.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.hkfyg.camp.BaseActivity
import com.hkfyg.camp.R
import com.hkfyg.camp.model.InstructionImage
import com.hkfyg.camp.utils.EnumUtils
import com.hkfyg.camp.widget.LoadingImageView

class InstructionImageRecyclerViewAdapter: RecyclerView.Adapter<InstructionImageRecyclerViewAdapter.ViewHolder>{
    private var baseActivity: BaseActivity
    private var locationName: String? = null
    private var list: ArrayList<InstructionImage> = ArrayList<InstructionImage>()

    var qrCodeLocationImageCount = 0
    var mapCount = 0
    var instructionImageCount = 0

    constructor(baseActivity: BaseActivity, locationName: String, list: ArrayList<InstructionImage>): super(){
        this.baseActivity = baseActivity
        this.locationName = locationName
        this.setList(list)
    }

    fun setList(list: ArrayList<InstructionImage>){
        this.list = list

        qrCodeLocationImageCount = 0
        mapCount = 0
        instructionImageCount = 0

        for(image in list){
            when(image.imageType) {
                EnumUtils.InstructionImageType.QR_CODE_LOCATION.ordinal -> {
                    this.qrCodeLocationImageCount += 1
                }
                EnumUtils.InstructionImageType.MAP.ordinal -> {
                    this.mapCount += 1
                } else -> {
                this.instructionImageCount += 1
            }
            }
        }
    }

    class ViewHolder(val rootView: ViewGroup): RecyclerView.ViewHolder(rootView){
        var textView: TextView? = null
        var loadingImageView: LoadingImageView? = null

        init{
            this.textView = this.rootView.findViewById<TextView>(R.id.textView)
            this.loadingImageView = this.rootView.findViewById<LoadingImageView>(R.id.loadingImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rootView = LayoutInflater.from(parent.context).inflate(R.layout.cell_instruction_image, parent, false) as ViewGroup
        return ViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val image = this.list.get(position)
        when(image.imageType){
            EnumUtils.InstructionImageType.QR_CODE_LOCATION.ordinal -> {
                holder.textView?.text = this.baseActivity.getLocalizedStringById(R.string.qr_code_location_description)
            }
            EnumUtils.InstructionImageType.MAP.ordinal -> {
                holder.textView?.text = String.format(this.baseActivity.getLocalizedStringById(R.string.map_description), this.locationName)
            } else -> {
                holder.textView?.text = String.format(this.baseActivity.getLocalizedStringById(R.string.instruction_image), position + 1 - qrCodeLocationImageCount - mapCount)
            }
        }

        if(image.thumbnail != null) {
            holder.loadingImageView?.loadImage(image.thumbnail!!)
        } else {
            holder.loadingImageView?.imageView?.setImageBitmap(null)
        }
    }

    override fun getItemCount(): Int {
        return this.list.size
    }
}