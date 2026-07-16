package com.aimobile.handlers

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import com.aimobile.models.CommandResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AlarmHandler(private val context: Context) {

    suspend fun setAlarm(hour: Int, minute: Int): CommandResult = withContext(Dispatchers.Main) {
        return@withContext try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            CommandResult(status = "Success", message = "Alarm set for $hour:$minute")
        } catch (e: Exception) {
            CommandResult(status = "Failed", message = "Could not set alarm: ${e.message}")
        }
    }

    suspend fun setTimer(seconds: Int): CommandResult = withContext(Dispatchers.Main) {
        return@withContext try {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            CommandResult(status = "Success", message = "Timer set for $seconds seconds")
        } catch (e: Exception) {
            CommandResult(status = "Failed", message = "Could not set timer: ${e.message}")
        }
    }
}
