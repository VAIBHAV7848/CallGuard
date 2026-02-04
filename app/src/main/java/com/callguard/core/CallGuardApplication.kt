package com.callguard.core

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.callguard.history.CallHistoryDatabase

class CallGuardApplication : Application() {

    lateinit var database: CallHistoryDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = CallHistoryDatabase.getInstance(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            // Incoming call channel
            val incomingChannel = NotificationChannel(
                Constants.CHANNEL_INCOMING_CALL,
                "Incoming Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for incoming calls"
                setSound(null, null)
            }

            // Active call channel
            val activeChannel = NotificationChannel(
                Constants.CHANNEL_ACTIVE_CALL,
                "Active Calls",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing call notifications"
            }

            // AI channel
            val aiChannel = NotificationChannel(
                Constants.CHANNEL_AI_ACTIVE,
                "AI Assistant",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "AI call assistant notifications"
            }

            manager.createNotificationChannels(listOf(incomingChannel, activeChannel, aiChannel))
        }
    }

    companion object {
        lateinit var instance: CallGuardApplication
            private set
    }
}
