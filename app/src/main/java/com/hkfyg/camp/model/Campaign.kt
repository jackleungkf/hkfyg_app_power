package com.hkfyg.camp.model

import com.google.gson.annotations.SerializedName

class Campaign{
    var id: Int? = null
    @SerializedName("unique_id")
    var uniqueId: String? = null
    @SerializedName("start_ts")
    var startTimeString: String? = null
    @SerializedName("end_ts")
    var endTimeString: String? = null
    var activated: Boolean? = false
    var tasks: ArrayList<Task>? = null
}