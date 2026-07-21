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
            var targetNumber = number
            
            // Try to resolve contact name to number if it contains letters
            if (number.any { it.isLetter() }) {
                val resolvedNum = resolveContactName(number)
                if (resolvedNum != null) {
                    targetNumber = resolvedNum
                } else {
                    return@withContext CommandResult(status = "Failed", message = "Could not find contact: $number")
                }
            }

            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$targetNumber")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            CommandResult(status = "Success", message = "Calling $targetNumber")
        } catch (e: SecurityException) {
            CommandResult(status = "Permission Denied", message = "CALL_PHONE permission is required")
        } catch (e: Exception) {
            CommandResult(status = "Failed", message = "Could not place call: ${e.message}")
        }
    }

    private fun resolveContactName(name: String): String? {
        try {
            val uri = android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val projection = arrayOf(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
            val selection = "${android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
            val selectionArgs = arrayOf("%$name%")
            val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            
            cursor?.use {
                if (it.moveToFirst()) {
                    val numIndex = it.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
                    if (numIndex >= 0) {
                        return it.getString(numIndex).replace(" ", "").replace("-", "")
                    }
                }
            }
        } catch (e: SecurityException) {
            // No permission
        } catch (e: Exception) {
            // Error querying
        }
        return null
    }
}
