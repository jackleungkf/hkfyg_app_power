package com.hkfyg.camp.model

import com.google.gson.annotations.SerializedName

class AccessToken{
    @SerializedName("access_token")
    var accessToken: String? = null
    @SerializedName("expires_in")
    var expiresIn: Int? = null
    @SerializedName("token_type")
    var tokenType: String? = null
    var scope: String? = null
    @SerializedName("refresh_token")
    var refreshToken: String? = null
}