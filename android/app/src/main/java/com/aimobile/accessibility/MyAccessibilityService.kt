package com.aimobile.accessibility

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MyAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("AccessibilityService", "Service Connected")
        instance = this
        _isServiceEnabled.value = true
    }

    var macroRecorderManager: com.aimobile.accessibility.macro.MacroRecorderManager? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            if (it.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                currentActivePackage = it.packageName?.toString() ?: ""
                Log.d("AccessibilityService", "Window State Changed: $currentActivePackage")
            }

            if (it.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || 
                it.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                autoAcceptScreenCapturePrompt()
            }

            // Macro recording event listener
            macroRecorderManager?.let { recorder ->
                if (recorder.isRecording.value) {
                    val pkg = it.packageName?.toString() ?: currentActivePackage
                    when (it.eventType) {
                        AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                            val node = it.source
                            val viewId = node?.viewIdResourceName
                            val text = it.text?.firstOrNull()?.toString() ?: node?.text?.toString()
                            val desc = it.contentDescription?.toString() ?: node?.contentDescription?.toString()
                            recorder.recordClick(pkg, viewId, text, desc)
                        }
                        AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                            val typedText = it.text?.firstOrNull()?.toString()
                            if (!typedText.isNullOrBlank()) {
                                recorder.recordTextInput(pkg, typedText)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun autoAcceptScreenCapturePrompt() {
        val root = rootInActiveWindow ?: return
        val promptKeywords = listOf("Start now", "Start recording", "Start casting", "Start", "Allow", "ચાલુ કરો", "શરૂ કરો", "शुरू करें")
        for (keyword in promptKeywords) {
            val nodes = root.findAccessibilityNodeInfosByText(keyword)
            if (!nodes.isNullOrEmpty()) {
                for (node in nodes) {
                    val text = (node.text?.toString() ?: node.contentDescription?.toString() ?: "").trim().lowercase()
                    if (text == keyword.lowercase() || text == "start now" || text == "start recording" || text == "start") {
                        com.aimobile.accessibility.automation.AutomationManager.performClick(node)
                        Log.d("AccessibilityService", "Auto-clicked screen capture prompt button: $keyword")
                        return
                    }
                }
            }
        }
    }

    fun performGesture(startXPercent: Float, startYPercent: Float, endXPercent: Float, endYPercent: Float, durationMs: Long) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            try {
                val metrics = resources.displayMetrics
                val screenWidth = metrics.widthPixels
                val screenHeight = metrics.heightPixels

                val startX = startXPercent * screenWidth
                val startY = startYPercent * screenHeight
                val endX = endXPercent * screenWidth
                val endY = endYPercent * screenHeight

                val path = android.graphics.Path()
                path.moveTo(startX, startY)
                path.lineTo(endX, endY)

                val duration = if (durationMs < 50L) 50L else durationMs
                
                val stroke = android.accessibilityservice.GestureDescription.StrokeDescription(path, 0, duration)
                val gesture = android.accessibilityservice.GestureDescription.Builder().addStroke(stroke).build()
                
                dispatchGesture(gesture, null, null)
                Log.d("AccessibilityService", "Dispatched gesture: start($startX, $startY) to end($endX, $endY) dur:$duration")
            } catch (e: Exception) {
                Log.e("AccessibilityService", "Failed to dispatch gesture", e)
            }
        }
    }

    companion object {
        var instance: MyAccessibilityService? = null
            private set

        private val _isServiceEnabled = MutableStateFlow(false)
        val isServiceEnabled: StateFlow<Boolean> = _isServiceEnabled.asStateFlow()

        private val _currentActivePackageFlow = MutableStateFlow("")
        val currentActivePackageFlow = _currentActivePackageFlow.asStateFlow()

        var currentActivePackage: String
            get() = _currentActivePackageFlow.value
            set(value) {
                _currentActivePackageFlow.value = value
            }
            
        // Buffer to prevent dropped characters during fast typing
        private var localTextBuffer: String = ""
        private var lastInjectTime: Long = 0
        private var lastInjectedNodeId: String? = null
    }

    private fun findFocusedEditableNode(node: android.view.accessibility.AccessibilityNodeInfo?): android.view.accessibility.AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.isFocused && node.isEditable) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val focusedChild = findFocusedEditableNode(child)
            if (focusedChild != null) return focusedChild
        }
        return null
    }

    fun injectTextToFocusedNode(text: String) {
        val root = rootInActiveWindow ?: return
        var focusedNode = root.findFocus(android.view.accessibility.AccessibilityNodeInfo.FOCUS_INPUT)
        
        // Fallback: Recursively search if findFocus returns null
        if (focusedNode == null) {
            focusedNode = findFocusedEditableNode(root)
        }
        
        if (focusedNode != null) {
            val systemText = focusedNode.text?.toString() ?: ""
            val currentTime = System.currentTimeMillis()
            val nodeId = focusedNode.viewIdResourceName ?: focusedNode.hashCode().toString()
            
            // If it's been more than 1.5 seconds since the last keystroke, or we switched text boxes,
            // we re-sync our local buffer with the actual text on the screen.
            if (currentTime - lastInjectTime > 1500 || lastInjectedNodeId != nodeId) {
                localTextBuffer = systemText
                lastInjectedNodeId = nodeId
            }
            
            // Apply the new keystroke to our fast local buffer
            if (text == "\b" || text == "Backspace") {
                if (localTextBuffer.isNotEmpty()) {
                    localTextBuffer = localTextBuffer.dropLast(1)
                }
            } else if (text == "\n" || text == "Enter") {
                localTextBuffer += "\n"
            } else {
                localTextBuffer += text
            }
            
            lastInjectTime = currentTime
            
            val arguments = android.os.Bundle()
            arguments.putCharSequence(android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, localTextBuffer)
            focusedNode.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            
            // Important: We don't recycle focusedNode here if we plan to use it again, but since this is the end:
            focusedNode.recycle()
            Log.d("AccessibilityService", "Injected text: $localTextBuffer")
        } else {
            Log.d("AccessibilityService", "No input focused node found to inject text")
        }
    }

    override fun onInterrupt() {
        Log.d("AccessibilityService", "Service Interrupted")
        _isServiceEnabled.value = false
        instance = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("AccessibilityService", "Service Destroyed")
        _isServiceEnabled.value = false
        instance = null
    }
}
