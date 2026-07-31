package com.aimobile.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aimobile.router.IntentRouter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.aimobile.models.RoutineItem
import com.aimobile.repository.RoutineRepository
import com.aimobile.command.RoutineExecutor

@HiltViewModel
class AutomationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val routineRepository: RoutineRepository,
    private val routineExecutor: RoutineExecutor
) : ViewModel() {

    val routines: StateFlow<List<RoutineItem>> = routineRepository.routines
    val executingRoutineId: StateFlow<String?> = routineExecutor.executingRoutineId
    val executionFeedback: StateFlow<String?> = routineExecutor.executionFeedback

    fun toggleRoutine(id: String) {
        routineRepository.toggleRoutine(id)
    }

    fun deleteRoutine(id: String) {
        routineRepository.deleteRoutine(id)
    }

    fun addOrUpdateRoutine(routine: RoutineItem) {
        routineRepository.addOrUpdateRoutine(routine)
    }

    fun executeRoutine(routine: RoutineItem) {
        routineExecutor.executeRoutine(routine) { completedRoutine ->
            val nowTime = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US).format(java.util.Date())
            val updated = completedRoutine.copy(lastRun = "Today at $nowTime")
            routineRepository.addOrUpdateRoutine(updated)
        }
    }
}
