package com.aimobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aimobile.api.AnalyticsResponse
import com.aimobile.api.CloudDeviceItem
import com.aimobile.api.HistoryItem
import com.aimobile.repository.CloudRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CloudViewModel @Inject constructor(
    private val repository: CloudRepository
) : ViewModel() {

    private val _historyList = MutableStateFlow<List<HistoryItem>>(emptyList())
    val historyList: StateFlow<List<HistoryItem>> = _historyList.asStateFlow()

    private val _analytics = MutableStateFlow<AnalyticsResponse?>(null)
    val analytics: StateFlow<AnalyticsResponse?> = _analytics.asStateFlow()

    private val _devicesList = MutableStateFlow<List<CloudDeviceItem>>(emptyList())
    val devicesList: StateFlow<List<CloudDeviceItem>> = _devicesList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _backupStatus = MutableStateFlow<String?>(null)
    val backupStatus: StateFlow<String?> = _backupStatus.asStateFlow()

    private val _restorePayload = MutableStateFlow<String?>(null)
    val restorePayload: StateFlow<String?> = _restorePayload.asStateFlow()

    private val defaultMockHistory = listOf(
        HistoryItem(_id = "h1", command = "What is the weather today?", intent = "CHECK_WEATHER", deviceName = "Vivo V2250", status = "Completed", createdAt = "2026-07-23T09:45:00.000Z"),
        HistoryItem(_id = "h2", command = "Open YouTube app", intent = "OPEN_APP", deviceName = "Vivo V2250", status = "Completed", createdAt = "2026-07-23T09:30:12.000Z"),
        HistoryItem(_id = "h3", command = "Turn on Flashlight", intent = "SYSTEM_TOGGLE", deviceName = "Vivo V2250", status = "Completed", createdAt = "2026-07-23T09:15:00.000Z"),
        HistoryItem(_id = "h4", command = "Set alarm for 7 AM", intent = "SET_ALARM", deviceName = "Vivo V2250", status = "Completed", createdAt = "2026-07-23T08:00:00.000Z"),
        HistoryItem(_id = "h5", command = "Turn on WiFi connection", intent = "SYSTEM_TOGGLE", deviceName = "Vivo V2250", status = "Completed", createdAt = "2026-07-23T07:45:00.000Z")
    )

    fun loadAllData(deviceId: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            val remoteHistory = repository.getHistory(deviceId)
            if (remoteHistory.isNotEmpty()) {
                _historyList.value = remoteHistory
            } else if (_historyList.value.isEmpty()) {
                _historyList.value = defaultMockHistory
            }
            _analytics.value = repository.getAnalytics()
            _devicesList.value = repository.getAllDevices()
            _isLoading.value = false
        }
    }

    fun addHistoryItem(command: String, intent: String = "COMMAND_EXECUTE", status: String = "Completed") {
        val newItem = HistoryItem(
            _id = System.currentTimeMillis().toString(),
            command = command,
            intent = intent,
            deviceName = "Vivo V2250",
            status = status,
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(java.util.Date())
        )
        _historyList.value = listOf(newItem) + _historyList.value
    }

    fun backup(deviceId: String, deviceName: String, settingsPayload: String) {
        viewModelScope.launch {
            _backupStatus.value = "Backing up settings..."
            val success = repository.backupSettings(deviceId, deviceName, settingsPayload)
            _backupStatus.value = if (success) "Backup Completed successfully" else "Backup Failed"
        }
    }

    fun restore(deviceId: String) {
        viewModelScope.launch {
            _backupStatus.value = "Restoring settings..."
            val payload = repository.restoreSettings(deviceId)
            if (payload != null) {
                _restorePayload.value = payload
                _backupStatus.value = "Restore Completed successfully"
            } else {
                _backupStatus.value = "No backup found or restore failed"
            }
        }
    }

    fun clearBackupStatus() {
        _backupStatus.value = null
    }
}
