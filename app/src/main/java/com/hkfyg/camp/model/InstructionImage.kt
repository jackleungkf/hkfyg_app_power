package com.hkfyg.camp.model

import com.google.gson.annotations.SerializedName
import com.hkfyg.camp.network.ListResponse

class InstructionImage{
    var id: Int? = null
    @SerializedName("image_type")
    var imageType: Int? = null
    @SerializedName("task_type")
    var taskType: Int? = null
    var file: String? = null
    var thumbnail: String? = null
    var order: Int? = null

    class InstructionImageList: ListResponse<InstructionImage>(){
    }
}