package com.aimobile.utils

import android.util.Log

object AiLogger {

    private const val TAG = "AiMobileAgent"

    fun logServiceStart(serviceName: String) {
        Log.i(TAG, "🟢 [SERVICE_START] Service started: $serviceName | Time: ${System.currentTimeMillis()}")
    }

    fun logServiceStop(serviceName: String) {
        Log.i(TAG, "🔴 [SERVICE_STOP] Service stopped: $serviceName | Time: ${System.currentTimeMillis()}")
    }

    fun logSocketReconnect(attempt: Int, status: String) {
        Log.d(TAG, "🔄 [SOCKET_RECONNECT] Reconnect attempt #$attempt | Status: $status")
    }

    fun logCommandExecuted(intentName: String, status: String, executionTimeMs: Long) {
        Log.i(TAG, "⚡ [COMMAND_EXEC] Intent: $intentName | Status: $status | Duration: ${executionTimeMs}ms")
    }

    fun logWarning(tag: String, message: String) {
        Log.w(TAG, "⚠️ [$tag] $message")
    }

    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(TAG, "❌ [$tag] $message", throwable)
    }
}
