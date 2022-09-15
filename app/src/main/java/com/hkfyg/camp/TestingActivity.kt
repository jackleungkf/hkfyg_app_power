package com.hkfyg.camp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.hkfyg.camp.utils.Constants
import okhttp3.*
import okio.ByteString
import org.json.JSONObject

class TestingActivity: BaseActivity(){
    private var textView: TextView? = null
    private var button: Button? = null

    private var client: OkHttpClient? = null
    private var webSocket: WebSocket? = null

    private var webSocketStarted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing)

        this.textView = findViewById<TextView>(R.id.textView)
        this.button = findViewById<Button>(R.id.button)

        this.button?.setOnClickListener{
            if(!this.webSocketStarted){
                this.connectWebSocket()
                this.webSocketStarted = true
                this.button?.text = "Close"
            } else {
                this.disconnectWebSocket()
                this.webSocketStarted = false
                this.button?.text = "Start"
            }
        }
    }

    private fun connectWebSocket(){
        //val request = Request.Builder().url("ws://192.168.1.10:8000/ws/team/1/").build()
        val request = Request.Builder().url(Constants.webSocketUrl + "1/").build()
        this.client = OkHttpClient()
        this.webSocket = this.client?.newWebSocket(request, object: WebSocketListener(){
            private val NORMAL_CLOSURE_STATUS = 1000

            override fun onOpen(webSocket: WebSocket?, response: Response?) {
                Log.d("WebSocket", "onOpen")
            }

            override fun onMessage(webSocket: WebSocket?, text: String?) {
                Log.d("webSocket", "onMessage, text: ${text}")
                runOnUiThread{
                    val t = this@TestingActivity.textView?.text.toString()
                    this@TestingActivity.textView?.text = ("${t}\n${text}")

                    if (text.toString().equals("ended")) {
                        this@TestingActivity.showAlertDialog("ended", "", {})
                    }
                }
            }

            override fun onMessage(webSocket: WebSocket?, bytes: ByteString?) {
                Log.d("webSocket", "onMessage, bytes: ${bytes?.hex()}")
            }

            override fun onClosed(webSocket: WebSocket?, code: Int, reason: String?) {
                Log.d("webSocket", "onClosing, code: ${code}, reason: ${reason}")
                this@TestingActivity.webSocket = null
                this@TestingActivity.client = null
            }

            override fun onFailure(webSocket: WebSocket?, t: Throwable?, response: Response?) {
                Log.d("webSocket", "onFailure, error; ${t?.message}")
            }
        })
        this.client?.dispatcher()?.executorService()?.shutdown()
    }

    private fun disconnectWebSocket(){
        val json = JSONObject()
        json.put("type", "chat_message")
        json.put("message", "ended")
        this.webSocket?.send(json.toString())
        this.webSocket?.close(1000, "ended")
    }
}