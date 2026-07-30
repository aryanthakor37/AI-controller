package com.aimobile.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aimobile.MainActivity
import com.aimobile.models.CommandRequest
import com.aimobile.router.IntentRouter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WidgetCommandReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_EXECUTE_COMMAND = "com.aimobile.widget.ACTION_EXECUTE_COMMAND"
        const val ACTION_LAUNCH_APP = "com.aimobile.widget.ACTION_LAUNCH_APP"
        
        const val EXTRA_INTENT = "extra_intent"
        const val EXTRA_APP = "extra_app"
        const val EXTRA_QUERY = "extra_query"
        const val EXTRA_ROUTE = "extra_route"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_EXECUTE_COMMAND -> {
                val intentName = intent.getStringExtra(EXTRA_INTENT) ?: return
                val appName = intent.getStringExtra(EXTRA_APP)
                val queryText = intent.getStringExtra(EXTRA_QUERY)

                val request = CommandRequest(
                    intent = intentName,
                    app = appName,
                    query = queryText,
                    message = queryText ?: appName
                )

                // Log command to recent commands widget
                val friendlyName = when (intentName) {
                    "FLASHLIGHT_ON" -> "Turn on Flashlight"
                    "OPEN_CAMERA" -> "Open Camera"
                    "OPEN_MAPS" -> "Open Google Maps"
                    "CHECK_WEATHER" -> "Check Weather"
                    "MUTE_VOLUME" -> "Mute Volume"
                    "OPEN_APP" -> "Open ${appName ?: "App"}"
                    else -> intentName.replace('_', ' ').lowercase().capitalize()
                }
                WidgetHelper.logRecentCommand(context, friendlyName)

                // Route via existing IntentRouter
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val router = IntentRouter(context)
                        router.route(request)
                    } catch (e: Exception) {
                        android.util.Log.e("WidgetReceiver", "Failed to route widget command: ${e.message}")
                    } finally {
                        WidgetHelper.updateAllWidgets(context)
                        pendingResult.finish()
                    }
                }
            }

            ACTION_LAUNCH_APP -> {
                val route = intent.getStringExtra(EXTRA_ROUTE) ?: "dashboard"
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra("navigate_route", route)
                }
                context.startActivity(launchIntent)
            }
        }
    }
}
