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

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Can monitor target package changes if needed
        event?.let {
            if (it.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                currentActivePackage = it.packageName?.toString() ?: ""
                Log.d("AccessibilityService", "Window State Changed: $currentActivePackage")
            }
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
    }
}
