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

            // 1. Try to click a search button/icon using AutomationManager (which handles unclickable nodes by clicking parents)
            var clicked = AutomationManager.clickNodeByContentDescription(service, "search", 2)
            if (!clicked) clicked = AutomationManager.clickNodeByContentDescription(service, "Search", 1)
            if (!clicked) clicked = AutomationManager.clickNodeByText(service, "Search", 1)
            if (!clicked) clicked = AutomationManager.clickNodeByText(service, "search", 1)
            
            if (clicked) {
                delay(1000)
            }

            // 2. Try to find the focused input node or any EditText and paste the query
            val inputSuccess = AutomationManager.findAndInputText(service, query, 3)
            if (inputSuccess) {
                Log.d("UniversalSearch", "Successfully inputted text into focused node")
                
                // Optional: try to click enter/search action on keyboard, or click a "Search" button again
            } else {
                // Fallback: manually find an EditText
                var searchNode = findSearchNode(service.rootInActiveWindow)
                if (searchNode != null) {
                    Log.d("UniversalSearch", "Found fallback search node, typing query: $query")
                    val arguments = Bundle()
                    arguments.putCharSequence(
                        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                        query
                    )
                    searchNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                } else {
                    Log.d("UniversalSearch", "Could not find any search bar in $packageName")
                }
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
