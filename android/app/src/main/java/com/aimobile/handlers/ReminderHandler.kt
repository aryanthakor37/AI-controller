package com.aimobile.handlers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.aimobile.models.CommandResult
import com.aimobile.receivers.ReminderReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ReminderHandler(private val context: Context) {

    suspend fun scheduleReminder(
        title: String,
        dateStr: String?,
        timeStr: String?,
        repeat: String?,
        contact: String?
    ): CommandResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val calendar = Calendar.getInstance()
            val timeParts = (timeStr ?: "09:00").split(":")
            val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 9
            val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

            if (!dateStr.isNullOrEmpty()) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val parsedDate = dateFormat.parse(dateStr)
                if (parsedDate != null) {
                    val dateCal = Calendar.getInstance().apply { time = parsedDate }
                    calendar.set(Calendar.YEAR, dateCal.get(Calendar.YEAR))
                    calendar.set(Calendar.MONTH, dateCal.get(Calendar.MONTH))
                    calendar.set(Calendar.DAY_OF_MONTH, dateCal.get(Calendar.DAY_OF_MONTH))
                }
            }

            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            // If time has passed today and no date set, schedule for tomorrow
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                if (repeat == "YEARLY") {
                    calendar.add(Calendar.YEAR, 1)
                } else if (dateStr.isNullOrEmpty()) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            val reminderId = (title + (dateStr ?: "")).hashCode()

            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = "com.aimobile.ACTION_TRIGGER_REMINDER"
                putExtra("title", title)
                putExtra("contact", contact ?: "")
                putExtra("repeat", repeat ?: "NONE")
                putExtra("reminderId", reminderId)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }

            val formattedDate = SimpleDateFormat("dd MMM yyyy 'at' hh:mm a", Locale.getDefault()).format(calendar.time)
            CommandResult(
                status = "Success",
                message = "Reminder set for $title on $formattedDate"
            )
        } catch (e: Exception) {
            CommandResult(
                status = "Failed",
                message = "Could not schedule reminder: ${e.message}"
            )
        }
    }

    fun rescheduleYearly(reminderId: Int, title: String, contact: String) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val calendar = Calendar.getInstance().apply {
                add(Calendar.YEAR, 1)
            }

            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = "com.aimobile.ACTION_TRIGGER_REMINDER"
                putExtra("title", title)
                putExtra("contact", contact)
                putExtra("repeat", "YEARLY")
                putExtra("reminderId", reminderId)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
