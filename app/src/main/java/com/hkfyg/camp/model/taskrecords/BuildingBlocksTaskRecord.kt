package com.hkfyg.camp.model.taskrecords

import com.google.gson.annotations.SerializedName
import com.hkfyg.camp.network.ListResponse

class BuildingBlocksTaskRecord{
    var id: Int? = null
    @SerializedName("used_item_number")
    var usedItemNumber: Int? = null
    @SerializedName("created_ts")
    var createdTime: String? = null
    @SerializedName("end_ts")
    var endTime: String? = null
    var records: ArrayList<BuildingBlocksImageRecord>? = null

    class BuildingBlocksImage{
        var id: Int? = null
        @SerializedName("unique_id")
        var uniqueId: String? = null
        var file: String? = null
        var thumbnail: String? = null
        var order: Int? = null
    }

    class BuildingBlocksImageRecord{
        var id: Int? = null
        var image: BuildingBlocksImage? = null
        var similarity: Double? = null
        @SerializedName("created_ts")
        var createdTime: String? = null
    }

    class BuildingBlocksImageList: ListResponse<BuildingBlocksImage>(){
    }
}