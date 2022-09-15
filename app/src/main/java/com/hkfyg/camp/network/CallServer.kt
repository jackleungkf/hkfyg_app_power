package com.hkfyg.camp.network

import android.graphics.Bitmap
import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import com.hkfyg.camp.utils.Constants
import org.json.JSONObject
import java.io.File

object CallServer{
    var headers: MutableMap<Any, Any> = mutableMapOf(
        Pair<String, String>("Content-Type", "application/json"),
        Pair<String, String>("Accept", "application/json")
    )

    fun setToken(tokenType: String, token: String){
        this.headers.put("Authorization", tokenType + " " + token)
    }

    fun removeToken(){
        this.headers.remove("Authorization")
    }

    fun <T>get(endpoint: String, queryParameters: MutableMap<Any, Any>?, objectClass: Class<T>, success: (response: T) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        AndroidNetworking.get(Constants.host+endpoint)
            .addQueryParameter(queryParameters)
            .addHeaders(this.headers)
            .build()
            .getAsJSONObject(object: JSONObjectRequestListener{
                override fun onResponse(response: JSONObject?) {
                    this@CallServer.successCallback(objectClass, response, success, failure)
                }

                override fun onError(anError: ANError?) {
                    this@CallServer.failureCallback(anError, failure)
                }
            })
    }

    fun <T>post(endpoint: String, bodyParameters: JSONObject?, objectClass: Class<T>, success: (response: T) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        AndroidNetworking.post(Constants.host+endpoint)
            .addJSONObjectBody(bodyParameters)
            .addHeaders(this.headers)
            .build()
            .getAsJSONObject(object: JSONObjectRequestListener{
                override fun onResponse(response: JSONObject?) {
                    this@CallServer.successCallback(objectClass, response, success, failure)
                }

                override fun onError(anError: ANError?) {
                    this@CallServer.failureCallback(anError, failure)
                }
            })
    }

    fun <T>put(endpoint: String, bodyParameters: JSONObject?, objectClass: Class<T>, success: (response: T) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        AndroidNetworking.put(Constants.host+endpoint)
            .addJSONObjectBody(bodyParameters)
            .addHeaders(this.headers)
            .build()
            .getAsJSONObject(object: JSONObjectRequestListener{
                override fun onResponse(response: JSONObject?) {
                    this@CallServer.successCallback(objectClass, response, success, failure)
                }

                override fun onError(anError: ANError?) {
                    this@CallServer.failureCallback(anError, failure)
                }
            })
    }

    fun <T>patch(endpoint: String, bodyParameters: JSONObject?, objectClas: Class<T>, success: (response: T) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){

        Log.e("endpoint:String", Constants.host+endpoint + "")
        Log.e("JSONParams:JSONObject", bodyParameters.toString())

        AndroidNetworking.patch(Constants.host+endpoint)
            .addJSONObjectBody(bodyParameters)
            .addHeaders(this.headers)
            .build()
            .getAsJSONObject(object: JSONObjectRequestListener{
                override fun onResponse(response: JSONObject?) {
                    this@CallServer.successCallback(objectClas, response, success, failure)
                }

                override fun onError(anError: ANError?) {
                    this@CallServer.failureCallback(anError, failure)
                }
            })
    }

    fun <T>postFile(url: String, headers: MutableMap<Any, Any>?, file: File, objectClass: Class<T>, success: (response: T) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        AndroidNetworking.post(url)
            .setContentType(Constants.predictionContentType)
            .addHeaders(headers)
            .addFileBody(file)
            .build()
            .getAsJSONObject(object: JSONObjectRequestListener{
                override fun onResponse(response: JSONObject?) {
                    this@CallServer.successCallback(objectClass, response, success, failure)
                }

                override fun onError(anError: ANError?) {
                    this@CallServer.failureCallback(anError, failure)
                }
            })
    }

    private fun <T>successCallback(objectClass: Class<T>, response: JSONObject?, success: (response: T) -> Unit, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        Log.d("CallServer", "successCallback")
        val responseObject = Gson().fromJson<T>(response?.toString(), objectClass)
        if (responseObject != null){
            success(responseObject)
        } else {
            failure(false, null)
        }
    }

    fun logLargeString(str: String?) {
        if (str != null) {
            if (str.length > 3000) {
                Log.i("Error Long Log", str.substring(0, 3000))
                if (str != null) {
                    logLargeString(str.substring(3000))
                }
            } else {
                Log.i("Error Long Log", str) // continuation
            }
        }
    }

    private fun failureCallback(error: ANError?, failure: (sessionExpired: Boolean, error: Any?) -> Unit){
        Log.d("CallServer", "failureCallback, code: " + error?.errorCode + ", message: " + error?.errorBody)
        logLargeString(error?.errorBody)
        Log.e("Errrorrrr", error?.errorDetail)
        var e: Any? = null

        try{
            e = Gson().fromJson<ErrorMessage>(error?.errorBody?.toString(), ErrorMessage::class.java)
        } catch(exception: Exception){
            e = error
        }

        if(error?.errorCode == 401 || error?.errorCode == 403){
            failure(true, e)
        } else {
            failure(false, e)
        }
    }
}