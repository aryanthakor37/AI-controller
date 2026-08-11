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
                // Exact text match check or exact match with 'Off'/'On' state
                val exactNode = nodes.firstOrNull { 
                    val t = it.text?.toString()?.trim()?.lowercase() ?: ""
                    val target = text.lowercase()
                    t == target || t == "$target off" || t == "$target\noff" || t == "$target on" || t == "$target\non"
                }
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

    suspend fun clickNodeByContentDescription(service: AccessibilityService, desc: String, retryCount: Int = 3): Boolean {
        addLog("Trying to click content description: $desc")
        for (i in 0 until retryCount) {
            val rootNode = service.rootInActiveWindow ?: continue
            val nodes = rootNode.findAccessibilityNodeInfosByText(desc)
            if (!nodes.isNullOrEmpty()) {
                val match = nodes.firstOrNull { 
                    val d = it.contentDescription?.toString()?.trim()?.lowercase() ?: ""
                    val target = desc.lowercase()
                    d == target || d == "$target off" || d == "$target\noff" || d == "$target on" || d == "$target\non"
                }
                val clicked = performClick(match ?: nodes[0])
                if (clicked) {
                    addLog("Successfully clicked content description: $desc")
                    return true
                }
            }
            delay(1000)
        }
        addLog("Failed to click content description: $desc")
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

    fun performHorizontalScrollGesture(service: AccessibilityService, isScrollRight: Boolean): Boolean {
        val path = Path().apply {
            if (isScrollRight) {
                moveTo(800f, 1000f)
                lineTo(200f, 1000f) // swipe left to view content on right
            } else {
                moveTo(200f, 1000f)
                lineTo(800f, 1000f)
            }
        }
        val gesture = GestureDescription.Builder().apply {
            addStroke(GestureDescription.StrokeDescription(path, 0, 400))
        }.build()

        return service.dispatchGesture(gesture, null, null)
    }

    fun performTap(service: AccessibilityService, x: Float, y: Float): Boolean {
        val path = android.graphics.Path().apply {
            moveTo(x, y)
        }
        val gesture = android.accessibilityservice.GestureDescription.Builder().apply {
            addStroke(android.accessibilityservice.GestureDescription.StrokeDescription(path, 0, 50))
        }.build()
        return service.dispatchGesture(gesture, null, null)
    }

    suspend fun toggleSwitch(service: AccessibilityService, targetState: Boolean? = null, retryCount: Int = 3, targetKeyword: String? = null): Boolean {
        addLog("Trying to auto-toggle switch to state: $targetState for keyword: $targetKeyword")
        for (i in 0 until retryCount) {
            val rootNode = service.rootInActiveWindow ?: continue
            val switchNodes = mutableListOf<AccessibilityNodeInfo>()
            findSwitchNodes(rootNode, switchNodes)
            
            // Filter by keyword if provided (e.g. "dark")
            val targetNodes = if (targetKeyword != null) {
                switchNodes.filter { 
                    (it.text?.toString() ?: it.contentDescription?.toString() ?: "").contains(targetKeyword, ignoreCase = true) ||
                    (it.parent?.text?.toString() ?: it.parent?.contentDescription?.toString() ?: "").contains(targetKeyword, ignoreCase = true)
                }
            } else {
                switchNodes
            }

            if (targetNodes.isNotEmpty()) {
                val node = targetNodes[0]
                var currentState = node.isChecked
                val nodeText = (node.text?.toString() ?: node.contentDescription?.toString() ?: "").lowercase()
                if (nodeText.contains("on") || nodeText.contains("connected")) {
                    currentState = true
                } else if (nodeText.contains("off") || nodeText.contains("disconnected")) {
                    currentState = false
                }

                if (targetState == null || currentState != targetState) {
                    val clicked = performClick(node)
                    if (clicked) {
                        addLog("Successfully clicked switch!")
                        return true
                    }
                } else {
                    addLog("Switch is already in desired state: $targetState")
                    return true
                }
            }
            delay(800)
        }
        addLog("Could not find or click switch node")
        return false
    }

    private fun findSwitchNodes(root: AccessibilityNodeInfo?, result: MutableList<AccessibilityNodeInfo>) {
        if (root == null) return
        val className = root.className?.toString() ?: ""
        if (className.contains("Switch", ignoreCase = true) || className.contains("ToggleButton", ignoreCase = true) || root.isCheckable) {
            result.add(root)
        }
        for (i in 0 until root.childCount) {
            findSwitchNodes(root.getChild(i), result)
        }
    }

    fun getAllTextFromScreen(service: AccessibilityService): String {
        val rootNode = service.rootInActiveWindow ?: return ""
        val stringBuilder = java.lang.StringBuilder()
        extractTextRecursively(rootNode, stringBuilder)
        return stringBuilder.toString().trim()
    }

    private fun extractTextRecursively(node: AccessibilityNodeInfo?, sb: java.lang.StringBuilder) {
        if (node == null) return
        
        val text = node.text?.toString()?.trim()
        val contentDesc = node.contentDescription?.toString()?.trim()
        
        if (!text.isNullOrEmpty()) {
            sb.append(text).append("\n")
        } else if (!contentDesc.isNullOrEmpty()) {
            sb.append(contentDesc).append("\n")
        }

        for (i in 0 until node.childCount) {
            extractTextRecursively(node.getChild(i), sb)
        }
    }
}
