package com.aimobile.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.aimobile.ui.screens.FloatingOverlayContent
import com.aimobile.voice.repository.VoiceCommandResult
import com.aimobile.voice.repository.VoiceRepository
import com.aimobile.voice.speech.SpeechRecognitionManager
import com.aimobile.voice.tts.TTSManager
import com.aimobile.voice.viewmodel.VoiceState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest

@AndroidEntryPoint
class FloatingOverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    @Inject
    lateinit var repository: VoiceRepository

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private lateinit var windowManager: WindowManager
    private lateinit var composeView: ComposeView
    private lateinit var params: WindowManager.LayoutParams

    private lateinit var speechRecognitionManager: SpeechRecognitionManager
    private lateinit var ttsManager: TTSManager
    private val serviceScope = CoroutineScope(Dispatchers.Main)

    // States for Compose UI
    private var voiceState by mutableStateOf<VoiceState>(VoiceState.Idle)
    private var transcript by mutableStateOf("")
    private var aiResponse by mutableStateOf("")

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        speechRecognitionManager = SpeechRecognitionManager(this)
        ttsManager = TTSManager(this)

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        composeView = ComposeView(this)

        // Set layout parameters for WindowManager
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 300
        }

        // Set owner components on the view so Compose can compile correctly
        composeView.setViewTreeLifecycleOwner(this)
        composeView.setViewTreeViewModelStoreOwner(this)
        composeView.setViewTreeSavedStateRegistryOwner(this)

        composeView.setContent {
            FloatingOverlayContent(
                voiceState = voiceState,
                transcript = transcript,
                aiResponse = aiResponse,
                onMicClick = { toggleListening() },
                onSendText = { processTextCommand(it) },
                onDrag = { dx, dy -> updatePosition(dx, dy) },
                onCloseClick = { stopSelf() },
                onExpandToggle = { expanded -> toggleFocus(expanded) }
            )
        }

        observeSpeechRecognition()
        observeActivePackage()
    }

    private fun observeSpeechRecognition() {
        serviceScope.launch {
            speechRecognitionManager.transcript.collectLatest { text ->
                transcript = text
            }
        }

        serviceScope.launch {
            speechRecognitionManager.isListening.collectLatest { listening ->
                if (!listening && voiceState == VoiceState.Listening) {
                    val finalTranscript = speechRecognitionManager.transcript.value
                    if (finalTranscript.isNotBlank()) {
                        processTextCommand(finalTranscript)
                    } else {
                        voiceState = VoiceState.Idle
                    }
                }
            }
        }

        serviceScope.launch {
            speechRecognitionManager.error.collectLatest { err ->
                if (err != null) {
                    voiceState = VoiceState.Failed(err)
                    ttsManager.speak("Error: $err")
                }
            }
        }
    }

    private fun toggleListening() {
        if (voiceState == VoiceState.Listening) {
            speechRecognitionManager.stopListening()
        } else {
            // Verify permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                voiceState = VoiceState.PermissionDenied
                ttsManager.speak("Microphone permission is required to listen.")
                return
            }

            aiResponse = ""
            transcript = ""
            voiceState = VoiceState.Listening
            speechRecognitionManager.startListening()
        }
    }

    private fun processTextCommand(text: String) {
        voiceState = VoiceState.Thinking
        serviceScope.launch {
            try {
                when (val result = repository.sendVoiceCommand(text)) {
                    is VoiceCommandResult.Success -> {
                        voiceState = VoiceState.Executing(result.intent)
                        val execResult = repository.executeCommandLocally(result.request)

                        val speakText = if (execResult.status == "Success") {
                            result.reply.ifBlank { "Command executed successfully" }
                        } else {
                            "Sorry, I failed to execute that command: ${execResult.message}"
                        }

                        aiResponse = speakText
                        voiceState = VoiceState.Completed(speakText)
                        ttsManager.speak(speakText)

                        // Fail-safe: Hide the overlay if it successfully opened an app/intent
                        if (execResult.status == "Success" && (
                            result.intent.startsWith("OPEN_") ||
                            result.intent.startsWith("SEARCH_") ||
                            result.intent.startsWith("MESSAGE_") ||
                            result.intent.startsWith("SEND_") ||
                            result.intent.startsWith("CALL_")
                        )) {
                            hideOverlayView()
                        }
                    }
                    is VoiceCommandResult.Error -> {
                        val errorMsg = result.message
                        voiceState = VoiceState.Failed(errorMsg)
                        ttsManager.speak("Sorry, command not recognized or server is offline")
                    }
                }
            } catch (e: Exception) {
                voiceState = VoiceState.Failed(e.message ?: "Execution error")
                ttsManager.speak("Error running command")
            }
        }
    }

    private var isViewAdded = false

    private fun showOverlayView() {
        if (!isViewAdded) {
            try {
                windowManager.addView(composeView, params)
                isViewAdded = true
            } catch (e: Exception) {
                android.util.Log.e("FloatingOverlayService", "Failed to add view: ${e.message}")
            }
        }
    }

    private fun hideOverlayView() {
        if (isViewAdded) {
            try {
                windowManager.removeView(composeView)
                isViewAdded = false
            } catch (e: Exception) {
                android.util.Log.e("FloatingOverlayService", "Failed to remove view: ${e.message}")
            }
        }
    }

    private fun getLauncherPackages(context: Context): List<String> {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolveInfos = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        val list = resolveInfos.map { it.activityInfo.packageName }.toMutableList()
        // Standard fallbacks for system launchers just in case
        list.add("com.android.launcher")
        list.add("com.android.launcher3")
        list.add("com.google.android.apps.nexuslauncher")
        list.add("com.sec.android.app.launcher")
        list.add("com.bbk.launcher2")
        list.add("com.vivo.launcher")
        return list.distinct()
    }

    private fun observeActivePackage() {
        serviceScope.launch {
            val launcherPkgs = getLauncherPackages(this@FloatingOverlayService)
            com.aimobile.accessibility.MyAccessibilityService.currentActivePackageFlow.collectLatest { pkg ->
                val isAccessibilityEnabled = com.aimobile.accessibility.MyAccessibilityService.isServiceEnabled.value
                val isHome = !isAccessibilityEnabled || pkg in launcherPkgs || pkg.isEmpty()

                if (isHome) {
                    showOverlayView()
                } else {
                    hideOverlayView()
                }
            }
        }
    }

    private fun toggleFocus(expanded: Boolean) {
        params.flags = if (expanded) {
            params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
        } else {
            params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        }
        if (isViewAdded) {
            try {
                windowManager.updateViewLayout(composeView, params)
            } catch (_: Exception) {}
        }
    }

    private fun updatePosition(dx: Float, dy: Float) {
        params.x += dx.toInt()
        params.y += dy.toInt()
        if (isViewAdded) {
            try {
                windowManager.updateViewLayout(composeView, params)
            } catch (_: Exception) {}
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        return START_STICKY
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        hideOverlayView()
        speechRecognitionManager.destroy()
        ttsManager.destroy()
        serviceScope.cancel()
        super.onDestroy()
    }
}
