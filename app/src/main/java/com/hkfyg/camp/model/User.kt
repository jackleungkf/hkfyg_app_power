package com.hkfyg.camp.model

import com.google.gson.annotations.SerializedName
import com.hkfyg.camp.network.ListResponse

class User{
    var id: Int? = null
    var username: String? = null
    var name: String? = null
    var school: School? = null
    var team: Team? = null
    var taskRecords: TaskRecords? = null
    @SerializedName("is_staff")
    var isStaff: Boolean? = null

    class UserList: ListResponse<User>(){
    }
}