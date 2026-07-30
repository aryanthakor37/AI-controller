package com.aimobile.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.aimobile.R

class AiAssistantWidgetProvider : AppWidgetProvider() {

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
        val views = RemoteViews(context.packageName, R.layout.widget_ai_assistant)

        val greeting = WidgetHelper.getGreeting()
        val (battery, isCharging) = WidgetHelper.getBatteryStatus(context)
        val chargingIcon = if (isCharging) "⚡ " else "🔋 "
        
        views.setTextViewText(R.id.widget_greeting, greeting)
        views.setTextViewText(R.id.widget_status, "AI Agent • $chargingIcon$battery%")

        // Open AI Chat Pending Intent
        val chatIntent = Intent(context, WidgetCommandReceiver::class.java).apply {
            action = WidgetCommandReceiver.ACTION_LAUNCH_APP
            putExtra(WidgetCommandReceiver.EXTRA_ROUTE, "ai_chat")
        }
        val chatPending = PendingIntent.getBroadcast(
            context,
            101,
            chatIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_open_chat, chatPending)

        // Open Voice Assistant Pending Intent
        val voiceIntent = Intent(context, WidgetCommandReceiver::class.java).apply {
            action = WidgetCommandReceiver.ACTION_LAUNCH_APP
            putExtra(WidgetCommandReceiver.EXTRA_ROUTE, "voice")
        }
        val voicePending = PendingIntent.getBroadcast(
            context,
            102,
            voiceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_open_voice, voicePending)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
