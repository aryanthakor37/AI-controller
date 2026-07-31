package com.aimobile.models

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
