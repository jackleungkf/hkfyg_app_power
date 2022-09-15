package com.hkfyg.camp.model.taskrecords

import com.google.gson.annotations.SerializedName
import com.hkfyg.camp.BaseActivity
import com.hkfyg.camp.R
import com.hkfyg.camp.model.local.TaskResult
import com.hkfyg.camp.network.ListResponse
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.Utils

class CubeTaskRecord{
    var id: Int? = null
    @SerializedName("created_ts")
    var createdTime: String? = null
    var records: ArrayList<CubeRecord>? = null

    fun getTaskResultList(baseActivity: BaseActivity): ArrayList<TaskResult>{
        val list: ArrayList<TaskResult> = arrayListOf()
        if(this.createdTime != null && this.records != null){
            for(item in DataStore.cubeCombinationList.list){
                val record = this.records?.find{
                    when(it.combination?.id){
                        null -> false
                        else -> it.combination!!.id!! == item.id
                    }
                }
                if(record?.createdTime != null && record.endTime != null && record.combination != null){
                    val timeString = Utils.getDifferenceBetweenTwoTime(record.createdTime!!, record.endTime!!, "yyyy-MM-dd'T'HH:mm:ss'Z'")
                    val drawable = when(record.success){
                        true -> R.drawable.subtask_cube_success
                        else -> R.drawable.subtask_cube_fail
                    }
                    list.add(TaskResult("${record.combination?.firstColor?.name} / ${record.combination?.secondColor?.name}", R.string.time_used, timeString, drawable))
                } else {
                    list.add(TaskResult("${item.firstColor?.name} / ${item.secondColor?.name}", R.string.time_used, baseActivity.getLocalizedStringById(R.string.time_out), R.drawable.subtask_cube_fail))
                }
            }
        }
        return list
    }

    class CubeColor{
        var id: Int? = null
        var name: String? = null
        var code: String? = null
    }

    class CubeCombination{
        var id: Int? = null
        @SerializedName("first_color")
        var firstColor: CubeColor? = null
        @SerializedName("second_color")
        var secondColor: CubeColor? = null
    }

    class CubeRecord{
        var id: Int? = null
        @SerializedName("task_record")
        var taskRecord: Int? = null
        var combination: CubeCombination? = null
        var success: Boolean? = null
        @SerializedName("created_ts")
        var createdTime: String? = null
        @SerializedName("end_ts")
        var endTime: String? = null
    }

    class CubeCombinationList: ListResponse<CubeCombination>(){
    }
}