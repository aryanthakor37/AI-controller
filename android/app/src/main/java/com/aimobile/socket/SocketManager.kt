package com.aimobile.socket

import android.content.Context
import android.util.Log
import com.aimobile.command.CommandDispatcher
import com.aimobile.utils.TokenManager
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.util.Collections

class SocketManager(private val context: Context) {

    private var socket: Socket? = null
    private val commandDispatcher = CommandDispatcher(context)
    private val tokenManager = TokenManager(context)
    private val gson = Gson()

    fun connect(serverUrl: String = "http://10.47.197.129:5000") {
        if (socket?.connected() == true) return
        try {
            val token = tokenManager.getToken()
            if (token == null) {
                Log.e("SocketManager", "Cannot connect to socket without a token")
                return
            }
            
            val options = IO.Options()
            options.transports = arrayOf(io.socket.engineio.client.transports.Polling.NAME)
            options.auth = Collections.singletonMap("token", token)
            
            socket = IO.socket(serverUrl, options)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d("SocketManager", "Connected to Backend")
                
                // Register device
                val deviceData = JSONObject().apply {
                    put("deviceId", "android_device_1")
                    put("model", android.os.Build.MODEL)
                }
                socket?.emit("device:register", deviceData)
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

            // Start client-side ping to keep NAT/Hotspot connection alive
            Thread {
                while (true) {
                    try {
                        Thread.sleep(10000)
                        if (socket?.connected() == true) {
                            val pingData = JSONObject().apply { put("timestamp", System.currentTimeMillis()) }
                            socket?.emit("device:heartbeat", pingData)
                        }
                    } catch (e: Exception) {
                        break
                    }
                }
            }.start()

            socket?.connect()
        } catch (e: Exception) {
            Log.e("SocketManager", "Socket connection failed: \${e.message}")
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
    }
}
