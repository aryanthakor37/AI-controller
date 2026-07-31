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

    suspend fun unmuteVolume(): CommandResult = withContext(Dispatchers.Main) {
        adjustVolume(AudioManager.ADJUST_UNMUTE)
    }

    private fun adjustVolume(direction: Int): CommandResult {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (direction == AudioManager.ADJUST_MUTE) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI)
                try { audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, AudioManager.FLAG_SHOW_UI) } catch (_: Exception) {}
                CommandResult(status = "Success", message = "Muted phone volume")
            } else if (direction == AudioManager.ADJUST_UNMUTE) {
                // To unmute and restore some volume, we can set it to a reasonable level like index 5
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 5, AudioManager.FLAG_SHOW_UI)
                try { audioManager.setStreamVolume(AudioManager.STREAM_RING, 5, AudioManager.FLAG_SHOW_UI) } catch (_: Exception) {}
                CommandResult(status = "Success", message = "Unmuted phone volume")
            } else {
                // Increase/Decrease by 3 steps to make it noticeable
                for (i in 0 until 3) {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
                }
                try { 
                    for (i in 0 until 3) {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_RING, direction, AudioManager.FLAG_SHOW_UI) 
                    }
                } catch (_: Exception) {}
                CommandResult(status = "Success", message = if (direction == AudioManager.ADJUST_RAISE) "Increased volume" else "Decreased volume")
            }
        } catch (e: Exception) {
            CommandResult(status = "Failed", message = "Failed to adjust volume: ${e.message}")
        }
    }
}
