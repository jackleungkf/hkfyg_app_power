package com.hkfyg.camp.model

import com.hkfyg.camp.network.ListResponse

class Configuration{
    var id: Int? = null
    var name: String? = null
    var key: String? = null
    var value: String? = null

    class ConfigurationList: ListResponse<Configuration>(){
    }
}