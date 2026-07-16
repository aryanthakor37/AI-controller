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
            searchClicked = AutomationManager.clickNodeByText(service, "Search")
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

    suspend fun runYouTubeAutomation(service: AccessibilityService, context: Context, query: String): CommandResult {
        AutomationManager.addLog("Starting YouTube search for: $query")

        val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
            ?: return CommandResult("Failed", "YouTube not installed")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        delay(2000)

        // Click search icon
        var searchClicked = AutomationManager.clickNodeById(service, "com.google.android.youtube:id/search_button")
        if (!searchClicked) {
            searchClicked = AutomationManager.clickNodeByText(service, "Search")
        }
        if (!searchClicked) {
            return CommandResult("Failed", "Could not find YouTube search button")
        }
        delay(1000)

        // Type query
        var inputDone = AutomationManager.inputTextIntoId(service, "com.google.android.youtube:id/search_edit_text", query)
        if (!inputDone) {
            inputDone = AutomationManager.findAndInputText(service, query)
        }
        if (!inputDone) {
            return CommandResult("Failed", "Could not enter YouTube search query")
        }
        delay(1000)

        // Press suggestion or click search
        val goClicked = AutomationManager.clickNodeById(service, "com.google.android.youtube:id/search_type_suggest")
        if (!goClicked) {
            // Send default search key action
            Log.d("YouTubeAuto", "Suggested suggestion search not found, searching suggestions list...")
        }

        return CommandResult("Success", "Searching YouTube for: $query")
    }

    suspend fun runChromeAutomation(service: AccessibilityService, context: Context, url: String): CommandResult {
        AutomationManager.addLog("Starting Chrome automation: $url")

        val intent = context.packageManager.getLaunchIntentForPackage("com.android.chrome")
            ?: return CommandResult("Failed", "Chrome not installed")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        delay(2000)

        // Click search/url bar
        var barClicked = AutomationManager.inputTextIntoId(service, "com.android.chrome:id/url_bar", url)
        if (!barClicked) {
            barClicked = AutomationManager.findAndInputText(service, url)
        }

        return if (barClicked) {
            CommandResult("Success", "Loading website: $url in Chrome")
        } else {
            CommandResult("Failed", "Could not locate URL bar in Chrome")
        }
    }
}
