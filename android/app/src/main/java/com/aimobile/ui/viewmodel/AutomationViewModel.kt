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

data class RoutineItem(
    val id: String,
    val name: String,
    val description: String,
    val scheduleTime: String,
    val actions: List<String>,
    val isEnabled: Boolean = true,
    val category: String = "General",
    val lastRun: String = "Never"
)

@HiltViewModel
class AutomationViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val intentRouter = IntentRouter(context)

    private val defaultRoutines = listOf(
        RoutineItem(
            id = "r1",
            name = "Good Morning",
            description = "WiFi ON + Brightness 80% + Open Spotify",
            scheduleTime = "07:00 AM Daily",
            actions = listOf("Turn on WiFi connection", "Set brightness to 80%", "Open Spotify and play focus music"),
            isEnabled = true,
            category = "Morning"
        ),
        RoutineItem(
            id = "r2",
            name = "Office Mode",
            description = "Silent + Maps + Bluetooth ON",
            scheduleTime = "09:00 AM Weekdays",
            actions = listOf("Mute phone volume", "Open Google Maps", "Turn on Bluetooth connection"),
            isEnabled = true,
            category = "Focus"
        ),
        RoutineItem(
            id = "r3",
            name = "Sleep Mode",
            description = "DND + Alarm + Brightness 10%",
            scheduleTime = "10:30 PM Daily",
            actions = listOf("Mute phone volume", "Set alarm for 7 AM", "Set brightness to 10%"),
            isEnabled = false,
            category = "Night"
        )
    )

    private val _routines = MutableStateFlow<List<RoutineItem>>(defaultRoutines)
    val routines: StateFlow<List<RoutineItem>> = _routines.asStateFlow()

    private val _executingRoutineId = MutableStateFlow<String?>(null)
    val executingRoutineId: StateFlow<String?> = _executingRoutineId.asStateFlow()

    private val _executionFeedback = MutableStateFlow<String?>(null)
    val executionFeedback: StateFlow<String?> = _executionFeedback.asStateFlow()

    fun toggleRoutine(id: String) {
        _routines.value = _routines.value.map {
            if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it
        }
    }

    fun deleteRoutine(id: String) {
        _routines.value = _routines.value.filter { it.id != id }
    }

    fun addOrUpdateRoutine(routine: RoutineItem) {
        val list = _routines.value.toMutableList()
        val index = list.indexOfFirst { it.id == routine.id }
        if (index >= 0) {
            list[index] = routine
        } else {
            list.add(0, routine)
        }
        _routines.value = list
    }

    fun executeRoutine(routine: RoutineItem) {
        viewModelScope.launch {
            _executingRoutineId.value = routine.id
            _executionFeedback.value = "Executing routine '${routine.name}'..."
            
            routine.actions.forEachIndexed { index, action ->
                _executionFeedback.value = "Step ${index + 1}/${routine.actions.size}: $action"
                val req = parseActionToCommandRequest(action)
                if (req != null) {
                    try { intentRouter.route(req) } catch (_: Exception) {}
                }
                delay(1000)
            }

            val nowTime = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US).format(java.util.Date())
            _routines.value = _routines.value.map {
                if (it.id == routine.id) it.copy(lastRun = "Today at $nowTime") else it
            }

            _executionFeedback.value = "Routine '${routine.name}' completed successfully!"
            delay(1200)
            _executingRoutineId.value = null
            _executionFeedback.value = null
        }
    }

    private fun parseActionToCommandRequest(action: String): com.aimobile.models.CommandRequest? {
        val clean = action.lowercase(java.util.Locale.getDefault()).trim()
        return when {
            clean.contains("wifi") -> com.aimobile.models.CommandRequest(intent = "OPEN_APP", app = "Wifi")
            clean.contains("bluetooth") -> com.aimobile.models.CommandRequest(intent = "OPEN_APP", app = "Bluetooth")
            clean.contains("brightness") || clean.contains("display") -> com.aimobile.models.CommandRequest(intent = "OPEN_APP", app = "Brightness")
            clean.contains("silent") || clean.contains("mute") || clean.contains("dnd") -> com.aimobile.models.CommandRequest(intent = "MUTE_VOLUME")
            clean.contains("maps") || clean.contains("navigation") -> com.aimobile.models.CommandRequest(intent = "OPEN_MAPS")
            clean.contains("spotify") -> com.aimobile.models.CommandRequest(intent = "OPEN_APP", app = "Spotify")
            clean.contains("alarm") -> com.aimobile.models.CommandRequest(intent = "SET_ALARM", time = "07:00")
            clean.contains("weather") -> com.aimobile.models.CommandRequest(intent = "CHECK_WEATHER", query = "weather today")
            else -> com.aimobile.models.CommandRequest(intent = "OPEN_APP", app = action)
        }
    }
}
