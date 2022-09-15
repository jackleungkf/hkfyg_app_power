package com.hkfyg.camp.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.hkfyg.camp.R
import java.util.*

class NavBarView: RelativeLayout{
    var backButton: ImageButton? = null
    var titleTextView: TextView? = null
    var rightButton: ImageButton? = null

    constructor(context: Context): super(context){
        this.initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs){
        this.initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet, defaultStyleAttr: Int): super(context, attrs, defaultStyleAttr){
        this.initialize(context)
    }

    private fun initialize(context: Context){
        LayoutInflater.from(context).inflate(R.layout.view_nav_bar, this, true)
        this.titleTextView = findViewById<TextView>(R.id.titleTextView)
        this.backButton = findViewById<ImageButton>(R.id.backButton)
        this.rightButton = findViewById<ImageButton>(R.id.rightButton)
    }
}