package com.aimobile.accessibility.macro

import android.util.Log
import com.aimobile.data.local.MacroDao
import com.aimobile.data.local.MacroEntity
import com.aimobile.data.local.MacroStep
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MacroRecorderManager @Inject constructor(
    private val macroDao: MacroDao
) {
    private val gson = Gson()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingName = MutableStateFlow("")
    val recordingName: StateFlow<String> = _recordingName.asStateFlow()

    private val _recordedSteps = MutableStateFlow<List<MacroStep>>(emptyList())
    val recordedSteps: StateFlow<List<MacroStep>> = _recordedSteps.asStateFlow()

    private var lastRecordedTime = System.currentTimeMillis()

    fun startRecording(name: String) {
        _recordingName.value = name.ifBlank { "Recorded Macro ${System.currentTimeMillis() / 1000}" }
        _recordedSteps.value = emptyList()
        lastRecordedTime = System.currentTimeMillis()
        _isRecording.value = true
        Log.d("MacroRecorderManager", "Started recording macro: ${recordingName.value}")
    }

    suspend fun stopRecording(): MacroEntity? {
        if (!_isRecording.value) return null

        _isRecording.value = false
        val steps = _recordedSteps.value
        val name = _recordingName.value

        if (steps.isEmpty()) {
            Log.d("MacroRecorderManager", "No steps recorded for $name")
            return null
        }

        val json = gson.toJson(steps)
        val entity = MacroEntity(
            name = name,
            triggerPhrase = name,
            stepsJson = json
        )

        val id = macroDao.insertMacro(entity)
        Log.d("MacroRecorderManager", "Saved macro '$name' with ID $id and ${steps.size} steps")
        return entity.copy(id = id.toInt())
    }

    fun recordClick(packageName: String, viewId: String?, text: String?, contentDesc: String?) {
        if (!_isRecording.value) return

        val now = System.currentTimeMillis()
        val delay = (now - lastRecordedTime).coerceAtLeast(300L)
        lastRecordedTime = now

        val (actionType, targetValue) = when {
            !viewId.isNullOrBlank() -> "CLICK_ID" to viewId
            !text.isNullOrBlank() -> "CLICK_TEXT" to text
            !contentDesc.isNullOrBlank() -> "CLICK_DESC" to contentDesc
            else -> return
        }

        val newStep = MacroStep(
            stepOrder = _recordedSteps.value.size + 1,
            packageName = packageName,
            actionType = actionType,
            targetValue = targetValue,
            delayMs = delay
        )

        _recordedSteps.value = _recordedSteps.value + newStep
        Log.d("MacroRecorderManager", "Recorded step #${newStep.stepOrder}: $actionType -> $targetValue")
    }

    fun recordTextInput(packageName: String, text: String) {
        if (!_isRecording.value || text.isBlank()) return

        val now = System.currentTimeMillis()
        val delay = (now - lastRecordedTime).coerceAtLeast(300L)
        lastRecordedTime = now

        val newStep = MacroStep(
            stepOrder = _recordedSteps.value.size + 1,
            packageName = packageName,
            actionType = "INPUT_TEXT",
            targetValue = text,
            delayMs = delay
        )

        _recordedSteps.value = _recordedSteps.value + newStep
        Log.d("MacroRecorderManager", "Recorded text input step #${newStep.stepOrder}: $text")
    }
}
