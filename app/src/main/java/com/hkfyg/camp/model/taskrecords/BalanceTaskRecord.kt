package com.hkfyg.camp.model.taskrecords

import com.google.gson.annotations.SerializedName
import com.hkfyg.camp.BaseActivity
import com.hkfyg.camp.R
import com.hkfyg.camp.model.local.TaskResult
import com.hkfyg.camp.network.ListResponse
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.Utils

class BalanceTaskRecord{
    var id: Int? = null
    @SerializedName("created_ts")
    var createdTime: String? = null
    @SerializedName("last_access_point")
    var lastAccessPoint: Int? = null
    var records: ArrayList<BalanceRecord>? = null

    fun getTaskResultList(baseActivity: BaseActivity): ArrayList<TaskResult> {
        val list: ArrayList<TaskResult> = arrayListOf()
        this.records?.let{
            for(item in DataStore.balanceItemList.list) {
                val record = this.records?.find{
                    when(it.item?.id){
                        null -> false
                        else -> it.item!!.id!!.equals(item.id)
                    }
                }
                if(record?.createdTime != null && record.endTime != null && record.item != null){
                    val timeString = Utils.getDifferenceBetweenTwoTime(record.createdTime!!, record.endTime!!, "yyyy-MM-dd'T'HH:mm:ss'Z'")
                    val drawableId = when (record.success) {
                        true -> R.drawable.subtask_balance_success
                        else -> R.drawable.subtask_balance_fail
                    }
                    list.add(TaskResult(record.item!!.name.toString(), R.string.time_used, timeString, drawableId))
                } else {
                    list.add(TaskResult(item.name.toString(), R.string.time_used, baseActivity.getLocalizedStringById(R.string.time_out), R.drawable.subtask_balance_fail))
                }
            }

            this.lastAccessPoint?.takeIf { it > 0 }?.let{
                list.add(TaskResult(R.string.last_record, it.toString()))
            }
        }
        return list
    }

    class BalanceRecord{
        var id: Int? = null
        var item: BalanceItem? = null
        var success: Boolean? = null
        @SerializedName("created_ts")
        var createdTime: String? = null
        @SerializedName("end_ts")
        var endTime: String? = null
    }

    class BalanceItem{
        var id: Int? = null
        var name: String? = null
    }

    class BalanceItemList: ListResponse<BalanceItem>(){
    }
}