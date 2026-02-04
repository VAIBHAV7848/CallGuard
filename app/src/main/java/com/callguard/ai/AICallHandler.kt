package com.callguard.ai

import android.content.Context
import android.content.Intent
import com.callguard.call.CallStateManager
import com.callguard.call.InCallActivity
import com.callguard.core.CallGuardApplication
import com.callguard.core.Constants
import com.callguard.history.AIIntent
import com.callguard.history.CallLogEntry
import com.callguard.history.CallType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AICallHandler {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var webSocketClient: WebSocketClient? = null
    private var audioStreamer: AudioStreamer? = null
    
    private val _isActive = MutableStateFlow(false)
    val isActiveFlow: StateFlow<Boolean> = _isActive
    val isActive: Boolean get() = _isActive.value
    
    private val _transcript = MutableStateFlow("")
    val transcriptFlow: StateFlow<String> = _transcript
    
    private val _intent = MutableStateFlow<String?>(null)
    val intentFlow: StateFlow<String?> = _intent
    
    private val _summary = MutableStateFlow<String?>(null)
    val summaryFlow: StateFlow<String?> = _summary
    
    private var phoneNumber: String = ""
    private var fullTranscript = StringBuilder()
    private var keyPoints = mutableListOf<String>()
    private var isMuted = false

    fun scheduleInterception(context: Context, number: String) {
        phoneNumber = number
        // AI will auto-intercept when call is answered
        // This is called from CallScreeningService
    }

    fun interceptCurrentCall(context: Context) {
        scope.launch {
            _isActive.value = true
            CallStateManager.setAIActive(true)
            
            // Answer the call
            CallStateManager.answerCall()
            
            // Start foreground service
            val serviceIntent = Intent(context, AIForegroundService::class.java)
            context.startForegroundService(serviceIntent)
            
            // Connect to backend
            webSocketClient = WebSocketClient(Constants.BACKEND_WS_URL)
            webSocketClient?.connect()
            
            // Set up listeners
            webSocketClient?.onTranscript = { text, isFinal ->
                fullTranscript.append(text).append(" ")
                _transcript.value = fullTranscript.toString()
            }
            
            webSocketClient?.onIntent = { intent ->
                _intent.value = intent
            }
            
            webSocketClient?.onAudio = { audioBytes ->
                if (!isMuted) {
                    audioStreamer?.playAudio(audioBytes)
                }
            }
            
            // Start audio streaming
            audioStreamer = AudioStreamer { audioData ->
                webSocketClient?.sendAudio(audioData)
            }
            audioStreamer?.start()
        }
    }

    fun userTakeover() {
        scope.launch {
            // IMMEDIATE YIELD - No delay
            _isActive.value = false
            CallStateManager.setAIActive(false)
            
            // Stop AI audio INSTANTLY
            audioStreamer?.stop()
            webSocketClient?.sendCommand("STOP")
            
            // Request summary before disconnecting
            requestSummary()
        }
    }

    fun muteAI(mute: Boolean) {
        isMuted = mute
        webSocketClient?.sendCommand(if (mute) "MUTE_TTS" else "UNMUTE_TTS")
    }

    fun endCall() {
        scope.launch {
            _isActive.value = false
            CallStateManager.setAIActive(false)
            
            // Request summary
            requestSummary()
            
            // Cleanup
            audioStreamer?.stop()
            webSocketClient?.disconnect()
            
            // Save to database
            saveCallToHistory()
            
            // End call
            CallStateManager.endCall()
        }
    }

    private suspend fun requestSummary() {
        webSocketClient?.requestSummary { summary, intent, points ->
            _summary.value = summary
            _intent.value = intent
            keyPoints.clear()
            keyPoints.addAll(points)
        }
    }

    private suspend fun saveCallToHistory() {
        val db = CallGuardApplication.instance.database
        val entry = CallLogEntry(
            phoneNumber = phoneNumber,
            contactName = null,
            contactPhotoUri = null,
            timestamp = System.currentTimeMillis(),
            durationSeconds = ((System.currentTimeMillis() - CallStateManager.callStartTime.value) / 1000).toInt(),
            callType = CallType.INCOMING,
            wasAIHandled = true,
            aiIntent = _intent.value?.let { 
                try { AIIntent.valueOf(it) } catch (e: Exception) { AIIntent.UNKNOWN }
            },
            aiSummary = _summary.value,
            aiKeyPoints = keyPoints.toList(),
            aiTranscript = fullTranscript.toString()
        )
        db.callHistoryDao().insertCall(entry)
    }

    fun reset() {
        _isActive.value = false
        _transcript.value = ""
        _intent.value = null
        _summary.value = null
        fullTranscript.clear()
        keyPoints.clear()
        phoneNumber = ""
        isMuted = false
    }
}
