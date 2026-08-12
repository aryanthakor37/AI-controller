package com.aimobile.services

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.aimobile.accessibility.MyAccessibilityService
import com.aimobile.managers.ConnectionManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ClipboardSyncActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clipData = clipboard?.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val text = clipData.getItemAt(0).text?.toString() ?: ""
                if (text.isNotEmpty()) {
                    MyAccessibilityService.lastCopiedText = text
                    ConnectionManager.instance?.onPhoneClipboardChanged(text)
                    Log.d("ClipboardSyncActivity", "Successfully read phone clipboard: $text")
                }
            }
        } catch (e: Throwable) {
            Log.e("ClipboardSyncActivity", "Failed to read phone clipboard", e)
        }
        
        finish()
    }
}
