package com.aimobile.accessibility.automation

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.aimobile.accessibility.MyAccessibilityService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AutomationManager {

    private val _automationLog = MutableStateFlow<List<String>>(emptyList())
    val automationLog: StateFlow<List<String>> = _automationLog.asStateFlow()

    fun addLog(msg: String) {
        Log.d("AutomationManager", msg)
        _automationLog.value = _automationLog.value + msg
    }

    suspend fun clickNodeById(service: AccessibilityService, viewId: String, retryCount: Int = 3): Boolean {
        addLog("Trying to click ID: $viewId")
        for (i in 0 until retryCount) {
            val rootNode = service.rootInActiveWindow ?: continue
            val nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId)
            if (!nodes.isNullOrEmpty()) {
                val clicked = performClick(nodes[0])
                if (clicked) {
                    addLog("Successfully clicked ID: $viewId")
                    return true
                }
            }
            delay(1000)
        }
        addLog("Failed to click ID: $viewId after $retryCount retries")
        return false
    }

    suspend fun clickNodeByText(service: AccessibilityService, text: String, retryCount: Int = 3): Boolean {
        addLog("Trying to click text: $text")
        for (i in 0 until retryCount) {
            val rootNode = service.rootInActiveWindow ?: continue
            val nodes = rootNode.findAccessibilityNodeInfosByText(text)
            if (!nodes.isNullOrEmpty()) {
                // Exact text match check
                val exactNode = nodes.firstOrNull { it.text?.toString()?.equals(text, ignoreCase = true) == true }
                val clicked = performClick(exactNode ?: nodes[0])
                if (clicked) {
                    addLog("Successfully clicked text: $text")
                    return true
                }
            }
            delay(1000)
        }
        addLog("Failed to click text: $text")
        return false
    }

    suspend fun inputTextIntoId(service: AccessibilityService, viewId: String, text: String, retryCount: Int = 3): Boolean {
        addLog("Trying to input text into ID: $viewId")
        for (i in 0 until retryCount) {
            val rootNode = service.rootInActiveWindow ?: continue
            val nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId)
            if (!nodes.isNullOrEmpty()) {
                val inputNode = nodes[0]
                val arguments = Bundle().apply {
                    putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
                }
                val set = inputNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                if (set) {
                    addLog("Successfully entered text into ID: $viewId")
                    return true
                }
            }
            delay(1000)
        }
        addLog("Failed to enter text into ID: $viewId")
        return false
    }

    suspend fun findAndInputText(service: AccessibilityService, text: String, retryCount: Int = 3): Boolean {
        addLog("Looking for focused input node...")
        for (i in 0 until retryCount) {
            val rootNode = service.rootInActiveWindow ?: continue
            val focusedNode = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            if (focusedNode != null) {
                val arguments = Bundle().apply {
                    putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
                }
                val set = focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                if (set) {
                    addLog("Successfully entered text into focused node")
                    return true
                }
            }
            delay(1000)
        }
        return false
    }

    fun performClick(node: AccessibilityNodeInfo?): Boolean {
        var tempNode = node
        while (tempNode != null) {
            if (tempNode.isClickable) {
                return tempNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            tempNode = tempNode.parent
        }
        return false
    }

    suspend fun performScroll(service: AccessibilityService, isScrollDown: Boolean = true): Boolean {
        val rootNode = service.rootInActiveWindow ?: return false
        val scrollableNode = findScrollableNode(rootNode)
        return if (scrollableNode != null) {
            val action = if (isScrollDown) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
            scrollableNode.performAction(action)
        } else {
            // Fallback: Perform drag gesture using coordinates
            performScrollGesture(service, isScrollDown)
        }
    }

    private fun findScrollableNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (root.isScrollable) return root
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            val found = findScrollableNode(child)
            if (found != null) return found
        }
        return null
    }

    private fun performScrollGesture(service: AccessibilityService, isScrollDown: Boolean): Boolean {
        val path = Path().apply {
            if (isScrollDown) {
                moveTo(500f, 1500f)
                lineTo(500f, 500f)
            } else {
                moveTo(500f, 500f)
                lineTo(500f, 1500f)
            }
        }
        val gesture = GestureDescription.Builder().apply {
            addStroke(GestureDescription.StrokeDescription(path, 0, 400))
        }.build()

        return service.dispatchGesture(gesture, null, null)
    }
}
