package com.aimobile.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.aimobile.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DeviceStatusWidgetProvider : AppWidgetProvider() {

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
        val views = RemoteViews(context.packageName, R.layout.widget_device_status)

        val timeStr = SimpleDateFormat("hh:mm a", Locale.US).format(Date())
        val (battery, isCharging) = WidgetHelper.getBatteryStatus(context)
        val chargingIcon = if (isCharging) "⚡ " else "🔋 "

        views.setTextViewText(R.id.widget_time, timeStr)
        views.setTextViewText(R.id.widget_battery, "$chargingIcon$battery%")
        views.setTextViewText(R.id.widget_wifi_status, "📶 WiFi: Active")
        views.setTextViewText(R.id.widget_bt_status, "ᛡ BT: Active")

        // Clicking widget launches dashboard
        val dashboardIntent = Intent(context, WidgetCommandReceiver::class.java).apply {
            action = WidgetCommandReceiver.ACTION_LAUNCH_APP
            putExtra(WidgetCommandReceiver.EXTRA_ROUTE, "dashboard")
        }
        val pending = PendingIntent.getBroadcast(
            context,
            401,
            dashboardIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pending)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
