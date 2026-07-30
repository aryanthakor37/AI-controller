package com.aimobile.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.aimobile.MainActivity
import com.aimobile.managers.ConnectionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import com.aimobile.voice.HotwordArchitectureManager
import com.aimobile.voice.HotwordListener
import com.aimobile.voice.speech.SpeechRecognitionManager
import com.aimobile.voice.tts.TTSManager
import com.aimobile.voice.repository.VoiceRepository
import com.aimobile.voice.repository.VoiceCommandResult

@AndroidEntryPoint
class MainService : Service() {

    @Inject
    lateinit var connectionManager: ConnectionManager

    @Inject
    lateinit var voiceRepository: VoiceRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private lateinit var hotwordManager: HotwordArchitectureManager
    private lateinit var speechRecognitionManager: SpeechRecognitionManager
    private lateinit var ttsManager: TTSManager

    companion object {
        private const val CHANNEL_ID = "ai_mobile_control_channel"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private var wakeLock: android.os.PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        speechRecognitionManager = SpeechRecognitionManager(this)
        ttsManager = TTSManager(this)
        
        initHotwordEngine()
        observeSpeechRecognition()
    }
    
    private fun initHotwordEngine() {
        hotwordManager = HotwordArchitectureManager.getInstance(this)
        hotwordManager.prepareEngine("Hey PhoneAI") // No API key needed for Native

        hotwordManager.registerListener(object : HotwordListener {
            override fun onHotwordDetected(phrase: String) {
                serviceScope.launch(Dispatchers.Main) {
                    ttsManager.speak("Listening")
                    speechRecognitionManager.startListening()
                }
            }
            override fun onError(error: String) {
                android.util.Log.e("MainService", "Hotword error: $error")
            }
        })
        hotwordManager.startListening()
    }
    
    private fun observeSpeechRecognition() {
        serviceScope.launch(Dispatchers.Main) {
            speechRecognitionManager.isListening.collectLatest { listening ->
                if (!listening) {
                    val transcript = speechRecognitionManager.transcript.value
                    if (transcript.isNotBlank()) {
                        processTextCommand(transcript)
                    } else {
                        hotwordManager.startListening()
                    }
                }
            }
        }
    }
    
    private fun processTextCommand(text: String) {
        serviceScope.launch(Dispatchers.IO) {
            when (val result = voiceRepository.sendVoiceCommand(text)) {
                is VoiceCommandResult.Success -> {
                    val execResult = voiceRepository.executeCommandLocally(result.request)
                    val speakText = if (execResult.status == "Success") {
                        result.reply.ifBlank { "Command executed successfully" }
                    } else {
                        "Sorry, I failed to execute that command"
                    }
                    ttsManager.speak(speakText)
                }
                is VoiceCommandResult.Error -> {
                    ttsManager.speak("Sorry, command not recognized or server is offline")
                }
            }
            launch(Dispatchers.Main) {
                hotwordManager.startListening()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Acquire WakeLock to keep CPU alive when screen is locked/off
        val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        wakeLock = powerManager.newWakeLock(
            android.os.PowerManager.PARTIAL_WAKE_LOCK,
            "AiMobileControl::BackgroundWakeLock"
        ).apply {
            acquire()
        }

        // Start Foreground Service with persistent notification
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34+
            startForeground(
                NOTIFICATION_ID, 
                notification, 
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // API 29+
            startForeground(
                NOTIFICATION_ID, 
                notification, 
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // Start Socket.IO connection
        connectionManager.connect()
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        try {
            val restartServiceIntent = Intent(applicationContext, MainService::class.java).apply {
                setPackage(packageName)
            }
            val restartPendingIntent = PendingIntent.getService(
                applicationContext, 999, restartServiceIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
            )
            val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            alarmManager.set(
                android.app.AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 1000,
                restartPendingIntent
            )
        } catch (e: Exception) {
            android.util.Log.e("MainService", "Failed to schedule restart on task removed: ${e.message}")
        }
    }

    override fun onDestroy() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        super.onDestroy()
        connectionManager.disconnect()
        
        try {
            if (::hotwordManager.isInitialized) {
                hotwordManager.stopListening()
            }
        } catch (e: Exception) {}
        
        speechRecognitionManager.destroy()
        ttsManager.destroy()
        serviceScope.cancel()
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
        )

        val voiceIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_route", "voice")
        }
        val voicePending = PendingIntent.getActivity(
            this, 1, voiceIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
        )

        val chatIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_route", "ai_chat")
        }
        val chatPending = PendingIntent.getActivity(
            this, 2, chatIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AI Mobile Assistant Active 🟢")
            .setContentText("Listening for voice & remote system commands...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_btn_speak_now, "🎙️ Voice", voicePending)
            .addAction(android.R.drawable.stat_notify_chat, "💬 Chat", chatPending)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "AI Mobile Control Service Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for persistent AI Mobile Control connection service"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
