package com.hkfyg.camp.network

open class ListResponse<T>{
    var count: Int? = null
    var next: String? = null
    var previous: String? = null
    var results: ArrayList<T> = ArrayList<T>()
}