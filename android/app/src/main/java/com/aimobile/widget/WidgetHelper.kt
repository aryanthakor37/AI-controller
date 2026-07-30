package com.aimobile.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import java.util.Calendar

object WidgetHelper {

    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good Morning ☀️"
            in 12..16 -> "Good Afternoon 🌤️"
            in 17..21 -> "Good Evening 🌙"
            else -> "Late Night AI 🌃"
        }
    }

    fun getBatteryStatus(context: Context): Pair<Int, Boolean> {
        return try {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 85
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
            val pct = (level * 100 / scale.toFloat()).toInt()
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            Pair(pct, isCharging)
        } catch (_: Exception) {
            Pair(85, false)
        }
    }

    fun getRecentCommands(context: Context): List<String> {
        val prefs = context.getSharedPreferences("aimobile_widget_prefs", Context.MODE_PRIVATE)
        val jsonStr = prefs.getString("recent_cmds", null) ?: return listOf(
            "Turn on Flashlight",
            "Open Camera",
            "Check Weather"
        )
        return try {
            jsonStr.split("|||").filter { it.isNotBlank() }.take(3)
        } catch (_: Exception) {
            listOf("Turn on Flashlight", "Open Camera", "Check Weather")
        }
    }

    fun logRecentCommand(context: Context, command: String) {
        try {
            val list = getRecentCommands(context).toMutableList()
            list.remove(command)
            list.add(0, command)
            val top3 = list.take(3)
            val prefs = context.getSharedPreferences("aimobile_widget_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("recent_cmds", top3.joinToString("|||")).apply()
        } catch (_: Exception) {}
    }

    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        
        val aiProviders = appWidgetManager.getAppWidgetIds(ComponentName(context, AiAssistantWidgetProvider::class.java))
        if (aiProviders.isNotEmpty()) {
            val intent = Intent(context, AiAssistantWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, aiProviders)
            }
            context.sendBroadcast(intent)
        }

        val qcProviders = appWidgetManager.getAppWidgetIds(ComponentName(context, QuickCommandsWidgetProvider::class.java))
        if (qcProviders.isNotEmpty()) {
            val intent = Intent(context, QuickCommandsWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, qcProviders)
            }
            context.sendBroadcast(intent)
        }

        val voiceProviders = appWidgetManager.getAppWidgetIds(ComponentName(context, VoiceWidgetProvider::class.java))
        if (voiceProviders.isNotEmpty()) {
            val intent = Intent(context, VoiceWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, voiceProviders)
            }
            context.sendBroadcast(intent)
        }

        val deviceProviders = appWidgetManager.getAppWidgetIds(ComponentName(context, DeviceStatusWidgetProvider::class.java))
        if (deviceProviders.isNotEmpty()) {
            val intent = Intent(context, DeviceStatusWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, deviceProviders)
            }
            context.sendBroadcast(intent)
        }

        val recentProviders = appWidgetManager.getAppWidgetIds(ComponentName(context, RecentCommandsWidgetProvider::class.java))
        if (recentProviders.isNotEmpty()) {
            val intent = Intent(context, RecentCommandsWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, recentProviders)
            }
            context.sendBroadcast(intent)
        }
    }
}
