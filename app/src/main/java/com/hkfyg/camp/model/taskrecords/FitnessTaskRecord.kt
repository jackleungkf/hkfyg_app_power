package com.hkfyg.camp.model.taskrecords

import com.google.gson.annotations.SerializedName
import com.hkfyg.camp.BaseActivity
import com.hkfyg.camp.R
import com.hkfyg.camp.model.Location
import com.hkfyg.camp.model.local.TaskResult
import com.hkfyg.camp.network.ListResponse
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.EnumUtils
import com.hkfyg.camp.utils.Utils

class FitnessTaskRecord{
    var id: Int? = null
    var location: Int? = null
    @SerializedName("location_obj")
    var locationObject: Location? = null
    @SerializedName("task_record_campaign")
    var taskRecordCampaign: Int? = null
    var user: Int? = null
    @SerializedName("created_ts")
    var createdTime: String? = null
    @SerializedName("arrival_ts")
    var arrivalTime: String? = null
    @SerializedName("sit_up_count")
    var sitUpCount: Int? = null
    @SerializedName("push_up_count")
    var pushUpCount: Int? = null
    @SerializedName("burpee_count")
    var burpeeCount: Int? = null
    var records: ArrayList<FitnessRecord>? = null

    fun taskCompleted(): Boolean{
        return this.arrivalTime != null && this.sitUpCount != null && this.pushUpCount != null && this.burpeeCount != null
    }

    /*fun getTaskResultList(baseActivity: BaseActivity): ArrayList<TaskResult>{
        val list: ArrayList<TaskResult> = arrayListOf()
        if(this.createdTime != null && this.arrivalTime != null){
            val timeString = Utils.getDifferenceBetweenTwoTime(this.createdTime!!, this.arrivalTime!!, "yyyy-MM-dd'T'HH:mm:ss'Z'")
            val sitUpString = when(this.sitUpCount){
                null -> "0"
                else -> this.sitUpCount.toString()
            }
            val pushUpString = when(this.pushUpCount){
                null -> "0"
                else -> this.pushUpCount.toString()
            }
            val burpeeString = when(this.burpeeCount){
                null -> "0"
                else -> this.burpeeCount.toString()
            }
            list.add(TaskResult(baseActivity.getLocalizedStringById(R.string.cardiorespiratory_test), R.string.time_used, timeString, R.drawable.task_result_running))
            list.add(TaskResult(baseActivity.getLocalizedStringById(R.string.sit_up_test), R.string.count, sitUpString, R.drawable.task_result_sit_up))
            list.add(TaskResult(baseActivity.getLocalizedStringById(R.string.push_up_test), R.string.count, pushUpString, R.drawable.task_result_push_up))
            list.add(TaskResult(baseActivity.getLocalizedStringById(R.string.burpee_test), R.string.count, burpeeString, R.drawable.task_result_burpee))
        }
        return list
    }*/

    fun getTaskResultList(baseActivity: BaseActivity): ArrayList<TaskResult>{
        val list: ArrayList<TaskResult> = arrayListOf()
        if(this.createdTime != null && this.records != null){
            for(i in 0 until DataStore.fitnessItemList.list.size){
                val item = DataStore.fitnessItemList.list.get(i)

                val record = this.records?.find{
                    when(it.item?.id){
                        null -> false
                        else -> it.item!!.id!! == item.id
                    }
                }

                var drawable = R.drawable.task_result_running
                var descriptionResId = R.string.count
                var valueString = ""

                when(item.type){
                    EnumUtils.SubTaskUniqueId.CARDIORESPIRATORY_TEST.uniqueId -> {
                        drawable = R.drawable.task_result_running
                        if(i == 0) {
                            descriptionResId = R.string.time_used
                            if (record != null && record.createdTime != null && record.endTime != null) {
                                valueString = Utils.getDifferenceBetweenTwoTime(record.createdTime!!, record.endTime!!, "yyyy-MM-dd'T'HH:mm:ss'Z'")
                            } else {
                                valueString = baseActivity.getLocalizedStringById(R.string.time_out)
                            }
                        } else {
                            descriptionResId = R.string.number_of_laps_title
                        }
                    }
                    EnumUtils.SubTaskUniqueId.SITUP_TEST.uniqueId -> {
                        drawable = R.drawable.task_result_sit_up
                    }
                    EnumUtils.SubTaskUniqueId.PUSHUP_TEST.uniqueId -> {
                        drawable = R.drawable.task_result_push_up
                    }
                    EnumUtils.SubTaskUniqueId.BURPEE_TEST.uniqueId -> {
                        drawable = R.drawable.task_result_burpee
                    } else -> {}
                }

                if(valueString.isEmpty()) {
                    valueString = when (record?.count) {
                        null -> "0"
                        else -> record.count.toString()
                    }
                }
                list.add(TaskResult(item.name.toString(), descriptionResId, valueString, drawable))
            }
        }
        return list
    }

    class FitnessItem{
        var id: Int? = null
        var name: String? = null
        var type: String? = null
        var description: String? = null
        @SerializedName("time_limit")
        var timeLimit: Int? = null
        var location: Int? = null
        var order: Int? = null
    }

    class FitnessRecord{
        var id: Int? = null
        @SerializedName("task_record")
        var taskRecord: Int? = null
        var item: FitnessItem? = null
        var count: Int? = null
        @SerializedName("created_ts")
        var createdTime: String? = null
        @SerializedName("end_ts")
        var endTime: String? = null
    }

    class FitnessItemList: ListResponse<FitnessItem>(){
    }
}