package com.aimobile.voice

import android.content.Context
import com.aimobile.utils.AiLogger

enum class HotwordState {
    DISABLED,
    READY,
    LISTENING,
    TRIGGERED
}

interface HotwordListener {
    fun onHotwordDetected(phrase: String)
    fun onError(error: String)
}

class HotwordArchitectureManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: HotwordArchitectureManager? = null

        fun getInstance(context: Context): HotwordArchitectureManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HotwordArchitectureManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private var currentState: HotwordState = HotwordState.DISABLED
    private val listeners = mutableListOf<HotwordListener>()
    private var speechRecognizer: android.speech.SpeechRecognizer? = null
    private var isListening = false
    private var hotwordPhrase = "hey phone ai"

    fun getState(): HotwordState = currentState

    fun registerListener(listener: HotwordListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun unregisterListener(listener: HotwordListener) {
        listeners.remove(listener)
    }

    fun prepareEngine(phrase: String = "hey phone ai", accessKey: String? = null) {
        hotwordPhrase = phrase.lowercase()
        AiLogger.logServiceStart("HotwordArchitectureEngine (Native) ($phrase)")
        currentState = HotwordState.READY
    }

    fun startListening() {
        if (currentState == HotwordState.READY || currentState == HotwordState.DISABLED) {
            isListening = true
            // Native SpeechRecognizer disabled as per user request to stop battery drain and beeps
            // startSpeechRecognizer()
        }
    }

    private fun startSpeechRecognizer() {
        if (!isListening) return

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        val currentVolume = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)
        // Mute stream to suppress the beep
        audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, 0, 0)

        android.os.Handler(android.os.Looper.getMainLooper()).post {
            speechRecognizer = android.speech.SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : android.speech.RecognitionListener {
                override fun onReadyForSpeech(params: android.os.Bundle?) {
                    currentState = HotwordState.LISTENING
                    // Unmute after start
                    audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, currentVolume, 0)
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}

                override fun onError(error: Int) {
                    if (isListening && error != android.speech.SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                        restartListening()
                    }
                }

                override fun onResults(results: android.os.Bundle?) {
                    val matches = results?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                    if (matches != null) {
                        for (match in matches) {
                            val lowerMatch = match.lowercase()
                            if (lowerMatch.contains(hotwordPhrase) || lowerMatch.contains("hey phone") || lowerMatch.contains("phone ai")) {
                                notifyTrigger(hotwordPhrase)
                                return // Stop restarting immediately to let MainService take over
                            }
                        }
                    }
                    if (isListening) restartListening()
                }

                override fun onPartialResults(partialResults: android.os.Bundle?) {}
                override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
            })

            val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
                putExtra(android.speech.RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
                putExtra(android.speech.RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
                putExtra(android.speech.RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 15000)
            }
            try {
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, currentVolume, 0)
            }
        }
    }

    private fun restartListening() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        if (isListening) {
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                startSpeechRecognizer()
            }, 500)
        }
    }

    fun stopListening() {
        isListening = false
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        currentState = HotwordState.READY
        AiLogger.logServiceStart("Hotword Engine Stopped")
    }

    fun notifyTrigger(phrase: String) {
        stopListening()
        currentState = HotwordState.TRIGGERED
        AiLogger.logCommandExecuted("HOTWORD_TRIGGER", phrase, 0)
        listeners.forEach { it.onHotwordDetected(phrase) }
        currentState = HotwordState.READY
    }

    fun destroy() {
        stopListening()
        currentState = HotwordState.DISABLED
    }
}


