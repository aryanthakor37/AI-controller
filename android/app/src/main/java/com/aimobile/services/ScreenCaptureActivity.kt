package com.aimobile.services

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ScreenCaptureActivity : ComponentActivity() {

    @Inject
    lateinit var screenCaptureManager: ScreenCaptureManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(android.graphics.Color.parseColor("#0F172A"))
        }
        val textView = android.widget.TextView(this).apply {
            text = "Starting Remote Screen Control..."
            setTextColor(android.graphics.Color.WHITE)
            textSize = 16f
        }
        layout.addView(textView)
        setContentView(layout)

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            try {
                val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                startActivityForResult(projectionManager.createScreenCaptureIntent(), 1001)
            } catch (e: Exception) {
                finish()
            }
        }, 150)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            screenCaptureManager.pendingResultCode = resultCode
            screenCaptureManager.pendingData = data
            val serviceIntent = Intent(this, ScreenCaptureService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            // Delay finish to ensure startForeground executes while activity is still visible
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                finish()
            }, 1000)
        } else {
            finish()
        }
    }
}
