package com.callguard.core

object Constants {
    // Backend WebSocket URL - Update with your deployed backend
    const val BACKEND_WS_URL = "wss://callguard-backend.fly.dev/ws/call"
    
    // Notification Channels
    const val CHANNEL_INCOMING_CALL = "incoming_call"
    const val CHANNEL_ACTIVE_CALL = "active_call"
    const val CHANNEL_AI_ACTIVE = "ai_active"
    
    // Notification IDs
    const val NOTIFICATION_INCOMING = 1001
    const val NOTIFICATION_ACTIVE = 1002
    const val NOTIFICATION_AI = 1003
    
    // Audio Settings
    const val AUDIO_SAMPLE_RATE = 16000
    const val AUDIO_CHANNEL_CONFIG = 16 // CHANNEL_IN_MONO
    const val AUDIO_ENCODING = 2 // ENCODING_PCM_16BIT
    const val AUDIO_BUFFER_SIZE = 3200 // 100ms at 16kHz
    
    // Intent Extras
    const val EXTRA_PHONE_NUMBER = "phone_number"
    const val EXTRA_CONTACT_NAME = "contact_name"
    const val EXTRA_IS_AI_CALL = "is_ai_call"
    const val EXTRA_CALL_LOG_ID = "call_log_id"
    
    // Owner Name (configurable)
    const val OWNER_NAME = "User"
}
