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
        // Make activity completely invisible / transparent
        window.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            try {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                val clipData = clipboard?.primaryClip
                if (clipData != null && clipData.itemCount > 0) {
                    val text = clipData.getItemAt(0).text?.toString() ?: ""
                    if (text.isNotEmpty()) {
                        MyAccessibilityService.lastCopiedText = text
                        ConnectionManager.instance?.onPhoneClipboardChanged(text)
                        Log.d("ClipboardSyncActivity", "Successfully read phone clipboard on focus: $text")
                    }
                }
            } catch (e: Throwable) {
                Log.e("ClipboardSyncActivity", "Failed to read phone clipboard on focus", e)
            }
            finish()
        }
    }
}
