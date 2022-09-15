package com.hkfyg.camp.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.hkfyg.camp.R

class InputView: LinearLayout {
    var textView: TextView? = null
    var editText: EditText? = null
    var button: Button? = null

    constructor(context: Context): super(context){
        this.initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs){
        this.initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr){
        this.initialize(context)
    }

    fun setButtonInputView(){
        this.editText?.visibility = View.GONE
        this.button?.visibility = View.VISIBLE
    }

    private fun initialize(context: Context){
        LayoutInflater.from(context).inflate(R.layout.view_input, this, true)
        this.textView = findViewById<TextView>(R.id.textView)
        this.editText = findViewById<EditText>(R.id.editText)
        this.button = findViewById<Button>(R.id.button)
    }
}