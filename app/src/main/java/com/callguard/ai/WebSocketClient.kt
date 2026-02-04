package com.callguard.ai

import kotlinx.coroutines.*
import okhttp3.*
import okio.ByteString
import org.json.JSONObject

class WebSocketClient(private val url: String) {
    private val client = OkHttpClient.Builder().build()
    private var webSocket: WebSocket? = null
    
    var onTranscript: ((text: String, isFinal: Boolean) -> Unit)? = null
    var onIntent: ((intent: String) -> Unit)? = null
    var onAudio: ((audioBytes: ByteArray) -> Unit)? = null
    var onSummary: ((summary: String, intent: String, keyPoints: List<String>) -> Unit)? = null

    fun connect() {
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                handleTextMessage(text)
            }
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                onAudio?.invoke(bytes.toByteArray())
            }
        })
    }

    private fun handleTextMessage(text: String) {
        val json = JSONObject(text)
        when (json.optString("type")) {
            "transcript" -> onTranscript?.invoke(json.optString("text"), json.optBoolean("is_final"))
            "intent" -> onIntent?.invoke(json.optString("intent"))
            "summary" -> {
                val keyPoints = mutableListOf<String>()
                json.optJSONArray("key_points")?.let { arr ->
                    for (i in 0 until arr.length()) keyPoints.add(arr.getString(i))
                }
                onSummary?.invoke(json.optString("summary"), json.optString("intent"), keyPoints)
            }
        }
    }

    fun sendAudio(audioData: ByteArray) { webSocket?.send(ByteString.of(*audioData)) }
    fun sendCommand(command: String) { webSocket?.send(JSONObject().put("command", command).toString()) }
    fun requestSummary(cb: (String, String, List<String>) -> Unit) { onSummary = cb; sendCommand("GET_SUMMARY") }
    fun disconnect() { webSocket?.close(1000, null) }
}
