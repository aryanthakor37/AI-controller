package com.aimobile.router
 
import android.content.Context
import android.content.Intent
import android.os.Build
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
    private val screenAnalysisHandler = ScreenAnalysisHandler(context)
    private val translationHandler = TranslationHandler(context)
    private val reminderHandler = ReminderHandler(context)

    suspend fun route(request: CommandRequest): CommandResult {
        return try {
            // Wake up screen & dismiss keyguard if phone is locked/asleep
            com.aimobile.utils.UnlockHelper.turnScreenOnAndUnlock(context)

            when (request.intent) {
                "DYNAMIC_MACRO" -> {
                    val steps = request.steps
                    if (steps == null || steps.isEmpty()) {
                        CommandResult("Failed", "No steps provided for DYNAMIC_MACRO")
                    } else {
                        val macroExecutor = com.aimobile.accessibility.macro.MacroExecutor()
                        val success = macroExecutor.executeDynamicMacro(context, steps, request.app ?: "Dynamic Automation") { cur, tot, status ->
                            Log.d("IntentRouter", "Macro Progress: $cur/$tot - $status")
                        }
                        if (success) {
                            CommandResult("Success", "Executed dynamic automation successfully.")
                        } else {
                            CommandResult("Failed", "Failed to execute dynamic automation.")
                        }
                    }
                }
                "FLASHLIGHT_ON" -> flashlightHandler.turnOn()
                "FLASHLIGHT_OFF" -> flashlightHandler.turnOff()
                
                "VOLUME_MUTE" -> volumeHandler.muteVolume()
                "VOLUME_UNMUTE" -> volumeHandler.unmuteVolume()
                
                "OPEN_APP" -> {
                    if (request.app != null) {
                        openAppHandler.openAppByName(request.app)
                    } else {
                        CommandResult("Failed", "No app name specified")
                    }
                }
                
                "CALL_CONTACT" -> {
                    if (request.contact != null) {
                        callHandler.callNumber(request.contact)
                    } else {
                        CommandResult("Failed", "No contact name specified")
                    }
                }
                
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
                            val turnOn = !cleanName.contains("off")
                            val service = com.aimobile.accessibility.MyAccessibilityService.instance
                            com.aimobile.accessibility.automation.AppAutomations.runBluetoothToggleAutomation(service, context, turnOn)
                        }
                        cleanName == "wifi" || cleanName == "wi-fi" || cleanName.contains("wifi") || cleanName.contains("wi-fi") -> {
                            val turnOn = !cleanName.contains("off")
                            val service = com.aimobile.accessibility.MyAccessibilityService.instance
                            com.aimobile.accessibility.automation.AppAutomations.runWifiToggleAutomation(service, context, turnOn)
                        }
                        cleanName.contains("dark mode") || cleanName.contains("dark theme") -> {
                            val service = com.aimobile.accessibility.MyAccessibilityService.instance
                            com.aimobile.accessibility.automation.AppAutomations.runQuickSettingToggle(service, context, "Dark mode")
                        }
                        cleanName.contains("brightness") || cleanName.contains("display") || cleanName == "screen brightness" -> {
                            try {
                                val intent = Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                                CommandResult("Success", "Opened Display Settings")
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
                            val mapUri = android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=" + android.net.Uri.encode(query))
                            val mapIntent = Intent(Intent.ACTION_VIEW, mapUri).apply {
                                setPackage("com.google.android.apps.maps")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(mapIntent)
                            CommandResult("Success", "Searching Maps for: $query")
                        } catch (e: Exception) {
                            CommandResult("Failed", "Maps search error: ${e.message}")
                        }
                    } else if (appName.equals("Play Store", ignoreCase = true) || appName.equals("PlayStore", ignoreCase = true) || appName.equals("Google Play Store", ignoreCase = true) || packageName?.contains("vending") == true) {
                        try {
                            val playUri = android.net.Uri.parse("market://search?q=" + android.net.Uri.encode(query))
                            val playIntent = Intent(Intent.ACTION_VIEW, playUri).apply {
                                setPackage("com.android.vending")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(playIntent)
                            CommandResult("Success", "Searching Play Store for: $query")
                        } catch (e: Exception) {
                            CommandResult("Failed", "Play Store search error: ${e.message}")
                        }
                    } else if (appName.equals("Telegram", ignoreCase = true) || appName.equals("telegram", ignoreCase = true) || packageName?.contains("telegram") == true) {
                        try {
                            val teleUri = android.net.Uri.parse("tg://search?query=" + android.net.Uri.encode(query))
                            val teleIntent = Intent(Intent.ACTION_VIEW, teleUri).apply {
                                setPackage(packageName ?: "org.telegram.messenger")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(teleIntent)
                            CommandResult("Success", "Searching Telegram for: $query")
                        } catch (e: Exception) {
                            CommandResult("Failed", "Telegram search error: ${e.message}")
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
                
                "SET_BRIGHTNESS" -> {
                    val msg = request.app ?: request.message ?: request.query ?: ""
                    val cleanMsg = msg.trim().lowercase()
                    val percentage = when {
                        cleanMsg.contains("max") || cleanMsg.contains("full") || cleanMsg.contains("high") -> 100
                        cleanMsg.contains("very low") || cleanMsg.contains("verylow") -> 10
                        cleanMsg.contains("low") || cleanMsg.contains("dim") -> 30
                        cleanMsg.contains("medium") || cleanMsg.contains("half") -> 50
                        else -> {
                            val match = Regex("(\\d+)\\s*%").find(cleanMsg) ?: Regex("(\\d+)").find(cleanMsg)
                            match?.groupValues?.get(1)?.toIntOrNull() ?: 50
                        }
                    }
                    com.aimobile.accessibility.automation.AppAutomations.setBrightness(context, percentage)
                }

                "INCREASE_VOLUME" -> volumeHandler.increaseVolume()
                "DECREASE_VOLUME" -> volumeHandler.decreaseVolume()
                "MUTE_VOLUME", "MUTE_PHONE" -> volumeHandler.muteVolume()
                
                "TOGGLE_QUICK_SETTING" -> {
                    val tileName = request.app ?: request.message ?: request.query ?: ""
                    val service = com.aimobile.accessibility.MyAccessibilityService.instance
                    com.aimobile.accessibility.automation.AppAutomations.runQuickSettingToggle(service, context, tileName)
                }

                "TOGGLE_WIFI", "WIFI_ON", "WIFI_OFF" -> {
                    val service = com.aimobile.accessibility.MyAccessibilityService.instance
                    com.aimobile.accessibility.automation.AppAutomations.runQuickSettingToggle(service, context, "wifi")
                }

                "TOGGLE_BLUETOOTH", "BLUETOOTH_ON", "BLUETOOTH_OFF" -> {
                    val service = com.aimobile.accessibility.MyAccessibilityService.instance
                    com.aimobile.accessibility.automation.AppAutomations.runQuickSettingToggle(service, context, "bluetooth")
                }

                "SET_ALARM" -> {
                    val rawTime = request.time ?: request.message ?: request.query ?: ""
                    val (h, m) = parseAlarmTime(rawTime)
                    alarmHandler.setAlarm(h, m)
                }

                "SCHEDULE_AUTOMATION" -> {
                    val rawText = request.query ?: request.message ?: ""
                    val (h, m) = parseAlarmTime(rawText)
                    val timeHandler = com.aimobile.handlers.TimeAutomationHandler(context)
                    val commandToRun = rawText.replace(Regex("\\b\\d{1,2}\\s*(am|pm|vage|baje)\\b", RegexOption.IGNORE_CASE), "").trim()
                    timeHandler.scheduleRoutine(h, m, commandToRun)
                }
                "START_TIMER", "SET_TIMER" -> alarmHandler.setTimer(request.duration ?: 0)
                "SET_REMINDER", "CREATE_REMINDER", "SET_BIRTHDAY" -> {
                    reminderHandler.scheduleReminder(
                        title = request.title ?: request.message ?: "Special Event Reminder",
                        dateStr = request.date,
                        timeStr = request.time ?: "09:00",
                        repeat = request.repeat ?: "NONE",
                        contact = request.contact
                    )
                }
                
                "BATTERY_STATUS" -> deviceInfoHandler.getBatteryStatus()
                "NETWORK_STATUS", "NETWORK_STATUS_CHECK" -> deviceInfoHandler.getNetworkStatus()
                "DEVICE_INFO" -> deviceInfoHandler.getDeviceInfo()
                
                "TAKE_SELFIE", "SELFIE" -> {
                    val service = com.aimobile.accessibility.MyAccessibilityService.instance
                    com.aimobile.accessibility.automation.AppAutomations.runCameraSelfieAutomation(service, context)
                }
                
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
                
                "CHECK_WEATHER", "WEATHER", "WEATHER_INFO" -> {
                    val query = request.query ?: request.message ?: "today's weather"
                    try {
                        val weatherUri = android.net.Uri.parse("https://www.google.com/search?q=" + android.net.Uri.encode(query))
                        val intent = Intent(Intent.ACTION_VIEW, weatherUri).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                        CommandResult("Success", "Opened weather search for: $query")
                    } catch (e: Exception) {
                        CommandResult("Success", "Weather info processed")
                    }
                }

                "GET_DIRECTIONS", "NAVIGATE", "MAP_DIRECTIONS" -> {
                    val query = request.query ?: request.message ?: ""
                    launchMapsNavigation(context, query, request.origin, request.destination)
                }

                "ANALYZE_SCREEN", "SUMMARIZE_SCREEN", "READ_SCREEN", "SCREEN_SUMMARY" -> {
                    val query = request.query ?: request.message ?: ""
                    screenAnalysisHandler.analyzeCurrentScreen(query)
                }

                "TRANSLATE_TEXT", "TRANSLATE_VOICE", "LIVE_TRANSLATE", "TRANSLATION" -> {
                    val query = request.query ?: request.message ?: request.app
                    translationHandler.translate(query)
                }

                "GENERAL_CHAT" -> {
                    val msg = request.message ?: ""
                    val lower = msg.lowercase()
                    if (lower.contains("direction") || lower.contains("map") || lower.contains("navigate")) {
                        launchMapsNavigation(context, msg, request.origin, request.destination)
                    } else if (lower.contains("screen") && (lower.contains("summarize") || lower.contains("read") || lower.contains("what") || lower.contains("summary"))) {
                        screenAnalysisHandler.analyzeCurrentScreen()
                    } else if (lower.contains("translate") || lower.contains("ટ્રાન્સલેટ") || lower.contains("અનુવાદ")) {
                        translationHandler.translate(msg)
                    } else {
                        CommandResult("Success", msg.ifEmpty { "Chat processed" })
                    }
                }

                "BATTERY_STATUS", "GET_BATTERY_STATUS" -> {
                    val intentFilter = android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
                    val batteryStatus = context.registerReceiver(null, intentFilter)
                    val level = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
                    val scale = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
                    val battery = if (level == -1 || scale == -1) 100 else (level * 100 / scale.toFloat()).toInt()
                    CommandResult("Success", "Battery Level: $battery%")
                }

                "TAKE_SCREENSHOT" -> {
                    val service = com.aimobile.accessibility.MyAccessibilityService.instance
                    if (service != null) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                            kotlin.coroutines.suspendCoroutine { continuation ->
                                service.takeScreenshot(
                                    android.view.Display.DEFAULT_DISPLAY,
                                    context.mainExecutor,
                                    object : android.accessibilityservice.AccessibilityService.TakeScreenshotCallback {
                                        override fun onSuccess(screenshot: android.accessibilityservice.AccessibilityService.ScreenshotResult) {
                                            try {
                                                val hwBuffer = screenshot.hardwareBuffer
                                                val bitmap = android.graphics.Bitmap.wrapHardwareBuffer(hwBuffer, screenshot.colorSpace)
                                                if (bitmap != null) {
                                                    // Hardware bitmaps cannot be compressed directly. Must copy to software bitmap.
                                                    val softwareBitmap = bitmap.copy(android.graphics.Bitmap.Config.ARGB_8888, false)
                                                    
                                                    // Scale down to prevent OutOfMemoryError and reduce payload size
                                                    val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(
                                                        softwareBitmap, 
                                                        softwareBitmap.width / 2, 
                                                        softwareBitmap.height / 2, 
                                                        true
                                                    )
                                                    
                                                    val baos = java.io.ByteArrayOutputStream()
                                                    scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 60, baos)
                                                    val base64 = android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.NO_WRAP)
                                                    continuation.resumeWith(Result.success(CommandResult("Success", "Screenshot captured", "SCREENSHOT:$base64")))
                                                    
                                                    softwareBitmap.recycle()
                                                    scaledBitmap.recycle()
                                                } else {
                                                    continuation.resumeWith(Result.success(CommandResult("Failed", "Failed to wrap buffer")))
                                                }
                                                hwBuffer.close()
                                            } catch (e: Throwable) {
                                                continuation.resumeWith(Result.success(CommandResult("Failed", "Error converting screenshot: ${e.message}")))
                                            }
                                        }
                                        override fun onFailure(errorCode: Int) {
                                            continuation.resumeWith(Result.success(CommandResult("Failed", "Screenshot failed code: $errorCode")))
                                        }
                                    }
                                )
                            }
                        } else {
                            val success = service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
                            CommandResult(if (success) "Success" else "Failed", "Screenshot executed (Saved to gallery)")
                        }
                    } else {
                        CommandResult("Failed", "Accessibility service not running")
                    }
                }
                
                "START_SCREEN_STREAM" -> {
                    val streamIntent = android.content.Intent(context, com.aimobile.services.ScreenCaptureActivity::class.java)
                    streamIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(streamIntent)
                    CommandResult("Success", "Starting live screen stream...")
                }
                
                "STOP_SCREEN_STREAM" -> {
                    com.aimobile.managers.ConnectionManager.instance?.stopScreenStream()
                    CommandResult("Success", "Stopped screen stream")
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

    private fun parseAlarmTime(rawTime: String): Pair<Int, Int> {
        val clean = rawTime.lowercase().trim()
        val match = Regex("(\\d{1,2})(?:\\s*:\\s*(\\d{1,2}))?\\s*(am|pm)?").find(clean)
        if (match != null) {
            var h = match.groupValues[1].toIntOrNull() ?: 7
            val mStr = match.groupValues[2]
            val m = if (mStr.isNotEmpty()) mStr.toIntOrNull() ?: 0 else 0
            val ampm = match.groupValues[3]
            if (ampm == "pm" && h < 12) h += 12
            else if (ampm == "am" && h == 12) h = 0
            return Pair(h.coerceIn(0, 23), m.coerceIn(0, 59))
        }
        // Default to 07:00 AM instead of 00:00 (12:00 AM)
        return Pair(7, 0)
    }

    private fun launchMapsNavigation(
        context: Context,
        rawQuery: String,
        originParam: String? = null,
        destParam: String? = null
    ): CommandResult {
        return try {
            var origin = originParam
            var destination = destParam

            if (destination.isNullOrBlank() && origin.isNullOrBlank()) {
                val clean = rawQuery.lowercase()
                    .replace("give me direction for the", "")
                    .replace("give me directions for the", "")
                    .replace("give me direction for", "")
                    .replace("give me directions for", "")
                    .replace("give me direction to", "")
                    .replace("give me directions to", "")
                    .replace("directions to", "")
                    .replace("direction to", "")
                    .replace("directions for", "")
                    .replace("direction for", "")
                    .replace("directions", "")
                    .replace("direction", "")
                    .replace("in map", "")
                    .replace("on map", "")
                    .replace("in google maps", "")
                    .trim()

                if (clean.contains(" to ")) {
                    val parts = clean.split(" to ")
                    origin = parts[0].trim()
                    destination = parts[1].trim()
                } else if (clean.contains(" from ")) {
                    val parts = clean.split(" from ")
                    destination = parts[0].trim()
                    origin = parts[1].trim()
                } else {
                    destination = clean
                }
            }

            val mapUri = if (!origin.isNullOrBlank() && !destination.isNullOrBlank()) {
                android.net.Uri.parse("https://www.google.com/maps/dir/?api=1&origin=" + android.net.Uri.encode(origin) + "&destination=" + android.net.Uri.encode(destination) + "&travelmode=driving")
            } else if (!destination.isNullOrBlank()) {
                android.net.Uri.parse("google.navigation:q=" + android.net.Uri.encode(destination))
            } else {
                android.net.Uri.parse("https://www.google.com/maps")
            }

            val mapIntent = Intent(Intent.ACTION_VIEW, mapUri).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(mapIntent)
            CommandResult("Success", "Opening directions: ${origin ?: "Current Location"} ➔ $destination")
        } catch (e: Exception) {
            try {
                val webUri = android.net.Uri.parse("https://www.google.com/maps/dir/" + android.net.Uri.encode(originParam ?: "") + "/" + android.net.Uri.encode(destParam ?: rawQuery))
                val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
                CommandResult("Success", "Opened Google Maps directions in web browser")
            } catch (e2: Exception) {
                CommandResult("Failed", "Maps navigation error: ${e.message}")
            }
        }
    }
}
