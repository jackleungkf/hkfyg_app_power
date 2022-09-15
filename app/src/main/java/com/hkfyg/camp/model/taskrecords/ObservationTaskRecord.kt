package com.hkfyg.camp.model.taskrecords

import com.google.gson.annotations.SerializedName
import com.hkfyg.camp.model.Location
import com.hkfyg.camp.network.ListResponse

class ObservationTaskRecord{
    var id: Int? = null
    var location: Location? = null
    @SerializedName("created_ts")
    var createdTime: String? = null
    @SerializedName("end_ts")
    var endTime: String? = null
    var records: ArrayList<ObservationRecord>? = null

    class ObservationRecord{
        var id: Int? = null
        var image: ObservationImage? = null
        @SerializedName("created_ts")
        var createdTime: String? = null
    }

    class ObservationImage{
        var id: Int? = null
        @SerializedName("unique_id")
        var uniqueId: String? = null
        var location: Int? = null
        var file: String? = null
        var thumbnail: String? = null
        var order: Int? = null
    }

    class ObservationImageList: ListResponse<ObservationImage>(){
    }
}