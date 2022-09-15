package com.hkfyg.camp.model.taskrecords

import com.google.gson.annotations.SerializedName
import com.hkfyg.camp.network.ListResponse

class PowerTaskRecord{
    var id: Int? = null
    var records: ArrayList<PowerQuestionRecord>? = null
    @SerializedName("created_ts")
    var createTime: String? = null
    @SerializedName("end_ts")
    var endTime: String? = null

    class PowerQuestion: TeamQuestion(){
    }

    class PowerQuestionRecord{
        var id: Int? = null
        var question: Int? = null
        var mark: Double? = null
    }

    class PowerQuestionList: ListResponse<PowerQuestion>(){
    }
}