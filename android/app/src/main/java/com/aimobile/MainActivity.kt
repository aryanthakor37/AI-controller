package com.aimobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.aimobile.socket.SocketManager
import com.aimobile.ui.navigation.AppNavigation
import com.aimobile.ui.theme.AIMobileTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private lateinit var socketManager: SocketManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Prevent screen from turning off
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        socketManager = SocketManager(this)

        setContent {
            AIMobileTheme {
                AppNavigation()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::socketManager.isInitialized) {
            socketManager.connect()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        socketManager.disconnect()
    }
}
