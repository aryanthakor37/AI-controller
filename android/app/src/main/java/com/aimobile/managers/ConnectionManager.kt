package com.aimobile.managers

import android.content.Context
import android.util.Log
import com.aimobile.command.CommandDispatcher
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
    @ApplicationContext private val context: Context
) {
    private var socket: Socket? = null
    private val commandDispatcher = CommandDispatcher(context)
    private val gson = Gson()
    
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
                        socket?.emit("command:result", resultJson)
                    }
                }
            }

            socket?.connect()
        } catch (e: URISyntaxException) {
            Log.e("ConnectionManager", "Socket URL error", e)
        }
    }

    fun disconnect() {
        socket?.disconnect()
        _connectionState.value = false
    }

    fun isConnected(): Boolean = socket?.connected() ?: false
}
