package com.aimobile.command

import com.aimobile.models.RoutineItem
import com.aimobile.voice.repository.VoiceCommandResult
import com.aimobile.voice.repository.VoiceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineExecutor @Inject constructor(
    private val voiceRepository: VoiceRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _executingRoutineId = MutableStateFlow<String?>(null)
    val executingRoutineId: StateFlow<String?> = _executingRoutineId.asStateFlow()

    private val _executionFeedback = MutableStateFlow<String?>(null)
    val executionFeedback: StateFlow<String?> = _executionFeedback.asStateFlow()

    fun executeRoutine(
        routine: RoutineItem, 
        onComplete: ((RoutineItem) -> Unit)? = null
    ) {
        scope.launch {
            if (_executingRoutineId.value != null) return@launch // Already executing

            _executingRoutineId.value = routine.id
            _executionFeedback.value = "Executing routine '${routine.name}'..."
            
            routine.actions.forEachIndexed { index, action ->
                _executionFeedback.value = "Step ${index + 1}/${routine.actions.size}: $action"
                
                val result = voiceRepository.sendVoiceCommand(action)
                if (result is VoiceCommandResult.Success) {
                    try {
                        voiceRepository.executeCommandLocally(result.request)
                    } catch (_: Exception) {}
                }
                
                // Allow enough time for location toggle and GPS lock before next command
                delay(4000)
            }

            _executionFeedback.value = "Routine '${routine.name}' completed successfully!"
            delay(1200)
            
            onComplete?.invoke(routine)
            
            _executingRoutineId.value = null
            _executionFeedback.value = null
        }
    }
}
