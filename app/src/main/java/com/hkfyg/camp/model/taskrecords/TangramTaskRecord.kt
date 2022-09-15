package com.hkfyg.camp.model.taskrecords

import com.google.gson.annotations.SerializedName
import com.hkfyg.camp.BaseActivity
import com.hkfyg.camp.R
import com.hkfyg.camp.model.local.TaskResult
import com.hkfyg.camp.network.ListResponse
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.Utils

class TangramTaskRecord {
    var id: Int? = null
    @SerializedName("created_ts")
    var createdTime: String? = null
    var records: ArrayList<TangramRecord>? = null

    fun getTaskResultList(baseActivity: BaseActivity): ArrayList<TaskResult> {
        val list: ArrayList<TaskResult> = arrayListOf()
        if (this.createdTime != null && this.records != null){
            for(item in DataStore.tangramItemList.list) {
                val record = this.records?.find{
                    when(it.item?.id){
                        null -> false
                        else -> it.item!!.id!!.equals(item.id)
                    }
                }
                if(record != null && record.createdTime != null && record.endTime != null && record.item != null){
                    val timeString = Utils.getDifferenceBetweenTwoTime(record.createdTime!!, record.endTime!!, "yyyy-MM-dd'T'HH:mm:ss'Z'")
                    val drawableId = when (record.success) {
                        true -> R.drawable.subtask_tangram_success
                        else -> R.drawable.subtask_tangram_fail
                    }
                    list.add(TaskResult(record.item!!.name.toString(), R.string.time_used, timeString, drawableId))
                } else {
                    list.add(TaskResult(item.name.toString(), R.string.time_used, baseActivity.getLocalizedStringById(R.string.time_out), R.drawable.subtask_tangram_fail))
                }
            }
        }
        return list
    }

    class TangramRecord{
        var id: Int? = null
        var item: TangramItem? = null
        var success: Boolean? = null
        @SerializedName("created_ts")
        var createdTime: String? = null
        @SerializedName("end_ts")
        var endTime: String? = null
    }

    class TangramItem{
        var id: Int? = null
        var name: String? = null
    }

    class TangramItemList: ListResponse<TangramItem>(){
    }
}