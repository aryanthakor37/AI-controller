package com.aimobile.handlers

import android.content.Context
import android.media.AudioManager
import com.aimobile.models.CommandResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VolumeHandler(private val context: Context) {
    
    suspend fun increaseVolume(): CommandResult = withContext(Dispatchers.Main) {
        adjustVolume(AudioManager.ADJUST_RAISE)
    }

    suspend fun decreaseVolume(): CommandResult = withContext(Dispatchers.Main) {
        adjustVolume(AudioManager.ADJUST_LOWER)
    }

    suspend fun muteVolume(): CommandResult = withContext(Dispatchers.Main) {
        adjustVolume(AudioManager.ADJUST_MUTE)
    }

    private fun adjustVolume(direction: Int): CommandResult {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
            CommandResult(status = "Success", message = "Volume adjusted")
        } catch (e: Exception) {
            CommandResult(status = "Failed", message = "Failed to adjust volume: ${e.message}")
        }
    }
}
