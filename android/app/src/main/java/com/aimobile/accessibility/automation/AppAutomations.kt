package com.aimobile.accessibility.automation

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.util.Log
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
}
