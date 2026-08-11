package com.aimobile.managers

import android.content.Context
import android.util.Log
import com.aimobile.command.CommandDispatcher
import com.aimobile.services.ScreenCaptureManager
import com.aimobile.utils.TokenManager
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import java.net.URISyntaxException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionManager @Inject constructor(
    private val deviceInfoManager: DeviceInfoManager,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context,
    private val commandDispatcher: CommandDispatcher,
    private val screenCaptureManager: ScreenCaptureManager
) {
    private var socket: Socket? = null
    private val gson = Gson()
    
    companion object {
        var instance: ConnectionManager? = null
            private set
    }
    
    init {
        instance = this
        screenCaptureManager.onFrameAvailable = { base64Frame ->
            val payload = org.json.JSONObject()
            payload.put("frame", base64Frame)
            socket?.emit("device:screen_frame", payload)
        }
        screenCaptureManager.onError = { errorMsg ->
            val payload = org.json.JSONObject()
            payload.put("error", errorMsg)
            socket?.emit("device:screen_frame_error", payload)
        }
    }
    
    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState

    fun connect() {
        if (socket?.connected() == true) return

        try {
            val token = tokenManager.getToken()
            if (token == null) {
                Log.e("ConnectionManager", "Cannot connect to socket without a token")
                return
            }
            val options = IO.Options()
            options.transports = arrayOf(io.socket.engineio.client.transports.WebSocket.NAME)
            options.query = "token=$token"
            
            // Fetch dynamically stored server URL
            val serverUrl = tokenManager.getServerUrl().removeSuffix("/")
            Log.d("ConnectionManager", "Connecting to socket URL: $serverUrl")
            socket = IO.socket(serverUrl, options)
            
            socket?.on(Socket.EVENT_CONNECT) {
                Log.d("ConnectionManager", "Socket Connected")
                _connectionState.value = true
                
                // Emitting Registration
                val payload = deviceInfoManager.getDeviceRegistrationPayload()
                socket?.emit("device:register", payload)
            }
            
            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d("ConnectionManager", "Socket Disconnected")
                _connectionState.value = false
            }

            socket?.on("device:ping") { args ->
                if (args.isNotEmpty()) {
                    val data = args[0] as JSONObject
                    val timestamp = data.optLong("timestamp", 0)
                    
                    val response = JSONObject()
                    response.put("timestamp", timestamp)
                    socket?.emit("device:heartbeat", response)
                    Log.d("ConnectionManager", "Heartbeat acknowledged")
                }
            }

            socket?.on("command:execute") { args ->
                if (args.isNotEmpty()) {
                    val rawJson = args[0].toString()
                    
                    // Show a visual toast so we know it arrived!
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        android.widget.Toast.makeText(context, "Command Received: $rawJson", android.widget.Toast.LENGTH_LONG).show()
                    }

                    commandDispatcher.dispatchCommand(rawJson) { result ->
                        // Send result back to server
                        val resultJson = JSONObject(gson.toJson(result))
                        
                        if (result.data?.startsWith("SCREENSHOT:") == true) {
                            val payload = JSONObject()
                            payload.put("image", result.data.substring(11))
                            socket?.emit("device:screenshot_result", payload)
                            resultJson.put("data", "Screenshot sent to Dashboard")
                        }
                        
                        socket?.emit("command:result", resultJson)
                    }
                }
            }

            socket?.on("device:perform_gesture") { args ->
                if (args.isNotEmpty()) {
                    try {
                        val gestureObj = args[0] as JSONObject
                        val startX = gestureObj.getDouble("startX").toFloat()
                        val startY = gestureObj.getDouble("startY").toFloat()
                        val endX = gestureObj.getDouble("endX").toFloat()
                        val endY = gestureObj.getDouble("endY").toFloat()
                        val duration = gestureObj.getLong("durationMs")
                        
                        com.aimobile.accessibility.MyAccessibilityService.instance?.performGesture(
                            startX, startY, endX, endY, duration
                        )
                    } catch (e: Exception) {
                        Log.e("ConnectionManager", "Error performing gesture", e)
                    }
                }
            }

            socket?.on("device:inject_text") { args ->
                if (args.isNotEmpty()) {
                    try {
                        val payload = args[0] as JSONObject
                        val text = payload.getString("text")
                        com.aimobile.accessibility.MyAccessibilityService.instance?.injectTextToFocusedNode(text)
                    } catch (e: Exception) {
                        Log.e("ConnectionManager", "Error injecting text", e)
                    }
                }
            }

            socket?.connect()
        } catch (e: URISyntaxException) {
            Log.e("ConnectionManager", "Socket URL error", e)
        }
    }

    fun stopScreenStream() {
        screenCaptureManager.stopStream()
    }

    fun disconnect() {
        socket?.disconnect()
        _connectionState.value = false
    }

    fun isConnected(): Boolean = socket?.connected() ?: false
}
