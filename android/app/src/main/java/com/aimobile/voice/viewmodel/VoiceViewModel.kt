package com.aimobile.voice.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aimobile.voice.permission.VoicePermissionManager
import com.aimobile.voice.repository.VoiceRepository
import com.aimobile.voice.repository.VoiceCommandResult
import com.aimobile.voice.speech.SpeechRecognitionManager
import com.aimobile.voice.tts.TTSManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class VoiceViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: VoiceRepository
) : ViewModel() {

    private val speechRecognitionManager = SpeechRecognitionManager(context)
    private val ttsManager = TTSManager(context)
    private val permissionManager = VoicePermissionManager(context)

    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    val transcript: StateFlow<String> = speechRecognitionManager.transcript
    val isListening: StateFlow<Boolean> = speechRecognitionManager.isListening

    private val _aiResponse = MutableStateFlow("")
    val aiResponse: StateFlow<String> = _aiResponse.asStateFlow()

    // Settings
    private val _isVoiceEnabled = MutableStateFlow(true)
    val isVoiceEnabled: StateFlow<Boolean> = _isVoiceEnabled.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _speechPitch = MutableStateFlow(1.0f)
    val speechPitch: StateFlow<Float> = _speechPitch.asStateFlow()

    init {
        // Collect Speech recognition outcomes
        viewModelScope.launch {
            speechRecognitionManager.transcript.collectLatest { text ->
                if (text.isNotBlank() && _voiceState.value == VoiceState.Listening) {
                    // Speech finished or partial text received
                }
            }
        }

        viewModelScope.launch {
            speechRecognitionManager.isListening.collectLatest { listening ->
                if (!listening && _voiceState.value == VoiceState.Listening) {
                    val finalTranscript = speechRecognitionManager.transcript.value
                    if (finalTranscript.isNotBlank()) {
                        processTranscript(finalTranscript)
                    } else {
                        _voiceState.value = VoiceState.Idle
                    }
                }
            }
        }

        viewModelScope.launch {
            speechRecognitionManager.error.collectLatest { err ->
                if (err != null) {
                    _voiceState.value = VoiceState.Failed(err)
                    ttsManager.speak("Sorry, I encountered an error: $err")
                }
            }
        }
    }

    fun startListening() {
        if (!permissionManager.hasRecordAudioPermission()) {
            _voiceState.value = VoiceState.PermissionDenied
            return
        }
        _aiResponse.value = ""
        _voiceState.value = VoiceState.Listening
        speechRecognitionManager.startListening()
    }

    fun stopListening() {
        speechRecognitionManager.stopListening()
    }

    fun toggleVoiceEnabled(enabled: Boolean) {
        _isVoiceEnabled.value = enabled
        ttsManager.setVoiceEnabled(enabled)
    }

    fun setSpeechRate(rate: Float) {
        _speechRate.value = rate
        ttsManager.setSpeechRate(rate)
    }

    fun setSpeechPitch(pitch: Float) {
        _speechPitch.value = pitch
        ttsManager.setPitch(pitch)
    }

    fun retry() {
        startListening()
    }

    fun cancel() {
        stopListening()
        ttsManager.stop()
        _voiceState.value = VoiceState.Idle
    }

    private fun processTranscript(text: String) {
        _voiceState.value = VoiceState.Thinking
        viewModelScope.launch {
            when (val result = repository.sendVoiceCommand(text)) {
                is VoiceCommandResult.Success -> {
                    _voiceState.value = VoiceState.Executing(result.intent)
                    val execResult = repository.executeCommandLocally(result.request)
                    
                    val speakText = if (execResult.status == "Success") {
                        result.reply.ifBlank { "Command executed successfully" }
                    } else {
                        "Sorry, I failed to execute that command: ${execResult.message}"
                    }

                    _aiResponse.value = speakText
                    _voiceState.value = VoiceState.Completed(speakText)
                    ttsManager.speak(speakText)
                }
                is VoiceCommandResult.Error -> {
                    val errorMsg = result.message
                    _voiceState.value = VoiceState.Failed(errorMsg)
                    ttsManager.speak("Sorry, command not recognized or server is offline")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognitionManager.destroy()
        ttsManager.destroy()
    }
}
