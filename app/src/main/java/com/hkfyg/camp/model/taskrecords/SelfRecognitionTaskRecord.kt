package com.hkfyg.camp.model.taskrecords

import com.google.gson.annotations.SerializedName

class SelfRecognitionTaskRecord{
    var id: Int? = null
    @SerializedName("body_fat_ratio")
    var bodyFatRatio: Double? = null
    @SerializedName("orgin_fat")
    var orginFat: Double? = null
    var muscle: Double? = null
    @SerializedName("metabolism_rate")
    var metabolism_rate: Double? = null
    @SerializedName("body_age")
    var bodyAge: Double? = null
    @SerializedName("body_shape")
    var bodyShape: String? = null
    var bmi: Double? = null
    var gender: String? = null
}