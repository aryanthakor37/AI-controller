package com.aimobile.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.aimobile.R

class QuickCommandsWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_quick_commands)

        // 1. Camera
        views.setOnClickPendingIntent(
            R.id.btn_cmd_camera,
            createCommandPendingIntent(context, 201, "OPEN_CAMERA")
        )

        // 2. Flashlight
        views.setOnClickPendingIntent(
            R.id.btn_cmd_torch,
            createCommandPendingIntent(context, 202, "FLASHLIGHT_ON")
        )

        // 3. Wi-Fi
        views.setOnClickPendingIntent(
            R.id.btn_cmd_wifi,
            createCommandPendingIntent(context, 203, "OPEN_APP", appName = "Wifi")
        )

        // 4. Bluetooth
        views.setOnClickPendingIntent(
            R.id.btn_cmd_bluetooth,
            createCommandPendingIntent(context, 204, "OPEN_APP", appName = "Bluetooth")
        )

        // 5. Settings
        views.setOnClickPendingIntent(
            R.id.btn_cmd_settings,
            createCommandPendingIntent(context, 205, "OPEN_APP", appName = "Settings")
        )

        // 6. Voice Assistant
        val voiceIntent = Intent(context, WidgetCommandReceiver::class.java).apply {
            action = WidgetCommandReceiver.ACTION_LAUNCH_APP
            putExtra(WidgetCommandReceiver.EXTRA_ROUTE, "voice")
        }
        val voicePending = PendingIntent.getBroadcast(
            context,
            206,
            voiceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_cmd_voice, voicePending)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun createCommandPendingIntent(
        context: Context,
        requestCode: Int,
        intentName: String,
        appName: String? = null
    ): PendingIntent {
        val intent = Intent(context, WidgetCommandReceiver::class.java).apply {
            action = WidgetCommandReceiver.ACTION_EXECUTE_COMMAND
            putExtra(WidgetCommandReceiver.EXTRA_INTENT, intentName)
            if (appName != null) putExtra(WidgetCommandReceiver.EXTRA_APP, appName)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
