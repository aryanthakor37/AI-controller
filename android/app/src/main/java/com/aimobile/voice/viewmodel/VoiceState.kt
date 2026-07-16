package com.aimobile.voice.viewmodel

sealed class VoiceState {
    object Idle : VoiceState()
    object Listening : VoiceState()
    object Processing : VoiceState() // converting speech / waiting for Gemini
    object Thinking : VoiceState() // thinking animation
    data class Executing(val command: String) : VoiceState() // executing intent
    data class Completed(val message: String) : VoiceState()
    data class Failed(val error: String) : VoiceState()
    object PermissionDenied : VoiceState()
    object Offline : VoiceState()
}
