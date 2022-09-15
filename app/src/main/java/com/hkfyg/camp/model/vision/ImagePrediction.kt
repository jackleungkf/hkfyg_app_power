package com.hkfyg.camp.model.vision

import com.google.gson.annotations.SerializedName

class ImagePrediction{
    var id: String? = null
    var project: String? = null
    var iteration: String? = null
    var created: String? = null
    var predictions: ArrayList<Prediction>? = null

    class Prediction{
        var probability: Double? = null
        @SerializedName("tagId")
        var tagId: String? = null
        var tagName: String? = null
    }
}