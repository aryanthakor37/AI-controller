package com.aimobile.voice.repository

import android.content.Context
import android.util.Log
import com.aimobile.api.ApiService
import com.aimobile.api.ChatRequest
import com.aimobile.models.CommandRequest
import com.aimobile.models.CommandResult
import com.aimobile.router.IntentRouter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService
) {
    private val intentRouter = IntentRouter(context)

    suspend fun sendVoiceCommand(command: String): VoiceCommandResult = withContext(Dispatchers.IO) {
        try {
            val response = apiService.sendChat(ChatRequest(command))
            if (response.isSuccessful && response.body() != null) {
                val chatData = response.body()!!.data
                if (chatData != null) {
                    val intent = chatData.intent ?: "UNKNOWN_COMMAND"
                    val reply = chatData.reply ?: ""
                    
                    // Parse args
                    val request = mapChatDataToCommandRequest(chatData)
                    
                    return@withContext VoiceCommandResult.Success(intent, reply, request)
                } else {
                    return@withContext parseLocalFallback(command)
                }
            } else {
                return@withContext parseLocalFallback(command)
            }
        } catch (e: Exception) {
            Log.e("VoiceRepository", "Error processing voice command: ${e.message}. Falling back to local parser.", e)
            return@withContext parseLocalFallback(command)
        }
    }

    suspend fun executeCommandLocally(request: CommandRequest): CommandResult {
        return intentRouter.route(request)
    }

    private fun parseLocalFallback(command: String): VoiceCommandResult {
        val clean = command.lowercase(java.util.Locale.getDefault()).trim()
        
        val intent: String
        var appName: String? = null
        var contactName: String? = null
        var smsMsg: String? = null
        var hour: Int? = null
        var minute: Int? = null
        var timerDuration: String? = null

        when {
            clean.contains("flashlight on") || clean.contains("torch on") || clean.contains("turn on flashlight") || clean.contains("turn on torch") || clean.contains("flashlight open") || clean.contains("fleshlight open") -> {
                intent = "FLASHLIGHT_ON"
            }
            clean.contains("flashlight off") || clean.contains("torch off") || clean.contains("turn off flashlight") || clean.contains("turn off torch") || clean.contains("flashlight close") -> {
                intent = "FLASHLIGHT_OFF"
            }
            clean.contains("open camera") || clean.contains("camera") -> {
                intent = "OPEN_CAMERA"
            }
            clean.contains("open gallery") || clean.contains("gallery") -> {
                intent = "OPEN_GALLERY"
            }
            clean.contains("open chrome") || clean.contains("chrome") || clean.contains("browser") -> {
                intent = "OPEN_CHROME"
            }
            clean.contains("open youtube") || clean.contains("youtube") -> {
                intent = "OPEN_YOUTUBE"
            }
            clean.contains("open maps") || clean.contains("maps") -> {
                intent = "OPEN_MAPS"
            }
            clean.contains("open spotify") || clean.contains("spotify") -> {
                intent = "OPEN_APP"
                appName = "Spotify"
            }
            clean.contains("open whatsapp") || clean.contains("whatsapp") -> {
                intent = "OPEN_APP"
                appName = "WhatsApp"
            }
            clean.contains("alarm") || clean.contains("clock") || clean.contains("alaram") -> {
                intent = "SET_ALARM"
                val match = Regex("(\\d+)(?:\\s*:\\s*(\\d+))?\\s*(am|pm)?").find(clean)
                if (match != null) {
                    var h = match.groupValues[1].toIntOrNull() ?: 0
                    val mStr = match.groupValues[2]
                    val m = if (mStr.isNotEmpty()) mStr.toIntOrNull() ?: 0 else 0
                    val ampm = match.groupValues[3]
                    
                    if (ampm == "pm" && h < 12) h += 12
                    else if (ampm == "am" && h == 12) h = 0
                    
                    hour = h
                    minute = m
                }
            }
            else -> {
                if (clean.startsWith("open ")) {
                    intent = "OPEN_APP"
                    appName = command.substring(5).trim()
                } else {
                    intent = "UNKNOWN_COMMAND"
                }
            }
        }

        if (intent == "UNKNOWN_COMMAND") {
            return VoiceCommandResult.Error("Offline: Command not recognized.")
        }

        val request = CommandRequest(
            intent = intent,
            number = contactName,
            hour = hour,
            minute = minute,
            message = appName ?: smsMsg
        )
        
        val reply = when (intent) {
            "FLASHLIGHT_ON" -> "Turning on flashlight locally"
            "FLASHLIGHT_OFF" -> "Turning off flashlight locally"
            "OPEN_CAMERA" -> "Opening camera locally"
            "OPEN_GALLERY" -> "Opening gallery locally"
            "OPEN_CHROME" -> "Opening Chrome locally"
            "OPEN_YOUTUBE" -> "Opening YouTube locally"
            "OPEN_MAPS" -> "Opening Maps locally"
            "OPEN_APP" -> "Opening $appName locally"
            "SET_ALARM" -> "Opening Alarms locally"
            else -> "Executing command locally"
        }

        return VoiceCommandResult.Success(intent, reply, request)
    }

    private fun mapChatDataToCommandRequest(chatData: com.aimobile.api.ChatData): CommandRequest {
        val intent = chatData.intent ?: "UNKNOWN_COMMAND"
        
        // Parse time if format is "HH:MM"
        var hour: Int? = null
        var minute: Int? = null
        val timeStr = chatData.time
        if (!timeStr.isNullOrEmpty()) {
            if (timeStr.contains(":")) {
                val parts = timeStr.split(":")
                if (parts.size >= 2) {
                    var h = parts[0].toIntOrNull()
                    val mStr = parts[1].filter { it.isDigit() }
                    val m = mStr.toIntOrNull()
                    
                    if (timeStr.contains("pm", ignoreCase = true) && h != null && h < 12) h += 12
                    if (timeStr.contains("am", ignoreCase = true) && h != null && h == 12) h = 0
                    
                    hour = h
                    minute = m
                }
            } else {
                var h = timeStr.filter { it.isDigit() }.toIntOrNull()
                if (timeStr.contains("pm", ignoreCase = true) && h != null && h < 12) h += 12
                if (timeStr.contains("am", ignoreCase = true) && h != null && h == 12) h = 0
                
                hour = h
                minute = 0
            }
        }
        
        // Parse duration in minutes from duration string (e.g., "5" or "5 minutes")
        var durationMinutes: Int? = null
        val durStr = chatData.duration
        if (!durStr.isNullOrEmpty()) {
            val digitsOnly = durStr.filter { it.isDigit() }
            durationMinutes = digitsOnly.toIntOrNull()
        }

        return CommandRequest(
            intent = intent,
            number = chatData.number ?: chatData.contact,
            hour = hour,
            minute = minute ?: durationMinutes, // maps minute parameter to timer duration if needed
            message = chatData.message ?: chatData.app
        )
    }
}

sealed class VoiceCommandResult {
    data class Success(val intent: String, val reply: String, val request: CommandRequest) : VoiceCommandResult()
    data class Error(val message: String) : VoiceCommandResult()
}
