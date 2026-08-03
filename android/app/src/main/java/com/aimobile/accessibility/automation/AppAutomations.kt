package com.aimobile.accessibility.automation

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.aimobile.models.CommandResult
import kotlinx.coroutines.delay

object AppAutomations {

    suspend fun runWhatsAppAutomation(service: AccessibilityService, context: Context, contact: String, message: String): CommandResult {
        AutomationManager.addLog("Starting WhatsApp automation to contact: $contact")
        
        // 1. Launch WhatsApp
        val intent = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
        if (intent == null) {
            return CommandResult("Failed", "WhatsApp not installed")
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        delay(2000) // Wait for app load

        // 2. Click Search icon
        // Try standard WhatsApp view IDs or search text fallback
        var searchClicked = AutomationManager.clickNodeById(service, "com.whatsapp:id/menuitem_search")
        if (!searchClicked) {
            searchClicked = AutomationManager.clickNodeByText(service, "Search") ||
                            AutomationManager.clickNodeByText(service, "શોધો") ||
                            AutomationManager.clickNodeByText(service, "खोजें")
        }
        if (!searchClicked) {
            return CommandResult("Failed", "Could not find search button in WhatsApp")
        }
        delay(1000)

        // 3. Type contact name into Search input
        // WhatsApp search input view ID is com.whatsapp:id/search_src_text
        var inputDone = AutomationManager.inputTextIntoId(service, "com.whatsapp:id/search_src_text", contact)
        if (!inputDone) {
            inputDone = AutomationManager.findAndInputText(service, contact)
        }
        if (!inputDone) {
            return CommandResult("Failed", "Could not input contact name")
        }
        delay(1500)

        // 4. Click contact from results list (matching contact name text)
        val contactClicked = AutomationManager.clickNodeByText(service, contact)
        if (!contactClicked) {
            return CommandResult("Failed", "Could not find contact '$contact' in search results")
        }
        delay(1500)

        // 5. Type message into input box
        // WhatsApp chat edit box ID is com.whatsapp:id/entry
        var msgEntered = AutomationManager.inputTextIntoId(service, "com.whatsapp:id/entry", message)
        if (!msgEntered) {
            msgEntered = AutomationManager.findAndInputText(service, message)
        }
        if (!msgEntered) {
            return CommandResult("Failed", "Could not input message text")
        }
        delay(1000)

        // 6. Click Send button
        // WhatsApp send button ID is com.whatsapp:id/send
        val sendClicked = AutomationManager.clickNodeById(service, "com.whatsapp:id/send")
        if (!sendClicked) {
            return CommandResult("Failed", "Could not find send button")
        }

        return CommandResult("Success", "Message sent to $contact via WhatsApp")
    }

    suspend fun runYouTubeAutomation(service: AccessibilityService?, context: Context, query: String): CommandResult {
        AutomationManager.addLog("Starting YouTube search for: $query")

        return try {
            val searchUrl = "https://www.youtube.com/results?search_query=" + android.net.Uri.encode(query)
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(searchUrl)).apply {
                setPackage("com.google.android.youtube")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            CommandResult("Success", "Searching YouTube for: $query")
        } catch (e: Exception) {
            try {
                val searchUrl = "https://www.youtube.com/results?search_query=" + android.net.Uri.encode(query)
                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(searchUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                CommandResult("Success", "Searching YouTube via Browser for: $query")
            } catch (e2: Exception) {
                CommandResult("Failed", "Error opening search: ${e2.message}")
            }
        }
    }

    suspend fun runSpotifyAutomation(service: AccessibilityService?, context: Context, query: String): CommandResult {
        AutomationManager.addLog("Starting Spotify search for: $query")

        return try {
            val searchUrl = "https://open.spotify.com/search/" + android.net.Uri.encode(query)
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(searchUrl)).apply {
                setPackage("com.spotify.music")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            CommandResult("Success", "Searching Spotify for: $query")
        } catch (e: Exception) {
            try {
                val searchUrl = "https://open.spotify.com/search/" + android.net.Uri.encode(query)
                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(searchUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                CommandResult("Success", "Searching Spotify via Browser for: $query")
            } catch (e2: Exception) {
                CommandResult("Failed", "Error opening search: ${e2.message}")
            }
        }
    }

    suspend fun runChromeAutomation(service: AccessibilityService?, context: Context, url: String): CommandResult {
        AutomationManager.addLog("Starting Chrome automation: $url")
        return try {
            val intent = Intent(Intent.ACTION_VIEW)
            var finalUrl = url.trim()
            if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
                if (finalUrl.contains(".") && !finalUrl.contains(" ")) {
                    finalUrl = "https://$finalUrl"
                } else {
                    finalUrl = "https://www.google.com/search?q=" + android.net.Uri.encode(finalUrl)
                }
            }
            intent.data = android.net.Uri.parse(finalUrl)
            intent.setPackage("com.android.chrome")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            CommandResult("Success", "Loaded in Chrome: $url")
        } catch (e: Exception) {
            CommandResult("Failed", "Chrome not installed or error: ${e.message}")
        }
    }

    suspend fun runGeneralAppSearchAutomation(service: AccessibilityService?, context: Context, packageName: String, query: String, appName: String): CommandResult {
        AutomationManager.addLog("Starting general search automation for $appName: $query")
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                
                if (service != null) {
                    delay(2000) // Wait for app to load
                    
                    // Try to click search icon/button
                    var clicked = AutomationManager.clickNodeByContentDescription(service, "search", 2)
                    if (!clicked) clicked = AutomationManager.clickNodeByContentDescription(service, "Search", 1)
                    if (!clicked) clicked = AutomationManager.clickNodeByText(service, "Search", 1)
                    if (!clicked) clicked = AutomationManager.clickNodeByText(service, "search", 1)
                    if (!clicked) clicked = AutomationManager.clickNodeByText(service, "Search in emails", 1)
                    
                    if (clicked) {
                        delay(1000)
                    }
                    
                    // 1. Try focused input node
                    val inputSuccess = AutomationManager.findAndInputText(service, query, 2)
                    if (inputSuccess) {
                        return CommandResult("Success", "Searching in $appName for: $query")
                    }
                    
                    // 2. Try manually finding any EditText or Editable node
                    val root = service.rootInActiveWindow
                    if (root != null) {
                        val searchNode = findAnyEditTextNode(root)
                        if (searchNode != null) {
                            AutomationManager.addLog("Found an editable node as fallback, setting text.")
                            val args = android.os.Bundle().apply {
                                putCharSequence(android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, query)
                            }
                            searchNode.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                            
                            // Optional: some apps require click to submit search
                            searchNode.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)
                            
                            return CommandResult("Success", "Searching in $appName for: $query")
                        }
                    }
                    
                    CommandResult("Success", "Opened $appName (Could not auto-type search)")
                } else {
                    CommandResult("Success", "Opened $appName (Enable Accessibility for auto-search)")
                }
            } else {
                CommandResult("Failed", "App not found: $appName")
            }
        } catch (e: Exception) {
            CommandResult("Failed", "Error searching in $appName: ${e.message}")
        }
    }

    private fun findAnyEditTextNode(root: android.view.accessibility.AccessibilityNodeInfo?): android.view.accessibility.AccessibilityNodeInfo? {
        if (root == null) return null
        
        if (root.isEditable || root.className?.toString()?.contains("EditText") == true || root.className?.toString()?.contains("AutoCompleteTextView") == true) {
            return root
        }
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            val found = findAnyEditTextNode(child)
            if (found != null) return found
        }
        return null
    }

    suspend fun runInstagramAutomation(service: AccessibilityService?, context: Context, query: String): CommandResult {
        AutomationManager.addLog("Starting Instagram automation for: $query")
        return try {
            val intent = Intent(Intent.ACTION_VIEW)
            val cleanQuery = query.replace(" ", "").trim()
            intent.data = android.net.Uri.parse("https://www.instagram.com/$cleanQuery/")
            intent.setPackage("com.instagram.android")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            CommandResult("Success", "Opening Instagram profile: $cleanQuery")
        } catch (e: Exception) {
            CommandResult("Failed", "Instagram not installed or error: ${e.message}")
        }
    }

    suspend fun runWifiToggleAutomation(service: AccessibilityService?, context: Context, turnOn: Boolean): CommandResult {
        AutomationManager.addLog("Starting Wi-Fi toggle automation to: $turnOn")
        return try {
            val intent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                Intent(android.provider.Settings.Panel.ACTION_WIFI).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                Intent(android.provider.Settings.ACTION_WIFI_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(intent)
            delay(1000)

            if (service != null) {
                val toggled = AutomationManager.toggleSwitch(service, turnOn)
                if (toggled) {
                    CommandResult("Success", "Wi-Fi turned ${if (turnOn) "ON" else "OFF"} automatically")
                } else {
                    CommandResult("Success", "Opened Wi-Fi settings (Auto-click switch)")
                }
            } else {
                CommandResult("Success", "Opened Wi-Fi settings (Enable Accessibility Service in Settings for auto-click)")
            }
        } catch (e: Exception) {
            CommandResult("Failed", "Could not toggle Wi-Fi: ${e.message}")
        }
    }

    suspend fun runBluetoothToggleAutomation(service: AccessibilityService?, context: Context, turnOn: Boolean): CommandResult {
        AutomationManager.addLog("Starting Bluetooth toggle automation to: $turnOn")
        return try {
            val intent = Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            delay(1000)

            if (service != null) {
                val toggled = AutomationManager.toggleSwitch(service, turnOn)
                if (toggled) {
                    CommandResult("Success", "Bluetooth turned ${if (turnOn) "ON" else "OFF"} automatically")
                } else {
                    CommandResult("Success", "Opened Bluetooth settings (Auto-click switch)")
                }
            } else {
                CommandResult("Success", "Opened Bluetooth settings (Enable Accessibility Service in Settings for auto-click)")
            }
        } catch (e: Exception) {
            CommandResult("Failed", "Could not toggle Bluetooth: ${e.message}")
        }
    }

    suspend fun runQuickSettingToggle(service: AccessibilityService?, context: Context, tileName: String): CommandResult {
        if (service == null) {
            return CommandResult("Failed", "Accessibility Service disabled (Cannot open Quick Settings).")
        }
        
        AutomationManager.addLog("Opening Quick Settings to toggle: $tileName")
        val opened = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)
        if (!opened) return CommandResult("Failed", "Could not open Quick Settings.")
        
        delay(1000) // Wait for panel to drop down

        val namesToTry = when (tileName.lowercase().trim()) {
            "wifi", "wi-fi", "internet" -> listOf("Internet", "Wi-Fi", "WLAN", "Wi‑Fi", tileName)
            "bluetooth" -> listOf("Bluetooth", tileName)
            "location", "gps" -> listOf("Location", tileName)
            "mobile data", "data" -> listOf("Mobile data", tileName)
            "airplane mode", "flight mode", "aeroplane mode" -> listOf("Airplane mode", "Flight mode", tileName)
            else -> listOf(tileName)
        }

        var clicked = false
        var successfulName = tileName
        // Try finding it on current page, if not, swipe and try again
        for (page in 0..2) {
            for (name in namesToTry) {
                clicked = AutomationManager.clickNodeByText(service, name, 1)
                if (!clicked) {
                    clicked = AutomationManager.clickNodeByContentDescription(service, name, 1)
                }
                if (clicked) {
                    successfulName = name
                    break
                }
            }
            if (clicked) break
            
            // Swipe right (scroll to next page in quick settings)
            AutomationManager.performHorizontalScrollGesture(service, true)
            delay(500)
        }

        // Close panel
        delay(500)
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)

        return if (clicked) {
            CommandResult("Success", "Toggled $successfulName in Quick Settings")
        } else {
            CommandResult("Failed", "Could not find $tileName in Quick Settings")
        }
    }

    suspend fun runDarkModeToggleAutomation(service: AccessibilityService?, context: Context, turnOn: Boolean): CommandResult {
        AutomationManager.addLog("Starting Dark Mode toggle automation to: $turnOn")
        return try {
            val intent = Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            delay(1000)

            if (service != null) {
                val toggled = AutomationManager.toggleSwitch(service, turnOn, targetKeyword = "dark")
                if (toggled) {
                    CommandResult("Success", "Dark Mode turned ${if (turnOn) "ON" else "OFF"} automatically")
                } else {
                    CommandResult("Success", "Opened Display Settings (Auto-click Dark Mode switch)")
                }
            } else {
                CommandResult("Success", "Opened Display Settings (Enable Accessibility Service in Settings for auto-click)")
            }
        } catch (e: Exception) {
            CommandResult("Failed", "Could not toggle Dark Mode: ${e.message}")
        }
    }

    fun setBrightness(context: Context, percentage: Int): CommandResult {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (!android.provider.Settings.System.canWrite(context)) {
                    val intent = Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                        data = android.net.Uri.parse("package:" + context.packageName)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    return CommandResult("Failed", "Please grant 'Modify system settings' permission to allow brightness control.")
                }
            }
            // Max brightness is usually 255
            val brightnessValue = (255 * (percentage / 100f)).toInt().coerceIn(0, 255)
            
            // Turn off auto-brightness first
            android.provider.Settings.System.putInt(
                context.contentResolver,
                android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
                android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            
            // Set brightness
            android.provider.Settings.System.putInt(
                context.contentResolver,
                android.provider.Settings.System.SCREEN_BRIGHTNESS,
                brightnessValue
            )
            CommandResult("Success", "Set brightness to $percentage%")
        } catch (e: Exception) {
            CommandResult("Failed", "Could not set brightness: ${e.message}")
        }
    }

    fun openApp(context: Context, packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e("AppAutomations", "Failed to open package $packageName: ${e.message}")
            false
        }
    }

    suspend fun runCameraSelfieAutomation(service: AccessibilityService?, context: Context): CommandResult {
        return try {
            val intent = Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)

            if (service != null) {
                delay(1500) // Wait for camera to open
                
                // Try to switch to front camera
                val switched = AutomationManager.clickNodeByContentDescription(service, "Switch camera", 2) || 
                               AutomationManager.clickNodeByContentDescription(service, "Front camera", 1) ||
                               AutomationManager.clickNodeByContentDescription(service, "Flip camera", 1) ||
                               AutomationManager.clickNodeByContentDescription(service, "switch", 1)
                
                if (switched) {
                    delay(1000) // Wait for lens to switch
                }
                
                // Click shutter
                var captured = AutomationManager.clickNodeByContentDescription(service, "Shutter", 2) ||
                               AutomationManager.clickNodeByContentDescription(service, "Take photo", 1) ||
                               AutomationManager.clickNodeByContentDescription(service, "Capture", 1) ||
                               AutomationManager.clickNodeByText(service, "Capture", 1)
                               
                if (!captured) {
                    val metrics = context.resources.displayMetrics
                    val x = metrics.widthPixels / 2f
                    val y = metrics.heightPixels * 0.88f
                    captured = AutomationManager.performTap(service, x, y)
                }
                               
                if (captured) {
                    CommandResult("Success", "📸 Selfie captured automatically!")
                } else {
                    CommandResult("Success", "Opened Camera, but couldn't auto-click shutter.")
                }
            } else {
                CommandResult("Success", "Opened Camera.")
            }
        } catch (e: Exception) {
            CommandResult("Failed", "Could not open Camera: ${e.message}")
        }
    }
}
