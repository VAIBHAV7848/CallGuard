package com.callguard.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_history")
data class CallLogEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val contactName: String?,
    val contactPhotoUri: String?,
    val timestamp: Long,
    val durationSeconds: Int,
    val callType: CallType,
    val wasAIHandled: Boolean,
    val aiIntent: AIIntent?,
    val aiSummary: String?,
    val aiKeyPoints: List<String>?,
    val aiTranscript: String?
)

enum class CallType {
    INCOMING,
    OUTGOING,
    MISSED
}

enum class AIIntent {
    SPAM,
    DELIVERY,
    PERSONAL,
    FRAUD,
    UNKNOWN
}
