package com.aimobile.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.aimobile.R

class RecentCommandsWidgetProvider : AppWidgetProvider() {

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
        val views = RemoteViews(context.packageName, R.layout.widget_recent_commands)
        val recentCmds = WidgetHelper.getRecentCommands(context)

        val cmd1 = recentCmds.getOrNull(0) ?: "Turn on Flashlight"
        val cmd2 = recentCmds.getOrNull(1) ?: "Open Camera"
        val cmd3 = recentCmds.getOrNull(2) ?: "Check Weather"

        views.setTextViewText(R.id.recent_cmd_1, "⚡ 1. $cmd1")
        views.setTextViewText(R.id.recent_cmd_2, "⚡ 2. $cmd2")
        views.setTextViewText(R.id.recent_cmd_3, "⚡ 3. $cmd3")

        // Wire click pending intents to re-execute the exact command
        views.setOnClickPendingIntent(
            R.id.recent_cmd_1,
            createReExecutePendingIntent(context, 501, cmd1)
        )

        views.setOnClickPendingIntent(
            R.id.recent_cmd_2,
            createReExecutePendingIntent(context, 502, cmd2)
        )

        views.setOnClickPendingIntent(
            R.id.recent_cmd_3,
            createReExecutePendingIntent(context, 503, cmd3)
        )

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun createReExecutePendingIntent(
        context: Context,
        requestCode: Int,
        commandText: String
    ): PendingIntent {
        val clean = commandText.lowercase()
        val (intentName, appName) = when {
            clean.contains("flashlight") || clean.contains("torch") -> Pair("FLASHLIGHT_ON", null)
            clean.contains("camera") -> Pair("OPEN_CAMERA", null)
            clean.contains("weather") -> Pair("CHECK_WEATHER", null)
            clean.contains("map") -> Pair("OPEN_MAPS", null)
            clean.contains("spotify") -> Pair("OPEN_APP", "Spotify")
            clean.contains("wifi") -> Pair("OPEN_APP", "Wifi")
            clean.contains("bluetooth") -> Pair("OPEN_APP", "Bluetooth")
            else -> Pair("OPEN_APP", commandText)
        }

        val intent = Intent(context, WidgetCommandReceiver::class.java).apply {
            action = WidgetCommandReceiver.ACTION_EXECUTE_COMMAND
            putExtra(WidgetCommandReceiver.EXTRA_INTENT, intentName)
            if (appName != null) putExtra(WidgetCommandReceiver.EXTRA_APP, appName)
            putExtra(WidgetCommandReceiver.EXTRA_QUERY, commandText)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
