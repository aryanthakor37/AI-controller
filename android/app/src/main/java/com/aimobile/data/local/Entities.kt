package com.aimobile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_info")
data class DeviceInfoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deviceName: String,
    val androidVersion: String,
    val batteryPercentage: Int
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val theme: String = "dark",
    val voiceLanguage: String = "en-US"
)

@Entity(tableName = "command_history")
data class CommandHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val intent: String,
    val timestamp: Long,
    val status: String
)

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val scheduleTime: String,
    val actionsJson: String,
    val isEnabled: Boolean,
    val category: String,
    val lastRun: String
)
