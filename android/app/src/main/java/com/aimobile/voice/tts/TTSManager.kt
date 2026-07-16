package com.aimobile.voice.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TTSManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private var speechRate = 1.0f
    private var speechPitch = 1.0f
    private var preferredLocale: Locale = Locale.getDefault()
    private var isVoiceEnabled = true

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(preferredLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTSManager", "Language not supported or missing data")
            } else {
                tts?.setSpeechRate(speechRate)
                tts?.setPitch(speechPitch)
                _isInitialized.value = true
                Log.d("TTSManager", "TextToSpeech successfully initialized")
            }
        } else {
            Log.e("TTSManager", "Initialization failed")
        }
    }

    fun speak(text: String) {
        if (!isVoiceEnabled) return
        if (_isInitialized.value) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "AgentAI_TTS_ID")
        } else {
            Log.w("TTSManager", "TTS not initialized yet. Skipping speech.")
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun setSpeechRate(rate: Float) {
        speechRate = rate
        tts?.setSpeechRate(rate)
    }

    fun setPitch(pitch: Float) {
        speechPitch = pitch
        tts?.setPitch(pitch)
    }

    fun setLanguage(locale: Locale) {
        preferredLocale = locale
        tts?.setLanguage(locale)
    }

    fun setVoiceEnabled(enabled: Boolean) {
        isVoiceEnabled = enabled
        if (!enabled) {
            stop()
        }
    }

    fun getVoiceEnabled(): Boolean = isVoiceEnabled

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
