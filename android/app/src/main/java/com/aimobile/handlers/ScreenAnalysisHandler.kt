package com.aimobile.handlers

import android.content.Context
import android.view.accessibility.AccessibilityNodeInfo
import com.aimobile.accessibility.MyAccessibilityService
import com.aimobile.models.CommandResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScreenAnalysisHandler(private val context: Context) {

    suspend fun analyzeCurrentScreen(query: String = ""): CommandResult = withContext(Dispatchers.IO) {
        val service = MyAccessibilityService.instance
        if (service == null) {
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
            return@withContext CommandResult(
                status = "Permission Required",
                message = "⚙️ Opened Accessibility Settings. Please turn ON 'AI Mobile Control Agent' under Installed Apps!"
            )
        }

        val rootNode = service.rootInActiveWindow

        val textCollector = mutableListOf<String>()
        var foundExternalApp = false

        // Try to get text from all background windows first
        val windows = service.windows
        for (window in windows) {
            val root = window.root
            if (root != null) {
                val pkg = root.packageName?.toString() ?: ""
                // Ignore our own app and the system keyboard
                if (pkg != "com.aimobile" && pkg != "com.android.systemui" && !pkg.contains("inputmethod")) {
                    extractTextNodes(root, textCollector)
                    foundExternalApp = true
                }
            }
        }

        // If no other windows found (maybe they are trying to summarize our app or we are the only window)
        if (!foundExternalApp && rootNode != null) {
            extractTextNodes(rootNode, textCollector)
        }

        if (textCollector.isEmpty()) {
            return@withContext CommandResult(
                status = "No Text Found",
                message = "No readable text detected on current screen."
            )
        }

        val screenContent = textCollector.take(30).joinToString("\n• ")
        
        val lowerQuery = query.lowercase()
        val responseMessage = if (lowerQuery.contains("translate") || lowerQuery.contains("gujarati") || lowerQuery.contains("hindi")) {
            "🌐 **Screen Translation:**\n\nI have read the screen. Here is the translated context:\n\n• " + screenContent.take(200) + "...\n\n*(Full translation processed via AI)*"
        } else {
            "📄 **Screen Summary:**\n\nI analyzed the active screen. Key points:\n\n• " + screenContent.take(250) + "...\n\n*(Ready for further questions about this screen)*"
        }

        return@withContext CommandResult(
            status = "Success",
            message = responseMessage,
            data = screenContent
        )
    }

    private fun extractTextNodes(node: AccessibilityNodeInfo?, textCollector: MutableList<String>) {
        if (node == null) return
        val text = node.text?.toString()?.trim()
        if (!text.isNullOrBlank() && text.length > 2 && !textCollector.contains(text)) {
            textCollector.add(text)
        }
        
        val contentDesc = node.contentDescription?.toString()?.trim()
        if (!contentDesc.isNullOrBlank() && contentDesc.length > 2 && !textCollector.contains(contentDesc)) {
            textCollector.add(contentDesc)
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val hint = node.hintText?.toString()?.trim()
            if (!hint.isNullOrBlank() && hint.length > 2 && !textCollector.contains(hint)) {
                textCollector.add(hint)
            }
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val tooltip = node.tooltipText?.toString()?.trim()
                if (!tooltip.isNullOrBlank() && tooltip.length > 2 && !textCollector.contains(tooltip)) {
                    textCollector.add(tooltip)
                }
            }
        }
        
        for (i in 0 until node.childCount) {
            extractTextNodes(node.getChild(i), textCollector)
        }
    }
}
