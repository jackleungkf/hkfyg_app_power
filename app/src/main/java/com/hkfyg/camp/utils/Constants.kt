package com.hkfyg.camp.utils

import android.content.Context
import android.content.res.Resources

object Constants{
    var screenWidth: Int = 0
    var screenHeight: Int = 0

    val clientId = "DyxXn0MEP4cW9R6wCEOgK0hy31bwuBfvZslG1sXU"

    // local
    //val domain = "192.168.208.173:8000"
    //val domain = "192.168.1.8:8000"

    // dev
    //val domain = "3.1.3.0"

    //prod
    val domain = "13.213.167.218"

    val host = "http://${this.domain}/api/"
    val webSocketUrl = "ws://${this.domain}/ws/team/"

    //limited trial project
    //val imagePredictionUrl = "https://southcentralus.api.cognitive.microsoft.com/customvision/v2.0/Prediction/3075f2d3-eaa7-4bdc-9b9f-fa717b4ec78f/image?iterationId=512094b4-a274-4095-a19b-43603be94a39"
    //val predictionKey = "7def5c3a2e6a43d5aef91cf130ec11bc"
    //val predictionContentType = "application/octet-stream"

    //azure resource
    var imagePredictionUrl = "https://japaneast.api.cognitive.microsoft.com/customvision/v3.0/Prediction/8c94aee6-b281-4654-852c-b0c4f7056cdc/classify/iterations/Iteration8/image"
    var predictionKey = "3b11678f748740e295aee95e6078a5d7"
    var predictionContentType = "application/octet-stream"

    //val estimoteAppId = "smallhubs-tech-limited-s-y-fh5"
    //val estimoteAppToken = "2867afe259ad9f1485fd0bba790418bc"

    private var versionName: String? = null
    private var screenSize: Pair<Int, Int>? = null

    fun getVersionName(context: Context): String?{
        if(this.versionName == null) {
            try {
                this.versionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName
                val env = when(this.domain){
                    "3.0.254.237" -> ""
                    "3.1.3.0" -> "dev"
                    else -> ""
                }
                this.versionName = "${this.versionName} ${env}"
            } catch (e: Exception) {
            }
        }
        return this.versionName
    }

    fun getScreenSize(): Pair<Int, Int>{
        if(this.screenSize == null){
            Resources.getSystem().displayMetrics.widthPixels
            val width = Resources.getSystem().getDisplayMetrics().widthPixels
            val height = Resources.getSystem().getDisplayMetrics().heightPixels
            this.screenSize = Pair<Int, Int>(width, height)
        }
        return this.screenSize!!
    }
}