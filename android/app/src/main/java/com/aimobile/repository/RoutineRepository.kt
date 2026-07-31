package com.aimobile.repository

import android.content.Context
import android.content.SharedPreferences
import com.aimobile.models.RoutineItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ai_routines", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val defaultRoutines = listOf(
        RoutineItem(
            id = "r1",
            name = "Good Morning",
            description = "WiFi ON + Brightness 80% + Open Spotify",
            scheduleTime = "07:00 AM Daily",
            actions = listOf("Turn on WiFi", "Set brightness to 80%", "Open Spotify and play focus music"),
            isEnabled = true,
            category = "Morning"
        ),
        RoutineItem(
            id = "r2",
            name = "Office Mode",
            description = "Silent + Maps + Bluetooth ON",
            scheduleTime = "09:00 AM Weekdays",
            actions = listOf("Mute phone volume", "Open Google Maps", "Turn on Bluetooth"),
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

    private val _routines = MutableStateFlow<List<RoutineItem>>(loadRoutines())
    val routines: StateFlow<List<RoutineItem>> = _routines.asStateFlow()

    private fun loadRoutines(): List<RoutineItem> {
        val json = prefs.getString("routines_list", null)
        return if (json != null) {
            val type = object : TypeToken<List<RoutineItem>>() {}.type
            try {
                gson.fromJson(json, type)
            } catch (e: Exception) {
                defaultRoutines
            }
        } else {
            defaultRoutines
        }
    }

    private fun saveRoutines(list: List<RoutineItem>) {
        val json = gson.toJson(list)
        prefs.edit().putString("routines_list", json).apply()
        _routines.value = list
    }

    fun addOrUpdateRoutine(routine: RoutineItem) {
        val list = _routines.value.toMutableList()
        val index = list.indexOfFirst { it.id == routine.id }
        if (index >= 0) {
            list[index] = routine
        } else {
            list.add(0, routine)
        }
        saveRoutines(list)
    }

    fun deleteRoutine(id: String) {
        val list = _routines.value.filter { it.id != id }
        saveRoutines(list)
    }

    fun toggleRoutine(id: String) {
        val list = _routines.value.map {
            if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it
        }
        saveRoutines(list)
    }

    fun findRoutineByName(name: String): RoutineItem? {
        val cleanName = name.lowercase().trim()
        return _routines.value.find { it.name.lowercase().trim() == cleanName }
    }
}
