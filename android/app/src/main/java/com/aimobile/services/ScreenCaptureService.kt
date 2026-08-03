package com.aimobile.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ScreenCaptureService : Service() {

    @Inject
    lateinit var screenCaptureManager: ScreenCaptureManager

    companion object {
        private const val CHANNEL_ID = "screen_capture_channel"
        private const val NOTIFICATION_ID = 2002
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") {
            screenCaptureManager.stopStream()
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }

        val resultCode = screenCaptureManager.pendingResultCode
        val data = screenCaptureManager.pendingData

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
        } else {
            0
        }

        if (resultCode != -1 && data != null) {
            try {
                screenCaptureManager.initProjection(resultCode, data, this)
                startForeground(NOTIFICATION_ID, createNotification(), type)
                screenCaptureManager.startStream(this)
            } catch (e: Exception) {
                screenCaptureManager.onError?.invoke("Service error: ${e.message}")
                stopSelf()
            }
        } else {
            screenCaptureManager.onError?.invoke("Screen capture permission denied by user or system.")
            startForeground(NOTIFICATION_ID, createNotification(), type)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Live Screen Active")
            .setContentText("Your screen is being streamed to Dashboard")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Screen Capture Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
