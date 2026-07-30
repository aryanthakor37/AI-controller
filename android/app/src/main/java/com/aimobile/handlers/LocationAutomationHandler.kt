package com.aimobile.handlers

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import com.aimobile.data.LocationRuleStorage
import com.aimobile.models.CommandResult
import com.aimobile.models.LocationRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocationAutomationHandler(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val storage = LocationRuleStorage(context)

    fun isDndPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.isNotificationPolicyAccessGranted
        } else {
            true
        }
    }

    fun openDndPermissionSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isDndPermissionGranted()) {
            try {
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Suppress("MissingPermission")
    fun getCurrentGpsLocation(): Location? {
        var loc: Location? = null
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
            if (loc == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return loc
    }

    suspend fun setRingerMode(mode: String): CommandResult = withContext(Dispatchers.IO) {
        return@withContext try {
            when (mode.uppercase()) {
                "SILENT" -> {
                    var modeSet = false
                    if (isDndPermissionGranted()) {
                        try {
                            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                            modeSet = true
                        } catch (_: Exception) {}
                    }

                    if (!modeSet) {
                        try {
                            audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0)
                            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0)
                            audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                        } catch (_: Exception) {}
                    }
                    CommandResult("Success", "Phone muted & set to SILENT Mode")
                }
                "VIBRATE" -> {
                    try {
                        audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0)
                        audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                    } catch (_: Exception) {}
                    CommandResult("Success", "Phone set to VIBRATE Mode")
                }
                "NORMAL_SOUND", "NORMAL" -> {
                    try {
                        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
                        val targetVol = (maxVol * 0.75).toInt().coerceAtLeast(1)
                        audioManager.setStreamVolume(AudioManager.STREAM_RING, targetVol, 0)
                        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, targetVol, 0)
                        if (isDndPermissionGranted()) {
                            try { audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL } catch (_: Exception) {}
                        } else {
                            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                        }
                    } catch (_: Exception) {}
                    CommandResult("Success", "Phone sound mode restored to NORMAL")
                }
                else -> {
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0)
                    CommandResult("Success", "Phone muted")
                }
            }
        } catch (e: Exception) {
            CommandResult("Failed", "Could not change audio mode: ${e.message}")
        }
    }

    fun calculateDistanceMeters(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(startLat, startLng, endLat, endLng, results)
        return results[0]
    }

    suspend fun checkRealLocationAndApplyRules(): CommandResult = withContext(Dispatchers.IO) {
        val currentLocation = getCurrentGpsLocation()
            ?: return@withContext CommandResult("NoGPS", "GPS location not available. Turn ON GPS/Location on phone.")

        val rules = storage.loadRules()
        var matchedRuleName = ""
        var actionExecuted = ""

        for (rule in rules) {
            if (!rule.isEnabled) continue
            val distance = calculateDistanceMeters(
                currentLocation.latitude,
                currentLocation.longitude,
                rule.latitude,
                rule.longitude
            )

            val isInside = distance <= rule.radiusMeters

            if (isInside) {
                matchedRuleName = rule.name
                actionExecuted = rule.enterAction
                setRingerMode(rule.enterAction)
                rule.isInside = true
                break
            } else if (rule.isInside) {
                rule.isInside = false
                setRingerMode(rule.exitAction)
            }
        }

        storage.saveRules(rules)

        return@withContext if (matchedRuleName.isNotEmpty()) {
            CommandResult("Inside", "📍 Matched area '$matchedRuleName': Applied $actionExecuted")
        } else {
            CommandResult("Outside", "📍 Outside all set areas (${currentLocation.latitude.toString().take(7)}, ${currentLocation.longitude.toString().take(7)})")
        }
    }
}
