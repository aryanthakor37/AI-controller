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
        // Try local parsing first for instant execution of basic commands
        val localResult = parseLocalFallback(command)
        if (localResult is VoiceCommandResult.Success && localResult.intent != "UNKNOWN_COMMAND") {
            return@withContext localResult
        }

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
                    return@withContext VoiceCommandResult.Error("API returned empty data.")
                }
            } else {
                return@withContext VoiceCommandResult.Error("API Error: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("VoiceRepository", "Error processing voice command: ${e.message}", e)
            return@withContext VoiceCommandResult.Error("Network error. Could not connect to AI.")
        }
    }

    suspend fun executeCommandLocally(request: CommandRequest): CommandResult {
        return intentRouter.route(request)
    }

    private fun levenshteinDistance(a: String, b: String): Int {
        val costs = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            var nw = i - 1
            costs[0] = i
            for (j in 1..b.length) {
                val cj = minOf(1 + minOf(costs[j], costs[j - 1]), if (a[i - 1] == b[j - 1]) nw else nw + 1)
                nw = costs[j]
                costs[j] = cj
            }
        }
        return costs[b.length]
    }

    private fun matchesKeyword(input: String, vararg keywords: String): Boolean {
        val words = input.split(" ", "-", "_")
        for (keyword in keywords) {
            if (input.contains(keyword)) return true
            val maxDist = if (keyword.length > 5) 2 else 1
            if (keyword.length > 3) {
                for (word in words) {
                    if (word.length >= 3 && levenshteinDistance(word, keyword) <= maxDist) {
                        return true
                    }
                }
            }
        }
        return false
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
            clean.contains("search ") || clean.contains("play ") -> {
                intent = "SEARCH_APP"
                val searchIndex = clean.indexOf("search ")
                val playIndex = clean.indexOf("play ")
                val startIndex = if (searchIndex != -1) searchIndex + 7 else playIndex + 5
                
                val rawQuery = clean.substring(startIndex).trim()
                val appKeywords = listOf("youtube", "spotify", "maps", "map", "chrome", "google", "browser", "instagram", "play store", "playstore", "store", "telegram")
                var detectedApp: String? = null
                var finalQuery = rawQuery

                if (clean.startsWith("open ") || clean.startsWith("launch ")) {
                    val prefixLen = if (clean.startsWith("open ")) 5 else 7
                    val openTarget = clean.substring(prefixLen).substringBefore(" search ").substringBefore(" and search ").trim()
                    if (openTarget.isNotEmpty() && openTarget != clean.substring(prefixLen).trim()) {
                        detectedApp = openTarget
                        finalQuery = clean.substringAfter("search ").trim()
                    }
                }

                if (detectedApp == null) {
                    for (app in appKeywords) {
                        if (rawQuery.endsWith(" on $app") || rawQuery.endsWith(" in $app")) {
                            detectedApp = app
                            finalQuery = rawQuery.substring(0, rawQuery.length - (app.length + 4)).trim()
                            break
                        }
                    }
                }

                if (detectedApp == null) {
                    detectedApp = when {
                        clean.contains("youtube") -> "YouTube"
                        clean.contains("spotify") -> "Spotify"
                        clean.contains("map") || clean.contains("maps") -> "Maps"
                        clean.contains("chrome") || clean.contains("browser") -> "Chrome"
                        clean.contains("instagram") -> "Instagram"
                        clean.contains("play store") || clean.contains("playstore") || clean.contains("store") -> "Play Store"
                        clean.contains("telegram") -> "Telegram"
                        else -> "YouTube"
                    }
                }

                appName = detectedApp
                smsMsg = finalQuery // smsMsg is used for queryText in VoiceRepository
            }
            matchesKeyword(clean, "flashlight", "fleshlight", "torch") -> {
                if (clean.contains("off") || clean.contains("close") || clean.contains("bandh") || clean.contains("band")) {
                    intent = "FLASHLIGHT_OFF"
                } else {
                    intent = "FLASHLIGHT_ON"
                }
            }
            matchesKeyword(clean, "camera", "camra", "photo", "photos", "gallery") -> {
                if (clean.contains("gallery") || clean.contains("photo")) {
                    intent = "OPEN_GALLERY"
                } else {
                    intent = "OPEN_CAMERA"
                }
            }
            matchesKeyword(clean, "chrome", "browser", "internet") && !matchesKeyword(clean, "wifi") -> {
                intent = "OPEN_CHROME"
            }
            matchesKeyword(clean, "youtube", "utube", "youtub") -> {
                intent = "OPEN_YOUTUBE"
            }
            matchesKeyword(clean, "map", "maps", "naksho") -> {
                intent = "OPEN_MAPS"
            }
            matchesKeyword(clean, "bluetooth", "bluetoth", "blutoth") -> {
                intent = "OPEN_APP"
                appName = clean
            }
            matchesKeyword(clean, "wifi", "wi-fi", "wfi") -> {
                intent = "OPEN_APP"
                appName = clean
            }
            matchesKeyword(clean, "dark mode", "dark theme", "dark") -> {
                intent = "OPEN_APP"
                appName = clean
            }
            matchesKeyword(clean, "brightness", "display", "bruitnes", "bright") -> {
                intent = "SET_BRIGHTNESS"
                appName = clean
            }
            matchesKeyword(clean, "nearby", "quick share") -> {
                intent = "OPEN_APP"
                appName = "Nearby Share"
            }
            matchesKeyword(clean, "spotify", "music", "song", "songs") && !clean.contains("youtube") -> {
                intent = "OPEN_APP"
                appName = "Spotify"
            }
            matchesKeyword(clean, "whatsapp", "watsap", "whatsap") -> {
                intent = "OPEN_APP"
                appName = "WhatsApp"
            }
            clean.contains("call ") || clean.contains("dial ") || clean.startsWith("call") -> {
                intent = "CALL_CONTACT"
                val target = clean.replace("call ", "").replace("dial ", "").replace("to ", "").replace("contact ", "").trim()
                contactName = if (target.isNotBlank()) target else "Mom"
            }
            matchesKeyword(clean, "volume", "sound", "awaj", "ringtone") -> {
                if (clean.contains("up") || clean.contains("increase") || clean.contains("vadhare") || clean.contains("vadhar") || clean.contains("vadaar")) {
                    intent = "INCREASE_VOLUME"
                } else if (clean.contains("down") || clean.contains("decrease") || clean.contains("ochhu") || clean.contains("ghatad") || clean.contains("low")) {
                    intent = "DECREASE_VOLUME"
                } else if (clean.contains("mute") || clean.contains("bandh") || clean.contains("band")) {
                    intent = "MUTE_VOLUME"
                } else {
                    intent = "OPEN_APP"
                    appName = "Volume"
                }
            }
            clean.contains("screen") || (clean.contains("summarize") && !clean.contains("news")) -> {
                intent = "SUMMARIZE_SCREEN"
            }
            clean.contains("translate") || clean.contains("ટ્રાન્સલેટ") || clean.contains("અનુવાદ") -> {
                intent = "TRANSLATE_TEXT"
                smsMsg = command
            }
            clean.contains("alarm") || clean.contains("clock") || clean.contains("alaram") || clean.contains("wake me") -> {
                intent = "SET_ALARM"
                val match = Regex("(\\d+)(?:\\s*:\\s*(\\d+))?\\s*(am|pm)?").find(clean)
                if (match != null) {
                    var h = match.groupValues[1].toIntOrNull() ?: 7
                    val mStr = match.groupValues[2]
                    val m = if (mStr.isNotEmpty()) mStr.toIntOrNull() ?: 0 else 0
                    val ampm = match.groupValues[3]
                    
                    if (ampm == "pm" && h < 12) h += 12
                    else if (ampm == "am" && h == 12) h = 0
                    
                    hour = h
                    minute = m
                } else {
                    hour = 7
                    minute = 0
                }
            }
            else -> {
                if (clean.contains("turn on ") || clean.contains("turn off ") || clean.contains("toggle ") || clean.contains("enable ") || clean.contains("disable ") || clean.endsWith(" on") || clean.endsWith(" off")) {
                    intent = "TOGGLE_QUICK_SETTING"
                    appName = clean.replace("turn on ", "").replace("turn off ", "").replace("toggle ", "").replace("enable ", "").replace("disable ", "").let { if (it.endsWith(" on")) it.dropLast(3) else if (it.endsWith(" off")) it.dropLast(4) else it }.trim()
                } else if (clean.startsWith("open ") || clean.startsWith("launch ") || clean.startsWith("start ")) {
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
            app = chatData.app,
            steps = chatData.steps
        )
    }
}

sealed class VoiceCommandResult {
    data class Success(val intent: String, val reply: String, val request: CommandRequest) : VoiceCommandResult()
    data class Error(val message: String) : VoiceCommandResult()
}
