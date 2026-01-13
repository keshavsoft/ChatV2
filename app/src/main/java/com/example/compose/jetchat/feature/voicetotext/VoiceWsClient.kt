package com.example.compose.jetchat.feature.voicetotext

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.*

object VoiceWsClient {

    private const val TAG = "VoiceWsClient"

    // TODO: put your actual ws url here
    private const val WS_URL = "wss://keshavsoft.com/"

    private val client by lazy { OkHttpClient() }
    private var webSocket: WebSocket? = null

    // incoming messages flow
    private val _incomingMessages = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<String> = _incomingMessages

    private val request: Request by lazy {
        Request.Builder()
            .url(WS_URL)
            .build()
    }

    private val listener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "WebSocket opened")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "Server -> $text")
            _incomingMessages.tryEmit(text)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(code, reason)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "WebSocket error", t)
        }
    }

    fun connect() {
        if (webSocket != null) return
        webSocket = client.newWebSocket(request, listener)
    }

    fun close() {
        webSocket?.close(1000, "Client closing")
        webSocket = null
    }

    fun sendFinal(text: String) {
        if (text.isBlank()) return
        webSocket?.send("FINAL:$text")
    }

    fun sendPartial(text: String) {
        if (text.isBlank()) return
        webSocket?.send("PARTIAL:$text")
    }
}
