package com.aimobile.handlers

import android.content.Context
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
        } catch (e: SecurityException) {
            CommandResult(status = "Permission Denied", message = "SEND_SMS permission is required")
        } catch (e: Exception) {
            CommandResult(status = "Failed", message = "Could not send SMS: ${e.message}")
        }
    }
}
