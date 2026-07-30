package com.aimobile.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.aimobile.services.MainService
import com.aimobile.utils.AiLogger
import com.aimobile.utils.TokenManager

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED || action == "android.intent.action.QUICKBOOT_POWERON" || action == "android.intent.action.LOCKED_BOOT_COMPLETED") {
            AiLogger.logServiceStart("BootCompletedReceiver ($action)")
            val serviceIntent = Intent(context, MainService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } catch (e: Exception) {
                AiLogger.logError("BootReceiver", "Failed to start MainService on boot", e)
            }
        }
    }
}
