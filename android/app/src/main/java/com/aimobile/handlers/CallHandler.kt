package com.aimobile.handlers

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.aimobile.models.CommandResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CallHandler(private val context: Context) {

    suspend fun callNumber(number: String): CommandResult = withContext(Dispatchers.Main) {
        return@withContext try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$number")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            CommandResult(status = "Success", message = "Calling $number")
        } catch (e: SecurityException) {
            CommandResult(status = "Permission Denied", message = "CALL_PHONE permission is required")
        } catch (e: Exception) {
            CommandResult(status = "Failed", message = "Could not place call: ${e.message}")
        }
    }
}
