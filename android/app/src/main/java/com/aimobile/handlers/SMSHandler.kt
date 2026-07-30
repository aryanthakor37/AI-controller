package com.aimobile.handlers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import com.aimobile.models.CommandResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SMSHandler(private val context: Context) {

    suspend fun sendSMS(number: String, message: String): CommandResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val smsManager: SmsManager = context.getSystemService(SmsManager::class.java)
            smsManager.sendTextMessage(number, null, message, null, null)
            CommandResult(status = "Success", message = "SMS sent to $number")
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:$number")
                    putExtra("sms_body", message)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                CommandResult(status = "Success", message = "Opened Messages App for $number")
            } catch (e2: Exception) {
                CommandResult(status = "Permission Denied", message = "SMS permission or app required")
            }
        }
    }
}
