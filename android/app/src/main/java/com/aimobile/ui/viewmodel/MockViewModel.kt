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
    private val apiService: ApiService
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
                val response = apiService.sendChat(ChatRequest(command = text))
                val aiText = if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val data = body.data
                    when {
                        data == null -> "Sorry, I couldn't understand that."
                        data.reply != null && data.reply.isNotBlank() -> data.reply
                        data.intent == "UNKNOWN_COMMAND" ->
                            "I didn't understand that command. Try something like:\n• 'Open camera'\n• 'Turn on flashlight'\n• 'Set alarm for 7am'\n• 'What's the battery level?'"
                        data.intent != null -> {
                            // Route the successful command locally as well to make it work from Chat screen!
                            val request = com.aimobile.models.CommandRequest(
                                intent = data.intent,
                                number = null,
                                time = null,
                                duration = null,
                                query = null,
                                message = data.message ?: data.app
                            )
                            intentRouter.route(request)
                            "✅ Command sent & executed: ${data.intent.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }}"
                        }
                        else -> "I didn't understand that. Please try again."
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

    private suspend fun executeOfflineFallback(command: String): String {
        val clean = command.lowercase(java.util.Locale.getDefault()).trim()
        
        val intent: String
        var appName: String? = null

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
            return "⚠️ Server is offline.\nI couldn't recognize this command locally either."
        }

        val request = com.aimobile.models.CommandRequest(
            intent = intent,
            number = null,
            time = null,
            duration = null,
            query = null,
            message = appName
        )
        
        val routeResult = intentRouter.route(request)
        
        return if (routeResult.status == "Success") {
            val friendlyName = intent.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
            "⚠️ Server is offline.\n⚡ Executed locally: $friendlyName"
        } else {
            "⚠️ Server offline.\nFailed to execute locally: ${routeResult.message}"
        }
    }
}
