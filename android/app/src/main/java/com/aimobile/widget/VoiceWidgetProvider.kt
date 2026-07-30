package com.aimobile.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.aimobile.R

class VoiceWidgetProvider : AppWidgetProvider() {

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
        val views = RemoteViews(context.packageName, R.layout.widget_voice)

        // Single tap or long press launches Voice Assistant screen directly
        val voiceIntent = Intent(context, WidgetCommandReceiver::class.java).apply {
            action = WidgetCommandReceiver.ACTION_LAUNCH_APP
            putExtra(WidgetCommandReceiver.EXTRA_ROUTE, "voice")
        }
        val voicePending = PendingIntent.getBroadcast(
            context,
            301,
            voiceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_voice_mic, voicePending)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
