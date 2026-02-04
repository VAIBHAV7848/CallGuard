package com.callguard.ai

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.callguard.R
import com.callguard.call.InCallActivity
import com.callguard.core.Constants

class AIForegroundService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationIntent = Intent(this, InCallActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        
        val notification = NotificationCompat.Builder(this, Constants.CHANNEL_AI_ACTIVE)
            .setContentTitle("AI Assistant Active")
            .setContentText("Handling call...")
            .setSmallIcon(R.drawable.ic_ai)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(Constants.NOTIFICATION_AI, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(Constants.NOTIFICATION_AI, notification)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() { super.onDestroy(); stopForeground(STOP_FOREGROUND_REMOVE) }
}
