package com.hkfyg.camp.model

import com.hkfyg.camp.network.ListResponse

class Beacon{
    var id: Int? = null
    var uuid: String? = null
    var name: String? = null

    class BeaconRecord{
        var id: Int? = null
        var campaign: Int? = null
        var user: Int? = null
        var beacon: Int? = null
    }

    class BeaconList: ListResponse<Beacon>(){
    }
}