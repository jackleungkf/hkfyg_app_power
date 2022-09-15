package com.hkfyg.camp.model

import com.google.gson.annotations.SerializedName
import com.hkfyg.camp.model.local.SelectionItem

class Task{
    var id: Int? = null
    @SerializedName("unique_id")
    var uniqueId: String? = null
    var name: String? = null
    var group: Int? = null
    var objective: String? = null
    var procedure: String? = null
    var subtasks: ArrayList<SubTask>? = null
    var locations: ArrayList<Location>? = null

    class SubTask: SelectionItem {
        var id: Int? = null
        @SerializedName("unique_id")
        var uniqueId: String? = null
        var name: String? = null
        var task: Int? = null
        var objective: String? = null
        var procedure: String? = null
        @SerializedName("time_limit")
        var timeLimit: Int? = null
        var locations: ArrayList<Location>? = null
        var order: Int? = null

        override fun getVariableByName(item: SelectionItem, name: String): Any? {
            try{
                return SubTask::class.java.getDeclaredField(name).get(item)
            } catch (e: Exception){
                return null
            }
        }

        override fun getDisplayName(): String {
            return this.name.toString()
        }
    }
}