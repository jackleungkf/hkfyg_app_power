package com.hkfyg.camp.model.taskrecords

import com.google.gson.annotations.SerializedName

abstract class TeamQuestion{
    var id: Int? = null
    var question: String? = null
    var factor: Double? = null

    var mark: Double? = null
}