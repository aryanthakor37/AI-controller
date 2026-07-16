package com.aimobile.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.aimobile.managers.ConnectionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainService : Service() {

    @Inject
    lateinit var connectionManager: ConnectionManager

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start Socket.IO connection
        connectionManager.connect()
        return START_STICKY
    }
}
