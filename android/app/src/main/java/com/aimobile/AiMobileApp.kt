package com.aimobile

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AiMobileApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Logging System here later
    }
}
