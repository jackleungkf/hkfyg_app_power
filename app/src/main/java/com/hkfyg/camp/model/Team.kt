package com.hkfyg.camp.model

import com.hkfyg.camp.network.ListResponse

class Team{
    var id: Int? = null
    var name: String? = null
    var campaign: Int? = null

    class TeamList: ListResponse<Team>(){
    }
}