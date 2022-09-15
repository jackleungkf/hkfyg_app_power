package com.hkfyg.camp.model.local

class Gender: SelectionItem {
    var value: String? = null
    var name: String? = null

    constructor(value: String, name: String){
        this.value = value
        this.name = name
    }

    override fun getVariableByName(item: SelectionItem, name: String): Any? {
        try{
            return Gender::class.java.getDeclaredField(name).get(item)
        } catch (e: Exception){
            return null
        }
    }

    override fun getDisplayName(): String {
        return this.name.toString()
    }
}