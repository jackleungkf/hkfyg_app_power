package com.hkfyg.camp.model

import com.hkfyg.camp.model.local.SelectionItem
import com.hkfyg.camp.network.ListResponse

class School: SelectionItem {
    var id: Int? = null
    var name: String? = null

    override fun toString(): String {
        return "id: " + this.id + ", name: " + this.name
    }

    override fun getVariableByName(item: SelectionItem, name: String): Any? {
        try{
            return School::class.java.getDeclaredField(name).get(item)
        } catch (e: Exception){
            return null
        }
    }

    override fun getDisplayName(): String {
        return this.name.toString()
    }

    class SchoolList: ListResponse<School>(){
    }
}