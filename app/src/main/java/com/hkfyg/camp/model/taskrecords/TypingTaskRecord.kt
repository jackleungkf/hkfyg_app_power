package com.hkfyg.camp.model.taskrecords

import com.google.gson.annotations.SerializedName
import com.hkfyg.camp.BaseActivity
import com.hkfyg.camp.R
import com.hkfyg.camp.model.local.TaskResult
import com.hkfyg.camp.network.ListResponse
import com.hkfyg.camp.utils.DataStore
import com.hkfyg.camp.utils.Utils

class TypingTaskRecord{
    var id: Int? = null
    var records: ArrayList<TypingRecord>? = null

    fun getTaskResultList(baseActivity: BaseActivity): ArrayList<TaskResult>{
        val list: ArrayList<TaskResult> = arrayListOf()
        this.records?.let{
            for(script in DataStore.typingScriptList.list){
                val record = this.records?.find{
                    when(it.script?.id){
                        null -> false
                        else -> it.script!!.id!! == script.id
                    }
                }

                var taskName = ""
                when(script.language){
                    "en" -> taskName = String.format(baseActivity.getLocalizedStringById(R.string.test_format), baseActivity.getLocalizedStringById(R.string.english_typing))
                    "zh" -> taskName = String.format(baseActivity.getLocalizedStringById(R.string.test_format), baseActivity.getLocalizedStringById(R.string.chinese_typing))
                }

                if(record?.createdTime != null && record.endTime != null && record.script != null){
                    val timeString = Utils.getDifferenceBetweenTwoTime(record.createdTime!!, record.endTime!!, "yyyy-MM-dd'T'HH:mm:ss'Z'")
                    list.add(TaskResult(taskName, R.string.time_used, timeString, R.string.correctly_typed_count, record.correctCount.toString()))
                } else {
                    list.add(TaskResult(taskName, R.string.time_used, baseActivity.getLocalizedStringById(R.string.time_out), R.string.correctly_typed_count, baseActivity.getLocalizedStringById(R.string.time_out)))
                }
            }
        }
        return list
    }

    class TypingScript{
        var id: Int? = null
        var language: String? = null
        var text: String? = null
        var order: Int? = null
    }

    class TypingRecord{
        var id: Int? = null
        var script: TypingScript? = null
        @SerializedName("correct_count")
        var correctCount: Int? = null
        @SerializedName("created_ts")
        var createdTime: String? = null
        @SerializedName("end_ts")
        var endTime: String? = null
    }

    class TypingScriptList: ListResponse<TypingScript>(){
    }
}