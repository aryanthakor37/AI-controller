package com.aimobile.managers

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import java.net.URISyntaxException
import javax.inject.Inject
import javax.inject.Singleton
import com.aimobile.utils.TokenManager

@Singleton
class ConnectionManager @Inject constructor(
    private val deviceInfoManager: DeviceInfoManager,
    private val tokenManager: TokenManager
) {
    private var socket: Socket? = null
    
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
            options.query = "token=$token"
            
            // Point to Cloudflare URL
            socket = IO.socket("https://initiative-equations-pix-kept.trycloudflare.com", options)
            
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

            socket?.connect()
        } catch (e: URISyntaxException) {
            Log.e("ConnectionManager", "Socket URL error", e)
        }
    }

    fun disconnect() {
        socket?.disconnect()
    }

    fun isConnected(): Boolean = socket?.connected() ?: false
}
