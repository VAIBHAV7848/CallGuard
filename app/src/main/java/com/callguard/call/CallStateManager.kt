package com.callguard.call

import android.telecom.Call
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object CallStateManager {
    
    private val _currentCall = MutableStateFlow<Call?>(null)
    val currentCall: StateFlow<Call?> = _currentCall

    private val _callState = MutableStateFlow(CallState.IDLE)
    val callState: StateFlow<CallState> = _callState

    private val _isAIActive = MutableStateFlow(false)
    val isAIActive: StateFlow<Boolean> = _isAIActive

    private val _callStartTime = MutableStateFlow(0L)
    val callStartTime: StateFlow<Long> = _callStartTime

    private var callCallback: Call.Callback? = null

    fun setCall(call: Call?) {
        _currentCall.value = call
        
        callCallback?.let { _currentCall.value?.unregisterCallback(it) }
        
        if (call != null) {
            callCallback = object : Call.Callback() {
                override fun onStateChanged(call: Call, state: Int) {
                    updateState(state)
                }
            }
            call.registerCallback(callCallback!!)
            updateState(call.state)
        } else {
            _callState.value = CallState.IDLE
            _isAIActive.value = false
            _callStartTime.value = 0L
        }
    }

    private fun updateState(state: Int) {
        _callState.value = when (state) {
            Call.STATE_DIALING, Call.STATE_CONNECTING -> CallState.DIALING
            Call.STATE_RINGING -> CallState.RINGING
            Call.STATE_ACTIVE -> {
                if (_callStartTime.value == 0L) {
                    _callStartTime.value = System.currentTimeMillis()
                }
                CallState.ACTIVE
            }
            Call.STATE_HOLDING -> CallState.HOLDING
            Call.STATE_DISCONNECTED, Call.STATE_DISCONNECTING -> CallState.DISCONNECTED
            else -> CallState.IDLE
        }
    }

    fun setAIActive(active: Boolean) {
        _isAIActive.value = active
    }

    fun answerCall() {
        _currentCall.value?.answer(0)
    }

    fun rejectCall() {
        _currentCall.value?.reject(false, null)
    }

    fun endCall() {
        _currentCall.value?.disconnect()
    }

    fun toggleMute(): Boolean {
        // Audio routing handled by InCallService
        return true
    }

    fun toggleSpeaker(): Boolean {
        // Audio routing handled by InCallService
        return true
    }

    fun sendDtmf(digit: Char) {
        _currentCall.value?.playDtmfTone(digit)
        _currentCall.value?.stopDtmfTone()
    }

    fun getPhoneNumber(): String? {
        return _currentCall.value?.details?.handle?.schemeSpecificPart
    }
}

enum class CallState {
    IDLE,
    DIALING,
    RINGING,
    ACTIVE,
    HOLDING,
    DISCONNECTED
}
