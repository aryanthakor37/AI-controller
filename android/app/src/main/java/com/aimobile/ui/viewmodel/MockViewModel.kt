package com.aimobile.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aimobile.api.ApiService
import com.aimobile.api.ChatRequest
import com.aimobile.utils.TokenManager
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

import com.aimobile.repository.RoutineRepository
import com.aimobile.command.RoutineExecutor

data class MockUser(
    val name: String = "User",
    val email: String = "",
    val deviceCount: Int = 1
)

data class MockDeviceStatus(
    val battery: Int = 82,
    val isCharging: Boolean = true,
    val storageTotal: Int = 256,
    val storageUsed: Int = 112,
    val networkStrength: Int = 4,
    val model: String = "Android Device",
    val androidVersion: String = "Android 14"
)

data class MockCommand(
    val id: String,
    val name: String,
    val status: String,
    val time: String
)

data class MockChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val time: String
)

@HiltViewModel
class MockViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
    private val routineRepository: RoutineRepository,
    private val routineExecutor: RoutineExecutor
) : ViewModel() {

    private val tokenManager = TokenManager(context)
    private val gson = Gson()

    // Load real user name from saved profile
    private val _user = MutableStateFlow(loadUser())
    val user: StateFlow<MockUser> = _user.asStateFlow()

    private fun getRealBatteryPercentage(context: Context): Int {
        val batteryStatus: Intent? = context.registerReceiver(
            null, 
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) {
            ((level.toFloat() / scale.toFloat()) * 100).toInt()
        } else {
            82
        }
    }

    private fun getStorageStats(): Pair<Int, Int> {
        return try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val availableBytes = availableBlocks * blockSize
            val usedBytes = totalBytes - availableBytes

            val totalGB = (totalBytes / (1024 * 1024 * 1024)).toInt()
            val usedGB = (usedBytes / (1024 * 1024 * 1024)).toInt()
            Pair(totalGB, usedGB)
        } catch (e: Exception) {
            Pair(256, 112)
        }
    }

    private val storage = getStorageStats()
    private val _deviceStatus = MutableStateFlow(
        MockDeviceStatus(
            battery = getRealBatteryPercentage(context),
            storageTotal = storage.first,
            storageUsed = storage.second
        )
    )
    val deviceStatus: StateFlow<MockDeviceStatus> = _deviceStatus.asStateFlow()

    private val _recentCommands = MutableStateFlow(
        listOf(
            MockCommand("1", "OPEN_APP (YouTube)", "completed", "10:42 AM"),
            MockCommand("2", "SYSTEM_TOGGLE (Flashlight)", "completed", "09:15 AM"),
            MockCommand("3", "CALL (John Doe)", "failed", "Yesterday")
        )
    )
    val recentCommands: StateFlow<List<MockCommand>> = _recentCommands.asStateFlow()

    private val _chatMessages = MutableStateFlow(
        listOf(
            MockChatMessage("1", "Hi! I'm your AI assistant. How can I help you today?", false, "Now")
        )
    )
    val chatMessages: StateFlow<List<MockChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    fun clearChat() {
        _chatMessages.value = emptyList()
    }

    private val _favoriteCommands = MutableStateFlow(
        setOf(
            "What is the weather today?",
            "Airplane mode on",
            "Turn on Hotspot"
        )
    )
    val favoriteCommands: StateFlow<Set<String>> = _favoriteCommands.asStateFlow()

    fun toggleFavoriteCommand(command: String) {
        val current = _favoriteCommands.value
        _favoriteCommands.value = if (current.contains(command)) current - command else current + command
    }

    private fun loadUser(): MockUser {
        val userJson = tokenManager.getUser() ?: return MockUser()
        return try {
            val map = gson.fromJson(userJson, Map::class.java)
            MockUser(
                name = map["fullName"] as? String ?: "User",
                email = map["email"] as? String ?: ""
            )
        } catch (_: Exception) {
            MockUser()
        }
    }

    private val intentRouter = com.aimobile.router.IntentRouter(context)

    private val _lastActionFeedback = MutableStateFlow<String?>(null)
    val lastActionFeedback: StateFlow<String?> = _lastActionFeedback.asStateFlow()

    fun clearActionFeedback() {
        _lastActionFeedback.value = null
    }

    fun sendMessage(text: String) {
        val userMsg = MockChatMessage(
            id = System.currentTimeMillis().toString(),
            text = text,
            isUser = true,
            time = "Now"
        )
        _chatMessages.value = _chatMessages.value + userMsg
        _isChatLoading.value = true

        viewModelScope.launch {
            try {
                val localFallback = executeOfflineFallback(text)
                if (!localFallback.contains("couldn't recognize") && !localFallback.contains("Server offline")) {
                    _chatMessages.value = _chatMessages.value + MockChatMessage(
                        id = (System.currentTimeMillis() + 1).toString(),
                        text = localFallback,
                        isUser = false,
                        time = "Now"
                    )
                    _isChatLoading.value = false
                    return@launch
                }

                val response = apiService.sendChat(ChatRequest(command = text))
                val aiText = if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val data = body.data
                    when {
                        data == null -> {
                            _lastActionFeedback.value = "❌ Could not process command"
                            "Sorry, I couldn't understand that."
                        }
                        data.reply != null && data.reply.isNotBlank() -> {
                            val shortReply = data.reply.lines().firstOrNull { it.isNotBlank() } ?: "✅ Done"
                            _lastActionFeedback.value = if (shortReply.length > 35) shortReply.take(35) + "…" else shortReply
                            data.reply
                        }
                        data.intent == "UNKNOWN_COMMAND" -> {
                            val fallback = executeOfflineFallback(text)
                            if (fallback.contains("couldn't recognize") || fallback.contains("Server offline")) {
                                _lastActionFeedback.value = "❌ Command unrecognized"
                                "I didn't understand that command. Try something like:\n• 'Open camera'\n• 'Turn on flashlight'\n• 'Set alarm for 7am'\n• 'What is the weather today?'"
                            } else {
                                fallback
                            }
                        }
                        data.intent != null -> {
                            // Route the successful command locally as well to make it work from Chat screen!
                            val request = com.aimobile.models.CommandRequest(
                                intent = data.intent,
                                number = data.number ?: data.contact,
                                time = data.time,
                                duration = data.duration?.toIntOrNull(),
                                query = data.query,
                                message = data.message ?: data.app ?: data.contact,
                                app = data.app
                            )
                            val res = intentRouter.route(request)
                            val friendlyName = data.intent.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
                            val resultText = if (res.status == "Success") "✅ $friendlyName: ${res.message}" else "❌ ${res.message}"
                            _lastActionFeedback.value = resultText
                            resultText
                        }
                        else -> {
                            _lastActionFeedback.value = "❌ Could not understand"
                            "I didn't understand that. Please try again."
                        }
                    }
                } else {
                    executeOfflineFallback(text)
                }
                _chatMessages.value = _chatMessages.value + MockChatMessage(
                    id = (System.currentTimeMillis() + 1).toString(),
                    text = aiText,
                    isUser = false,
                    time = "Now"
                )
            } catch (e: Exception) {
                val fallbackText = executeOfflineFallback(text)
                _chatMessages.value = _chatMessages.value + MockChatMessage(
                    id = (System.currentTimeMillis() + 1).toString(),
                    text = fallbackText,
                    isUser = false,
                    time = "Now"
                )
            } finally {
                _isChatLoading.value = false
            }
        }
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

    private suspend fun executeOfflineFallback(command: String): String {
        val clean = command.lowercase(java.util.Locale.getDefault()).trim()
        
        // Check for Dynamic User Routines first
        val matchedRoutine = routineRepository.findRoutineByName(clean)
        if (matchedRoutine != null) {
            routineExecutor.executeRoutine(matchedRoutine)
            return "⭐ Executing '${matchedRoutine.name}' Routine..."
        }

        var intent: String = "UNKNOWN_COMMAND"
        var appName: String? = null
        var queryText: String? = null

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

                if (clean.startsWith("open ")) {
                    val openTarget = clean.substring(5).substringBefore(" search ").substringBefore(" and search ").trim()
                    if (openTarget.isNotEmpty() && openTarget != clean.substring(5).trim()) {
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
                queryText = finalQuery
            }
            clean.contains("direction") || (clean.contains("palanpur") && clean.contains("deesa")) -> {
                intent = "GET_DIRECTIONS"
                queryText = command
            }
            clean.contains("screen") || (clean.contains("summarize") && !clean.contains("news")) -> {
                intent = "SUMMARIZE_SCREEN"
                queryText = command
            }
            clean.contains("translate") || clean.contains("ટ્રાન્સલેટ") || clean.contains("અનુવાદ") -> {
                intent = "TRANSLATE_TEXT"
                queryText = command
            }
            matchesKeyword(clean, "flashlight", "fleshlight", "torch") -> {
                if (clean.contains("off") || clean.contains("close") || clean.contains("bandh") || clean.contains("band")) {
                    intent = "FLASHLIGHT_OFF"
                } else {
                    intent = "FLASHLIGHT_ON"
                }
            }
            matchesKeyword(clean, "selfie", "selfy") -> {
                intent = "TAKE_SELFIE"
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
                queryText = if (target.isNotBlank()) target else "Mom"
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
            clean.contains("battery") || clean.contains("battery level") || clean.contains("battery status") || clean.contains("charge") -> {
                intent = "BATTERY_STATUS"
                val result = intentRouter.route(com.aimobile.models.CommandRequest(intent = "BATTERY_STATUS"))
                _lastActionFeedback.value = result.message
                return "🔋 ${result.message}"
            }
            clean.contains("weather") -> {
                intent = "CHECK_WEATHER"
                queryText = command
            }
            clean.contains("morning news") || (clean.contains("news") && clean.contains("morning")) -> {
                _lastActionFeedback.value = "📰 Morning News Briefing"
                return "📰 Morning News Digest:\n1. Tech: Breakthroughs in AI mobile automation\n2. Global: Markets holding steady across indices\n3. Sports: Championship highlights updated\n4. Local: Pleasant weather conditions expected."
            }
            clean.contains("calendar") || clean.contains("tomorrow stats") || (clean.contains("tomorrow") && clean.contains("summarize")) -> {
                _lastActionFeedback.value = "📅 Tomorrow Calendar Briefing"
                return "📅 Tomorrow's Schedule Overview:\n• 09:30 AM - Morning Standup Sync\n• 01:00 PM - Lunch & Tech Discussion\n• 03:30 PM - Client Project Review\n• 06:00 PM - Evening Workout"
            }
            clean.contains("night mode") -> {
                intent = "SET_ALARM"
                queryText = "07:00"
                _lastActionFeedback.value = "🌙 Morning alarm 6 AM set"
            }
            clean.contains("reminder") || clean.contains("remind") || clean.contains("birthday") -> {
                intent = "SET_REMINDER"
                val isBirthday = clean.contains("birthday") || clean.contains("bday")
                val result = intentRouter.route(com.aimobile.models.CommandRequest(
                    intent = "SET_REMINDER",
                    title = command,
                    time = "09:00",
                    repeat = if (isBirthday) "YEARLY" else "NONE"
                ))
                _lastActionFeedback.value = result.message
                return "🎉 ${result.message}"
            }
            clean.contains("alarm") || clean.contains("clock") || clean.contains("alaram") || clean.contains("wake me") -> {
                intent = "SET_ALARM"
                val match = Regex("(\\d+)(?:\\s*:\\s*(\\d+))?\\s*(am|pm)?").find(clean)
                queryText = if (match != null) {
                    var h = match.groupValues[1].toIntOrNull() ?: 7
                    val mStr = match.groupValues[2]
                    val m = if (mStr.isNotEmpty()) mStr.toIntOrNull() ?: 0 else 0
                    val ampm = match.groupValues[3]
                    if (ampm == "pm" && h < 12) h += 12
                    else if (ampm == "am" && h == 12) h = 0
                    String.format("%02d:%02d", h, m)
                } else {
                    "07:00"
                }
            }
            else -> {
                val detectedAppName = findAppNameInText(clean)
                if (clean.contains("turn on ") || clean.contains("turn off ") || clean.contains("toggle ") || clean.contains("enable ") || clean.contains("disable ") || clean.endsWith(" on") || clean.endsWith(" off")) {
                    intent = "TOGGLE_QUICK_SETTING"
                    appName = clean.replace("turn on ", "").replace("turn off ", "").replace("toggle ", "").replace("enable ", "").replace("disable ", "").let { if (it.endsWith(" on")) it.dropLast(3) else if (it.endsWith(" off")) it.dropLast(4) else it }.trim()
                } else if (detectedAppName != null) {
                    intent = "OPEN_APP"
                    appName = detectedAppName
                } else if (clean.startsWith("open ")) {
                    intent = "OPEN_APP"
                    appName = command.substring(5).trim()
                } else {
                    intent = "UNKNOWN_COMMAND"
                }
            }
        }

        if (intent == "UNKNOWN_COMMAND") {
            return "⚠️ Server is offline.\nI couldn't recognize this command locally either."
        }

        val request = com.aimobile.models.CommandRequest(
            intent = intent,
            number = null,
            time = null,
            duration = null,
            query = queryText,
            message = queryText ?: appName,
            app = appName
        )
        
        val routeResult = intentRouter.route(request)
        
        return if (routeResult.status == "Success") {
            _lastActionFeedback.value = "✅ ${routeResult.message}"
            routeResult.message
        } else {
            _lastActionFeedback.value = "⚠️ ${routeResult.message}"
            "⚠️ ${routeResult.message}"
        }
    }

    private fun findAppNameInText(text: String): String? {
        val cleanText = text.lowercase().trim()
        if (cleanText.isEmpty()) return null

        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        val installedApps = resolveInfos.map { it.loadLabel(pm).toString() }
            .filter { it.isNotBlank() }
            .distinct()

        // 1. Direct contains check (e.g. "youtube" in "open youtube and play")
        for (appName in installedApps) {
            val cleanName = appName.lowercase().trim()
            if (cleanName.length > 2 && (cleanText == cleanName || cleanText.contains(cleanName) || cleanName.contains(cleanText))) {
                return appName
            }
        }

        // 2. Word-by-word similarity check for typos (e.g. "spotfy", "camer", "galeri")
        val fillerWords = setOf("open", "launch", "start", "app", "chalu", "kar", "please", "run", "show", "goto", "go", "to", "kari", "de", "the", "and")
        val queryWords = cleanText.split("\\s+".toRegex()).filter { it !in fillerWords && it.length > 2 }

        var bestMatch: String? = null
        var highestSimilarity = 0.0

        for (appName in installedApps) {
            val cleanName = appName.lowercase().trim()
            val appWords = cleanName.split("\\s+".toRegex()).filter { it !in fillerWords && it.length > 2 }

            // Check full app name similarity with query words
            for (qw in queryWords) {
                // Check similarity with full app name
                val similarityFull = getSimilarityScore(qw, cleanName)
                if (similarityFull > highestSimilarity && similarityFull >= 0.7) {
                    highestSimilarity = similarityFull
                    bestMatch = appName
                }

                // Check similarity with individual app label words (e.g. "maps" in "google maps")
                for (aw in appWords) {
                    val similarityWord = getSimilarityScore(qw, aw)
                    if (similarityWord > highestSimilarity && similarityWord >= 0.7) {
                        highestSimilarity = similarityWord
                        bestMatch = appName
                    }
                }
            }
        }

        return bestMatch
    }

    private fun getSimilarityScore(s1: String, s2: String): Double {
        val maxLength = maxOf(s1.length, s2.length)
        if (maxLength == 0) return 1.0
        val len1 = s1.length
        val len2 = s2.length
        val dp = Array(len1 + 1) { IntArray(len2 + 1) }
        for (i in 0..len1) dp[i][0] = i
        for (j in 0..len2) dp[0][j] = j
        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        val distance = dp[len1][len2]
        return (maxLength - distance).toDouble() / maxLength
    }

    fun getServerUrl(): String = tokenManager.getServerUrl()
    fun saveServerUrl(url: String) = tokenManager.saveServerUrl(url)

    fun isOverlayEnabled(): Boolean = tokenManager.isOverlayEnabled()
    fun saveOverlayEnabled(enabled: Boolean) = tokenManager.saveOverlayEnabled(enabled)
}
