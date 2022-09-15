package com.hkfyg.camp.utils

import android.text.format.DateUtils
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object Utils{
    fun getDifferenceBetweenTwoTime(startTimeString: String, endTimeString: String, format: String): String{
        try {
            val simpleDateFormat = SimpleDateFormat(format)
            val startTime = simpleDateFormat.parse(startTimeString)
            val endTime = simpleDateFormat.parse(endTimeString)

            val difference = (endTime.time - startTime.time) / 1000
            //val hour = difference / 3600
            //val minute = (difference % 3600) / 60
            //val second = difference % 60

            //return String.format("%02d:%02d:%02d", hour, minute, second)
            return this.getTimeString(difference)
        } catch (e: Exception){
            return ""
        }
    }

    fun getTimeLeftString(startTimeString: String, endTimeString: String, format: String, timeLimitInSeconds: Int): String{
        try{
            val simpleDateFormat = SimpleDateFormat(format)
            val startTime = simpleDateFormat.parse(startTimeString)
            val endTime = simpleDateFormat.parse(endTimeString)

            val difference = (startTime.time + timeLimitInSeconds * 1000 - endTime.time) / 1000
            //val hour = difference / 3600
            //val minute = (difference % 3600) / 60
            //val second = difference % 60

            //return String.format("%02d:%02d:%02d", hour, minute, second)
            if(difference <= 0){
                return this.getTimeString(0)
            } else {
                return this.getTimeString(difference)
            }
        } catch(e: Exception){
            return ""
        }
    }

    fun getTimeLeft(startTimeString: String, format: String, timeLimitInSeconds: Int): Long?{
        try{
            val simpleDateFormat = SimpleDateFormat(format)
            simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val startTime = simpleDateFormat.parse(startTimeString)
            val currentTime = Date()

            val timeLeft = (startTime.time + timeLimitInSeconds * 1000 - currentTime.time) / 1000
            return timeLeft
        } catch(e: Exception){
            return null
        }
    }

    fun getDifferenceSinceTime(startTimeString: String, format: String): Long?{
        try{
            val simpleDateFormat = SimpleDateFormat(format)
            simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val startTime = simpleDateFormat.parse(startTimeString)
            val currentTime = Date()

            val timeLeft = (currentTime.time - startTime.time) / 1000
            return timeLeft
        } catch(e: Exception){
            return null
        }
    }

    fun getTimeString(value: Long): String{
        val hour = (value / 3600).takeIf { it > 0 } ?: 0
        val minute = ((value % 3600) / 60).takeIf { it > 0 } ?: 0
        val second = (value % 60).takeIf { it > 0 } ?: 0

        return String.format("%02d:%02d:%02d", hour, minute, second)
    }
}