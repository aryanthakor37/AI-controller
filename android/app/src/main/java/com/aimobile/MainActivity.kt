package com.aimobile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.aimobile.ui.navigation.AppNavigation
import com.aimobile.ui.theme.AIMobileTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle results if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Allow screen wake-up & lock screen dismissal when executing background voice commands
        com.aimobile.utils.UnlockHelper.configureActivityLockScreenFlags(this)
        
        // Prevent screen from turning off
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        requestRequiredPermissions()

        setContent {
            AIMobileTheme {
                AppNavigation()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Start persistent foreground service if user is logged in / paired
        val tokenManager = com.aimobile.utils.TokenManager(this)
        if (tokenManager.getToken() != null) {
            // Check display over other apps overlay permission if enabled
            if (tokenManager.isOverlayEnabled()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(this)) {
                    try {
                        val intent = Intent(
                            android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            android.net.Uri.parse("package:$packageName")
                        )
                        startActivity(intent)
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "Failed to request overlay permission: ${e.message}")
                    }
                } else {
                    val overlayIntent = Intent(this, com.aimobile.services.FloatingOverlayService::class.java)
                    try {
                        startService(overlayIntent)
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "Failed to start FloatingOverlayService: ${e.message}")
                    }
                }
            } else {
                val overlayIntent = Intent(this, com.aimobile.services.FloatingOverlayService::class.java)
                try {
                    stopService(overlayIntent)
                } catch (e: Exception) {
                    // Ignore
                }
            }

            val serviceIntent = Intent(this, com.aimobile.services.MainService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Failed to start MainService: ${e.message}")
            }
        }
    }

    private fun requestRequiredPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) {
            requestPermissionLauncher.launch(notGranted.toTypedArray())
        }
    }
}
