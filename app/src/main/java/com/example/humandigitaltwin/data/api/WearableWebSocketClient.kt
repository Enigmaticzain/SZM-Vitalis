package com.example.humandigitaltwin.data.api

import com.example.humandigitaltwin.BuildConfig
import com.example.humandigitaltwin.data.models.WearableDataPoint
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import java.util.concurrent.TimeUnit

class WearableWebSocketClient(
    private val onDataReceived: (WearableDataPoint) -> Unit,
    private val onConnectionChanged: (Boolean) -> Unit
) {
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val adapter = moshi.adapter(WearableDataPoint::class.java)
    private var webSocket: WebSocket? = null

    fun connect() {
        val wsUrl = BuildConfig.BASE_URL.replace("http://", "ws://").replace("https://", "wss://") + "ws/wearable"
        val request = Request.Builder()
            .url(wsUrl)
            .header("X-API-Key", BuildConfig.API_KEY)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                onConnectionChanged(true)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val dataPoint = adapter.fromJson(text)
                    if (dataPoint != null) {
                        onDataReceived(dataPoint)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                onConnectionChanged(false)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                onConnectionChanged(false)
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnecting")
        webSocket = null
        onConnectionChanged(false)
    }
}
