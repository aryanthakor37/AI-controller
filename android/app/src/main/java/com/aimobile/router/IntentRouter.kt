package com.aimobile.router
 
import android.content.Context
import android.content.Intent
import android.util.Log
import com.aimobile.handlers.*
import com.aimobile.models.CommandRequest
import com.aimobile.models.CommandResult

class IntentRouter(private val context: Context) {
    
    private val flashlightHandler = FlashlightHandler(context)
    private val openAppHandler = OpenAppHandler(context)
    private val volumeHandler = VolumeHandler(context)
    private val alarmHandler = AlarmHandler(context)
    private val deviceInfoHandler = DeviceInfoHandler(context)
    private val callHandler = CallHandler(context)
    private val smsHandler = SMSHandler(context)

    suspend fun route(request: CommandRequest): CommandResult {
        return try {
            when (request.intent) {
                "FLASHLIGHT_ON" -> flashlightHandler.turnOn()
                "FLASHLIGHT_OFF" -> flashlightHandler.turnOff()
                
                "OPEN_CAMERA", "OPEN_GALLERY", "OPEN_YOUTUBE",
                "OPEN_MAPS", "OPEN_GMAIL", "OPEN_CALCULATOR", "OPEN_SETTINGS",
                "OPEN_CONTACTS", "OPEN_DIALER", "OPEN_PLAY_STORE", "OPEN_CLOCK",
                "OPEN_FILES", "OPEN_PHOTOS" -> openAppHandler.openApp(request.intent)
                
                "OPEN_APP" -> {
                    val appName = request.app ?: request.message ?: ""
                    val cleanName = appName.trim().lowercase()
                    when {
                        cleanName.contains("whatsapp") -> {
                            openAppHandler.openApp("OPEN_WHATSAPP")
                        }
                        cleanName == "bluetooth" || cleanName.contains("bluetooth") -> {
                            try {
                                val intent = Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                                CommandResult("Success", "Opened Bluetooth Settings")
                            } catch (e: Exception) {
                                CommandResult("Failed", "Could not open Bluetooth settings: ${e.message}")
                            }
                        }
                        cleanName == "wifi" || cleanName == "wi-fi" || cleanName.contains("wifi") || cleanName.contains("wi-fi") -> {
                            try {
                                val intent = Intent(android.provider.Settings.ACTION_WIFI_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                                CommandResult("Success", "Opened WiFi Settings")
                            } catch (e: Exception) {
                                CommandResult("Failed", "Could not open WiFi settings: ${e.message}")
                            }
                        }
                        cleanName.contains("brightness") || cleanName.contains("display") || cleanName == "screen brightness" -> {
                            try {
                                val intent = Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                                CommandResult("Success", "Opened Display / Brightness Settings")
                            } catch (e: Exception) {
                                CommandResult("Failed", "Could not open Display settings: ${e.message}")
                            }
                        }
                        cleanName.contains("volume") || cleanName.contains("sound") || cleanName.contains("ringtone") -> {
                            try {
                                val intent = Intent(android.provider.Settings.ACTION_SOUND_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                                CommandResult("Success", "Opened Sound / Volume Settings")
                            } catch (e: Exception) {
                                CommandResult("Failed", "Could not open Sound settings: ${e.message}")
                            }
                        }
                        cleanName.contains("nearby") || cleanName.contains("near by") || cleanName.contains("sharing") || cleanName.contains("quick share") -> {
                            try {
                                // Try Nearby Share settings activity first
                                val intent = Intent().apply {
                                    setClassName("com.google.android.gms", "com.google.android.gms.nearby.sharing.settings.NearbySharingSettingsActivity")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                                CommandResult("Success", "Opened Nearby Share Settings")
                            } catch (e: Exception) {
                                try {
                                    val intent = Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                    CommandResult("Success", "Opened Wireless Connection Settings")
                                } catch (e2: Exception) {
                                    CommandResult("Failed", "Could not open connection settings: ${e2.message}")
                                }
                            }
                        }
                        else -> {
                            val packageName = getPackageNameFromAppName(appName)
                            if (packageName != null) {
                                val pkgIntent = context.packageManager.getLaunchIntentForPackage(packageName)
                                if (pkgIntent != null) {
                                    pkgIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(pkgIntent)
                                    CommandResult("Success", "Opened $appName")
                                } else {
                                    CommandResult("Failed", "Could not launch $appName")
                                }
                            } else {
                                CommandResult("Failed", "App not found: $appName")
                            }
                        }
                    }
                }

                "SEARCH_APP" -> {
                    val appName = request.app ?: ""
                    val query = request.query ?: request.message ?: ""
                    val packageName = getPackageNameFromAppName(appName)
                    if (appName.equals("YouTube", ignoreCase = true) || packageName?.contains("youtube") == true) {
                        com.aimobile.accessibility.automation.AppAutomations.runYouTubeAutomation(null, context, query)
                    } else if (appName.equals("Spotify", ignoreCase = true) || packageName?.contains("spotify") == true) {
                        com.aimobile.accessibility.automation.AppAutomations.runSpotifyAutomation(null, context, query)
                    } else if (appName.equals("Instagram", ignoreCase = true) || packageName?.contains("instagram") == true) {
                        com.aimobile.accessibility.automation.AppAutomations.runInstagramAutomation(null, context, query)
                    } else if (appName.equals("Chrome", ignoreCase = true) || packageName?.contains("chrome") == true) {
                        com.aimobile.accessibility.automation.AppAutomations.runChromeAutomation(null, context, query)
                    } else if (appName.equals("Maps", ignoreCase = true) || appName.equals("Google Maps", ignoreCase = true) || packageName?.contains("apps.maps") == true) {
                        try {
                            val mapUri = android.net.Uri.parse("geo:0,0?q=" + android.net.Uri.encode(query))
                            val mapIntent = Intent(Intent.ACTION_VIEW, mapUri).apply {
                                setPackage("com.google.android.apps.maps")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(mapIntent)
                            CommandResult("Success", "Searching Maps for: $query")
                        } catch (e: Exception) {
                            CommandResult("Failed", "Maps search error: ${e.message}")
                        }
                    } else if (packageName != null) {
                        val service = com.aimobile.accessibility.MyAccessibilityService.instance
                        if (service == null) {
                            CommandResult("Permission Required", "Accessibility Service is disabled.")
                        } else {
                            com.aimobile.accessibility.automation.UniversalSearch.runUniversalSearch(
                                service, context, packageName, query
                            )
                        }
                    } else {
                        CommandResult("Failed", "App not found: $appName")
                    }
                }

                "SEND_WHATSAPP_MESSAGE", "MESSAGE_WHATSAPP" -> {
                    val service = com.aimobile.accessibility.MyAccessibilityService.instance
                    if (service == null) {
                        CommandResult("Permission Required", "Accessibility Service is disabled. Please enable it in Settings.")
                    } else {
                        com.aimobile.accessibility.automation.AppAutomations.runWhatsAppAutomation(
                            service,
                            context,
                            request.number ?: request.message ?: "Mom",
                            request.message ?: "Hello from AgentAI"
                        )
                    }
                }

                "SEARCH_YOUTUBE", "YOUTUBE_SEARCH" -> {
                    com.aimobile.accessibility.automation.AppAutomations.runYouTubeAutomation(
                        null,
                        context,
                        request.message ?: "AI news"
                    )
                }

                "OPEN_WEBSITE", "CHROME_SEARCH", "OPEN_CHROME" -> {
                    val query = request.query ?: request.message
                    if (query.isNullOrBlank()) {
                        // Just open Chrome
                        openAppHandler.openApp("OPEN_CHROME")
                    } else {
                        val service = com.aimobile.accessibility.MyAccessibilityService.instance
                        com.aimobile.accessibility.automation.AppAutomations.runChromeAutomation(
                            service,
                            context,
                            query
                        )
                    }
                }
                
                "INCREASE_VOLUME" -> volumeHandler.increaseVolume()
                "DECREASE_VOLUME" -> volumeHandler.decreaseVolume()
                "MUTE_VOLUME", "MUTE_PHONE" -> volumeHandler.muteVolume()
                
                "SET_ALARM" -> {
                    val timeStr = request.time ?: "00:00"
                    val parts = timeStr.split(":")
                    val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
                    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                    alarmHandler.setAlarm(h, m)
                }
                "START_TIMER", "SET_TIMER" -> alarmHandler.setTimer(request.duration ?: 0)
                
                "BATTERY_STATUS" -> deviceInfoHandler.getBatteryStatus()
                "NETWORK_STATUS", "NETWORK_STATUS_CHECK" -> deviceInfoHandler.getNetworkStatus()
                "DEVICE_INFO" -> deviceInfoHandler.getBatteryStatus() // fallback to basic status
                
                "CALL_NUMBER", "CALL_CONTACT" -> {
                    val targetNum = request.number ?: request.message
                    if (targetNum != null) callHandler.callNumber(targetNum)
                    else CommandResult("Failed", "No contact or number provided")
                }
                
                "SEND_SMS" -> {
                    val targetNum = request.number
                    val smsMsg = request.message
                    if (targetNum != null && smsMsg != null) {
                        smsHandler.sendSMS(targetNum, smsMsg)
                    } else {
                        CommandResult("Failed", "Number or message missing")
                    }
                }
                
                else -> CommandResult(status = "Unsupported", message = "Unknown intent: ${request.intent}")
            }
        } catch (e: Exception) {
            CommandResult(status = "Failed", message = "Router error: ${e.message}")
        }
    }

    private fun getPackageNameFromAppName(appName: String): String? {
        val cleanAppName = appName.trim().lowercase()
        if (cleanAppName.isEmpty()) return null
        
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)

        // 1. Direct contains check
        for (info in resolveInfos) {
            val label = info.loadLabel(pm).toString().trim().lowercase()
            if (label == cleanAppName || label.contains(cleanAppName) || cleanAppName.contains(label)) {
                return info.activityInfo.packageName
            }
        }

        // 2. Fuzzy matching
        var bestPackage: String? = null
        var highestSimilarity = 0.0
        
        for (info in resolveInfos) {
            val label = info.loadLabel(pm).toString().trim().lowercase()
            val similarity = getSimilarityScore(cleanAppName, label)
            if (similarity > highestSimilarity && similarity >= 0.7) {
                highestSimilarity = similarity
                bestPackage = info.activityInfo.packageName
            }
        }
        
        if (bestPackage != null) return bestPackage

        // 3. Fallback to getInstalledApplications
        try {
            val packages = pm.getInstalledApplications(0)
            for (packageInfo in packages) {
                val label = pm.getApplicationLabel(packageInfo).toString().trim().lowercase()
                if (label == cleanAppName || label.contains(cleanAppName) || cleanAppName.contains(label)) {
                    if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                        return packageInfo.packageName
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("IntentRouter", "Error getting installed applications: ${e.message}")
        }
        return null
    }

    private fun getSimilarityScore(s1: String, s2: String): Double {
        val maxLength = maxOf(s1.length, s2.length)
        if (maxLength == 0) return 1.0
        val len1 = s1.length
        val len2 = s2.length
        val dp = Array(len1 + 1) { IntArray(len2 + 1) }
        for (i in 0..len1) dp[i][0] = i
        for (j in 0..len2) dp[0][j] = j
        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        val distance = dp[len1][len2]
        return (maxLength - distance).toDouble() / maxLength
    }
}
