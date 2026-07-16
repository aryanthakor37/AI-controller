package com.aimobile.handlers

import android.content.Context
import android.hardware.camera2.CameraManager
import com.aimobile.models.CommandResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FlashlightHandler(private val context: Context) {
    
    suspend fun turnOn(): CommandResult = withContext(Dispatchers.IO) {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0] // Usually 0 is the back camera
            cameraManager.setTorchMode(cameraId, true)
            CommandResult(status = "Success", message = "Flashlight turned ON")
        } catch (e: Exception) {
            CommandResult(status = "Failed", message = "Could not turn on flashlight: ${e.message}")
        }
    }

    suspend fun turnOff(): CommandResult = withContext(Dispatchers.IO) {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, false)
            CommandResult(status = "Success", message = "Flashlight turned OFF")
        } catch (e: Exception) {
            CommandResult(status = "Failed", message = "Could not turn off flashlight: ${e.message}")
        }
    }
}
