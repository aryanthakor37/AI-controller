package com.aimobile.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object HapticHelper {
    fun performHaptic(context: Context, type: HapticType = HapticType.LIGHT) {
        try {
            val duration = when (type) {
                HapticType.LIGHT -> 15L
                HapticType.MEDIUM -> 35L
                HapticType.HEAVY -> 60L
                HapticType.SUCCESS -> 80L
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(VibratorManager::class.java)
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (e: Exception) {
            // Ignore if vibration is disabled or unsupported
        }
    }
}

enum class HapticType {
    LIGHT,
    MEDIUM,
    HEAVY,
    SUCCESS
}
