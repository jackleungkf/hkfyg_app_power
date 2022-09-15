package com.hkfyg.camp.model.taskrecords

import com.google.gson.annotations.SerializedName
import com.hkfyg.camp.model.local.SelectionItem
import com.hkfyg.camp.network.ListResponse

class MagicCircleTaskRecord{
    var id: Int? = null
    @SerializedName("created_ts")
    var createdTime: String? = null
    @SerializedName("end_ts")
    var endTime: String? = null
    var records: ArrayList<MagicCircleRecord>? = null

    class MagicCircleItem{
        var id: Int? = null
        var name: String? = null
    }

    class MagicCircleRecord: SelectionItem.RadioItem{
        var id: Int? = null
        var item: MagicCircleItem? = null
        @SerializedName("split_success")
        var splitSuccess: Boolean? = null
        @SerializedName("restore_success")
        var restoreSuccess: Boolean? = null

        constructor()

        constructor(item: MagicCircleItem){
            this.item = item
        }

        override fun getVariableByName(item: SelectionItem, name: String): Any? {
            try{
                return MagicCircleRecord::class.java.getDeclaredField(name).get(item)
            } catch (e: Exception){
                return null
            }
        }

        override fun getDisplayName(): String {
            this.item?.name?.let{
                return it
            }
            return ""
        }

        override fun isChecked(): Boolean{
            if(this.splitSuccess != null && this.restoreSuccess != null) {
                return (this.splitSuccess!! && this.restoreSuccess!!)
            }
            return false
        }
    }

    class MagicCircleItemList: ListResponse<MagicCircleItem>(){
    }
}