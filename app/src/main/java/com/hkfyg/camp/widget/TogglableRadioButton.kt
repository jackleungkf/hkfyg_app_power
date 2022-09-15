package com.hkfyg.camp.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.RadioButton
import android.widget.RadioGroup

class TogglableRadioButton: RadioButton {
    var togglable: Boolean = true

    constructor(context: Context): super(context)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    override fun toggle() {
        if(this.togglable) {
            if (this.isChecked) {
                (this.parent as? RadioGroup)?.let {
                    it.clearCheck()
                }
            } else {
                super.toggle()
            }
        }
    }
}