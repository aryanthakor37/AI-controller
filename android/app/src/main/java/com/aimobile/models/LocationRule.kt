package com.aimobile.models

data class LocationRule(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float = 150f,
    val enterAction: String = "SILENT", // "SILENT", "VIBRATE", "DND"
    val exitAction: String = "NORMAL_SOUND",
    var isEnabled: Boolean = true,
    var isInside: Boolean = false
)
