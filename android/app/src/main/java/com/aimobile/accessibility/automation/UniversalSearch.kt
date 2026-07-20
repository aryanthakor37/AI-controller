package com.aimobile.accessibility.automation

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.aimobile.models.CommandResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object UniversalSearch {

    fun runUniversalSearch(
        service: AccessibilityService,
        context: Context,
        packageName: String,
        query: String
    ): CommandResult {
        
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent == null) {
            return CommandResult("Failed", "App not found or cannot be launched: $packageName")
        }

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(launchIntent)

        CoroutineScope(Dispatchers.Main).launch {
            Log.d("UniversalSearch", "Waiting for app to load...")
            delay(2500) // Wait for app to open and render

            var searchNode = findSearchNode(service.rootInActiveWindow)
            if (searchNode == null) {
                // Sometime search is a button that opens a text field
                val searchBtn = findSearchIcon(service.rootInActiveWindow)
                if (searchBtn != null) {
                    searchBtn.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    delay(1000)
                    searchNode = findSearchNode(service.rootInActiveWindow)
                }
            }

            if (searchNode != null) {
                Log.d("UniversalSearch", "Found search node, typing query: $query")
                val arguments = Bundle()
                arguments.putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    query
                )
                searchNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                delay(500)
                
                // Try to trigger search action
                // In many apps, setting text is enough to trigger live search
                // Or we can try to press Enter (IME action) if supported
                searchNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                
            } else {
                Log.d("UniversalSearch", "Could not find any search bar in $packageName")
            }
        }
        
        return CommandResult("Success", "Searching '$query' in app.")
    }

    private fun findSearchNode(root: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (root == null) return null

        // Check if this node is an EditText that is meant for searching
        if (root.className?.toString()?.contains("EditText") == true) {
            return root
        }
        
        if (root.className?.toString()?.contains("Search") == true) {
            if (root.isEditable) return root
        }

        // Recursively search children
        for (i in 0 until root.childCount) {
            val child = root.getChild(i)
            val result = findSearchNode(child)
            if (result != null) return result
        }
        return null
    }

    private fun findSearchIcon(root: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (root == null) return null
        
        val contentDesc = root.contentDescription?.toString()?.lowercase() ?: ""
        val text = root.text?.toString()?.lowercase() ?: ""
        val viewId = root.viewIdResourceName?.toString()?.lowercase() ?: ""
        
        if (root.isClickable && (contentDesc.contains("search") || text.contains("search") || viewId.contains("search"))) {
            return root
        }

        for (i in 0 until root.childCount) {
            val child = root.getChild(i)
            val result = findSearchIcon(child)
            if (result != null) return result
        }
        return null
    }
}
