package com.hkfyg.camp.model.local

interface SelectionItem{
    fun getVariableByName(item: SelectionItem, name: String): Any?
    fun getDisplayName(): String

    interface RadioItem: SelectionItem{
        fun isChecked(): Boolean
    }
}