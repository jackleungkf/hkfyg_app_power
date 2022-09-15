package com.hkfyg.camp.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.hkfyg.camp.R

class CubeItemView: RelativeLayout {
    var imageView: ImageView? = null
    var textView: TextView? = null

    constructor(context: Context): super(context){
        this.initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs){
        this.initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet, defaultAttrStyle: Int): super(context, attrs, defaultAttrStyle){
        this.initialize(context)
    }

    private fun initialize(context: Context){
        LayoutInflater.from(context).inflate(R.layout.view_cube_item, this, true)
        this.imageView = findViewById<ImageView>(R.id.imageView)
        this.textView = findViewById<TextView>(R.id.textView)
    }
}