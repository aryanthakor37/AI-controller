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
            clean.startsWith("search ") || clean.startsWith("play ") -> {
                intent = "SEARCH_APP"
                val regexWord = if (clean.startsWith("search ")) "search" else "play"
                val match = Regex("$regexWord\\s+(.*?)\\s+(?:in|on)\\s+(.*)").find(clean)
                if (match != null) {
                    smsMsg = match.groupValues[1].trim() // query
                    appName = match.groupValues[2].trim() // app
                } else {
                    val simpleMatch = Regex("$regexWord\\s+(.*)").find(clean)
                    if (simpleMatch != null) {
                        smsMsg = simpleMatch.groupValues[1].trim() // query
                        appName = "YouTube" // default app
                    }
                }
            }
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
                if (clean.startsWith("open ") || clean.startsWith("launch ") || clean.startsWith("start ")) {
                    intent = "OPEN_APP"
                    appName = clean.replace("open ", "").replace("launch ", "").replace("start ", "").replace("app ", "").trim()
                } else {
                    intent = "UNKNOWN_COMMAND"
                }
            }
        }

        if (intent == "UNKNOWN_COMMAND") {
            return VoiceCommandResult.Error("Offline: Command not recognized.")
        }

        val timeStr = if (hour != null && minute != null) String.format("%02d:%02d", hour, minute) else null
        
        val request = CommandRequest(
            intent = intent,
            number = contactName,
            time = timeStr,
            duration = null,
            query = smsMsg,
            message = smsMsg,
            app = appName
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
        
        // Parse duration in seconds from duration string if possible
        var durationSeconds: Int? = null
        val durStr = chatData.duration
        if (!durStr.isNullOrEmpty()) {
            val digitsOnly = durStr.filter { it.isDigit() }
            durationSeconds = digitsOnly.toIntOrNull()
        }

        return CommandRequest(
            intent = intent,
            number = chatData.number ?: chatData.contact,
            time = chatData.time,
            duration = durationSeconds,
            query = chatData.query,
            message = chatData.message,
            app = chatData.app
        )
    }
}

sealed class VoiceCommandResult {
    data class Success(val intent: String, val reply: String, val request: CommandRequest) : VoiceCommandResult()
    data class Error(val message: String) : VoiceCommandResult()
}
