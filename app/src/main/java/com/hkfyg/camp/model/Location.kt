package com.hkfyg.camp.model

import com.google.gson.annotations.SerializedName
import com.hkfyg.camp.model.local.SelectionItem

class Location: SelectionItem {
    var id: Int? = null
    @SerializedName("unique_id")
    var uniqueId: String? = null
    var name: String? = null
    @SerializedName("map_image")
    var mapImage: String? = null
    @SerializedName("map_thumbnail")
    var mapImageThumbnail: String? = null
    @SerializedName("qr_code_image")
    var qrCodeImage: String? = null
    @SerializedName("qr_code_thumbnail")
    var qrCodeImageThumbnail: String? = null

    override fun getVariableByName(item: SelectionItem, name: String): Any? {
        try{
            return Location::class.java.getDeclaredField(name).get(item)
        } catch (e: Exception){
            return null
        }
    }

    override fun getDisplayName(): String {
        return this.name.toString()
    }
}