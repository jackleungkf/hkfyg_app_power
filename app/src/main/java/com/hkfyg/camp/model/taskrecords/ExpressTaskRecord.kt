package com.hkfyg.camp.model.taskrecords

import com.google.gson.annotations.SerializedName
import com.hkfyg.camp.network.ListResponse

class ExpressTaskRecord{
    var id: Int? = null
    var records: ArrayList<ExpressQuestionRecord>? = null
    @SerializedName("created_ts")
    var createdTime: String? = null
    @SerializedName("end_ts")
    var endTime: String? = null

    class ExpressQuestion: TeamQuestion(){
    }

    class ExpressQuestionRecord{
        var id: Int? = null
        var question: Int? = null
        var mark: Double? = null
    }

    class ExpressQuestionList: ListResponse<ExpressQuestion>(){
    }
}