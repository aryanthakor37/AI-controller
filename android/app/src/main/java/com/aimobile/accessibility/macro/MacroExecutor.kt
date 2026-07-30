package com.aimobile.accessibility.macro

import android.content.Context
import android.content.Intent
import android.util.Log
import com.aimobile.accessibility.MyAccessibilityService
import com.aimobile.accessibility.automation.AppAutomations
import com.aimobile.accessibility.automation.AutomationManager
import com.aimobile.data.local.MacroEntity
import com.aimobile.data.local.MacroStep
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MacroExecutor @Inject constructor() {
    private val gson = Gson()

    suspend fun executeMacro(
        context: Context,
        macro: MacroEntity,
        onProgress: (current: Int, total: Int, status: String) -> Unit
    ): Boolean {
        val type = object : TypeToken<List<MacroStep>>() {}.type
        val steps: List<MacroStep> = try {
            gson.fromJson(macro.stepsJson, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e("MacroExecutor", "Error parsing macro JSON: ${e.message}")
            return false
        }
        return executeSteps(context, steps, macro.name, onProgress)
    }
    suspend fun executeDynamicMacro(
        context: Context,
        steps: List<MacroStep>,
        macroName: String,
        onProgress: (current: Int, total: Int, status: String) -> Unit
    ): Boolean {
        return executeSteps(context, steps, macroName, onProgress)
    }

    private suspend fun executeSteps(
        context: Context,
        steps: List<MacroStep>,
        macroName: String,
        onProgress: (current: Int, total: Int, status: String) -> Unit
    ): Boolean {
        if (steps.isEmpty()) {
            onProgress(0, 0, "No steps found in macro '$macroName'")
            return false
        }

        val total = steps.size
        Log.d("MacroExecutor", "Executing macro '$macroName' ($total steps)")

        var currentPackage = ""

        for ((index, step) in steps.withIndex()) {
            val stepNum = index + 1

            val pkgName = step.packageName ?: ""
            if (pkgName.isNotBlank() && pkgName != currentPackage) {
                currentPackage = pkgName
                onProgress(stepNum, total, "Opening app $pkgName...")
                AppAutomations.openApp(context, pkgName)
                delay(1200L)
            }

            val service = MyAccessibilityService.instance
            if (service == null) {
                onProgress(stepNum, total, "Accessibility Service is not enabled!")
                return false
            }

            val action = step.actionType ?: ""
            val target = step.targetValue ?: ""

            val desc = when (action) {
                "CLICK_ID" -> "Clicking '$target'"
                "CLICK_TEXT" -> "Clicking text '$target'"
                "CLICK_DESC" -> "Clicking '$target'"
                "INPUT_TEXT" -> "Entering '$target'"
                "SCROLL_DOWN" -> "Scrolling down"
                "SCROLL_UP" -> "Scrolling up"
                else -> "Executing step $stepNum"
            }

            onProgress(stepNum, total, desc)
            delay((step.delayMs ?: 800L).coerceAtLeast(400L))

            val success = when (action) {
                "CLICK_ID" -> AutomationManager.clickNodeById(service, target)
                "CLICK_TEXT" -> AutomationManager.clickNodeByText(service, target)
                "CLICK_DESC" -> AutomationManager.clickNodeByContentDescription(service, target)
                "INPUT_TEXT" -> AutomationManager.findAndInputText(service, target)
                "SCROLL_DOWN" -> AutomationManager.performScroll(service, isScrollDown = true)
                "SCROLL_UP" -> AutomationManager.performScroll(service, isScrollDown = false)
                else -> true
            }

            if (!success) {
                Log.w("MacroExecutor", "Step $stepNum failed: $action $target")
            }
        }

        onProgress(total, total, "Completed macro '$macroName'")
        return true
    }
}
