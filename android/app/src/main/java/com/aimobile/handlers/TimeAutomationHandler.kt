package com.aimobile.handlers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.aimobile.models.CommandResult
import java.util.Calendar

class TimeAutomationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val command = intent.getStringExtra("COMMAND_TO_RUN") ?: return
        Log.d("TimeAutomation", "Executing scheduled command: $command")
        android.widget.Toast.makeText(context, "⏰ AI Routine Executing:\n$command", android.widget.Toast.LENGTH_LONG).show()
        
        // Let MacroExecutor handle the actual logic if app is in background
        try {
            val macroIntent = Intent(context, com.aimobile.services.FloatingOverlayService::class.java).apply {
                action = "RUN_MACRO"
                putExtra("command", command)
            }
            context.startService(macroIntent)
        } catch (e: Exception) {
            Log.e("TimeAutomation", "Failed to start macro service: ${e.message}")
        }
    }
}

class TimeAutomationHandler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleRoutine(hour: Int, minute: Int, command: String): CommandResult {
        try {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }

            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val intent = Intent(context, TimeAutomationReceiver::class.java).apply {
                putExtra("COMMAND_TO_RUN", command)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                command.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }

            val timeStr = String.format("%02d:%02d", hour, minute)
            return CommandResult(
                status = "Success",
                message = "⏰ Scheduled routine '$command' for $timeStr."
            )
        } catch (e: Exception) {
            return CommandResult("Failed", "Could not schedule routine: ${e.message}")
        }
    }
}
