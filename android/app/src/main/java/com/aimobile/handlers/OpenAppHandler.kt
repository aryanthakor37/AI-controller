package com.aimobile.handlers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import com.aimobile.models.CommandResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OpenAppHandler(private val context: Context) {

    suspend fun openApp(intentName: String): CommandResult = withContext(Dispatchers.Main) {
        try {
            val intent = when (intentName) {
                "OPEN_CHROME" -> context.packageManager.getLaunchIntentForPackage("com.android.chrome")
                "OPEN_CAMERA" -> getCameraIntent()
                "OPEN_YOUTUBE" -> context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
                "OPEN_MAPS" -> context.packageManager.getLaunchIntentForPackage("com.google.android.apps.maps")
                "OPEN_GMAIL" -> context.packageManager.getLaunchIntentForPackage("com.google.android.gm")
                "OPEN_SETTINGS" -> Intent(Settings.ACTION_SETTINGS)
                "OPEN_CONTACTS" -> Intent(Intent.ACTION_VIEW).apply { type = "vnd.android.cursor.dir/contact" }
                "OPEN_DIALER" -> Intent(Intent.ACTION_DIAL)
                "OPEN_PLAY_STORE" -> Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=")) // Or just launch package com.android.vending
                "OPEN_CLOCK" -> Intent(AlarmClock.ACTION_SHOW_ALARMS)
                "OPEN_CALCULATOR" -> getCalculatorIntent()
                "OPEN_GALLERY" -> Intent(Intent.ACTION_VIEW).apply { type = "image/*" }
                "OPEN_FILES" -> Intent(Intent.ACTION_GET_CONTENT).apply { type = "*/*" }
                "OPEN_PHOTOS" -> context.packageManager.getLaunchIntentForPackage("com.google.android.apps.photos")
                "OPEN_WHATSAPP" -> context.packageManager.getLaunchIntentForPackage("com.whatsapp")
                else -> null
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                CommandResult(status = "Success", message = "Opened $intentName")
            } else {
                CommandResult(status = "App Not Installed", message = "Could not find app for $intentName")
            }
        } catch (e: Exception) {
            CommandResult(status = "Failed", message = "Error opening app: ${e.message}")
        }
    }

    private fun getCalculatorIntent(): Intent? {
        val packages = listOf(
            "com.google.android.calculator",
            "com.android.calculator2",
            "com.sec.android.app.popupcalculator", // Samsung
            "com.miui.calculator", // Xiaomi
            "com.coloros.calculator", // Oppo/Vivo/Realme
            "com.vivo.calculator" // Vivo
        )
        for (pkg in packages) {
            val intent = context.packageManager.getLaunchIntentForPackage(pkg)
            if (intent != null) return intent
        }
        return null
    }

    private fun getCameraIntent(): Intent? {
        // Try the standard intent first
        var intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
        if (intent.resolveActivity(context.packageManager) != null) {
            return intent
        }
        
        // Fallback to specific packages (Vivo, Oppo, standard Android)
        val packages = listOf(
            "com.android.camera",
            "com.android.camera2",
            "com.sec.android.app.camera", // Samsung
            "com.vivo.camera", // Vivo specific
            "com.coloros.camera" // Oppo/Vivo
        )
        for (pkg in packages) {
            val pkgIntent = context.packageManager.getLaunchIntentForPackage(pkg)
            if (pkgIntent != null) return pkgIntent
        }
        
        // Final fallback: just open the default camera by action without checking resolution
        return Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
    }
}
