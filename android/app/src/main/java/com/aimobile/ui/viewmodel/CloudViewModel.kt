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

    fun loadAllData(deviceId: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _historyList.value = repository.getHistory(deviceId)
            _analytics.value = repository.getAnalytics()
            _devicesList.value = repository.getAllDevices()
            _isLoading.value = false
        }
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
