package com.hkfyg.camp.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.hkfyg.camp.R

class TeamItem: LinearLayout{
    var imageButton: ImageButton? = null
    var teamTextView: TextView? = null
    var nameTextView: TextView? = null

    constructor(context: Context): super(context){
        this.initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs){
        this.initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr){
        this.initialize(context)
    }

    fun setTextColor(color: Int){
        this.teamTextView?.setTextColor(color)
        this.nameTextView?.setTextColor(color)
    }

    private fun initialize(context: Context){
        LayoutInflater.from(context).inflate(R.layout.item_team, this, true)
        this.imageButton = findViewById<ImageButton>(R.id.imageButton)
        this.teamTextView = findViewById<TextView>(R.id.teamTextView)
        this.nameTextView = findViewById<TextView>(R.id.nameTextView)
    }
}